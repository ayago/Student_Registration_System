package com.acy.exam.metadata.srs.commons.domain;

import java.util.Set;

public class CommandValidationException extends RuntimeException{

    public final Set<FieldError> fieldErrors;

    public CommandValidationException(Set<FieldError> fieldErrors, String message){
        super(message);
        this.fieldErrors = fieldErrors;
    }

}
