/**
 * 
 */
package com.ewjordan.util.objectWrap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class PrivateMemberClass { // Do not edit!
	private boolean var;
}

/**
 * TODO
 */
public class PrimitiveReference {
	static private boolean garbage = false; //only used to avoid dead code pruning in JVM
	ReferenceType type;
	Field field;
	Object object;
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;
	private double standardDeviationScale = 1.0;
	
	/**
	 * Gets all available primitive references out of an object, after first checking
	 * whether or not private access is allowed through the SecurityManager.
	 * 
	 * Note that this will fail to grab Object references unless the objects
	 * define getAllReferences(boolean getPrivateRefs) methods that return
	 * PrimitiveReference[].  This method is grabbed via reflection, so no
	 * interface needs to be declared.
	 * @param o
	 * @return
	 */
	public static PrimitiveReference[] getAllReferencesFrom(Object o) {
		PrivateMemberClass obj = new PrivateMemberClass();
		Class<?> klazz = obj.getClass();
		Field[] fields = klazz.getDeclaredFields();
		
		for (int i=0; i<fields.length; ++i) {
			try { // Can we access private fields through reflection?
				fields[i].setAccessible(true);
				//set garbage var to avoid pruning of unused statement
				//TODO: see if JVM ever actually prunes unused code that might throw an exception...
				//might be prohibited by JLS
				garbage = fields[i].getBoolean(obj); 
			} catch (SecurityException e) { // If not, scrape the class for non-private references
				return getAllReferencesFrom(o,false);
			} catch (Exception e) {
				throw new RuntimeException(e); //some other problem came up...
			}
		}
		// We're all good - security policy lets us access private members thru reflection
		return getAllReferencesFrom(o, true);
	}
	
	private static void logPrint(String msg) { ; }
	private static void logPrintln(String msg) { ; }
	
	public static PrimitiveReference[] getAllReferencesFrom(Object o, boolean getPrivateReferences) {
		Class<?> klazz = o.getClass();
		Field[] fields = klazz.getDeclaredFields();
		
		List<PrimitiveReference> references = new ArrayList<PrimitiveReference>();
		
		for (Field f:fields) {
			f.setAccessible(true);
			if (Modifier.isPublic(f.getModifiers())) {
				logPrint("public ");
			} else if (Modifier.isProtected(f.getModifiers())) {
				logPrint("protected ");
			} else if (Modifier.isPrivate(f.getModifiers())) {
				logPrint("private ");
			}
			
			if (f.getAnnotation(Unwrapped.class) != null) {
				logPrint(" variable "+f.getName()+" not wrapped (annotated with @Unwrapped).");
				continue;
			}
			//System.out.println(f.getAnnotation(Unwrapped.class));
			double minValue = -Double.MAX_VALUE;
			double maxValue = Double.MAX_VALUE;
			double stdDev = 1.0;
			if (f.getAnnotation(MutationInfo.class) != null) {
				MutationInfo r = f.getAnnotation(MutationInfo.class);
				minValue = r.minimum();
				maxValue = r.maximum();
				stdDev = r.standardDeviation();
			}
			
			
			Class<?> fieldClass = f.getType();
			if (fieldClass.isPrimitive()) {
				if (fieldClass.equals(boolean.class)) {
					logPrintln("boolean");
					references.add(new PrimitiveReference(ReferenceType.BOOLEAN,o,f, minValue, maxValue, stdDev));
				} else if (fieldClass.equals(int.class)) {
					logPrintln("int");
					references.add(new PrimitiveReference(ReferenceType.INT,o,f, minValue, maxValue, stdDev));
				} else if (fieldClass.equals(float.class)) {
					logPrintln("float");
					references.add(new PrimitiveReference(ReferenceType.FLOAT,o,f, minValue, maxValue, stdDev));
				} else if (fieldClass.equals(double.class)) {
					logPrintln("double");
					references.add(new PrimitiveReference(ReferenceType.DOUBLE,o,f, minValue, maxValue, stdDev));
				} else if (fieldClass.equals(long.class)) {
					logPrintln("long");
					references.add(new PrimitiveReference(ReferenceType.LONG,o,f, minValue, maxValue, stdDev));
				}
			} else if (fieldClass.isArray()) {
				try {
					if (fieldClass.equals(boolean[].class)) {
						logPrintln("boolean array");
						boolean[] arr = (boolean[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.BOOLEAN,o,f,i, minValue, maxValue, stdDev));
							}
						}
					} else if (fieldClass.equals(int[].class)) {
						logPrintln("int array");
						int[] arr = (int[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.INT,o,f,i, minValue, maxValue, stdDev));
							}
						}
					} else if (fieldClass.equals(float[].class)) {
						logPrintln("float array");
						float[] arr = (float[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.FLOAT,o,f,i, minValue, maxValue, stdDev));
							}
						}
					} else if (fieldClass.equals(double[].class)) {
						logPrintln("double array");
						double[] arr = (double[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.DOUBLE,o,f,i, minValue, maxValue, stdDev));
							}
						}
					} else if (fieldClass.equals(long[].class)) {
						logPrintln("long array");
						long[] arr = (long[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.LONG,o,f,i, minValue, maxValue, stdDev));
							}
						}
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Method m = fieldClass.getMethod("getAllReferences", boolean.class);
					if (m != null) {
						PrimitiveReference[] addRefs = (PrimitiveReference[])m.invoke(o, getPrivateReferences);
						Collections.addAll(references, addRefs);
					} else {
						logPrintln("object of type " + o.getClass().getCanonicalName() + " (skipped)");
					}
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					logPrintln("object of type " + o.getClass().getCanonicalName() + " (skipped)");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return references.toArray(new PrimitiveReference[references.size()]);
	}
	
	public PrimitiveReference(PrimitiveReference copyMe) {
		this.type = copyMe.type;
		this.field = copyMe.field;
		this.object = copyMe.object;
		this.setMinValue(copyMe.getMinValue());
		this.setMaxValue(copyMe.getMaxValue());
		this.setStandardDeviationScale(copyMe.getStandardDeviationScale());
	}
	
	public PrimitiveReference clone() {
		return new PrimitiveReference(this);
	}
	
	public PrimitiveReference(ReferenceType type, Object object, Field field, double minValue, double maxValue, double standardDeviationScale) {
		this.type = type;
		this.object = object;
		this.field = field;
		this.setMinValue(minValue);
		this.setMaxValue(maxValue);
		this.setStandardDeviationScale(standardDeviationScale);
	}

	public String toString() {
		String result = "" + type + " field " + getFieldName() + " in " + getObjectTypeName();
		return result;
	}
	
	public String getFieldName() {
		return field.getName();
	}
	
	public String getObjectTypeName() {
		return object.getClass().getCanonicalName();
	}
	
	public void set(float newValue) throws IllegalArgumentException, IllegalAccessException {
		field.setFloat(object, newValue);
	}
	
	public void set(double newValue) throws IllegalArgumentException, IllegalAccessException {
		field.setDouble(object, newValue);
	}
	
	public void set(int newValue) throws IllegalArgumentException, IllegalAccessException {
		field.setInt(object, newValue);
	}
	
	public void set(long newValue) throws IllegalArgumentException, IllegalAccessException {
		field.setLong(object, newValue);
	}
	
	public void set(boolean newValue) throws IllegalArgumentException, IllegalAccessException {
		field.setBoolean(object, newValue);
	}
	
	public float getFloat() throws IllegalArgumentException, IllegalAccessException {
		return field.getFloat(object);
	}
	public double getDouble() throws IllegalArgumentException, IllegalAccessException {
		return field.getDouble(object);
	}
	public int getInt() throws IllegalArgumentException, IllegalAccessException {
		return field.getInt(object);
	}
	public long getLong() throws IllegalArgumentException, IllegalAccessException {
		return field.getLong(object);
	}
	public boolean getBoolean() throws IllegalArgumentException, IllegalAccessException {
		return field.getBoolean(object);
	}
	
	public ReferenceType getType() {
		return type;
	}
	
	public boolean isArrayMember() {
		return false;
	}

	void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	double getMinValue() {
		return minValue;
	}

	void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	double getMaxValue() {
		return maxValue;
	}

	void setStandardDeviationScale(double standardDeviationScale) {
		this.standardDeviationScale = standardDeviationScale;
	}

	double getStandardDeviationScale() {
		return standardDeviationScale;
	}
}