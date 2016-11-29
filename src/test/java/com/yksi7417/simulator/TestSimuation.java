package com.yksi7417.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.yksi7417.simulator.LimitOrder.Side;
import com.yksi7417.simulator.tickdata.TickDataConvertor;
import com.yksi7417.simulator.tickdata.TickDataSource;

public class TestSimuation {
	LimitOrderFactory lobFactory = new LimitOrderFactory();

	private LimitOrderBook lob; 
	private TickDataSource tds;

	@Before
	public void init(){
		String ticker = "0700.HK";
		this.lob = new LimitOrderBook(ticker); 
		this.tds = new TickDataConvertor(ticker);
	}
	
	@Test
	public void crossSpreadToLiftOffer_expectChangeOfQuoteAndRecieveTradeEvent() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500  , 400 , 300 , 300   , 100,  500  ,  1000 ,  2000 , 2500 , 3000 };

		this.tds.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = this.tds.getLimitOrders();
		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			lob.placeOrder(limitOrder);
		}
	
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a big order, should create 1 trade events that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 40000, 100.1));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 1);
			assertEquals(trades.get(0).getPrice(), 100.1, PriceUtils.Epsilon);
			assertEquals(trades.get(0).getSize(), 500);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trades
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		
		assertEquals(lastQuoteSnapshot.getBid(0), 100.1, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 39500);
		assertEquals(lastQuoteSnapshot.getBid(1), 100, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 100);
		assertEquals(lastQuoteSnapshot.getBid(2), 99.9, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(2), 300);
		assertEquals(lastQuoteSnapshot.getBid(3), 99.8, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(3), 300);
		assertEquals(lastQuoteSnapshot.getBid(4), 99.7, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(4), 400);
		assertEquals(lastQuoteSnapshot.getBid(5), 99.6, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(5), 500);
		
		assertEquals(lastQuoteSnapshot.getAsk(0), 100.2, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 1000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 100.3, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 2000);
		assertEquals(lastQuoteSnapshot.getAsk(2), 100.4, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(2), 2500);
		assertEquals(lastQuoteSnapshot.getAsk(3), 100.5, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(3), 3000);


		lob.unsubscribeTradeEvent(tcb);
		
        
	}
	
	
	
}
