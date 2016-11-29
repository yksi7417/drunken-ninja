package com.yksi7417.simulator.tickdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.yksi7417.simulator.LimitOrder;
import com.yksi7417.simulator.LimitOrder.Side;

/** 
 * Convert Tick Data from array format -> LimitOrders
 * @author SI
 *
 */

public class TickDataConvertor {

	final DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS");
	final int timestep = 100; 

	// internal clock that keep moving forward as there is new order coming; 
	DateTime now = dtf.parseDateTime("11/28/2016 09:30:00.000");
	
	private String ticker; 

	public TickDataConvertor(String ticker) {
		super();
		this.ticker = ticker;
	}

	ListMultimap<Integer, LimitOrder> myBuyOrders = ArrayListMultimap.create();
	ListMultimap<Integer, LimitOrder> mySellOrders = ArrayListMultimap.create();
	Map<Integer,Integer> bidSnapshot = new HashMap<>();
	Map<Integer,Integer> askSnapshot = new HashMap<>();

	List<LimitOrder> newlimitOrders = new ArrayList<>();
	List<Integer> cancelOrderIds = new ArrayList<>();


	/***
	   for (President pres : US_PRESIDENTS_IN_ORDER) {
	     multimap.put(pres.firstName(), pres.lastName());
	   }
	   for (String firstName : multimap.keySet()) {
	     List<String> lastNames = multimap.get(firstName);
	     out.println(firstName + ": " + lastNames);
	   }
	***/

	/*
	// assumption on behaviour 
		* if bid -> bid / ask -> ask , increase size -> add new order 
		* if bid -> bid / ask -> ask , decrease size -> find all orders of this price level, cancel and new 
		* if bid -> ask / ask -> bid , create an order big enough to fill the previous queue + put order in.  
	*/
	
	private int castKey(double price) {
		return (int) (price * 1000000); 
	}
	
	private LimitOrder determineLimitOrder(Side side, int qty, double px) {
		LimitOrder order = new LimitOrder(side, qty, px, now);
		now = now.plusMillis(timestep);
		return order;
	}
	
	public void applyLatestTickData(double[] rawPxData, int[] rawSizeData) {

		for (int i=0; i<rawPxData.length; i++) {
			Map<Integer,Integer> thisSideSnapshot = bidSnapshot;
			Map<Integer,Integer> otherSideSnapshot = askSnapshot;
			Side side = Side.BUY;
			
			if (i >= rawPxData.length /2) {
				side = Side.SELL;
				thisSideSnapshot = askSnapshot;
				otherSideSnapshot = bidSnapshot;
			}

			Integer pxKey = castKey(rawPxData[i]);
			int sizeDifference = 0;
			if (thisSideSnapshot.containsKey(pxKey)) {
				int prevSize = thisSideSnapshot.get(pxKey);
				sizeDifference = rawSizeData[i] - prevSize ;
			}
			else if (otherSideSnapshot.containsKey(pxKey)) {
				int prevSize = otherSideSnapshot.get(pxKey);
				sizeDifference = rawSizeData[i] + prevSize ;
			}
			else 
				sizeDifference = rawSizeData[i];	

			if (sizeDifference > 0)
				newlimitOrders.add(determineLimitOrder(side, sizeDifference, rawPxData[i]));
			else if (sizeDifference < 0) {
//				cancelOrders.add(findListOfOrdersThatMatch(i, rawPxData[i]));
				newlimitOrders.add(determineLimitOrder(side, rawSizeData[i], rawPxData[i]));
			}
		}

	}
	
	public String getTicker() {
		return ticker;
	}

	public List<LimitOrder> getLimitOrders() {
		return newlimitOrders;
	}

}
