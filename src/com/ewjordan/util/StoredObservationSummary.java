package com.ewjordan.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Stores observations and calculates summary statistics.
 * This class is able to calculate distributional characteristics
 * apart from mean and variance because it stores the observations
 * in addition to the summary statistics.
 * 
 * @author eric
 */
public class StoredObservationSummary extends ObservationSummary {
	protected DoubleList observations;
	private List<Double> sortedObservations;
	private boolean isSorted;
	
	/**
	 * Constructor.
	 */
	public StoredObservationSummary() {
		super();
		observations = new DoubleList();
		isSorted = false;
	}
	
	/**
	 * @see com.ewjordan.PaddyPower.Analysis.ObservationSummary#add(double)
	 */
	@Override
	public void add(double value) {
		observations.add(value);
		super.add(value);
		isSorted = false;
	}
	
	public DoubleList getObservations() {
		return observations;
	}
	
	private void sortObservations() {
		if (isSorted) return;
		sortedObservations = new ArrayList<Double>();
		for (int i=0; i<observations.size(); ++i) {
			sortedObservations.add(observations.get(i));
		}
		Collections.sort(sortedObservations);
		isSorted = true;
	}
	
	/** Return the median value of the distribution. */
	public double getMedian() {
		sortObservations();
		int size = sortedObservations.size();
		if (size == 0) return 0.0;
		if (size % 2 == 0) return .5*(sortedObservations.get(size/2) + sortedObservations.get(size/2-1));
		else return sortedObservations.get(size/2);
	}
	
	/** 
	 * Return the proportion of observations that are lower than this value.
	 */
	public double getProportionAtValue(double value) {
		//TODO: test this
		sortObservations();
		if (sortedObservations.size() == 0) return 0.0;
		if (sortedObservations.size() == 1) return (value > sortedObservations.get(0)) ? 1.0 : 0.0;
		int size = sortedObservations.size();
		int lastIndexBelow = -1;
		int firstIndexAbove = size;
		for (int i=0; i<size; ++i) {
			double d = sortedObservations.get(i);
			if (d < value) lastIndexBelow = i;
			if (d > value && firstIndexAbove == size) firstIndexAbove = i;
		}
//		System.out.println(lastIndexBelow + " " + firstIndexAbove + " " + value);
		if (firstIndexAbove >= size) return 1.0;
		if (lastIndexBelow <= -1) return 0.0;
		double proportion = (lastIndexBelow+1.0) / size;
//		double effectiveIndex = MathUtil.map(value, sortedObservations.get(lastIndexBelow), sortedObservations.get(firstIndexAbove), lastIndexBelow, firstIndexAbove);
//		double proportion = MathUtil.map(effectiveIndex, 0, size, 0.0, 1.0);
		return proportion;
	}
	
	/** 
	 * Return the value at the given fractional point of the cumulative distribution.
	 * For example, getValueAtProportion(0.5) will give the median of the distribution.
	 */
	public double getValueAtProportion(double fraction) {
		if (sortedObservations == null) return 0.0;
		if (sortedObservations.size() == 0) return 0.0;
		if (sortedObservations.size() == 1) return sortedObservations.get(0);
		sortObservations();
		int size = sortedObservations.size();
		int lowIndex = (int) (fraction * (size-1));
		int highIndex = lowIndex + 1;
		
		//catch bad parameters or near-the-edge glitches (fraction ~= 1, mainly)
		if (lowIndex < 0) lowIndex = 0;
		if (highIndex < 0) highIndex = 0;
		if (lowIndex >= size) lowIndex = size-1;
		if (highIndex >= size) highIndex = size-1;
		
		double proportion = fraction*(size-1) - lowIndex;
		double lowValue = sortedObservations.get(lowIndex);
		double highValue = sortedObservations.get(highIndex);
		//System.out.println(proportion + " " +lowValue + " " +highValue + ": "+lowIndex+" "+highIndex);
		return MathUtil.map(proportion, 0, 1, lowValue, highValue);
	}
	
	public void printResults() {
		System.out.println("Mean: "+getArithmeticMean());
		System.out.println("Stdev: "+getStandardDeviation());
		System.out.println("Median: "+getMedian());
		System.out.println("Confidence interval of mean @ 95%: +/- " + getMeanConfidence(0.95));
		double[] doubles = ArrayUtil.toDoubleArray(observations);
		DoubleHistogram histo = new DoubleHistogram(doubles, 20);
		histo.setTitle("Distribution of observations");
		histo.printTextRepresentation(80);
	}
	
	public void printResults(String numberFormatString) {
		System.out.println("Mean: "+getArithmeticMean());
		System.out.println("Stdev: "+getStandardDeviation());
		System.out.println("Median: "+getMedian());
		System.out.println("Confidence interval of mean @ 95%: +/- " + getMeanConfidence(0.95));
		double[] doubles = ArrayUtil.toDoubleArray(observations);
		DoubleHistogram histo = new DoubleHistogram(doubles, 20);
		histo.setTitle("Distribution of observations");
		histo.printTextRepresentation(80, numberFormatString);
	}
	
	public DoubleHistogram getDoubleHistogram(int nBuckets) {
		double[] doubles = ArrayUtil.toDoubleArray(observations);
		return new DoubleHistogram(doubles, nBuckets);
	}
	
	public DoubleHistogram getDoubleHistogram(int nBuckets, double minVal, double maxVal) {
		double[] doubles = ArrayUtil.toDoubleArray(observations);
		return new DoubleHistogram(doubles, nBuckets, minVal, maxVal);
	}

	/** Tests. */
	static public void main(String[] args) {
		StoredObservationSummary summary = new StoredObservationSummary();
		Random random = new Random();
		for (int i=0; i < 1000001; ++i) {
			double rand = random.nextGaussian();
//			rand = rand*rand;
			summary.add(rand);
		}
		System.out.println("Mean is "+summary.getArithmeticMean());
		System.out.println("Stdev is "+summary.getStandardDeviation());
		System.out.println("Median is "+summary.getMedian());
		System.out.println("which should equal "+summary.getValueAtProportion(0.5));
		System.out.println("Confidence interval of mean @ 95% is +/- " + summary.getMeanConfidence(0.95));
		System.out.println("Distribution value at 95% is " + summary.getValueAtProportion(0.95));
		
		double[] doubles = ArrayUtil.toDoubleArray(summary.observations);
		DoubleHistogram histo = new DoubleHistogram(doubles, 30);
		histo.printTextRepresentation(80);
		
	}
}