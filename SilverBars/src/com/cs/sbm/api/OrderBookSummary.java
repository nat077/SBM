package com.cs.sbm.api;

import java.util.List;

import com.cs.sbm.impl.PriceLevelSummary;

/**
 * The summary of orders.
 * @author Nat
 *
 */
public class OrderBookSummary {
	/**
	 * List of buy price levels.
	 */
	private final List<PriceLevelSummary> buys;
	/**
	 * List of SELL price levels.
	 */
	private final List<PriceLevelSummary> sells;
	
	/**
	 * Constructor to create the snapshot.
	 * @param buys List of buy price levels.
	 * @param sells List of sell price levels.
	 */
	public OrderBookSummary(List<PriceLevelSummary> buys, List<PriceLevelSummary> sells) {
		super();
		this.buys = buys;
		this.sells = sells;
	}
	/**
	 * 
	 * @return List of BUY price levels.
	 */
	public List<PriceLevelSummary> getBuys() {
		return buys;
	}
	/**
	 * 
	 * @return List of SELL price levels.
	 */
	public List<PriceLevelSummary> getSells() {
		return sells;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buys == null) ? 0 : buys.hashCode());
		result = prime * result + ((sells == null) ? 0 : sells.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderBookSummary other = (OrderBookSummary) obj;
		if (buys == null) {
			if (other.buys != null)
				return false;
		} else if (!buys.equals(other.buys))
			return false;
		if (sells == null) {
			if (other.sells != null)
				return false;
		} else if (!sells.equals(other.sells))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OrderBookSummary {buys=").append(buys).append(", sells=").append(sells).append("}");
		return builder.toString();
	}
	
}
