package com.cs.sbm.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.cs.sbm.api.OrderBoard;
import com.cs.sbm.api.OrderBookSummary;
import com.cs.sbm.api.OrderType;
import com.cs.sbm.api.OrderUpdateListener;

/**
 * An implementation of {@link OrderBoard} that performs register, cancellation of an order.
 * 
 * This implementation provides a method to retrieve summary of the orderboard at any time.
 * 
 *  Also, whenever an order is registered or cancelled, all registered listeners are notified.
 * ----------- IMPLEMENTATION NOTE ---------
 * 
 * There are 2 key operations on the orderbook. The first one is, locating price levels, so as to add an order to a price level. The level look up should be as fast as possible.
 * For this purpose, a {@link ConcurrentSkipListMap} is used, with order price being the key. For simplicity sake, its been assumed that that price is whole integer. 
 * Additionally {@link ConcurrentSkipListMap} offers thread safety. 
 * 
 * The second operation is Looking up the order itself for cancellation or execution purpose. For this, a {@link Map} could be used, with an orderId as key. 
 * However in terms of GC, this is not a great choice, as the memory needs to be dynamically allocated every time an item is added.
 * Instead, a big array is being used in this implementation. The array is initialised at the time of construction which helps in terms of GC.
 * We could probably use 3rd party libraries like LMAX Disruptor, but for the purpose of this exercise no 3rd party library has been used. 
 * 
 *  OrderId is defined as an Integer, and it is generated using an AtomicInteger. This orderId also serves as the index of the specified order, in the storage array.
 * -----------------------------------------
 * 
 * @author Nat Paramasivam
 */
public final class OrderBoardImpl implements OrderBoard {
	
	/**Maximum number of orders. */
	private static final int DEFAULT_MAX_ORDER_COUNT = 1024 * 5;
	
	//The logger.
	private final Logger logger = Logger.getLogger(OrderBoardImpl.class.getName());
	/**
	 * Bid price levels. Stored as a Skip list map, as it provides O(log n) search performance.
	 * The concurrent version provides safety in a multi-threaded environment.
	 */
	private ConcurrentSkipListMap<Integer, PriceLevel> buyPriceLevels = new ConcurrentSkipListMap<>(Comparator.comparing(Integer::intValue).reversed() );
	
	/**
	 * Bid price levels. Stored as a Skip list map, as it provides best search performance.
	 * The concurrent version provides safety in a multi threaded environment.
	 */
	private ConcurrentSkipListMap<Integer, PriceLevel> sellPriceLevels = new ConcurrentSkipListMap<>();
	
	/**
	 * Orders, stored as an array.
	 * The index of the array also serves as orderId, enabling O(1) lookup to locate an order by its ID.
	 * 
	 */
	private final Order[] orders;
	/**
	 * Atomic counter to generate unique Order Ids.
	 */
	private AtomicInteger orderCounter = new AtomicInteger(0);
	/**
	 * List of listeners interested in receiving notification from this OrderBoard.
	 * Number of read operations is expected to outnumber the number of write operations on this list, hence a CopyOnWriteArrayList is being used.
	 */
	private CopyOnWriteArrayList<OrderUpdateListener> listeners = new CopyOnWriteArrayList<>();
	/**
	 * Constructor to create an orderboard with default number (5120) of orders.
	 */
	public OrderBoardImpl(){
		this(DEFAULT_MAX_ORDER_COUNT);
	}
	/**
	 * Constructor to create an orderboard with <code>maxOrderCount<</code> of orders.
	 * 
	 * @param maxOrderCount The orderbook size
	 */
	public OrderBoardImpl(int maxOrderCount){
		this.orders  = new Order[maxOrderCount];
	}
	
	/**
	 * Registers an order.
	 * If the price level at orderPrice does not exist, a new price level will be created. 
	 * If the price level already exists, this order will be added to the tail of existing orders at that price level.
	 * Once the order is processed, all registered {@link OrderUpdateListener}s are notified with latest orderbook snapshot. 
	 * @param userName The user name.
	 * @param orderType The order type
	 * @param orderPrice The order price, in whole pounds. 
	 * @param quantity The order size
	 */
	@Override
	public int registerOrder(String userName, OrderType orderType, int orderPrice, BigDecimal quantity) {
		//The unique ID of this order.
		int orderId = orderCounter.getAndIncrement();
		//boundary check. If this is an attempt to add more orders than the storage can support, throw exception
		if(orderId >= orders.length){
			//Cannot add order, as it exceeds the max allowed count.
			throw new RuntimeException("Attempt to add more than "+this.orders.length+" orders");
		}
		Order order = new Order(orderId, userName, orderType, orderPrice, quantity);
		//locate the price level, this order belongs to.
		PriceLevel priceLevel = null;
		if(OrderType.BUY == orderType){
			priceLevel = buyPriceLevels.computeIfAbsent(orderPrice, k -> new PriceLevel(k));
		}else{
			priceLevel = sellPriceLevels.computeIfAbsent(orderPrice, k -> new PriceLevel(k));
		}
		//update the price level, with new order.
		priceLevel.addOrder(order);
		//Update the orders array.
		orders[orderId] = order;
		logger.log(Level.INFO, "Added "+orderType+" order for "+quantity+" @ £"+orderPrice);
		//Notify all registered listeners with updated orderbook summary.
		computeSummaryAndNotifyListeners();
		return orderId;
	}
	/**
	 * Cancels an order by its orderId.
	 * This method marks the order as cancelled and removes it from the price level. 
	 * However, the order itself is not being removed from the storage array. This may not sound like a great idea, however this reduces the need for GC cycles,
	 * and also avoids copying the entire array to adjust the index positions, which could be a significant overhead if the array is very big and there are frequent cancellations.
	 * Once the order is cancelled, all registered {@link OrderUpdateListener}s are notified with latest orderbook snapshot. 
	 */
	@Override
	public boolean cancelOrder(int orderId) {
		//boundary check.
		if(orderId <0 || orderId >= orders.length){
			//Invalid orderId. We should probably throw exception, something like InvalidOrderIdException, but for simplicity sake, we return false.
			return false;
		}
		//Locate the order, by index.
		Order order = orders[orderId];
		//Check there is an order with this id.
		if(order == null){
			//Invalid orderId. We should probably throw exception, something like NoSuchOrderException, but for simplicity sake, we return false.
			return false;
		}
		if(OrderState.FILLED == order.state){
			//Order is already filled. cannot be cancelled.
			return false;
		}
		//If the order state is already cancelled, no need to do the same work again.
		if(OrderState.CANCELLED == order.state){
			return true;
		}
		/*
		 * Mark the order as cancelled, and update the price level.
		 * Note that, the order itself is not being removed from the storage. This may not look great at the start,
		 * however, this avoids recopying of storage array, and also helps towards GC.
		 */
		order.state = OrderState.CANCELLED;
		PriceLevel priceLevel = OrderType.BUY == order.orderType ?  buyPriceLevels.get(order.orderPrice) : sellPriceLevels.get(order.orderPrice);
		priceLevel.removeOrder(order);
		logger.log(Level.INFO, "Cancelled "+order.orderType+" order for "+order.quantity+" @ £"+order.orderPrice);
		computeSummaryAndNotifyListeners();
		return true;
	}
	
	/**
	 * Method to get the current snapshot of the orderbook.
	 */
	@Override
	public OrderBookSummary getSummary() {
		List<PriceLevelSummary> buys = buyPriceLevels.entrySet().stream().map(entry -> entry.getValue())
				.map(priceLevel -> priceLevel.getSummary())
				.filter(summary -> summary.getCumulativeQuantity().compareTo(BigDecimal.ZERO) > 0) //filter any priceLevel with 0 cumulative quantity.
				.collect(Collectors.toList());
		List<PriceLevelSummary> sells = sellPriceLevels.entrySet().stream().map(entry -> entry.getValue())
				.map(priceLevel -> priceLevel.getSummary())
				.filter(summary -> summary.getCumulativeQuantity().compareTo(BigDecimal.ZERO) > 0)
				.collect(Collectors.toList());
		OrderBookSummary summary = new OrderBookSummary(buys, sells);
		return summary;
	}
	
	/**
	 * Registers a listener for OrderBook update events.
	 * @param the listener to be registered.
	 */
	@Override
	public void addUpdateListener(OrderUpdateListener listener) {
		listeners.addIfAbsent(listener);
	}

	/**
	 * Removes a listener from receiving orderbook update events.
	 * @param The listener to be removed.
	 */
	@Override
	public void removeUpdateListener(OrderUpdateListener listener) {
		listeners.remove(listener);
	}
	/**
	 * Utility method to compute the orderbook snapshot and notify all registered listeners.
	 */
	private void computeSummaryAndNotifyListeners(){
		OrderBookSummary summary = getSummary();
		listeners.forEach(listener -> listener.onUpdate(summary));
		
	}
	
}
