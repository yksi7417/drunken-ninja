package com.yksi7417.simulator.tickdata;

import com.yksi7417.simulator.LimitOrder;

/***
 * Return a LimitOrder object as sequence of events from a data source, could be file, database
 * return null for end of stream. 
 * 
 * @author asimoneta
 *
 */

public interface TickDataSource {
	public LimitOrder next();
}
