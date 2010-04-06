/**
 * 
 */
package com.ewjordan.util.objectWrap;



/**
 * Wraps an object so that its primitive fields (including primitive arrays)
 * of numerical and boolean type are condensed into a single array of doubles.
 * 
 * @author eric
 *
 */
public class WrappedObject {
	private Object object;
	private double[] values;
	
	private static double booleanThreshold = 0.5;
	private static final double defaultBooleanFalse = 0.25;
	private static final double defaultBooleanTrue = 0.75;
	
	private PrimitiveReference[] references; //fields that correspond to values stored here
	
	public WrappedObject(PrimitiveReference ... references) {
		this.references = references;
		this.values = new double[this.references.length];
		refreshValuesFromObject();
	}
	
	public WrappedObject(Object objectToWrap) {
		this(PrimitiveReference.getAllReferencesFrom(objectToWrap, true));
	}
	
	public double[] getValues() {
		return values;
	}
	
	public void setObject(Object o) { object = o; }
	public Object getObject() { return object; }
	
	public double getValue(int index) {
		return values[index];
	}
	
	public void setValue(int index, double value) {
		values[index] = value;
	}
	
	public int getNumberOfMembers() { return values.length; }
	
	/**
	 * Pulls values from object into WrappedObject's member array.
	 */
	public double[] refreshValuesFromObject() {
		for (int i=0; i<references.length; ++i) {
			try {
				switch(references[i].type) {
				case BOOLEAN:
					values[i] = references[i].getBoolean()?defaultBooleanTrue:defaultBooleanFalse;
					break;
				case INT:
					values[i] = references[i].getInt();
					break;
				case LONG:
					values[i] = references[i].getLong();
					break;
				case FLOAT:
					values[i] = references[i].getFloat();
					break;
				case DOUBLE:
					values[i] = references[i].getDouble();
					break;
				}
			} catch (Exception e) {
				System.err.println("Could not retrieve value from index " + i + ", reference " + references[i]);
				throw new RuntimeException(e);
			}
		}
		return getValues();
	}
	
	public void updateObjectValues() {
		for (int i=0; i<references.length; ++i) {
			try {
				switch(references[i].type) {
				case BOOLEAN:
					references[i].set(getBooleanAt(i));
					break;
				case INT:
					references[i].set(getIntAt(i));
					break;
				case LONG:
					references[i].set(getLongAt(i));
					break;
				case FLOAT:
					references[i].set(getFloatAt(i));
					break;
				case DOUBLE:
					references[i].set(getDoubleAt(i));
					break;
				}
			} catch (Exception e) {
				System.err.println("Could not update index " + i + " to reference " + references[i]);
				throw new RuntimeException(e);
			}
		}
	}
	
	private double getDoubleAt(int index) {
		return values[index];
	}
	
	private float getFloatAt(int index) {
		return (float)values[index];
	}
	
	private long getLongAt(int index) {
		return (long)values[index];
	}
	
	private int getIntAt(int index) {
		return (int)values[index];
	}
	
	private boolean getBooleanAt(int index) {
		return (values[index] > booleanThreshold);
	}
}






