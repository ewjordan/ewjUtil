/**
 * 
 */
package com.ewjordan.util.objectWrap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;


class TestHolder {
	public double publicDouble = 1.5;
	private double privateDouble = 2.0;
	
	protected boolean protectedBoolean = true;
	
	@Unwrapped public int[] publicIntArray = new int[10];
	public double[] publicDoubleArray = new double[5];
	
	{
		Arrays.fill(publicIntArray, -5);
		Arrays.fill(publicDoubleArray, 2.3);
	}
	
	public TestHolder testHolder; //should be ignored, as object field
}

/**
 * @author eric
 *
 */
public class WrappedObjectTest {
	TestHolder myHolder = new TestHolder();
	PrimitiveReference[] refs = null;
	WrappedObject myObject = null;

	@Before
	public void setup() {
		try {
			refs = PrimitiveReference.getAllReferencesFrom(myHolder,true);
			myObject = new WrappedObject(myHolder);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Test method for {@link com.ewjordan.objectWrap.WrappedObject#WrappedObject(com.ewjordan.objectWrap.PrimitiveReference[])}.
	 */
	@Test
	public void testWrappedObject() {
		try {
			assertTrue(refs.length == 8);
			assertTrue(refs[0].getDouble() == 1.5);
			assertTrue(refs[1].getDouble() == 2.0);
			assertTrue(refs[2].getBoolean() == true);
			for (int i=3; i<8; ++i) {
				assertTrue(refs[i].getDouble() == 2.3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Test method for {@link com.ewjordan.objectWrap.WrappedObject#updateObjectValues()}.
	 */
	@Test
	public void testUpdateObjectValues() {
		try {
			
		} catch (Exception e) {
			
		}
		fail("Not finished.");
	}

}
