package com.yksi7417.simulator.tickdata;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.yksi7417.simulator.LimitOrder;
import com.yksi7417.simulator.LimitOrder.Side;

/**
 * Assume to be use in a single thread environment;  not thread-safe 
 * a stub tick data source that returns a list of orders sequentially for testing
 * return null if end of stream
 * @author asimoneta
 *
 */

public class StubTickDataSource implements TickDataSource {

	private final List<LimitOrder> limitOrders = new ArrayList<LimitOrder>();
	private final ListIterator<LimitOrder> iter;
	
	public StubTickDataSource() {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS");
		DateTime now = dtf.parseDateTime("11/28/2016 09:30:00.000");
		limitOrders.add(new LimitOrder(Side.BUY, 10000, 9.87, now));
		limitOrders.add(new LimitOrder(Side.BUY, 10000, 9.86, now));
		limitOrders.add(new LimitOrder(Side.BUY, 10000, 9.85, now));
		limitOrders.add(new LimitOrder(Side.BUY, 10000, 9.84, now));
		limitOrders.add(new LimitOrder(Side.BUY, 10000, 9.83, now));
		limitOrders.add(new LimitOrder(Side.SELL, 10000, 9.88, now));
		limitOrders.add(new LimitOrder(Side.SELL, 10000, 9.89, now));
		limitOrders.add(new LimitOrder(Side.SELL, 10000, 9.90, now));
		limitOrders.add(new LimitOrder(Side.SELL, 10000, 9.91, now));
		limitOrders.add(new LimitOrder(Side.SELL, 10000, 9.92, now));
		now = now.plusMillis(1);
		iter = limitOrders.listIterator();
	}
	
	public LimitOrder next() {
		if (iter.hasNext()) 
			return iter.next();
		return null; 
	}
	
}
