package com.db.awmd.challenge.exception;

public class TransferMoneyException extends Exception {

	public TransferMoneyException(String message) {
		super(message);
	}

	public TransferMoneyException(String message, Exception e) {
		super(message, e);
	}
}
