/**
 * 
 */
package com.ewjordan.util.objectWrap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



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
	
	private static final double booleanThreshold = 0.5;
	private static final double defaultBooleanFalse = 0.25;
	private static final double defaultBooleanTrue = 0.75;
	
	private PrimitiveReference[] references; //fields that correspond to values stored here
	
	public WrappedObject(PrimitiveReference ... references) {
		this.references = references;
		this.values = new double[this.references.length];
		pullValuesFromObject();
	}
	
	public WrappedObject(Object objectToWrap) {
		this(PrimitiveReference.getAllReferencesFrom(objectToWrap, true));
		object = objectToWrap;
	}
	
	/**
	 * Clone the wrapper (does not clone the underlying object).
	 * 
	 * Note that a cloned wrapper may be changed separately from the original
	 * wrapper, and the underlying object's values will only change when values
	 * are pushed through.
	 * @param cloneMe
	 */
	public WrappedObject(WrappedObject cloneMe) {
		this.values = new double[cloneMe.values.length];
		this.references = new PrimitiveReference[cloneMe.references.length];
		this.object = cloneMe.object;
		System.arraycopy(cloneMe.values,0,this.values,0,cloneMe.values.length);
		System.arraycopy(cloneMe.references,0,this.references,0,cloneMe.references.length);
	}
	
	/**
	 * Save the current values to an {@link ObjectOutputStream}.
	 * Does not save the object or the references - these must be
	 * re-constituted manually at load time.
	 * 
	 * Load saved objects using {@link #load(Object, ObjectInputStream)}
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void save(ObjectOutputStream out) throws IOException {
		out.writeObject(values);
		out.flush();
	}
	
	/**
	 * Save the current values of the {@link WrappedObject} to an {@link ObjectOutputStream}.
	 * Does not save the object or the references - these must be
	 * re-constituted manually at load time.
	 * 
	 * Load saved objects using {@link #load(Object, ObjectInputStream)}
	 * 
	 * @param obj the {@link WrappedObject} to save
	 * @param out the stream to save the values to
	 * @throws IOException
	 */
	static public void save(WrappedObject obj, ObjectOutputStream out) throws IOException {
		obj.save(out);
	}
	
	/**
	 * Load values from an {@link ObjectInputStream} and interpret them
	 * as a WrappedObject with the target as the target object.  The values
	 * will be loaded into the object.
	 * 
	 * Note that the saved object contains no information about the target object
	 * other than the length of the fields, so no error checking can be done
	 * beyond a length check, which will throw an {@link IllegalArgumentException}
	 * if failed.
	 * 
	 * In particular, if the class of the object has changed since saving, the
	 * object may not be properly restored - in particular, if the order that
	 * the fields are returned via reflection changes, things will not work properly,
	 * and if the count remains the same, there will be no indication that anything
	 * is wrong.
	 * 
	 * This function loads values saved via {@link #save(ObjectOutputStream)}
	 * 
	 * @param target the base object to wrap
	 * @param in the input stream containing the values to load
	 * @return a {@link WrappedObject} that wraps the target
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	static public WrappedObject load(Object target, ObjectInputStream in) throws IOException, ClassNotFoundException {
		WrappedObject obj = new WrappedObject(target);
		obj.values = (double[])in.readObject();
		if (obj.references.length != obj.values.length) {
			throw new IllegalArgumentException("The target object does not match the format of the values you are trying to load into it.");
		}
		obj.pushValuesToObject();
		return obj;
	}
	
	/**
	 * Convert the {@link WrappedObject} to an {@link OptimizableWrappedObject}.
	 * The underlying {@link Object} that is wrapped must implement the {@link HasValue} interface,
	 * otherwise a {@link RuntimeException} will be thrown.
	 * 
	 * @return an {@link OptimizableWrappedObject} that is a clone of this {@link WrappedObject}
	 */
	public OptimizableWrappedObject optimizable() {
		final WrappedObject obj = this;
		if (obj.getObject() instanceof HasValue) {
			return new OptimizableWrappedObject(this) {
				// Note that from here on, "this" refers to the OptimizableWrappedObject,
				// and NOT the WrappedObject (obj).  It's very important not to confuse
				// the two, otherwise values won't get pushed properly
				@Override
				public double getValue() {
					this.pushValuesToObject();
					double val = ((HasValue)this.getObject()).getValue();
					return val;
				}
			};
		} else {
			throw new RuntimeException("Base object " + obj.getObject() + " does not implement HasValue interface");
		}
	}
	
	/**
	 * Create an OptimizableWrappedObject with a given function as
	 * the objective function.
	 * @param func
	 * @return
	 */
	public OptimizableWrappedObject optimizable(final WrappedObjectToDoubleFunction func) {
		final WrappedObject obj = this;
		OptimizableWrappedObject opt = new OptimizableWrappedObject(this) {
			@Override
			public double getValue() {
				return func.evaluate(obj);
			}
		};
		return opt;
	}
	
	/**
	 * @return a direct reference to the values array - alterations will
	 * be reflected in the WrappedObject values, but will not be pushed to
	 * the underlying object until {@link #pushValuesToObject()} is called.
	 */
	public double[] getValues() {
		return values;
	}
	
	/**
	 * Sets a new target object, and instructs all references to reseat
	 * themselves on the new target.
	 */
	public void setObject(Object o) { 
		object = o;
		for (int i=0; i<references.length; ++i) {
			references[i].object = o;
		}
	}
	public Object getObject() { return object; }
	
	public PrimitiveReference getReferenceAt(int index) {
		return references[index];
	}
	
	public double getValue(int index) {
		return values[index];
	}
	
	public void setValue(int index, double value) {
		values[index] = value;
	}
	
	public void setValues(double[] values) {
		System.arraycopy(values,0,this.values,0,values.length);
	}
	
	public int getNumberOfMembers() { return values.length; }
	
	/**
	 * Pulls values from object into WrappedObject's member array.
	 */
	public double[] pullValuesFromObject() {
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
	
	public void pushValuesToObject() {
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
	
	/**
	 * Pushes a set of values to the object, and stores
	 * them in the WrappedObject.
	 * @param values
	 */
	public void pushValuesToObject(double[] values) {
		if (values.length != this.getNumberOfMembers()) {
			throw new IllegalArgumentException("The array of values passed had length " + values.length
					+ ", but this object has " + this.getNumberOfMembers() + " fields to be set.");
		}
		setValues(values);
		pushValuesToObject();
	}
	
	public ReferenceType getTypeAt(int index) {
		return references[index].getType();
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
