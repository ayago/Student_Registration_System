package com.acy.exam.metadata.srs.commons.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

public final class CommandValidator{

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    public static <E> void validateCommand(E command, String message){

        Set<ConstraintViolation<E>> violations = VALIDATOR.validate(command);
        if(!violations.isEmpty()){
            var fieldErrors = parseFieldErrors(violations);
            throw new CommandValidationException(fieldErrors, message);
        }
    }

    public static <E> void validateState(E state, String message){

        Set<ConstraintViolation<E>> violations = VALIDATOR.validate(state);
        if(!violations.isEmpty()){
            var fieldErrors = parseFieldErrors(violations);
            throw new StateValidationException(fieldErrors, message);
        }
    }

    private static <E> Set<FieldError> parseFieldErrors(Set<ConstraintViolation<E>> constraintViolations) {
        Set<FieldError> fieldErrors = constraintViolations.stream()
            .map(CommandValidator::resolveFieldError)
            .collect(Collectors.toUnmodifiableSet());

        return fieldErrors;
    }

    private static FieldError resolveFieldError(ConstraintViolation<?> constraintViolation){
        return new FieldError(
            constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
    }
}
