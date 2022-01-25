package com.acy.exam.metadata.srs.studentdomain.exception;

public class StudentAggregateException extends RuntimeException{
    public StudentAggregateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentAggregateException(String message) {
        super(message);
    }
}
