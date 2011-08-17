package com.ewjordan.util.random;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Shuffler {

	/** Shuffle a list in-place */
	static public void shuffleInPlace(List<? extends Object> objects) {
		Collections.shuffle(objects);
	}
	
	static private List<Double> yankDoubles(List<? extends Object> objects, String fieldName) {
		try {
			List<Double> doubles = new ArrayList<Double>(objects.size());
			for (Object o : objects) {
				Class<?> clazz = o.getClass();
				Field field;
				field = clazz.getDeclaredField(fieldName);
				double dub = field.getDouble(o);
				doubles.add(dub);
			}
			return doubles;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static private void putDoubles(List<Double> doubles, List<? extends Object> objects, String fieldName) {
		if (doubles.size() != objects.size()) {
			throw new IllegalArgumentException("Sizes of lists do not match.");
		}
		try {
			for (int i=0; i<objects.size(); ++i) {
				Object o = objects.get(i);
				Class<?> clazz = o.getClass();
				Field field = clazz.getDeclaredField(fieldName);
				field.setDouble(o, doubles.get(i));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static public void shuffleDoubleFieldInPlace(List<?> objects, String fieldName) {
		List<Double> pulled = yankDoubles(objects, fieldName);
		Collections.shuffle(pulled);
		putDoubles(pulled, objects, fieldName);
	}
	
	/** Extract a list of doubles from a list of objects, shuffle it, and return it as a double array */
	static public double[] getShuffledDoubleFields(List<? extends Object> objects, String fieldName) {
		try {
			List<Double> doubles = yankDoubles(objects, fieldName);
			Collections.shuffle(doubles);
			double[] arr = new double[doubles.size()];
			for (int i=0; i<doubles.size(); ++i) {
				arr[i] = doubles.get(i);
			}
			return arr;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* EXAMPLE USAGE BELOW */
	
	static private void exampleShuffling() {
		List<TestClass> myList = new ArrayList<TestClass>();
		myList.add(new TestClass(1.0,5.0));
		myList.add(new TestClass(2.0,6.0));
		myList.add(new TestClass(3.0,7.0));
		myList.add(new TestClass(4.0,8.0));
		System.out.println("Raw: ");
		TestClass.print(myList);
		System.out.println("Shuffled in place: ");
		shuffleInPlace(myList);
		TestClass.print(myList);
		double[] shuffledDoubles = getShuffledDoubleFields(myList, "x");
		System.out.println("Shuffled xs: ");
		for (double d : shuffledDoubles) {
			System.out.println(d);
		}
		TestClass.print(myList);
	}
	
	static public void main(String[] args) {
		exampleShuffling();
	}
	
	static final private class TestClass {
		double x, y;
		public TestClass(double x, double y) { this.x = x; this.y = y; }
		
		static public void print(List<TestClass> list) {
			for (TestClass c : list) {
				System.out.println(c.x + ", " + c.y);
			}
		}
	}
}
