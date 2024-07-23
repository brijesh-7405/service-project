/**
 * 
 */
package com.workruit.us.application.exception;

/**
 * @author Mahesh
 *
 */
public class JobNotFoundException extends Exception {

	private int errorCode;
	private String message;

	public JobNotFoundException() {
		super();
	}
	public JobNotFoundException(String message) {
		super(message);
	}

	public JobNotFoundException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	public JobNotFoundException(Throwable cause) {
		super(cause);
		
	}


}
