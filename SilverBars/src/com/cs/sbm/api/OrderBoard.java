/**
 * 
 */
package com.cs.sbm.api;

import java.math.BigDecimal;

/**
 * Interface for OrderBoard. 
 * @author Nat Paramasivam
 *
 */
public interface OrderBoard {

	/**
	 * Registers an order.
	 * @param userName Name of the user placing this order.
	 * @param orderType The type of the order. Either by or sell.
	 * @param orderPrice The order price, in GBP.
	 * @param quantity Order quantity.
	 * @return The order ID.
	 */
	public int registerOrder(String userName, OrderType orderType, int orderPrice, BigDecimal quantity);
	/**
	 * Cancels an order identified by the orderId
	 * @param orderId The ID of the order to be cancelled.
	 * @return true if the order cancelled successfully, otherwise false.
	 */
	public boolean cancelOrder( int orderId);
	/**
	 * Gets the summary of the orders.
	 * @return The summary of the orders.
	 */
	public OrderBookSummary getSummary();
	/**
	 * Register a listener for orderbook update events.
	 * @param listener The listener.
	 */
	public void addUpdateListener(OrderUpdateListener listener);
	/**
	 * Unregisters a listener from receiving order update events. 
	 * @param listener The listener.
	 */
	public void removeUpdateListener(OrderUpdateListener listener);
}
