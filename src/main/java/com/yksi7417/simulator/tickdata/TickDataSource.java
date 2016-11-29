package com.yksi7417.simulator.tickdata;

import java.util.Queue;

import com.yksi7417.simulator.LimitOrder;

public interface TickDataSource {

	public abstract void applyLatestTickData(double[] rawPxData,
			int[] rawSizeData);

	public abstract String getTicker();

	public abstract Queue<LimitOrder> getLimitOrders();

	public abstract Queue<LimitOrder> getCancelOrders();

}