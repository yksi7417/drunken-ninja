package com.yksi7417.simulator;

import static org.junit.Assert.*;

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

}
