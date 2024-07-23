/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 *
 */
public enum RejectStatus {

	OFFER_REJECTED, NO_SHOW, NOT_FIT;
	
	public int getValue() {
	    return ordinal() + 1;
	}
}
