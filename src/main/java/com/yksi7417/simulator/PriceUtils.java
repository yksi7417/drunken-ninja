package com.yksi7417.simulator;

public class PriceUtils {
	public static double Epsilon = 1e-6; 
	public static boolean isEqual(double d1, double d2){
		return Math.abs(d1 - d2) < Epsilon;
	}
}
