package com.yksi7417.simulator;

import com.yksi7417.simulator.marketdata.Quote;

public class LimitOrderBook {
	private final String ticker;

	public LimitOrderBook(String ticker) {
		super();
		this.ticker = ticker;
	}

	public String getTicker() {
		return ticker;
	} 
	
	public Quote getQuoteSnapshot() {
		return Quote.EMPTY_QUOTE; 
	}
	
}
