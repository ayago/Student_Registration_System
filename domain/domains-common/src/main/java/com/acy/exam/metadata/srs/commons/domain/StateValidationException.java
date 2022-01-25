package com.acy.exam.metadata.srs.commons.domain;

import java.util.Set;

public class StateValidationException extends RuntimeException{

    public final Set<FieldError> fieldErrors;

    public StateValidationException(Set<FieldError> fieldErrors, String message){
        super(message);
        this.fieldErrors = fieldErrors;
    }
}
