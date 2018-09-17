package com.cs.sbm.impl;

import java.math.BigDecimal;

/**
 * summary of a price level.
 * @author Nat
 *
 */
public final class PriceLevelSummary{
	
	private final int price;
	
	/**
	 * Number of orders at this level.
	 */
	private final int numberOfOrders;
	
	/**
	 * The cumulative quantity.
	 */
	private final BigDecimal cumulativeQuantity;
	
	private final int hash;

	PriceLevelSummary(int price, int numberOfOrders, BigDecimal cumQty){
		this.price = price;
		this.numberOfOrders = numberOfOrders;
		this.cumulativeQuantity = cumQty;
		hash = calculateHash(); //hashcode can be calculated just once, as this object is immutable.
	}
	public int getPrice(){
		return this.price;
	}
	public int getNumberOfOrders() {
		return numberOfOrders;
	}
	public BigDecimal getCumulativeQuantity() {
		return cumulativeQuantity;
	}
	@Override
	public int hashCode() {
		return hash;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PriceLevelSummary other = (PriceLevelSummary) obj;
		if (cumulativeQuantity == null) {
			if (other.cumulativeQuantity != null)
				return false;
		} else if (cumulativeQuantity.compareTo(other.cumulativeQuantity) !=0)
			return false;
		if (numberOfOrders != other.numberOfOrders)
			return false;
		if (price != other.price)
			return false;
		return true;
	}
	
	private int calculateHash(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cumulativeQuantity == null) ? 0 : cumulativeQuantity.hashCode());
		result = prime * result + numberOfOrders;
		result = prime * result + price;
		return result;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PriceLevelSummary {price=").append(price).append(", numberOfOrders=").append(numberOfOrders)
				.append(", cumulativeQuantity=").append(cumulativeQuantity).append("}");
		return builder.toString();
	}
	
}