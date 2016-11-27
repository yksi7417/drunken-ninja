package com.yksi7417.simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class LimitOrderBook {
	private final String ticker;

	private final Comparator<LimitOrder> bidPxComparator = (LimitOrder o1, LimitOrder o2)-> (int)((o2.getPrice()-o1.getPrice())/PriceUtils.Epsilon); 
	private final Comparator<LimitOrder> askPxComparator = (LimitOrder o1, LimitOrder o2)-> (int)((o1.getPrice()-o2.getPrice())/PriceUtils.Epsilon); 

	PriorityQueue<LimitOrder> bidQueue = new PriorityQueue<LimitOrder>( new PriceTimeComparator(bidPxComparator));
	PriorityQueue<LimitOrder> askQueue = new PriorityQueue<LimitOrder>( new PriceTimeComparator(askPxComparator));
			
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
		switch (limitOrder.getSide()) {
		case BUY:
			bidQueue.add(limitOrder);
			break;
		case SELL:
		case SHORTSELL:
			askQueue.add(limitOrder);
			break;
		default:
			throw new RuntimeException("Do not expect any side other than BUY/SELL/SHORTSELL, please review design");
		}
	}
	
}
