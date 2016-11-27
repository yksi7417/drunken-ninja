package com.yksi7417.simulator;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class QuoteFactory {
	final static public Quote EMPTY_QUOTE = new Quote("EMPTY_QUOTE");
	
	static private void addToQuote(LimitOrder lo, List<Double> pxList, List<Integer> qtyList) {
		int size = pxList.size();
		if (size == 0) {
			pxList.add(lo.getPrice());
			qtyList.add(lo.getQty());
			return; 
		}
		else if (size > Quote.NUM_OF_LEVELS)
			return; 
		else {
			double latestPx = pxList.get(size -1);
			int latestQty = qtyList.get(size -1);
			if (PriceUtils.isEqual(latestPx, lo.getPrice())) 
				qtyList.set(size-1, latestQty + lo.getQty());
			else if (size <= Quote.NUM_OF_LEVELS) {
				pxList.add(lo.getPrice());
				qtyList.add(lo.getQty());
			}
			return; 
		}
	}
	
	/** 
	 * create a quote object with top 5 levels of bid/ask
	 * intent to be used to peek into the internal state of LOB
	 * do not intent to be called often, as it's expensive to construct an quote object 
	 * every time there is an update, as entire LOB is examined 
	 * 
	 * In this implementation, the backend of LOB is using an Priority Queue (PQ) 
	 * which is quick for matching, but iterating an PriorityQueue doesn't give you a sorted
	 * list, so in order to accurately reflect the top five level, a sorting is needed 
	 * 
	 * @param lob
	 * @return Quote object of top 5 levels 
	 */
	static public Quote create(LimitOrderBook lob) {
		Quote quote = new Quote(lob.getTicker());
		
		//convert the treeset to sort the bidQueue/askQueue
		Set<LimitOrder> bidTree = new TreeSet<LimitOrder>(lob.bidQueue.comparator());
		bidTree.addAll(lob.bidQueue);
		Set<LimitOrder> askTree = new TreeSet<LimitOrder>(lob.askQueue.comparator());
		askTree.addAll(lob.askQueue);
		
		bidTree.forEach(lo->addToQuote(lo, quote.bids, quote.bidSizes)) ; 
		askTree.forEach(lo->addToQuote(lo, quote.asks, quote.askSizes)) ; 
		return quote; 
	}
}
