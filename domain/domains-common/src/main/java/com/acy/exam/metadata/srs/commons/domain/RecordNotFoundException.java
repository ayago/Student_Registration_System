package com.acy.exam.metadata.srs.commons.domain;

public class RecordNotFoundException extends RuntimeException{
    public RecordNotFoundException(String message) {
        super(message);
    }
}
