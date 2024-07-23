/**
 * 
 */
package com.workruit.us.application.exception;

/**
 * @author Mahesh
 *
 */
public class CreateJobFailedException extends Exception {
	private int errorCode;
	private String message;

	public CreateJobFailedException() {
		super();
	}
	public CreateJobFailedException(String message) {
		super(message);
	}

	public CreateJobFailedException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	public CreateJobFailedException(Throwable cause) {
		super(cause);
		
	}
}
