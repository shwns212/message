package com.jun.message.exception;

public class FailedMessageDeserializeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FailedMessageDeserializeException(Throwable throwable) {
		super("Failed deserialize the message, check your data", throwable);
	}
}
