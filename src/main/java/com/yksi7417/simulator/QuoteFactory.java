package com.yksi7417.simulator;

import java.util.List;


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
	
	static public Quote create(LimitOrderBook lob) {
		Quote quote = new Quote(lob.getTicker());
		lob.bidQueue.forEach(lo->addToQuote(lo, quote.bids, quote.bidSizes)) ; 
		lob.askQueue.forEach(lo->addToQuote(lo, quote.asks, quote.askSizes)) ; 
		return quote; 
	}
}
