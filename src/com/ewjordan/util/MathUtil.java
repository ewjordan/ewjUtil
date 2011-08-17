package com.ewjordan.util;

import java.util.Arrays;

public class MathUtil {
	/** Linearly maps a value from one range to another. */
	public static double map(double val, double fromMin, double fromMax, double toMin, double toMax) {
		double mult = (val - fromMin) / (fromMax-fromMin);
		double res = toMin + mult*(toMax-toMin);
		return res;
	}
	
	public static double findFractile(double fract, double[] vals) {
		Arrays.sort(vals);
		double dindex = MathUtil.map(fract, 0.0, 1.0, 0, vals.length-1);
		int indexLo = (int)dindex;
		int indexHi = indexLo + 1;
		if (indexLo < 0) indexLo = 0;
		if (indexHi > vals.length-1) indexHi = vals.length-1;
		double resid = dindex - indexLo;
		return MathUtil.map(resid, 0, 1, vals[indexLo], vals[indexHi]);
	}
}
