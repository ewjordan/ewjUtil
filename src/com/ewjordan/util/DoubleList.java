package com.ewjordan.util;

/**
 * A quick substitute to handle the dual annoyances of
 * a) not being able to use arrays and collections with each other, and
 * b) the fact that collections take up 8x the space of primitive arrays and are slow
 * @author eric
 */
public class DoubleList {
	private double[] doubles;
	private int size;
	private float multiplier;
	
	/**
	 * Divide elements of one DoubleList by the elements of another.
	 * Usually useful for normalization purposes.
	 */
	public DoubleList dividedBy(DoubleList counts, double divByZeroValue) {
		if (this.size() != counts.size()) throw new IllegalArgumentException("DoubleLists must be same size to normalize one by another.");
		DoubleList res = new DoubleList(counts.size());
		for (int i=0; i<counts.size(); ++i) {
			if (counts.get(i) > 0.0) res.add(this.get(i) / counts.get(i));
			else res.add(divByZeroValue);
		}
		return res;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<size; ++i) {
			sb.append(get(i));
			if (i < size - 1) sb.append(",");
		}
		return sb.toString();
	}
	
	public String toColumnString() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<size; ++i) {
			sb.append(get(i));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	static public DoubleList createAndFill(int size, double value) {
		DoubleList dl = new DoubleList(size);
		for (int i=0; i<size; ++i) {
			dl.add(value);
		}
		return dl;
	}
	
	public DoubleList() {
		this(10);
	}
	
	public DoubleList(int capacity) {
		this.size = 0;
		doubles = new double[capacity];
		multiplier = 1.1f;
	}
	
	public DoubleList(int capacity, float multiplier) {
		this(capacity);
		this.multiplier = multiplier;
	}
	
	public DoubleList clone() {
		DoubleList copy = new DoubleList(this.size());
		copy.multiplier = multiplier;
		for (int i=0; i<this.size(); ++i) {
			copy.add(get(i));
		}
		return copy;
	}
	
	public int size() {
		return size;
	}
	
	public void add(DoubleList dl) {
		for (int i=0; i<dl.size(); ++i) {
			add(dl.get(i));
		}
	}
	
	public void add(double[] arr) {
		for (int i=0; i<arr.length; ++i) {
			add(arr[i]);
		}
	}
	
	public void add(double newdouble) {
		if (size == doubles.length) {
			//copy array and re-make
			int newSize = (int)(size * multiplier + 10);
			resize(newSize);
		}
		doubles[size++] = newdouble;
	}
	
	//Possibly destructive if newSize < doubles.length
	private void resize(int newSize) {
		double[] newArray = new double[newSize];
		System.arraycopy(doubles, 0, newArray, 0, Math.min(newSize, doubles.length));//doubles.length);
		doubles = newArray;
	}
	
	private void pack() {
		resize(size);
	}
	
	public double[] getBackingArray() {
		pack();
		return doubles;
	}
	
	public double get(int index) {
		if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException(index);
		return doubles[index];
	}
	
	public void set(int index, double value) {
		if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException(index);
		doubles[index] = value;
	}
	
	public void increment(int index, double addition) {
		if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException(index);
		doubles[index] += addition;
	}
}
