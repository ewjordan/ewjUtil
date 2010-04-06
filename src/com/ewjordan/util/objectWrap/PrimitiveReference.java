/**
 * 
 */
package com.ewjordan.util.objectWrap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


class PrivateMemberClass { // Do not edit!
	private boolean var;
}

/**
 * @author eric
 *
 */
public class PrimitiveReference {
	ReferenceType type;
	Field field;
	Object object;
	
	public static PrimitiveReference[] getAllReferencesFrom(Object o) {
		PrivateMemberClass obj = new PrivateMemberClass();
		Class<?> klazz = obj.getClass();
		Field[] fields = klazz.getDeclaredFields();
		
		for (int i=0; i<fields.length; ++i) {
			try { // Can we access private fields through reflection?
				fields[i].setAccessible(true);
				boolean b = fields[i].getBoolean(obj);
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
			if (Modifier.isPublic(f.getModifiers())) {
				logPrint("public ");
			} else if (Modifier.isProtected(f.getModifiers())) {
				logPrint("protected ");
			} else if (Modifier.isPrivate(f.getModifiers())) {
				logPrint("private ");
				if (getPrivateReferences) f.setAccessible(true);
				else continue;
			}
			
			if (f.getAnnotation(Unwrapped.class) != null) {
				logPrint(" variable "+f.getName()+" not wrapped (annotated with @Unwrapped).");
				continue;
			}
			//System.out.println(f.getAnnotation(Unwrapped.class));
			
			Class<?> fieldClass = f.getType();
			if (fieldClass.isPrimitive()) {
				if (fieldClass.equals(boolean.class)) {
					logPrintln("boolean");
					references.add(new PrimitiveReference(ReferenceType.BOOLEAN,o,f));
				} else if (fieldClass.equals(int.class)) {
					logPrintln("int");
					references.add(new PrimitiveReference(ReferenceType.INT,o,f));
				} else if (fieldClass.equals(float.class)) {
					logPrintln("float");
					references.add(new PrimitiveReference(ReferenceType.FLOAT,o,f));
				} else if (fieldClass.equals(double.class)) {
					logPrintln("double");
					references.add(new PrimitiveReference(ReferenceType.DOUBLE,o,f));
				} else if (fieldClass.equals(long.class)) {
					logPrintln("long");
					references.add(new PrimitiveReference(ReferenceType.LONG,o,f));
				}
			} else if (fieldClass.isArray()) {
				try {
					if (fieldClass.equals(boolean[].class)) {
						logPrintln("boolean array");
						boolean[] arr = (boolean[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.BOOLEAN,o,f,i));
							}
						}
					} else if (fieldClass.equals(int[].class)) {
						logPrintln("int array");
						int[] arr = (int[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.INT,o,f,i));
							}
						}
					} else if (fieldClass.equals(float[].class)) {
						logPrintln("float array");
						float[] arr = (float[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.FLOAT,o,f,i));
							}
						}
					} else if (fieldClass.equals(double[].class)) {
						logPrintln("double array");
						double[] arr = (double[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.DOUBLE,o,f,i));
							}
						}
					} else if (fieldClass.equals(long[].class)) {
						logPrintln("long array");
						long[] arr = (long[])f.get(o);
						if (arr != null) {
							for (int i=0; i<arr.length; ++i) {
								references.add(new PrimitiveReferenceIntoArray(ReferenceType.LONG,o,f,i));
							}
						}
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				logPrintln("object of type " + o.getClass().getCanonicalName() + " (skipped)");
			}
		}
		return references.toArray(new PrimitiveReference[references.size()]);
	}
	
	public PrimitiveReference(PrimitiveReference copyMe) {
		this.type = copyMe.type;
		this.field = copyMe.field;
		this.object = copyMe.object;
	}
	
	public PrimitiveReference clone() {
		return new PrimitiveReference(this);
	}
	
	public PrimitiveReference(ReferenceType type, Object object, Field field) {
		this.type = type;
		this.object = object;
		this.field = field;
		
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
}