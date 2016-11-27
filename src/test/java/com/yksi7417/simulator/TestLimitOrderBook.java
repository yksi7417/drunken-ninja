package com.yksi7417.simulator;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.yksi7417.simulator.marketdata.Quote;

public class TestLimitOrderBook {

	
	@Before
	public void init(){
	}
	
	@Test
	public void expectEmptyBook() {
		LimitOrderBook lob = new LimitOrderBook("0700.HK"); 
		Quote lastQuoteSnapshot = lob.getQuoteSnapshot();
		assertTrue(lastQuoteSnapshot.isEmpty());
	}

}
