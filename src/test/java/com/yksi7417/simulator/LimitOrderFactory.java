package com.yksi7417.simulator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.yksi7417.simulator.LimitOrder.Side;

/**
 * Assume to be use in a single thread environment;  not thread-safe 
 * utility object that simplify creation of LimitOrderObject by keeping internal clock
 * move clock forward everytime a new order is created without timestamp; 
 * @author asimoneta
 *
 */

public class LimitOrderFactory {
	final DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS");
	final int timestep = 100; 

	// internal clock that keep moving foward as there is new order coming; 
	DateTime now = dtf.parseDateTime("11/28/2016 09:30:00.000");

	public LimitOrder create(Side side, int qty, double px) {
		LimitOrder order = new LimitOrder(side, qty, px, now);
		now = now.plusMillis(timestep);
		return order; 
	}
	
}
