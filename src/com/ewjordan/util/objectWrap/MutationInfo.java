package com.ewjordan.util.objectWrap;

public @interface MutationInfo {
	double minimum() default -Double.MAX_VALUE;
	double maximum() default Double.MAX_VALUE;
	double standardDeviation() default 1.0;
}
