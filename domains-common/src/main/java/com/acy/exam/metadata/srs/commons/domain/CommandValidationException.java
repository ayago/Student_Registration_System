package com.acy.exam.metadata.srs.commons.domain;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CommandValidationException extends RuntimeException{

    public final Set<FieldError> fieldErrors;

    public CommandValidationException(Set<FieldError> fieldErrors, String message){
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public static class FieldError{
        public final String field;
        public final String errorMessage;

        public FieldError(String field, String errorMessage){
            this.field = field;
            this.errorMessage = errorMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldError that = (FieldError) o;
            return Objects.equals(field, that.field) && Objects.equals(errorMessage, that.errorMessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, errorMessage);
        }
    }
}
