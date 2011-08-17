package com.ewjordan.util;

/**
 * A holder for summary statistics for observations of a particular value
 * that stores mean and standard deviation.  Unable to handle medians or
 * other distributional characteristics because the observations are not
 * stored.
 * <p>
 * This class is meant to be used for extremely large numbers of observations -
 * if you have less than a million, more functionality is available in
 * {@link com.ewjordan.PaddyPower.Analysis.StoredObservationSummary}
 * 
 * @author eric
 */
public class ObservationSummary {
	/** Number of observations. */
	protected long observationCount;
	/** Sum of observations. */
	protected double sum;
	/** Product of observations. */
	protected double product;
	/** Sum of squares of observations. */
	protected double sumOfSquares;
	/** Min */
	protected double min;
	/** Max */
	protected double max;
	
	/** Create a new ObservationSummary. */
	public ObservationSummary() {
		sum = 0.0;
		product = 1.0;
		sumOfSquares = 0.0;
		observationCount = 0;
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
	}
	
	/** 
	 * Add a new observation to the summary.
	 * @param num new observation to add
	 */
	public void add(double num) {
		sum += num;
		product *= num;
		sumOfSquares += num*num;
		++observationCount;
		if (num > max) max = num;
		if (num < min) min = num;
	}
	
	/** Add a list of observations to the summary. */
	public void add(double ... nums) {
		for (double num:nums) {
			add(num);
		}
	}
	
	/** Add a boolean value as an observation, with 1 <-> true and 0 <-> false. */
	public void add(boolean bool) {
		add(bool?1.0:0.0);
	}

	/** Get the number of observations. */
	public long getCount() {
		return observationCount;
	}
	
	/** Minimum observation. */
	public double getMin() {
		return min;
	}
	
	/** Maximum observation. */
	public double getMax() {
		return max;
	}
	
	private boolean zeroMeansIfNoData;
	
	/** 
	 * Set to true if you prefer zero observation count means to be
	 * reported as 0.0 instead of Double.MAX_VALUE.
	 * @param tf
	 */
	public void setZeroMeansIfNoData(boolean tf) {
		zeroMeansIfNoData = tf;
	}
	
	/** Return the estimated arithmetic mean of the sample. */
	public double getArithmeticMean() {
		if (zeroMeansIfNoData && observationCount <= 0) return 0.0;
		if (observationCount <= 0) return Double.MAX_VALUE;
		return sum / observationCount;
	}
	
	/** Return the estimated geometric mean of the sample. */
	public double getGeometricMean() {
		if (zeroMeansIfNoData && observationCount <= 0) return 1.0;
		if (observationCount <= 0) return Double.MAX_VALUE;
		return Math.pow(product, 1.0 / observationCount);
	}
	
	/** Return an unbiased estimate of the sample variance. */
	public double getVariance() {
		if (observationCount <= 1) return Double.MAX_VALUE;
		return (sumOfSquares/(observationCount-1) - sum*sum/(observationCount*(observationCount-1)));
	}
	
	/** Return an unbiased estimate of the sample standard deviation. */
	public double getStandardDeviation() {
		if (zeroMeansIfNoData && observationCount <= 0) return 0.0;
		if (observationCount <= 1) return Double.MAX_VALUE;
		return Math.sqrt(getVariance());
	}
	
	/** Return the z score of the input relative to this set of observations. */
	public double getZScore(double num) {
		return ((num - getArithmeticMean()) / getStandardDeviation());
	}
	
	/** Lookup table for mean confidence intervals. */
	private static final double[] confidenceProbs = { 
		0.05,	0.062706778,
		0.1,	0.125661347,
		0.15,	0.189118426,
		0.2,	0.253347103,
		0.25,	0.318639364,
		0.3,	0.385320466,
		0.35,	0.45376219,
		0.4,	0.524400513,
		0.45,	0.597760126,
		0.5,	0.67448975,
		0.55,	0.755415026,
		0.6,	0.841621234,
		0.65,	0.934589291,
		0.7,	1.036433389,
		0.75,	1.15034938,
		0.8,	1.281551566,
		0.85,	1.439531471,
		0.9,	1.644853627,
		0.91,	1.69539771,
		0.92,	1.750686071,
		0.93,	1.811910673,
		0.94,	1.880793608,
		0.95,	1.959963985,
		0.96,	2.053748911,
		0.97,	2.170090378,
		0.98,	2.326347874,
		0.99,	2.575829304,
		0.991,	2.612054141,
		0.992,	2.652069808,
		0.993,	2.696844261,
		0.994,	2.747781385,
		0.995,	2.807033768,
		0.996,	2.878161739,
		0.997,	2.967737925,
		0.998,	3.090232306,
		0.999,	3.290526731
	};
	
	private double confidenceLookup(double probability) {
		if (probability < 0.05 || probability > 0.999) throw new RuntimeException("Requested confidence interval outside of stored range.");
		//Hardcode the most common cases...these three will cover _almost_ every use of this code
		if (probability == 0.95) return 1.959963985;
		else if (probability == 0.99) return 2.575829304;
		else if (probability == 0.9) return 1.644853627;
		
		//Just loop through the list to do the lookup - list is short, so this is probably not worth optimizing right now
		for (int i=0; i<confidenceProbs.length-3; i += 2) {
			if (confidenceProbs[i+2] > probability) {
				return MathUtil.map(probability, confidenceProbs[i], confidenceProbs[i+2], confidenceProbs[i+1], confidenceProbs[i+3]);
			}
		}
		assert(false); //should never get here
		return 0;
	}
	
	/**
	 * Get the value err such that the mean should be reported
	 * as mean = estimate (+/- err) at the given probability level.
	 * @param probability
	 * @return standard error of arithmetic mean
	 */
	public double getMeanConfidence(double probability) {
		return confidenceLookup(probability) * getStandardDeviation() / Math.sqrt(observationCount);
	}
	
	/** Tests */
	static public void main(String[] args) {
		double[] nums = {1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1};
		ObservationSummary summary = new ObservationSummary();
		summary.add(nums);
		System.out.println("Count is "+summary.getCount()+": should be 17");
		System.out.println("Mean is "+summary.getArithmeticMean()+": should be ~4.76");
		System.out.println("Variance is "+summary.getVariance()+": should be ~6.44");
		System.out.println("Stdev is "+summary.getStandardDeviation()+": should be ~2.54");
		System.out.println("95% confidence interval around mean is +/-"+summary.getMeanConfidence(0.95)+": should be +/- ~1.206");
	}
}
