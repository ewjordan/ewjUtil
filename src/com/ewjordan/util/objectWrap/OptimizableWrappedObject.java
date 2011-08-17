package com.ewjordan.util.objectWrap;

public abstract class OptimizableWrappedObject extends WrappedObject implements HasValue {
	public OptimizableWrappedObject(Object objectToWrap) {
		super(objectToWrap);
	}
	
	public OptimizableWrappedObject(WrappedObject cloneMe) {
		super(cloneMe);
	}
	/**
	 * Calculate the value to optimize based on the current wrapped object
	 * state.
	 * @return value to optimize
	 */
	abstract public double getValue();
}