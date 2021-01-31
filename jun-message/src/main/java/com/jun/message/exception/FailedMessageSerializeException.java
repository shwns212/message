package com.jun.message.exception;

public class FailedMessageSerializeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FailedMessageSerializeException(Throwable throwable) {
		super("Failed serialize the message, check your data", throwable);
	}
}
