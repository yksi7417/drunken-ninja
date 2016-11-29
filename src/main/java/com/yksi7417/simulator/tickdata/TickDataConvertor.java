package com.yksi7417.simulator.tickdata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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

	// internal clock that keep moving forward as there is new order coming; 
	final DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS");
	final int timestep = 100; 
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

	Queue<LimitOrder> newlimitOrders = new LinkedList<>();
	Queue<Integer> cancelOrderIds = new LinkedList<>();


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
	
	private Side getSide(int position, int arrayLength) {
		if (position >= arrayLength /2) 
			return Side.SELL;
		return Side.BUY;
	}
	
	public void applyLatestTickData(double[] rawPxData, int[] rawSizeData) {

		for (int i=0; i<rawPxData.length; i++) {
			Side side = getSide(i, rawPxData.length);
			Map<Integer, Integer> thisSideSnapshot = getThisSideSnapshot(side);
			Map<Integer, Integer> otherSideSnapshot = getOtherSideSnapshot(side);
			Integer pxKey = castKey(rawPxData[i]);
			
			int sizeDifference = getSizeDiff(rawSizeData[i], thisSideSnapshot,
					otherSideSnapshot, pxKey);	

			if (sizeDifference > 0)
				newlimitOrders.add(determineLimitOrder(side, sizeDifference, rawPxData[i]));
			else if (sizeDifference < 0) {
//				cancelOrders.add(findListOfOrdersThatMatch(i, rawPxData[i]));
				newlimitOrders.add(determineLimitOrder(side, rawSizeData[i], rawPxData[i]));
			}
		}
		
		takeSnapshot(rawPxData, rawSizeData);
	}

	private void takeSnapshot(double[] rawPxData, int[] rawSizeData) {
		this.bidSnapshot.clear();
		this.askSnapshot.clear();
		for (int i=0; i<rawPxData.length; i++) {
			Side side = getSide(i, rawPxData.length);
			Map<Integer, Integer> thisSideSnapshot = getThisSideSnapshot(side);
			thisSideSnapshot.put(castKey(rawPxData[i]), rawSizeData[i]);
		}
	}

	private int getSizeDiff(int thisSize, 
			Map<Integer, Integer> thisSideSnapshot,
			Map<Integer, Integer> otherSideSnapshot, Integer pxKey) {
		int sizeDifference = 0;
		if (thisSideSnapshot.containsKey(pxKey)) {
			int prevSize = thisSideSnapshot.get(pxKey);
			sizeDifference = thisSize - prevSize ;
		}
		else if (otherSideSnapshot.containsKey(pxKey)) {
			int prevSize = otherSideSnapshot.get(pxKey);
			sizeDifference = thisSize + prevSize ;
		}
		else 
			sizeDifference = thisSize;
		return sizeDifference;
	}

	private Map<Integer, Integer> getOtherSideSnapshot(Side side) {
		if (Side.SELL.equals(side)) return bidSnapshot;
		return askSnapshot;
	}

	private Map<Integer, Integer> getThisSideSnapshot(Side side) {
		if (Side.SELL.equals(side)) return askSnapshot;
		return bidSnapshot;
	}
	
	public String getTicker() {
		return ticker;
	}

	public Queue<LimitOrder> getLimitOrders() {
		return newlimitOrders;
	}

}
