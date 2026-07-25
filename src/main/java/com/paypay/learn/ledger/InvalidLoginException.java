package com.paypay.learn.ledger;


public class InvalidLoginException extends Exception {
  public InvalidLoginException() {
    super(
      "Invalid Login: The credentials don't match our record."
    );
  }
}
