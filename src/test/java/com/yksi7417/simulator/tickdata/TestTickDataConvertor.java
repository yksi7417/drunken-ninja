package com.yksi7417.simulator.tickdata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.yksi7417.simulator.LimitOrder;
import com.yksi7417.simulator.PriceUtils;
import com.yksi7417.simulator.LimitOrder.Side;

public class TestTickDataConvertor {
	
	@Test
	public void firstQuote_expectListOfLimitOrders() {
		double [] rawPxData = {99.6 ,99.7 , 99.8,  99.9 , 100,  100.1,  100.2,  100.3, 100.4, 100.5};
		int [] rawSizeData = { 500, 400, 300, 300, 100, 500, 1000, 2000, 2500, 3000 };
		
		TickDataConvertor convertor = new TickDataConvertor("0700.HK");
		convertor.applyLatestTickData(rawPxData, rawSizeData);
		List<LimitOrder> limitOrders = convertor.getLimitOrders();
        assertThat(limitOrders, hasSize(10));
        for (int i=0; i<10; i++) {
        	Side side = i < 5 ? Side.BUY : Side.SELL;
	        assertEquals(side, limitOrders.get(i).getSide());
	        assertEquals(rawPxData[i], limitOrders.get(i).getPrice(), PriceUtils.Epsilon);
	        assertEquals(rawSizeData[i], limitOrders.get(i).getQty());
        }

	}

}
