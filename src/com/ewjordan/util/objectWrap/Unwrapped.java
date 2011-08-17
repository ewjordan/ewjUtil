/**
 * 
 */
package com.ewjordan.util.objectWrap;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker to set certain fields as not wrapped during conversion to a WrappedObject.
 * 
 * @author eric
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Unwrapped {

}
