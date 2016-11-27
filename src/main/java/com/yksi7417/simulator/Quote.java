package com.yksi7417.simulator;

import java.util.ArrayList;
import java.util.List;

/**
 * fixed level of bid / ask as quote 
 * 0 is always best bid/ best ask 
 * @author asimoneta
 */

public class Quote {
	final static int NUM_OF_LEVELS = 5;
	final String ticker; 
	final List<Integer> bidSizes = new ArrayList<Integer>(NUM_OF_LEVELS);
	final List<Integer> askSizes = new ArrayList<Integer>(NUM_OF_LEVELS);
	final List<Double> bids = new ArrayList<Double>(NUM_OF_LEVELS);
	final List<Double> asks = new ArrayList<Double>(NUM_OF_LEVELS);
	
	public Quote(String ticker) {
		super();
		this.ticker = ticker;
	}
	public boolean isEmpty() {
		int sum = 0; 
		for (int i=0; i< bidSizes.size(); i++) 
			sum += bidSizes.get(i);
		for (int i=0; i< askSizes.size(); i++) 
			sum += askSizes.get(i);
		return sum == 0; 
	}
	public double getBid(int i) {
		return this.bids.get(i);
	}
	public int getBidSize(int i) {
		return this.bidSizes.get(i);
	}
}
