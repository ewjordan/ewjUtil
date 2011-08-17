package com.ewjordan.util.objectWrap;

import java.util.Random;

import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.genetics.Chromosome;
import org.apache.commons.math.genetics.ChromosomePair;
import org.apache.commons.math.genetics.CrossoverPolicy;
import org.apache.commons.math.genetics.ElitisticListPopulation;
import org.apache.commons.math.genetics.GeneticAlgorithm;
import org.apache.commons.math.genetics.MutationPolicy;
import org.apache.commons.math.genetics.Population;
import org.apache.commons.math.genetics.SelectionPolicy;
import org.apache.commons.math.genetics.StoppingCondition;
import org.apache.commons.math.genetics.TournamentSelection;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMeadSimplex;
import org.apache.commons.math.optimization.general.ConjugateGradientFormula;
import org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizer;

/**
 * This class optimizes OptimizableWrappedObjects against
 * their fitness functions, using either Apache's gradient-based
 * optimization routines or its genetic algorithm implementation.
 * 
 * The routines here will generate the necessary crossover, mutation,
 * and calculus quantities using a default implementation, however be
 * aware that these defaults will not always be ideal, and in some cases
 * you may have faster or more appropriate ways to do these things.
 * 
 * The default crossover policy is a single point crossover at a random
 * place along the chromosome - for a wrapped object, this means that
 * we simply switch over from reading one object's fields to the other's.
 * This means that the order of field declaration in each object matters.
 * It is suggested that for use with pure genetic algorithms a decoding
 * procedure is used to transform the chromosome (i.e. the object's fields)
 * into the final structure that is then tested for fitness.
 * 
 * @author Eric
 *
 */
public class ObjectOptimizer {
	static private double derivativeDelta = 0.001;
	static private final Random rand = new Random();
	static private CrossoverPolicy defaultWrappedObjectCrossoverPolicy = new CrossoverPolicy() {
		@Override
		public ChromosomePair crossover(Chromosome first,
				Chromosome second) {
			if ((first instanceof WrappedObjectChromosome)
					&& (second instanceof WrappedObjectChromosome)) {
				WrappedObjectChromosome wrappedFirst = (WrappedObjectChromosome) first;
				WrappedObjectChromosome wrappedSecond = (WrappedObjectChromosome) second;
				int nMembers = wrappedFirst.size();
				if (wrappedSecond.size() < nMembers) {
					nMembers = wrappedSecond.size();
				}
				int crossPoint = rand.nextInt(nMembers);
				WrappedObjectChromosome crossedFirst = WrappedObjectChromosome
						.cross(wrappedFirst, wrappedSecond, crossPoint);
				WrappedObjectChromosome crossedSecond = WrappedObjectChromosome
						.cross(wrappedSecond, wrappedFirst, crossPoint);
				ChromosomePair pair = new ChromosomePair(crossedFirst,
						crossedSecond);
				return pair;
			} else {
				throw new IllegalArgumentException(
						"WrappedObjectChromosomes are the only types accepted here");
			}
		}
	};
	
	static private MutationPolicy defaultWrappedObjectMutationPolicy = new MutationPolicy() {
		@Override
		public Chromosome mutate(Chromosome original) {
			if (original instanceof WrappedObjectChromosome) {
				WrappedObjectChromosome wrapped = (WrappedObjectChromosome)original;
				WrappedObjectChromosome result = WrappedObjectChromosome.mutate(wrapped);
				return result;
			} else {
				throw new IllegalArgumentException("WrappedObjectChromosomes are the only types accepted here");
			}
		}
	};
	
	static private SelectionPolicy defaultWrappedObjectSelectionPolicy = new TournamentSelection(10);
	
	static public WrappedObjectChromosome geneticallyOptimize(OptimizableWrappedObject obj, 
			final int generations, double crossoverRate, 
			double mutationRate, int populationLimit, double elitismRate) {
		final GeneticAlgorithm alg = new GeneticAlgorithm(defaultWrappedObjectCrossoverPolicy,crossoverRate,
				defaultWrappedObjectMutationPolicy, mutationRate,
				defaultWrappedObjectSelectionPolicy);
		Population initial = new ElitisticListPopulation(populationLimit, elitismRate);
		initial.addChromosome(new WrappedObjectChromosome(obj));
		Population finalPopulation = alg.evolve(initial, new StoppingCondition() {
			@Override
			public boolean isSatisfied(Population population) {
				if (alg.getGenerationsEvolved() > generations) {
					return true;
				} else {
					return false;
				}
			}
		});
		WrappedObjectChromosome chrom = (WrappedObjectChromosome)finalPopulation.getFittestChromosome();
		return chrom;
	}
	
	static final MutationPolicy getWrappedObjectMutationPolicy() {
		return defaultWrappedObjectMutationPolicy;
	}

	static final CrossoverPolicy getWrappedObjectCrossoverPolicy() {
		return defaultWrappedObjectCrossoverPolicy;
	}
	
	static final WrappedObjectChromosome getChromosome(final OptimizableWrappedObject opt) {
		WrappedObjectChromosome chromosome = new WrappedObjectChromosome(opt) {
			@Override
			public double fitness() {
				return opt.getValue();
			}
		};
		return chromosome;
	}
	
	/** 
	 * Optimize an OptimizableWrappedObject using the nonlinear conjugate gradient method.
	 * Returns number of iterations, or -1 if an exception was thrown.
	 * @throws IllegalArgumentException 
	 * @throws FunctionEvaluationException 
	 * @throws OptimizationException 
	 * 
	 */
	static public final int optimize(final OptimizableWrappedObject opt, GoalType goalType) throws OptimizationException, FunctionEvaluationException, IllegalArgumentException {
		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
		opt.pullValuesFromObject();
		RealPointValuePair pair = optimizer.optimize(getDifferentiableMultivariateRealFunction(opt),goalType, opt.getValues());
		opt.setValues(pair.getPoint());
		opt.pushValuesToObject();
		return optimizer.getIterations();
	}
	
	static public final double[] optimize(final MultivariateRealFunction func, GoalType goalType, final double[] startingPoint) throws FunctionEvaluationException, IllegalArgumentException {
		Object funcPlusParam = new HasValue() {
			MultivariateRealFunction f = func;
			double[] param = startingPoint;
			@Override
			public double getValue() {
				try {
					return f.value(param);
				} catch (FunctionEvaluationException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
				}
				return 0.0;
			}
		};
		WrappedObject w = new WrappedObject(funcPlusParam);
		final OptimizableWrappedObject opt = w.optimizable();
		NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
		opt.pullValuesFromObject();
		RealPointValuePair pair = null;
		try {
			optimizer.setMaxIterations(10);
			pair = optimizer.optimize(getDifferentiableMultivariateRealFunction(opt),goalType, opt.getValues());
		} catch (OptimizationException e) {
			return startingPoint;
		}
		
		return pair.getPoint();
	}
	
	/** 
	 * Optimize an OptimizableWrappedObject using the Nelder Mead direct search method.
	 * Returns number of iterations, or -1 if an exception was thrown.
	 * @throws IllegalArgumentException 
	 * @throws FunctionEvaluationException 
	 * @throws OptimizationException 
	 * 
	 */
	static public final int optimizeNelderMead(final OptimizableWrappedObject opt, GoalType goalType) throws OptimizationException, FunctionEvaluationException, IllegalArgumentException {
		NelderMeadSimplex optimizer = new NelderMeadSimplex(opt.getNumberOfMembers());
		opt.pullValuesFromObject();
		RealPointValuePair pair = optimizer.optimize(getDifferentiableMultivariateRealFunction(opt),goalType, opt.getValues());
		opt.setValues(pair.getPoint());
		opt.pushValuesToObject();
		return optimizer.getIterations();
	}
	
	static final MultivariateRealFunction getMultivariateRealFunction(final OptimizableWrappedObject opt) {
		MultivariateRealFunction func = new MultivariateRealFunction() {
			private double[] prevVals = null;
			@Override
			public double value(double[] point) throws IllegalArgumentException {
				double[] pvals = opt.getValues();
				if (prevVals == null) {
					prevVals = new double[pvals.length];
				}
				double value;
				synchronized(opt.getObject()) { //TODO: test thread safety here...
					System.arraycopy(pvals,0,prevVals,0,pvals.length);
					opt.pushValuesToObject(point);
					value = opt.getValue();
					opt.pushValuesToObject(prevVals);
				}
				return value;
			}
		};
		return func;
	}

	static final DifferentiableMultivariateRealFunction getDifferentiableMultivariateRealFunction(final OptimizableWrappedObject opt) {
		DifferentiableMultivariateRealFunction func = new DifferentiableMultivariateRealFunction() {
			private double[] prevVals = null;
			
			@Override
			public double value(double[] point)
					throws IllegalArgumentException {
				WrappedObject o = opt;
				double[] pvals = o.getValues();
				if (prevVals == null) {
					prevVals = new double[pvals.length];
				}
				double value;
				synchronized(o.getObject()) { //TODO: test thread safety here...
					System.arraycopy(pvals,0,prevVals,0,pvals.length);
					o.pushValuesToObject(point);
					value = opt.getValue();
					o.pushValuesToObject(prevVals);
				}
				return value;
			}

			private double getPartialDerivativeAtPoint(int k, double[] point) throws IllegalArgumentException {
				double initialValue = point[k];
				point[k] = initialValue + derivativeDelta;
				double resultPlus = value(point);
				point[k] = initialValue - derivativeDelta;
				double resultMinus = value(point);
				point[k] = initialValue;
				return (resultPlus - resultMinus) / (2 * derivativeDelta);
			}

			private MultivariateRealFunction[] partials = 
				new MultivariateRealFunction[opt.getNumberOfMembers()];

			@Override
			public MultivariateRealFunction partialDerivative(final int k) {
				if (partials[k] == null) {
					partials[k] = new MultivariateRealFunction() {
						@Override
						public double value(double[] point)
								throws IllegalArgumentException {
							return getPartialDerivativeAtPoint(k, point);
						}
					};
				}
				return partials[k];
			}

			private final MultivariateVectorialFunction grad = new MultivariateVectorialFunction() {
				@Override
				public double[] value(double[] point)
						throws IllegalArgumentException {
					double[] returnValue = new double[point.length];
					for (int i = 0; i < point.length; ++i) {
						returnValue[i] = getPartialDerivativeAtPoint(i, point);
					}
					return returnValue;
				}
			};

			@Override
			public MultivariateVectorialFunction gradient() {
				return grad;
			}
		};
		return func;
	}
}
