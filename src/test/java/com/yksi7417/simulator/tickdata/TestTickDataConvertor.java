package com.yksi7417.simulator.tickdata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.Queue;

import org.junit.Test;

import com.yksi7417.simulator.LimitOrder;
import com.yksi7417.simulator.LimitOrder.Side;
import com.yksi7417.simulator.PriceUtils;

public class TestTickDataConvertor {
	
	@Test
	public void firstQuote_expectListOfLimitOrders() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
        assertThat(limitOrders, hasSize(10));

        int i = 0; 
		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
        	Side side = i < 5 ? Side.BUY : Side.SELL;
	        assertEquals(side, limitOrder.getSide());
	        assertEquals(rawPxData[i], limitOrder.getPrice(), PriceUtils.Epsilon);
	        assertEquals(rawSizeData[i], limitOrder.getQty());
	        i++;
		}
        
	}

	@Test
	public void secondQuoteOfIncreasedBestBid_expectOneOrderOnly() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
        assertThat(limitOrders, hasSize(10));

		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			// some processing blahblahblah
		}

        assertThat(limitOrders, hasSize(0));
		int [] rawSizeData1 = { 500, 400, 300, 300, 700, 500, 1000, 2000, 2500, 3000 };
		
		convertor.applyLatestTickData(rawPxData, rawSizeData1);
        assertThat(limitOrders, hasSize(1));
		LimitOrder limitOrder = limitOrders.poll();
        assertThat(limitOrders, hasSize(0));

		assertEquals(Side.BUY, limitOrder .getSide());
        assertEquals(600, limitOrder.getQty());
        assertEquals(100.0, limitOrder.getPrice(), PriceUtils.Epsilon);

	}

	@Test
	public void secondQuoteOfIncreasedBestBidAndBestAsk_expectTwoOrders() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
		
		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			// some processing blahblahblah
		}

		double [] rawPxData1 = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData1 = { 500, 400, 300, 300, 200, 1500, 1000, 2000, 2500, 3000 };
		
		convertor.applyLatestTickData(rawPxData1, rawSizeData1);
        assertThat(limitOrders, hasSize(2));

		LimitOrder limitOrder = limitOrders.poll();
		assertEquals(Side.BUY, limitOrder .getSide());
        assertEquals(100.0, limitOrder.getPrice(), PriceUtils.Epsilon);
        assertEquals(100, limitOrder.getQty());

		limitOrder = limitOrders.poll();
		assertEquals(Side.SELL, limitOrder .getSide());
        assertEquals(100.1, limitOrder.getPrice(), PriceUtils.Epsilon);
        assertEquals(1000, limitOrder.getQty());

	}
	
	@Test
	public void secondQuoteBestBidIncreaseOneTick_expectOneBigBuyOrderToClearTheAsk() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
        assertThat(limitOrders, hasSize(10));

		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			// some processing blahblahblah
		}

        assertThat(limitOrders, hasSize(0));
		double [] rawPxData1 = {99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5, 100.6};
		int [] rawSizeData1 = { 400, 300, 300, 100, 2700, 1000, 2000, 2500, 3000, 5000 };
		
		convertor.applyLatestTickData(rawPxData1, rawSizeData1);
        assertThat(limitOrders, hasSize(2));
		LimitOrder limitOrder = limitOrders.poll();

		assertEquals(Side.BUY, limitOrder .getSide());
        assertEquals(3200, limitOrder.getQty());
        assertEquals(100.1, limitOrder.getPrice(), PriceUtils.Epsilon);

        assertThat(limitOrders, hasSize(1));
		limitOrder = limitOrders.poll();

		assertEquals(Side.SELL, limitOrder .getSide());
        assertEquals(5000, limitOrder.getQty());
        assertEquals(100.6, limitOrder.getPrice(), PriceUtils.Epsilon);
        
		Queue<LimitOrder> cancelOrders = convertor.getCancelOrders();
        assertThat(cancelOrders, hasSize(1));
        
		LimitOrder cancelOrder = cancelOrders.poll();
		assertEquals(Side.BUY, cancelOrder .getSide());
        assertEquals(500, cancelOrder.getQty());
        assertEquals(99.6, cancelOrder.getPrice(), PriceUtils.Epsilon);
        
		
        
	}
	
	@Test
	public void secondQuoteBestAskDecreaseOneTick_expectOneBigSellOrderToClearTheBid() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
        assertThat(limitOrders, hasSize(10));

		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			// some processing blahblahblah
		}

        assertThat(limitOrders, hasSize(0));
		double [] rawPxData1 = {99.5, 99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4};
		int [] rawSizeData1 = { 100, 500, 400, 300, 300, 8400, 500, 1000, 2000, 2500};
		
		convertor.applyLatestTickData(rawPxData1, rawSizeData1);
        assertThat(limitOrders, hasSize(2));
		LimitOrder limitOrder = limitOrders.poll();

		assertEquals(Side.BUY, limitOrder .getSide());
        assertEquals(100, limitOrder.getQty());
        assertEquals(99.5, limitOrder.getPrice(), PriceUtils.Epsilon);

        assertThat(limitOrders, hasSize(1));
		limitOrder = limitOrders.poll();

		assertEquals(Side.SELL, limitOrder .getSide());
        assertEquals(8500, limitOrder.getQty());
        assertEquals(100.0, limitOrder.getPrice(), PriceUtils.Epsilon);
        
		Queue<LimitOrder> cancelOrders = convertor.getCancelOrders();
        assertThat(cancelOrders, hasSize(1));
        
		LimitOrder cancelOrder = cancelOrders.poll();
		assertEquals(Side.SELL, cancelOrder .getSide());
        assertEquals(3000, cancelOrder.getQty());
        assertEquals(100.5, cancelOrder.getPrice(), PriceUtils.Epsilon);
        
	}

	@Test
	public void secondQuoteOfDecreaseBestBidAndBestAsk_expectTwoCancelAndTwoNewOrders() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 200, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		Queue<LimitOrder> limitOrders = convertor.getLimitOrders();
		
		while (!limitOrders.isEmpty()) {
			LimitOrder limitOrder = limitOrders.poll();
			// some processing blahblahblah
		}

		double [] rawPxData1 = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData1 = { 500, 400, 300, 300, 100, 100, 1000, 2000, 2500, 3000 };
		
		convertor.applyLatestTickData(rawPxData1, rawSizeData1);
        assertThat(limitOrders, hasSize(2));

		LimitOrder limitOrder = limitOrders.poll();
		assertEquals(Side.BUY, limitOrder .getSide());
        assertEquals(100.0, limitOrder.getPrice(), PriceUtils.Epsilon);
        assertEquals(100, limitOrder.getQty());

		limitOrder = limitOrders.poll();
		assertEquals(Side.SELL, limitOrder .getSide());
        assertEquals(100.1, limitOrder.getPrice(), PriceUtils.Epsilon);
        assertEquals(100, limitOrder.getQty());

		Queue<LimitOrder> cancelOrders = convertor.getCancelOrders();
        assertThat(cancelOrders, hasSize(2));
        
		LimitOrder cancelOrder = cancelOrders.poll();
		assertEquals(Side.BUY, cancelOrder .getSide());
        assertEquals(200, cancelOrder.getQty());
        assertEquals(100, cancelOrder.getPrice(), PriceUtils.Epsilon);

        cancelOrder = cancelOrders.poll();
		assertEquals(Side.SELL, cancelOrder .getSide());
        assertEquals(500, cancelOrder.getQty());
        assertEquals(100.1, cancelOrder.getPrice(), PriceUtils.Epsilon);
        
        
	}
	
	
}
