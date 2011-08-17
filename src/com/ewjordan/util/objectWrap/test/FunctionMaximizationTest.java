package com.ewjordan.util.objectWrap.test;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;

import com.ewjordan.util.objectWrap.ObjectOptimizer;
import com.ewjordan.util.objectWrap.OptimizableWrappedObject;
import com.ewjordan.util.objectWrap.WrappedObject;

public class FunctionMaximizationTest {
	static public void main(String[] args) throws OptimizationException, IllegalArgumentException {
		PolynomialFunction func = new PolynomialFunction(1,0,-4,0,4);
		WrappedObject w = new WrappedObject(func);
		OptimizableWrappedObject obj = w.optimizable();
		
		double val = 0.0;
		long nanosStart = System.currentTimeMillis();
		for (int i=0; i<10000; ++i) {
			func.x = Math.random();
			val += ObjectOptimizer.optimize(obj, GoalType.MINIMIZE);
//			val += ObjectOptimizer.optimizeNelderMead(obj, GoalType.MINIMIZE);
//			System.out.println(ObjectOptimizer.optimize(obj, GoalType.MINIMIZE) + " iterations.");
//			System.out.println("Best result: x = " + func.x + ", f(x) = " + func.getValue());
		}
		long nanosEnd = System.currentTimeMillis();

		System.out.println("Millis taken: " + (nanosEnd-nanosStart));
		System.out.println(val);
	}
}
