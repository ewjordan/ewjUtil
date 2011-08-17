package com.ewjordan.util.random;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Spinner {
	static public void main(String[] args) {
		testSpinner(0.1f);
		testSpinner(0.1f, 0.2f,0.3f, 0.4f,0.1f, 0.2f,0.3f, 0.4f,0.1f, 0.2f,0.3f, 0.4f,0.1f, 0.2f,0.3f, 0.4f,0.1f, 0.2f,0.3f, 0.4f);
		testSpinner(0.5f, 0.5f);
		testSpinner(0.5f, 0.5f, 0.5f);
		testSpinner(0.1f, 0.2f, 0.3f);
		testSpinner(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
		try {
			testSpinner();
		} catch (IllegalArgumentException e) {
			e.printStackTrace(); //zero prob
		}
		try {
			testSpinner(-1.0f, -2.0f);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(); //negative prob
		}
		try {
			testSpinner(1.0f, -0.5f);
		} catch (IllegalArgumentException e) {
			e.printStackTrace(); //negative prob
		}
	}
	
	static private void testSpinner(float ... probs) {
		final int N_TRIES = 10000000;
		int[] countsNorm = new int[probs.length];
		int[] countsNoAux = new int[probs.length];
		int[] countsAux = new int[probs.length];
		float[] aux = new float[probs.length];
		long ms = System.currentTimeMillis();
		for (int i=0; i<N_TRIES; ++i) {
			countsNorm[normalizedSpinner(probs)] += 1;
			countsNoAux[spinner(probs)] += 1;
			countsAux[spinner(aux,probs)] += 1;
		}
		System.out.println("Probs\tNorm\tNoAux\tAux");
		for (int i=0; i<probs.length; ++i) {
			float normProb = countsNorm[i] / (float)N_TRIES;
			float noAuxProb = countsNoAux[i] / (float)N_TRIES;
			float auxProb = countsAux[i] / (float)N_TRIES;
			System.out.println(probs[i]+"\t"+normProb+"\t"+noAuxProb+"\t"+auxProb);
		}
		long endms = System.currentTimeMillis();
		float randPerSec = 3000 * (N_TRIES / (float)(endms-ms));
		System.out.println("Took " + (endms-ms) + " ms, or " + ((endms-ms)/(float)(N_TRIES*3)) + " ms per randomization, for " + randPerSec + " randomizations per sec");
		
	}
	
	/**
	 * Spin a spinner with the given relative probabilities, get the index that results.  This
	 * method does not alter the passed array.
	 * <pre>
	 * normalizedSpinner(10, 20, 30) => 1/6 chance of 0, 1/3 chance of 1, 1/2 chance of 2
	 * normalizedSpinner(1,1,2) => 1/4 chance of 0, 1/4 chance of 1, 1/2 chance of 2
	 * </pre>
	 * Note: during intermediate steps, this method will cause two float[] creations, the
	 * same length as the probabilities array.  If creation is a concern, use {@link #spinner(float[], float[])}
	 * to pass your own pre-allocated intermediate array and handle the probability normalization yourself. 
	 * Also remember that unless probabilities is passed as an array, Java's varargs will create another
	 * intermediate array.
	 * 
	 * @param probabilities a list of relative probabilities
	 * @return the index of the event that is randomly chosen based on the provided probabilities
	 */
	public static int normalizedSpinner(float ... probabilities) {
		float sum = 0.0f;
		for (int i=0; i<probabilities.length; ++i) {
			sum += probabilities[i];
		}
		if (sum == 0.0f) {
			throw new IllegalArgumentException("At least one probability must be non-zero.");
		} else if (sum < 0.0f) {
			throw new IllegalArgumentException("Probabilities may not be negative.");
		}
		float[] normProbs = new float[probabilities.length];
		for (int i=0; i<probabilities.length; ++i) {
			normProbs[i] = probabilities[i] / sum;
		}
		return spinner(normProbs);
	}
	
    /**
     * Spin a spinner with the given probabilities, get the index that results.
     * Sum of probabilities should add up to 1f.
     * 
     * If the sum of probabilities is less than 1, the last index gets all the
     * remaining probability.
     * e.g. spinner(0.5f, 0.3f) => 50% chance of 0, 50% chance of 1
     * 
     * If the sum of probabilities is greater than 1, the probabilities are clipped
     * at 1.
     * 
     * e.g. spinner(0.5f, 0.3f, 0.5f) => 50% chance of 0, 30% chance of 1, 20% chance of 2 (extra 30% discarded)
     * 
     * This method creates an intermediate float array the same length as probabilities; if garbage
     * creation is a concern, use {@link #spinner(float[], float[])} instead, and provide a pre-allocated
     * array of the correct length as the first parameter.
     * 
     * @param probabilities a list of probabilities that should add up to 1
     * @return the index of the event that is randomly chosen based on the provided probabilities
     */
    public static int spinner(float ... probabilities) {
        float[] mins = new float[probabilities.length];
        return spinner(mins, probabilities);
    }
    
    /**
     * Used instead of {@link #spinner(float...)} to avoid garbage creation by manually supplying
     * intermediate array.
     * 
     * @param auxiliaryArray a pre-allocated array to use for intermediate step that should
     * have at least the same length as the probabilities array (used to avoid intermediate allocations)
     * @param probabilities a list of probabilities that should add up to 1
     * @return the index of the event that is chosen
     * @see Spinner#spinner(float...)
     */
    public static int spinner(float[] auxiliaryArray, float[] probabilities) {
    	if (auxiliaryArray.length < probabilities.length){
    		throw new IllegalArgumentException("Auxiliary array must have at least the same length as probabilities array.");
    	}
    	float sum = 0.0f;
        float[] mins = auxiliaryArray;
        for (int i = 0; i < probabilities.length; ++i) {
            if (probabilities[i] < 0.0f) {
                throw new IllegalArgumentException("Probabilities must be positive; received " + probabilities[i] +
                        " as parameter " + i + ".");
            }
            mins[i] = sum;
            sum += probabilities[i];
        }
        double randomNumber = Math.random();
        for (int i = probabilities.length - 1; i > 0; --i) {
            if (randomNumber >= mins[i]) {
                return i;
            }
        }
        return 0;
    }
    
	/**
	 * Spin a spinner with the given relative probabilities, get the index that results.  This
	 * method does not alter the passed array.
	 * <pre>
	 * normalizedSpinner(10, 20, 30) => 1/6 chance of 0, 1/3 chance of 1, 1/2 chance of 2
	 * normalizedSpinner(1,1,2) => 1/4 chance of 0, 1/4 chance of 1, 1/2 chance of 2
	 * </pre>
	 * Note: during intermediate steps, this method will cause two double[] creations, the
	 * same length as the probabilities array.  If creation is a concern, use {@link #spinner(double[], double[])}
	 * to pass your own pre-allocated intermediate array and handle the probability normalization yourself. 
	 * Also remember that unless probabilities is passed as an array, Java's varargs will create another
	 * intermediate array.
	 * 
	 * @param probabilities a list of relative probabilities
	 * @return the index of the event that is randomly chosen based on the provided probabilities
	 */
	public static int normalizedSpinner(double ... probabilities) {
		double sum = 0.0;
		for (int i=0; i<probabilities.length; ++i) {
			sum += probabilities[i];
		}
		if (sum == 0.0) {
			double[] arr = new double[probabilities.length];
			Arrays.fill(arr, 1);
			return normalizedSpinner(arr);
		} else if (sum < 0.0) {
			throw new IllegalArgumentException("Probabilities may not be negative.");
		}
		double[] normProbs = new double[probabilities.length];
		for (int i=0; i<probabilities.length; ++i) {
			normProbs[i] = probabilities[i] / sum;
		}
		return spinner(normProbs);
	}
	
    /**
     * Spin a spinner with the given probabilities, get the index that results.
     * Sum of probabilities should add up to 1f.
     * 
     * If the sum of probabilities is less than 1, the last index gets all the
     * remaining probability.
     * e.g. spinner(0.5f, 0.3f) => 50% chance of 0, 50% chance of 1
     * 
     * If the sum of probabilities is greater than 1, the probabilities are clipped
     * at 1.
     * 
     * e.g. spinner(0.5f, 0.3f, 0.5f) => 50% chance of 0, 30% chance of 1, 20% chance of 2 (extra 30% discarded)
     * 
     * This method creates an intermediate double array the same length as probabilities; if garbage
     * creation is a concern, use {@link #spinner(double[], double[])} instead, and provide a pre-allocated
     * array of the correct length as the first parameter.
     * 
     * @param probabilities a list of probabilities that should add up to 1
     * @return the index of the event that is randomly chosen based on the provided probabilities
     */
    public static int spinner(double ... probabilities) {
        double[] mins = new double[probabilities.length];
        return spinner(mins, probabilities);
    }
    
    /**
     * Used instead of {@link #spinner(double...)} to avoid garbage creation by manually supplying
     * intermediate array.
     * 
     * @param auxiliaryArray a pre-allocated array to use for intermediate step that should
     * have at least the same length as the probabilities array (used to avoid intermediate allocations)
     * @param probabilities a list of probabilities that should add up to 1
     * @return the index of the event that is chosen
     * @see Spinner#spinner(double...)
     */
    public static int spinner(double[] auxiliaryArray, double[] probabilities) {
    	if (auxiliaryArray.length < probabilities.length){
    		throw new IllegalArgumentException("Auxiliary array must have at least the same length as probabilities array.");
    	}
    	double sum = 0.0;
        double[] mins = auxiliaryArray;
        for (int i = 0; i < probabilities.length; ++i) {
            if (probabilities[i] < 0.0) {
                throw new IllegalArgumentException("Probabilities must be positive; received " + probabilities[i] +
                        " as parameter " + i + ".");
            }
            mins[i] = sum;
            sum += probabilities[i];
        }
        double randomNumber = Math.random();
        for (int i = probabilities.length - 1; i > 0; --i) {
            if (randomNumber >= mins[i]) {
                return i;
            }
        }
        return 0;
    }

	public static int spinner(List<Double> probabilities) {
		double[] mins = new double[probabilities.size()];
        for (int i=0; i<probabilities.size(); ++i) {
        	mins[i] = probabilities.get(i);
        }
        return spinner(mins);
	}
	
	public static int normalizedSpinner(List<Double> probabilities) {
		double[] mins = new double[probabilities.size()];
        for (int i=0; i<probabilities.size(); ++i) {
        	mins[i] = probabilities.get(i);
        }
        return normalizedSpinner(mins);
	}
}
