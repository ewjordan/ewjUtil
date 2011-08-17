package com.ewjordan.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {
	/**
	 * Print an array of doubles
	 * @param arr
	 */
	static public void printArray(double[] arr) {
		for (int i=0; i<arr.length; ++i) {
			System.out.println(i + " : " + arr[i]);
		}
	}
	
	static public List<Double> toDoubleList(int[] intarr) {
		ArrayList<Double> doubles = new ArrayList<Double>();
		for (int i=0; i<intarr.length; ++i) {
			doubles.add((double)intarr[i]);
		}
		return doubles;
	}
	
	static public double[] toDoubleArray(List<? extends Number> list) {
		double[] arr = new double[list.size()];
		for (int i=0; i<list.size(); ++i) {
			arr[i] = list.get(i).doubleValue();
		}
		return arr;
	}
	
	static public double[] toDoubleArray(DoubleList list) {
		double[] arr = new double[list.size()];
		for (int i=0; i<list.size(); ++i) {
			arr[i] = (double)list.get(i);
		}
		return arr;
	}
	
	static public double average(double[] arr) {
		return sum(arr) / arr.length;
	}
	
	static public double sum(double[] arr) {
		double sum = 0;
		for (double d : arr) { sum += d; }
		return sum;
	}
	
	static public double min(double[] arr) {
		double min = Double.MAX_VALUE;
		for (double d : arr) { if (d < min) min = d; }
		return min;
	}
	
	static public double max(double[] arr) {
		double max = -Double.MAX_VALUE;
		for (double d : arr) { if (d > max) max = d; }
		return max;
	}
	
	static public StoredObservationSummary storedSummary(double[] arr) {
		StoredObservationSummary summ = new StoredObservationSummary();
		summ.add(arr);
		return summ;
	}
	
	static public StoredObservationSummary fieldToStoredSummary(String doubleFieldName, List<? extends Object> objects) {
		return storedSummary(fieldToDoubleArray(doubleFieldName,objects));
	}
	
	static public StoredObservationSummary fieldToStoredSummary(String doubleFieldName, Object[] objects) {
		return storedSummary(fieldToDoubleArray(doubleFieldName,objects));
	}
	
	static public double[] fieldToDoubleArray(String doubleFieldName, List<? extends Object> objects) {
		Class<?> clazz = objects.get(0).getClass();
		double[] arr = new double[objects.size()];
		try {
			Field field = clazz.getDeclaredField(doubleFieldName);
			field.setAccessible(true);
			for (int i=0; i<objects.size(); ++i) {
				arr[i] = field.getDouble(objects.get(i));
			}
		} catch (NoSuchFieldException e) {
			Method m;
			try {
				m = clazz.getDeclaredMethod(doubleFieldName);
				m.setAccessible(true);
				for (int i=0; i<objects.size(); ++i) {
					arr[i] = (Double) m.invoke(objects.get(i));
				}
			} catch (Exception e1) {
				e.printStackTrace();
				throw new RuntimeException(e1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return arr;
	}
	
	static public double[] fieldToDoubleArray(String doubleFieldName, Object[] objects) {
		Class<?> clazz = objects[0].getClass();
		double[] arr = new double[objects.length];
		try {
			Field field = clazz.getDeclaredField(doubleFieldName);
			field.setAccessible(true);
			for (int i=0; i<objects.length; ++i) {
				arr[i] = field.getDouble(objects[i]);
			}
		} catch (NoSuchFieldException e) {
			Method m;
			try {
				m = clazz.getDeclaredMethod(doubleFieldName);
				m.setAccessible(true);
				for (int i=0; i<objects.length; ++i) {
					arr[i] = (Double) m.invoke(objects[i]);
				}
			} catch (Exception e1) {
				e.printStackTrace();
				throw new RuntimeException(e1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return arr;
	}
}
