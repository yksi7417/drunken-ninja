package com.yksi7417.simulator;

import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;

/**
 * the method matches is not thread safe.   Designed for thread-confinement environment 
 * @author asimoneta
 *
 */

public class LimitOrder {
	public enum Side { BUY, SELL }

	private static final AtomicInteger idGenerator = new AtomicInteger(1);

	private int orderid; 
	private Side side; 
	private int qty; 
	// price as double, but assume to be 6 decimal points precision
	private double price; 	
	private DateTime timestamp;
	
	public LimitOrder(LimitOrder other) {
		this(other.orderid, other.getSide(), other.getQty(), other.getPrice(), other.getTimestamp());
	}
	
	private LimitOrder(int orderId, Side side, int qty, double price, DateTime timestamp) {
		super();
		this.orderid = orderId;
		this.side = side;
		this.qty = qty;
		this.price = price;
		this.timestamp = timestamp; 
	}

	public LimitOrder(Side side, int qty, double price, DateTime timestamp) {
		super();
		this.orderid = idGenerator.incrementAndGet();
		this.side = side;
		this.qty = qty;
		this.price = price;
		this.timestamp = timestamp; 
	}

	public int getOrderid() {
		return orderid;
	}

	public Side getSide() {
		return side;
	}
	public int getQty() {
		return qty;
	}
	public double getPrice() {
		return price;
	}
	public DateTime getTimestamp() {
		return timestamp;
	} 
	
	private boolean isEqualOrBetter(double d1, double d2) {
		switch (this.side) {
		case BUY:
			return PriceUtils.isEqualGreaterThan(d1, d2);
		case SELL:
			return PriceUtils.isEqualLessThan(d1, d2);
		default:
			throw new RuntimeException("do not expect anything other than BUY/SELL order, please review design if you see this");
		}
	}
	
	/** 
	 * This method could make change to internal variables to this LimitOrder or the other during a match.   
	 * @return Trade
	 */
	public Trade matches(LimitOrder limitOrder) {
		if (this.getSide().equals(limitOrder.getSide()))
			return null; 
		if (!isEqualOrBetter(this.getPrice(), limitOrder.getPrice()))
			return null; 
		return generateMatchTrade(limitOrder);
	}

	private Trade generateMatchTrade(LimitOrder limitOrder) {
		int tradeQty = 0;
		
		if (this.getQty() > limitOrder.getQty()) {
			tradeQty = limitOrder.getQty();
			this.qty -= limitOrder.getQty();
			limitOrder.qty = 0;
		}
		else if (this.getQty() < limitOrder.getQty()) {
			tradeQty = this.getQty();
			this.qty = 0;
			limitOrder.qty -= tradeQty;
		}
		else {  // equal case
			tradeQty = limitOrder.getQty();
			this.qty = 0;
			limitOrder.qty = 0;
		}
		
		return new Trade(tradeQty, this.getPrice());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orderid;
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
		LimitOrder other = (LimitOrder) obj;
		if (orderid != other.orderid)
			return false;
		return true;
	}

}
