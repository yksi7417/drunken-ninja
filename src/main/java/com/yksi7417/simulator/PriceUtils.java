package com.yksi7417.simulator;

public class PriceUtils {
	public static double Epsilon = 1e-6; 
	public static boolean isEqual(double d1, double d2){
		return Math.abs(d1 - d2) < Epsilon;
	}
	public static boolean isGreaterThan(double d1, double d2){
		return d1 - d2 > Epsilon;
	}
	public static boolean isLessThan(double d1, double d2){
		return d2 - d1 > Epsilon;
	}
	public static boolean isEqualGreaterThan(double d1, double d2) {
		return isEqual(d1,d2) || isGreaterThan(d1,d2);		
	}
	public static boolean isEqualLessThan(double d1, double d2) {
		return isEqual(d1,d2) || isLessThan(d1,d2);		
	}
}
