package com.yksi7417.simulator;

import java.util.Comparator;

class PriceTimeComparator implements Comparator<LimitOrder> {
	final Comparator<LimitOrder> priceComparator;
	public PriceTimeComparator(Comparator<LimitOrder> priceComparator) {
		this.priceComparator = priceComparator; 
	}
	@Override
	public int compare(LimitOrder o1, LimitOrder o2) {
		if (PriceUtils.isEqual(o1.getPrice() , o2.getPrice()))
			return o1.getTimestamp().compareTo(o2.getTimestamp());
		else 
			return this.priceComparator.compare(o1,o2);
	}
}

