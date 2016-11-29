package com.yksi7417.simulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.yksi7417.simulator.LimitOrder.Side;

public class TestLimitOrderBook {
	
	LimitOrderFactory lobFactory = new LimitOrderFactory();

	
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
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a trade that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 1000, 9.88));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 1);
			Trade trade = trades.get(0);
			assertEquals(trade.getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trade.getSize(), 1000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trade
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 34000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 18000);
		
		lob.unsubscribeTradeEvent(tcb);
		
	}

	@Test
	public void placeBuyBigOrderAtBestAsk_expectTwoTradeEvent() {
		LimitOrderBook lob = buildDefaultLOB();
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a big order, should create 2 trade events that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 34000, 9.88));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 2);
			assertEquals(trades.get(0).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(0).getSize(), 20000);
			assertEquals(trades.get(1).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(1).getSize(), 14000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trade
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 1000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 18000);
		
		lob.unsubscribeTradeEvent(tcb);
		
	}

	@Test
	public void placeBuyVeryBigOrderAtBestAsk_expectChangeOfBestBidAndBestAsk() {
		LimitOrderBook lob = buildDefaultLOB();
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a big order, should create 2 trade events that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 40000, 9.88));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 2);
			assertEquals(trades.get(0).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(0).getSize(), 20000);
			assertEquals(trades.get(1).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(1).getSize(), 15000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trades
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 5000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 15000);
		assertEquals(lastQuoteSnapshot.getBid(2), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(2), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 18000);
		
		lob.unsubscribeTradeEvent(tcb);
		
	}

	@Test
	public void placeBuyVeryBigOrderAtOneLevelBetterThanAsk_expectTradeWithDifferentPrice() {
		LimitOrderBook lob = buildDefaultLOB();
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a big order, should create 2 trade events that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.BUY, 40000, 9.89));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 3);
			assertEquals(trades.get(0).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(0).getSize(), 20000);
			assertEquals(trades.get(1).getPrice(), 9.88, PriceUtils.Epsilon);
			assertEquals(trades.get(1).getSize(), 15000);
			assertEquals(trades.get(2).getPrice(), 9.89, PriceUtils.Epsilon);
			assertEquals(trades.get(2).getSize(), 5000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trades
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 13000);
		
		lob.unsubscribeTradeEvent(tcb);
		
	}

	@Test
	public void placeSellVeryBigOrderAtBestBid_expectChangeOfBestBidAndBestAsk() {
		LimitOrderBook lob = buildDefaultLOB();
		CompletableFuture<List<Trade>> future = new CompletableFuture<>();
		Consumer<List<Trade>> tcb = trades -> future.complete(trades); 
		lob.subscribeTradeEvent(tcb);
		
		// this is a big order, should create 2 trade events that would cause a match in order book
		lob.placeOrder(lobFactory.create(Side.SELL, 40000, 9.87));
		
		try {
			List<Trade> trades = future.get(200, TimeUnit.MILLISECONDS);
			assertEquals(trades.size(), 2);
			assertEquals(trades.get(0).getPrice(), 9.87, PriceUtils.Epsilon);
			assertEquals(trades.get(0).getSize(), 10000);
			assertEquals(trades.get(1).getPrice(), 9.87, PriceUtils.Epsilon);
			assertEquals(trades.get(1).getSize(), 5000);
		} catch (Exception e) {
			fail("failed during asserting of price & size " + e);
			e.printStackTrace();
		}
		
		// order book is updated accordingly, taking away the last trades
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 25000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 35000);
		assertEquals(lastQuoteSnapshot.getAsk(2), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(2), 18000);
		
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
	
	@Test
	public void cancelOutstandingOrder_expectReductionOfOrderSizeAtThatPriceLevel() {
		LimitOrderBook lob = buildDefaultLOB();
		
		LimitOrder testOrder = lobFactory.create(Side.SELL, 40000, 9.88);
		lob.placeOrder(testOrder);
		
		// order book is updated accordingly, taking away the last trades
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertFalse(lastQuoteSnapshot.isEmpty());
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 75000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 18000);
		
		boolean cancelSuccess = lob.cancelOrder(testOrder);
		assertTrue(cancelSuccess);

		lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertEquals(lastQuoteSnapshot.getBid(0), 9.87, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(0), 15000);
		assertEquals(lastQuoteSnapshot.getBid(1), 9.86, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getBidSize(1), 8000);
		assertEquals(lastQuoteSnapshot.getAsk(0), 9.88, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(0), 35000);
		assertEquals(lastQuoteSnapshot.getAsk(1), 9.89, PriceUtils.Epsilon);
		assertEquals(lastQuoteSnapshot.getAskSize(1), 18000);
		
		
	}
	
	
}
