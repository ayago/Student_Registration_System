package com.acy.exam.metadata.srs.commons.domain;

public class CommandConflictException extends RuntimeException{
    public CommandConflictException(String message) {
        super(message);
    }
}
