package com.pechterev.statemachine.exception;

/**
 * Исключение о некорректном переходе по стейтам заявки
 */
public class WrongStateTransitionException extends RuntimeException {

    public WrongStateTransitionException(String message) {
        super(message);
    }
}

