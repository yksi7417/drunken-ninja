package com.yksi7417.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public class LimitOrderBook {
	private final String ticker;

	private final Comparator<LimitOrder> bidPxComparator 
		= (LimitOrder o1, LimitOrder o2)-> (int)((o2.getPrice()-o1.getPrice())/PriceUtils.Epsilon); 
	private final Comparator<LimitOrder> askPxComparator 
		= (LimitOrder o1, LimitOrder o2)-> (int)((o1.getPrice()-o2.getPrice())/PriceUtils.Epsilon); 

	PriorityQueue<LimitOrder> bidQueue = new PriorityQueue<>( new PriceTimeComparator(bidPxComparator));
	PriorityQueue<LimitOrder> askQueue = new PriorityQueue<>( new PriceTimeComparator(askPxComparator));
	
	private List< Consumer <Trade> > tradeConsumers = new ArrayList<>();
			
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
	
	private LimitOrder matchLimitOrder(LimitOrder limitOrder, PriorityQueue<LimitOrder> oppsSideQueue) {
		LimitOrder remainingOrder = new LimitOrder(limitOrder); 
		LimitOrder topOrder = oppsSideQueue.peek();
		if (topOrder == null)
			return remainingOrder; 

		Trade tradeEvent = topOrder.matches(remainingOrder);
		if (tradeEvent != null) {
			
			tradeConsumers.forEach(tradeConsumer -> tradeConsumer.accept(tradeEvent));
		}
		
		if (remainingOrder.getQty() > 0)
			return remainingOrder; 
		
		return null; 
	}

	public void placeOrder(LimitOrder limitOrder) {
		PriorityQueue<LimitOrder> oppsSideQueue = getOppsSideQueue(limitOrder);
		LimitOrder remainingOrder = matchLimitOrder(limitOrder, oppsSideQueue);
		if (remainingOrder != null) {
			PriorityQueue<LimitOrder> sameSideQueue = getSameSideQueue(limitOrder);
			sameSideQueue.add(remainingOrder);
		}
	}

	private PriorityQueue<LimitOrder> getSameSideQueue(LimitOrder limitOrder) {
		switch (limitOrder.getSide()) {
		case BUY:
			return bidQueue; 
		case SELL:
			return askQueue; 
		default:
			throw new RuntimeException("Do not expect any side other than BUY/SELL/SHORTSELL, please review design");
		}
	}

	private PriorityQueue<LimitOrder> getOppsSideQueue(LimitOrder limitOrder) {
		switch (limitOrder.getSide()) {
		case BUY:
			return askQueue; 
		case SELL:
			return bidQueue; 
		default:
			throw new RuntimeException("Do not expect any side other than BUY/SELL/SHORTSELL, please review design");
		}
	}

	/**
	 * Subscribe for TradeEvents that is generated by match event 
	 */
	public void subscribeTradeEvent(Consumer<Trade> tradeConsumer) {
		tradeConsumers.add(tradeConsumer);
	}
	
	/**
	 * Unsubscribe for TradeEvents 
	 */
	public void unsubscribeTradeEvent(Consumer<Trade> tradeConsumer) {
		tradeConsumers.remove(tradeConsumer);
	}
}
