package com.ewjordan.evolution.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class SpeedLimit {
	static private final Random rand = new Random();
	static private final int genomeLength = 500;
	static private final int populationSize = 5000; // Total population size (after unfit members die off)
	static private final int generations = 1000;
	static private final int childrenPerFamily = 8;
	
	static private final double crossoverRate = 0.01;
	static private final double oneToZeroMutationRate = .01;
	static private final double zeroToOneMutationRate = .00001;
	static private final double initialFitnessProbability = 0.01; //probability that bits start off as true
	
	private List<Genome> population = new ArrayList<Genome>();
	
	static public void main(String[] args) {
		doEvolution();
	}
	
	static public void doEvolution() {
		SpeedLimit test = new SpeedLimit();
		for (int i=0; i<populationSize; ++i) {
			test.population.add(generateInitialGenome(genomeLength,initialFitnessProbability));
		}
		System.out.println("Generation,PopSize,"+getSummaryCSVTitleLine());
		for (int i=0; i<generations; ++i) {
			List<Genome> children = new ArrayList<Genome>(populationSize*childrenPerFamily/2);
			int count = 0;
			while (count < populationSize*childrenPerFamily/2) {
				Genome parentA = test.population.get(rand.nextInt(test.population.size()));
				Genome parentB = test.population.get(rand.nextInt(test.population.size()));
				Genome child = Genome.cross(parentA, parentB, crossoverRate);
				Genome.mutate(child, oneToZeroMutationRate, zeroToOneMutationRate);
				children.add(child);
				++count;
			}
			test.population = children;
			test.sortByFitness(false);
			List<Genome> accepted = test.population.subList(0, populationSize);
			test.population = new ArrayList<Genome>(accepted);
			System.out.println(i + "," + test.population.size() + "," + test.getSummaryCSVLine());
		}
	}
	
	static private Genome generateInitialGenome(int len, double probT) {
		Genome g = new Genome(len);
		for (int i=0; i<g.bits.length; ++i) {
			g.bits[i] = (Math.random() < probT);
		}
		return g;
	}
	
	private double getAverageFitness() {
		double sum = 0.0;
		for (Genome g : population) {
			sum += computeFitness(g);
		}
		return sum / population.size();
	}
	
	private double getStandardDeviation() {
		return getSummary().getStandardDeviation();
	}
	
	private String getSummaryCSVLine() {
		String line = "";
		DescriptiveStatistics summary = getSummary();
		line += summary.getMean() + "," + summary.getMin() + "," + summary.getPercentile(25) + "," + summary.getPercentile(50) + "," +
				summary.getPercentile(75) + "," + summary.getMax() + "," + summary.getStandardDeviation();
		return line;
	}
	
	private static String getSummaryCSVTitleLine() {
		return "Mean,Min,25%,50%,75%,Max,Stdev";
	}
	
	private DescriptiveStatistics getSummary() {
		DescriptiveStatistics summary = new DescriptiveStatistics();
		for (Genome g : population) {
			summary.addValue(computeFitness(g));
		}
		return summary;
	}
	
	private double getMin() {
		return getSummary().getMax();
	}
	
	private double getMax() {
		return getSummary().getMax();
	}
	
	private double computeFitness(Genome g) {
		if (!g.hasSavedFitness) {
			// Count number of ones divided by total length
			int count = 0;
			for (int i=0; i<g.bits.length; ++i) {
				if (g.bits[i]) ++count;
			}
			g.savedFitness = count / (1.0*g.bits.length);
			g.hasSavedFitness = true;
		}
		return g.savedFitness;
	}
	
	private void sortByFitness(final boolean ascending) {
		Collections.sort(population, new Comparator<Genome>() {
			@Override public int compare(Genome arg0, Genome arg1) {
				double diff = computeFitness(arg1) - computeFitness(arg0);
				if (ascending) {
					diff *= -1;
				}
				if (diff > 0) {
					return 1;
				} else if (diff == 0) {
					return 0;
				} else {
					return -1;
				}
			}
		});
	}
	
	static private class Genome {
		static void mutate(Genome g, double oneToZeroRate, double zeroToOneRate) {
			for (int i=0; i<g.bits.length; ++i) {
				if (g.bits[i]) {
					if (Math.random() < oneToZeroRate) g.bits[i] = false;
				} else {
					if (Math.random() < zeroToOneRate) g.bits[i] = true;
				}
			}
		}
		static Genome cross(Genome gA, Genome gB, double crossoverRate) {
			int len = gA.bits.length;
			Genome newGenome = new Genome(len);
			boolean onA = (Math.random() < 0.5);
			for (int i=0; i<len; ++i) {
				newGenome.bits[i] = onA ? gA.bits[i] : gB.bits[i];
				if (Math.random() < crossoverRate) {
					onA = !onA;
				}
			}
			return newGenome;
		}
		boolean[] bits;
		boolean hasSavedFitness = false;
		double savedFitness = 0.0;
		Genome(int length) {
			bits = new boolean[length];
		}
	}
}
