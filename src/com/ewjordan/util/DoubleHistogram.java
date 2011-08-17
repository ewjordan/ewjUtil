/**
 * 
 */
package com.ewjordan.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author eric
 *
 */
public class DoubleHistogram {
	
	static private boolean DEBUG = false;
	
	private double min;
	private double max;
	private double delta; //what range does each bucket contain?
	private int maxCount; //how many members does the most filled bucket contain?
	private int maxCountIndex; //which bucket contains the most members?
	private int[] buckets;
	private int totalMembers;
	
	private String title;
	
	
	/**
	 * @param vals
	 * @param nBuckets
	 */
	public DoubleHistogram(final double[] vals, final int nBuckets) {
		this(vals, nBuckets, min(vals), max(vals));
	}
	
	/**
	 * @param vals
	 * @param nBuckets
	 * @param minVal
	 * @param maxVal
	 */
	public DoubleHistogram(final double[] vals, final int nBuckets, final double minVal, final double maxVal) {
		buckets = new int[nBuckets];
		min = minVal;
		max = maxVal;
		maxCount = 0;
		maxCountIndex = -1;
		totalMembers = 0;
		title = "doubleHistogram";
		delta = (maxVal - minVal) / nBuckets;
		clearAndFillBuckets(vals);
	}
	
	/** 
	 * Set the bucket contents, premultiplying and then rounding to handle double error.
	 */
	public void setBuckets(final double[] bucketContents, double premultiplier) {
		buckets = new int[bucketContents.length];
		maxCount = 0;
		maxCountIndex = -1;
		totalMembers = 0;
		for (int i=0; i<buckets.length; ++i) {
			buckets[i] = (int)Math.round(premultiplier*bucketContents[i]);
			totalMembers += buckets[i];
			if (buckets[i] > maxCount) {
				maxCount = buckets[i];
				maxCountIndex = i;
			}
		}
		title = "doubleHistogram";
		delta = (max - min) / buckets.length;
	}
	
	public int getNumberOfObservations() {
		int sum = 0;
		for (int i=0; i<buckets.length; ++i) {
			sum += buckets[i];
		}
		return sum;
	}
	
	/**
	 * @return lowest value in range
	 */
	public double getMinValue() {
		return min;
	}
	
	/**
	 * @return highest value in range
	 */
	public double getMaxValue() {
		return max;
	}
	
	/**
	 * @return maximum number of members in a bucket
	 */
	public int getMaxCount() {
		return maxCount;
	}
	
	/**
	 * @return which bucket has the most members?
	 */
	public int getMaxCountIndex() {
		return maxCountIndex;
	}
	
	/**
	 * @return the bucket membership array
	 */
	public int[] getBuckets() {
		return buckets;
	}
	
	public List<Double> bucketsToDoubleList() {
		List<Double> doubles = new ArrayList<Double>();
		for (int i=0; i<buckets.length; ++i) {
			doubles.add((double)buckets[i]);
		}
		return doubles;
	}
	
	/**
	 * Fill the buckets based on the values.
	 * Clears buckets before filling.
	 * @param vals
	 */
	public void clearAndFillBuckets(double[] vals) {
		Arrays.fill(buckets, 0);
		maxCount = 0;
		totalMembers = 0;
		addValues(vals);
	}
	
	/**
	 * Add members to buckets.
	 * @param vals
	 */
	public void addValues(double ... vals) {
		for (double f:vals) {
			addValue(f);
		}
	}
	
	/**
	 * Add an additional member to the buckets.
	 * @param val
	 */
	public void addValue(double val) {
		int bucketToInc = (int)((val - min) / delta);
		if (bucketToInc < 0) {
			if (DEBUG) System.out.println("lt0: "+bucketToInc);
			bucketToInc = 0;
		} else if (bucketToInc >= buckets.length) {
			if (DEBUG) System.out.println("gtl: "+bucketToInc);
			bucketToInc = buckets.length - 1;
		}
		buckets[bucketToInc] += 1;
		if (buckets[bucketToInc] > maxCount) {
			maxCount = buckets[bucketToInc];
			maxCountIndex = bucketToInc;
		}
		++totalMembers;
	}
	
	private static double min(double[] vals) {
		double minVal = Double.MAX_VALUE;
		for (int i=0; i<vals.length; ++i) {
			if (vals[i] < minVal) minVal = vals[i];
		}
		return minVal;
	}
	
	private static double max(double[] vals) {
		double maxVal = -Double.MAX_VALUE;
		for (int i=0; i<vals.length; ++i) {
			if (vals[i] > maxVal) maxVal = vals[i];
		}
		return maxVal;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toExcelTSV() {
		String res = "Min\tMax\tCount\n";
		for (int i=0; i<buckets.length; ++i) {
			double minValInBucket = min + this.delta * i;
			double maxValInBucket = min + this.delta * (i+1);
			res += minValInBucket+"\t"+maxValInBucket+"\t"+buckets[i]+"\n";
		}
		return res;
	}
	
	public String toExcelTSVRow() {
		String res = "";
		for (int i=0; i<buckets.length; ++i) {
			if (i != 0) res += "\t";
			res += buckets[i];
		}
		return res;
	}
	
	public String toString() {
		return toString(80);
	}
	
	public String toString(final int charsWidth) {
		return toString(charsWidth, "%3.2f");
	}
	
	public String toString(final int charsWidth, String numberFormatString) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int i=0; i<charsWidth; ++i) {
			sb.append("-");
		}
		sb.append("\n");
		sb.append(getTitle()+" - "+totalMembers+" members.\n");
		for (int i=0; i<charsWidth; ++i) {
			sb.append("-");
		}
		sb.append("\n");
		double scaleFactor = charsWidth * 1.0f / getMaxCount();
		for (int i = 0; i < buckets.length; ++i) {
			double minValInBucket = min + this.delta * i;
			String s = String.format(numberFormatString, minValInBucket);
			s += "\n\t|";
			int ticks = (int) (buckets[i] * scaleFactor);
			for (int j = 0; j < ticks; ++j) {
				s += "#";
			}
			s += " ("+buckets[i]+")";
			sb.append(s + "\n");
		}
		sb.append(String.format("%.2f", (min + this.delta * buckets.length)) + "\n");
		sb.append("\n");
		for (int i=0; i<charsWidth; ++i) {
			sb.append("-");
		}
		sb.append("\n");
		sb.append("Total: "+this.getNumberOfObservations()+" observations.\n");
		for (int i=0; i<charsWidth; ++i) {
			sb.append("-");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void printTextRepresentation(final int charsWidth, String numberFormatString) {
		System.out.println(toString(charsWidth, numberFormatString));
	}
	
	/**
	 * Print an ASCII "picture" of the histogram to standard out.
	 * @param charsWidth
	 */
	public void printTextRepresentation(final int charsWidth) {
		System.out.println(toString(charsWidth));
//		// TODO: turn into toString() method instead of print method...
//		
//		System.out.println("");
//		for (int i=0; i<charsWidth; ++i) {
//			System.out.print("-");
//		}
//		System.out.println();System.out.println(getTitle()+" - "+totalMembers+" members.");
//		for (int i=0; i<charsWidth; ++i) {
//			System.out.print("-");
//		}
//		System.out.println();
//		double scaleFactor = charsWidth * 1.0f / getMaxCount();
//		for (int i = 0; i < buckets.length; ++i) {
//			double minValInBucket = min + this.delta * i;
//			String s = String.format("%3.2f", minValInBucket);
//			s += "\n\t|";
//			int ticks = (int) (buckets[i] * scaleFactor);
//			for (int j = 0; j < ticks; ++j) {
//				s += "#";
//			}
//			s += " ("+buckets[i]+")";
//			System.out.println(s);
//		}
//		System.out.println(String.format("%.2f", (min + this.delta * buckets.length)));
//		System.out.println("");
//		for (int i=0; i<charsWidth; ++i) {
//			System.out.print("-");
//		}
//		System.out.println();
//		System.out.println("Total: "+this.getNumberOfObservations()+" observations.");
//		for (int i=0; i<charsWidth; ++i) {
//			System.out.print("-");
//		}
//		System.out.println();
//		System.out.println("");
	}
}
