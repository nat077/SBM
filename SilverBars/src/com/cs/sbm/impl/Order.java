package com.cs.sbm.impl;

import java.math.BigDecimal;

import com.cs.sbm.api.OrderType;

/**
 * An order, with pointers to previous and next orders in the price level.
 * @author Nat
 *
 */
public class Order {
	
	/**The ID of this order */
	final int orderId;
	/** Name of the user, placed this order */
	final String userName;
	/** The type of this order. Either buy or sell. */
	final OrderType orderType;
	/** The order price, in pence. */
	final int orderPrice;
	/** The order quantity */
	final BigDecimal quantity;
	/** The current state of this order */
	OrderState state;
	/**The previous order at this price level */
	Order previous;
	/**The next order at this price level */
	Order next;
	/**
	 * Constructor to create an instance of an order.
	 * @param orderId The order ID
	 * @param userName The user name.
	 * @param orderType The type of the order.
	 * @param orderPrice The order price, in GBPx
	 * @param quantity Quantity.
	 */
	public Order(int orderId, String userName, OrderType orderType, int orderPrice, BigDecimal quantity) {
		super();
		this.orderId = orderId;
		this.userName = userName;
		this.orderType = orderType;
		this.orderPrice = orderPrice;
		this.quantity = quantity;
		this.state = OrderState.LIVE;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Order {userName=").append(userName).append(", orderType=").append(orderType)
				.append(", orderPrice=").append(orderPrice).append(", quantity=").append(quantity).append(", state=")
				.append(state).append(", next=").append(next).append("}");
		return builder.toString();
	}
}
