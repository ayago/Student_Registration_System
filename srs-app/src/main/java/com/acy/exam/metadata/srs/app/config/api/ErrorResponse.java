package com.acy.exam.metadata.srs.app.config.api;

import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorResponse {

    public final String message;
    public final List<PropertyWithError> fieldErrors;

    ErrorResponse(CommandValidationException exception) {
        this.message = exception.getMessage();
        this.fieldErrors = exception.fieldErrors.stream()
            .map(e -> new PropertyWithError(e.field, e.errorMessage))
            .collect(Collectors.toUnmodifiableList());
    }

    ErrorResponse(Throwable throwable) {
        this.message = throwable.getMessage();
        this.fieldErrors = Collections.emptyList();
    }

    public static class PropertyWithError {
        public final String field;
        public final String message;

        PropertyWithError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
