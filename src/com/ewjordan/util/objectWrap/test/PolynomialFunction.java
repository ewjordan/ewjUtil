/**
 * 
 */
package com.ewjordan.util.objectWrap.test;

import org.apache.commons.math.analysis.MultivariateRealFunction;

import com.ewjordan.util.objectWrap.HasValue;
import com.ewjordan.util.objectWrap.Unwrapped;

class PolynomialFunction implements HasValue, MultivariateRealFunction {
	@Unwrapped public double[] coefficients;
	public double x;
	
	public PolynomialFunction(double ... coefs) {
		coefficients = new double[coefs.length];
		System.arraycopy(coefs, 0, coefficients, 0, coefs.length);
	}
	
	public double getValue(float input) {
		double oldX = x;
		x = input;
		double res = getValue();
		x = oldX;
		return res;
	}
	
	public double getValue() {
		double value = 0.0f;
		double pow = 1.0f;
		for (int i=coefficients.length-1; i >= 0; --i) {
			value += coefficients[i] * pow;
			pow *= x;
		}
//		System.out.println("PF: " + x + " -> " + value);
		return value;
	}

	@Override
	public double value(double[] point) throws IllegalArgumentException {
		return getValue((float)point[0]);
	}
}