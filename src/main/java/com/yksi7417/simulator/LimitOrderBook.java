package com.yksi7417.simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class LimitOrderBook {
	private final String ticker;

	private final Comparator<LimitOrder> bidPxComparator = (LimitOrder o1, LimitOrder o2)-> (int)(o1.getPrice()-o2.getPrice()); 
	private final Comparator<LimitOrder> bidPxTimeComparator = new PriceTimeComparator(bidPxComparator);
	
	private final Comparator<LimitOrder> askPxComparator = (LimitOrder o1, LimitOrder o2)-> (int)(o2.getPrice()-o1.getPrice()); 
	private final Comparator<LimitOrder> askPxTimeComparator = new PriceTimeComparator(askPxComparator);

	PriorityQueue<LimitOrder> bidQueue = new PriorityQueue<LimitOrder>(bidPxTimeComparator);
	PriorityQueue<LimitOrder> askQueue = new PriorityQueue<LimitOrder>(askPxTimeComparator);
			
	public LimitOrderBook(String ticker) {
		super();
		this.ticker = ticker;
	}

	public String getTicker() {
		return ticker;
	} 
	
	/**
	 * Go through LOB, create Quote snapshot object, relatively expensive operation
	 * @return
	 */
	public Quote getQuoteSnapshot() {
		return QuoteFactory.create(this); 
	}

	public void placeOrder(LimitOrder limitOrder) {
		bidQueue.add(limitOrder);
	}
	
}
