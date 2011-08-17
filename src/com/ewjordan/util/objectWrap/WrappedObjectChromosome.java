package com.ewjordan.util.objectWrap;

import java.util.Random;

import org.apache.commons.math.genetics.Chromosome;

public class WrappedObjectChromosome extends Chromosome {
	static private Random rand = new Random();
	private WrappedObject wrappedObject;
	
	public WrappedObject getWrappedObject() {
		return wrappedObject;
	}

	public void setWrappedObject(WrappedObject wrappedObject) {
		this.wrappedObject = wrappedObject;
	}
	
	public WrappedObjectChromosome(WrappedObject obj) {
		super();
		this.wrappedObject = obj;
	}
	
	public int size() {
		return wrappedObject.getNumberOfMembers();
	}
	
	public static WrappedObjectChromosome cross(	WrappedObjectChromosome wrappedFirst,
													WrappedObjectChromosome wrappedSecond, 
													int crossPoint							) {
		WrappedObject o1 = wrappedFirst.getWrappedObject();
		WrappedObject o2 = wrappedSecond.getWrappedObject();
		WrappedObject result = new WrappedObject(o1);
		double[] resultVals = result.getValues();
		double[] crossVals = o2.getValues();
		System.arraycopy(crossVals, crossPoint, resultVals, crossPoint, resultVals.length-crossPoint);
		return new WrappedObjectChromosome(result);
	}
	
	public static WrappedObjectChromosome mutate(WrappedObjectChromosome original) {
		WrappedObject obj = original.getWrappedObject();
		int nMembers = obj.getNumberOfMembers();
		int memberToMutate = rand.nextInt(nMembers);
		PrimitiveReference ref = obj.getReferenceAt(memberToMutate);
		//mutate, while respecting the stated range of each reference
		double min = ref.getMinValue();
		double max = ref.getMaxValue();
		double dev = ref.getStandardDeviationScale();
		double newVal = constrainedRandomValue(obj.getValue(memberToMutate),min, max, dev);
		WrappedObject newObj = new WrappedObject(obj);
		newObj.setValue(memberToMutate, newVal);
		WrappedObjectChromosome mutated = new WrappedObjectChromosome(newObj);
		return mutated;
	}
	
	/**
	 * An arbitrary constrained random walk step.  Works by taking a random walk from the
	 * current location, and then applying the constraints.
	 */
	static private double constrainedRandomValue(double current, double min, double max, double stdDev) {
		double randGaussian = rand.nextGaussian() * stdDev;
		double newVal = current + randGaussian;
		if (newVal < min) {
			newVal = min;
		} else if (newVal > max) {
			newVal = max;
		}
		return newVal;
	}
	
	@Override
	public double fitness() {
		// TODO Auto-generated method stub
		return 0;
	}
}
