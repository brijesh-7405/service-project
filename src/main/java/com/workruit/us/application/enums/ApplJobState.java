/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 *
 */
public enum ApplJobState {
	APPLIED,REJECTED;
	
	public int getValue() {
	    return ordinal() + 1;
	}
}