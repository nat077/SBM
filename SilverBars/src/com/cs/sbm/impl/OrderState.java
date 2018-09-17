/**
 * 
 */
package com.cs.sbm.impl;

/**
 * State of an order at any given time.
 * Note that, there can be many more states of for an order, notably partially filled etc. 
 * However, for simplicity reasons, its assumed that an order is either fully filled or not yet filled.
 * @author Nat
 *
 */
public enum OrderState {
	LIVE,/** The order is live */
	FILLED,/** The order is completely filled*/
	CANCELLED;/** The order is cancelled*/
}
