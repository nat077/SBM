package com.cs.sbm.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * A PriceLevel with reference to the price of this level and list of orders that belongs to this price level.
 * 
 * Note that, this PriceLevel does not use any kind of List, 
 * instead the {@link Order}s themselves will point to previous and next orders in the level, thus creating a chain of orders.
 * Whenever an order is added to this price level or removed from this price level, respective previous/next pointers are updated to keep the list intact.
 * 
 * This avoids having to use a {@link List} implementation, which may trigger GC or the look up could costlier, in case of removal operations.
 * 
 * Also, any add/remove operations are performed under a lock to ensure thread safe PriceLevel. (StampedLock is used for the sake of performance)
 * 
 * Note that, the orders are maintained in the order they are inserted (ie: not sorted by any means)
 * @author Nat
 *
 */
public class PriceLevel {
	/**
	 * The price of this level.
	 */
	private final int price;
	

	/**
	 * The first order at this level. 
	 */
	private Order head;
	/**
	 * The last order at this level.
	 */
	private Order tail;
	/**
	 * The summary at this price level.
	 */
	private volatile PriceLevelSummary summary;
	/**
	 * Lock to synchronize operations at this price level.
	 */
	private StampedLock lock = new StampedLock();
	
	/**
	 * Construct this price level.
	 * @param price The price of this level.
	 */
	public PriceLevel(int price) {
		super();
		this.price = price;
	}
	/**
	 * Adds an order at the end of this price level, and computes the snapshot after this operation.
	 * @param order
	 * @return
	 */
	public Order addOrder(Order order){
		long stamp = lock.writeLock();
		final Order tmp = tail;
		order.previous = tmp;
		order.next = null;
		tail = order;
		if(tmp == null){
			head = order;
		}else{
			tmp.next = order;
		}
		//recompute cumulative quantity, under lock.
		computeSummary();
		lock.unlockWrite(stamp);
		
		return order;
	}
	/**
	 * Removes an order from this PriceLevel and computes the snapshot after this operation.
	 * The removal operation simply modifies the previous and next pointers of previous and next orders.
	 * @param order
	 * @return
	 */
	public Order removeOrder(Order order){
		long stamp = lock.writeLock();
		Order prev = order.previous;
		Order next = order.next;
		if(prev != null){
			prev.next = next;
		}
		if(next != null){
			next.previous = prev;
		}
		//recompute cumulative quantity, under lock.
		computeSummary();
		lock.unlockWrite(stamp);
		return order;
	}
	/**
	 * Utility method to compute the snapshot.
	 */
	private void computeSummary(){
		if(head == null){
			summary = new PriceLevelSummary(price, 0, BigDecimal.ZERO);
			return;
		}
		BigDecimal qty = BigDecimal.ZERO;
		int count = 0;
		Order ord = head;
		while(ord != null){
			//Count only LIVE orders, ignore FILLED and CANCELLED orders.
			if(OrderState.LIVE == ord.state){
				qty = qty.add(ord.quantity);
				count++;
			}
			
			ord = ord.next;
		}
		summary = new PriceLevelSummary(price, count, qty);
		return;
	}
	/**
	 * The price of this level
	 * @return The price of this level
	 */
	public int getPrice() {
		return price;
	}
	/**
	 * 
	 * @return The summary of this price level.
	 */
	public PriceLevelSummary getSummary(){
		long stamp = lock.tryOptimisticRead();
		PriceLevelSummary sum = this.summary;
		if(!lock.validate(stamp)){
			//compute under lock.
			stamp = lock.readLock();
			computeSummary();
			sum = this.summary;
			lock.unlockRead(stamp);
		}
		return sum;
	}
}
