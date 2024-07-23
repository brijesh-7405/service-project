/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 *
 */
public enum OfferStatus {
	SELECTED,OFFER_SENT,OFFER_RECIEVED,OFFER_SIGNED,OFFER_REJECTED,APPLICANT_NOT_JOINED,APPLICANT_JOINED,OFFER_ACCEPTED;
	//1        //2          //3           //4          //5           //6                   //7               //8
	public int getValue() {
	    return ordinal() + 1;
	}
}
