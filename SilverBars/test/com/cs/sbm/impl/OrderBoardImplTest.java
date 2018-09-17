/**
 * 
 */
package com.cs.sbm.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cs.sbm.api.OrderBookSummary;
import com.cs.sbm.api.OrderType;
import com.cs.sbm.api.OrderUpdateListener;


/**
 * Testcase for {@link OrderBoardImpl}
 * 
 * @author Nat
 *
 */
public class OrderBoardImplTest {

	OrderBoardImpl orderboard;
	DefaultOrderUpdateListener updateListener;
	@Before
	public void setup(){
		this.updateListener = new DefaultOrderUpdateListener();
		this.orderboard = new OrderBoardImpl();
		this.orderboard.addUpdateListener(updateListener);
	}
	
	@After
	public void teardown(){
		
	}
	
	/**
	 * Tests various order registration scenarios.
	 */
	@Test
	public void testRegisterOrder(){
		OrderBookSummary expected = null;
		//register one order and vlaidate
		orderboard.registerOrder("Nat", OrderType.BUY, 306, BigDecimal.valueOf(3.5));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		//register 2nd order.
		orderboard.registerOrder("Nat", OrderType.BUY, 310, BigDecimal.valueOf(1.2));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)), new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		//register 3rd order
		orderboard.registerOrder("Nat", OrderType.BUY, 307, BigDecimal.valueOf(1.5));
		updateListener.clear();
		
		//register 4th order and validate the orderbook summary.
		orderboard.registerOrder("Nat", OrderType.BUY, 306, BigDecimal.valueOf(2.0));
		expected = new OrderBookSummary(Arrays.asList(
				new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)),
				new PriceLevelSummary(307, 1, BigDecimal.valueOf(1.5)),
				new PriceLevelSummary(306, 2, BigDecimal.valueOf(5.5))),
				Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		
		orderboard.registerOrder("Nat", OrderType.SELL, 305, BigDecimal.valueOf(6.8));
		orderboard.registerOrder("Nat", OrderType.SELL, 301, BigDecimal.valueOf(1.5));
		orderboard.registerOrder("Nat", OrderType.SELL, 301, BigDecimal.valueOf(7.2));
		updateListener.clear();
		
		orderboard.registerOrder("Nat", OrderType.SELL, 307, BigDecimal.valueOf(3.5));
		
		expected = new OrderBookSummary(Arrays.asList(
				new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)),
				new PriceLevelSummary(307, 1, BigDecimal.valueOf(1.5)),
				new PriceLevelSummary(306, 2, BigDecimal.valueOf(5.5))),
				
				Arrays.asList(
				new PriceLevelSummary(301, 2, BigDecimal.valueOf(8.7)),
				new PriceLevelSummary(305, 1, BigDecimal.valueOf(6.8)),
				new PriceLevelSummary(307, 1, BigDecimal.valueOf(3.5))
				));
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
	}
	
	/**
	 * Tets order cancel scenarios.
	 */
	@Test
	public void testCancelOrder(){
	
		OrderBookSummary expected = null;
		int orderId1 = orderboard.registerOrder("Nat", OrderType.BUY, 306, BigDecimal.valueOf(3.5));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		
		int orderId2 = orderboard.registerOrder("Nat", OrderType.BUY, 310, BigDecimal.valueOf(1.2));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)), new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		//Cancel order 1
		orderboard.cancelOrder(orderId1);
		expected = new OrderBookSummary(Arrays.asList( new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		//Cancel order 2
		orderboard.cancelOrder(orderId2);
		expected = new OrderBookSummary(Collections.emptyList(), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
	}
	
	@Test
	/**
	 * Tests the cancellation attempt, when the orderId is less than 0
	 */
	public void testCancelOrder_NegativeOrderId(){
		Assert.assertFalse("Orderboard did not detect negative orderId", orderboard.cancelOrder(-3));
	}
	
	@Test
	/**
	 * Tests the cancellation attempt, when the orderId is higher than max orderId
	 */
	public void testCancelOrder_OrderId_higher_than_max_size(){
		Assert.assertFalse("Orderboard did not detect invalid orderId", orderboard.cancelOrder(5120));
	}
	
	@Test
	/**
	 * Tests the cancellation attempt, when the order does not exist.
	 */
	public void testCancelOrder_NonExistent_Order(){
		Assert.assertFalse("Orderboard did not detect invalid orderId", orderboard.cancelOrder(5119));
	}
	
	@Test
	public void testRemoveAndReRegister(){
		OrderBookSummary expected = null;
		int orderId1 = orderboard.registerOrder("Nat", OrderType.BUY, 306, BigDecimal.valueOf(3.5));
		updateListener.clear();
		int orderId2 = orderboard.registerOrder("Nat", OrderType.BUY, 310, BigDecimal.valueOf(1.2));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)), new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
		
		orderboard.cancelOrder(orderId1);
		orderboard.cancelOrder(orderId2);
		updateListener.clear();
		//reorder..
		orderId1 = orderboard.registerOrder("Nat", OrderType.BUY, 306, BigDecimal.valueOf(3.5));
		updateListener.clear();
		
		orderId2 = orderboard.registerOrder("Nat", OrderType.BUY, 310, BigDecimal.valueOf(1.2));
		expected = new OrderBookSummary(Arrays.asList(new PriceLevelSummary(310, 1, BigDecimal.valueOf(1.2)), new PriceLevelSummary(306, 1, BigDecimal.valueOf(3.5))), Collections.emptyList());
		validate(expected, updateListener.getUpdates().get(0) );
		updateListener.clear();
	}
	
	private void validate(OrderBookSummary expected, OrderBookSummary actual){
		Assert.assertEquals("OrderBookSummary doesnt match", expected, actual);
	}
	
	private class DefaultOrderUpdateListener implements OrderUpdateListener{
		
		List<OrderBookSummary> updates = new ArrayList<>();
		public List<OrderBookSummary> getUpdates() {
			return updates;
		}
		
		@Override
		public void onUpdate(OrderBookSummary summary) {
			updates.add(summary);
			System.out.println("Received summary update:"+ summary);
		}
		
		public void clear(){
			updates.clear();
		}
	}
}
