package com.yksi7417.simulator;

public class Trade {
	int size; 
	double price;
	public Trade(int size, double price) {
		super();
		this.size = size;
		this.price = price;
	}
	public int getSize() {
		return size;
	}
	public double getPrice() {
		return price;
	} 
	
}
