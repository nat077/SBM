/**
 * 
 */
package com.cs.sbm.api;

/**
 * Interface that should be implemented by all consumers to receive orderbook updates.
 * 
 * @author Nat
 *
 */
public interface OrderUpdateListener {
	/**
	 * A notification indicating an update on the orderbook.
	 * @param summary The snapshot of the orderbook.
	 */
	public void onUpdate(OrderBookSummary summary);
}
