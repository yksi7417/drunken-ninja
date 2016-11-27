package com.yksi7417.simulator;

import org.joda.time.DateTime;

public class LimitOrder {
	public enum Side { BUY, SELL, SHORTSELL }
	
	private Side side; 
	private int qty; 
	// price as double, but assume to be 6 decimal points precision
	private double price; 	
	private DateTime timestamp;
	
	public LimitOrder(Side side, int qty, double price, DateTime timestamp) {
		super();
		this.side = side;
		this.qty = qty;
		this.price = price;
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

}
