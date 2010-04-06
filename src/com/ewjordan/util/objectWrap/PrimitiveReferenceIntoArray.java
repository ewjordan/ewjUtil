/**
 * 
 */
package com.ewjordan.util.objectWrap;

import java.lang.reflect.Field;

/**
 * @author eric
 *
 */
public class PrimitiveReferenceIntoArray extends PrimitiveReference {
	int indexInArray;
	
	public String getFieldName() {
		return field.getName() + ", index " + indexInArray;
	}
	
	public PrimitiveReferenceIntoArray(PrimitiveReferenceIntoArray ref) {
		super(ref);
		this.indexInArray = ref.indexInArray;
	}
	
	public PrimitiveReferenceIntoArray clone() {
		return new PrimitiveReferenceIntoArray(this);
	}
	
	public PrimitiveReferenceIntoArray(ReferenceType type, Object object, Field arrayField, int arrayIndex) {
		super(type, object, arrayField);
		if (!arrayField.getType().isArray()) throw new IllegalArgumentException();
		this.indexInArray = arrayIndex;
	}
	
	public void set(float newValue) throws IllegalArgumentException, IllegalAccessException {
		((float[])field.get(object))[indexInArray] = newValue;
	}
	
	public void set(double newValue) throws IllegalArgumentException, IllegalAccessException {
		((double[])field.get(object))[indexInArray] = newValue;
	}
	
	public void set(int newValue) throws IllegalArgumentException, IllegalAccessException {
		((int[])field.get(object))[indexInArray] = newValue;
	}
	
	public void set(long newValue) throws IllegalArgumentException, IllegalAccessException {
		((long[])field.get(object))[indexInArray] = newValue;
	}
	
	public void set(boolean newValue) throws IllegalArgumentException, IllegalAccessException {
		((boolean[])field.get(object))[indexInArray] = newValue;
	}
	
	public float getFloat() throws IllegalArgumentException, IllegalAccessException {
		return ((float[])field.get(object))[indexInArray];
	}
	public double getDouble() throws IllegalArgumentException, IllegalAccessException {
		return ((double[])field.get(object))[indexInArray];
	}
	public int getInt() throws IllegalArgumentException, IllegalAccessException {
		return ((int[])field.get(object))[indexInArray];
	}
	public long getLong() throws IllegalArgumentException, IllegalAccessException {
		return ((long[])field.get(object))[indexInArray];
	}
	public boolean getBoolean() throws IllegalArgumentException, IllegalAccessException {
		return ((boolean[])field.get(object))[indexInArray];
	}
}

