package com.yksi7417.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.yksi7417.simulator.LimitOrder.Side;

public class TestLimitOrderBook {
	
	LimitOrderFactory lobFactory = new LimitOrderFactory();

	
	@Before
	public void init(){
	}
	
	@Test
	public void expectEmptyBook() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertTrue(lastQuoteSnapshot.isEmpty());
	}

	@Test
	public void placeBidOrderToAnEmptyBook_expectBidOrderIsBestBid() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		lob.placeOrder(lobFactory.create(Side.BUY, 10000, 9.87));
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 10000);
	}

	@Test
	public void placeBidOrderToSamePrice_expectBidSizeWouldAggregate() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		lob.placeOrder(lobFactory.create(Side.BUY, 10000, 9.87));
		lob.placeOrder(lobFactory.create(Side.BUY, 5000, 9.87));
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
	}

	@Test
	public void placeBidOrderToDifferentPrice_expectBidSizeWouldAggregateAndThereWillBeTwoLevels() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		lob.placeOrder(lobFactory.create(Side.BUY, 10000, 9.87));
		lob.placeOrder(lobFactory.create(Side.BUY, 5000, 9.87));
		lob.placeOrder(lobFactory.create(Side.BUY, 8000, 9.86));
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
	}

	@Test
	public void placeBidOrderToDifferentPrice_expectBidSizeWouldAggregateAndThereWillBeTwoLevelsOrderDoesnotMatter() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		lob.placeOrder(lobFactory.create(Side.BUY, 10000, 9.87));
		lob.placeOrder(lobFactory.create(Side.BUY, 8000, 9.86));
		lob.placeOrder(lobFactory.create(Side.BUY, 5000, 9.87));
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
	}

	@Test
	public void placeAskOrderToDifferentPrice_expectAskSizeWouldAggregateAndThereWillBeTwoLevelsOrderDoesnotMatter() {
		LimitOrderBook lob = buildDefaultLOB();
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 35000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 18000);
	}

	@Test
	public void placeBuyOrderAtBestAsk_expectTradeEvent() {
		LimitOrderBook lob = buildDefaultLOB();
		CompletableFuture<Trade> future = new CompletableFuture<>();
		Consumer<Trade> tcb = trade -> future.complete(trade); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a trade that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 1000, 9.88));
		
		try {
			Trade trade = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trade.getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trade.getSize(), 1000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		lob.unsubscribeTradeEvent(tcb);
		
	}

	private LimitOrderBook buildDefaultLOB() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		lob.placeOrder(lobFactory.create(Side.SELL, 20000, 9.88));
		lob.placeOrder(lobFactory.create(Side.BUY, 10000, 9.87));
		lob.placeOrder(lobFactory.create(Side.SELL, 18000, 9.89));
		lob.placeOrder(lobFactory.create(Side.BUY, 8000, 9.86));
		lob.placeOrder(lobFactory.create(Side.SELL, 15000, 9.88));
		lob.placeOrder(lobFactory.create(Side.BUY, 5000, 9.87));
		return lob;
	}
}
