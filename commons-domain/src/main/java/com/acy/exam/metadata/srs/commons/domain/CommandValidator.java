package com.acy.exam.metadata.srs.commons.domain;

import com.acy.exam.metadata.srs.commons.domain.CommandValidationException.FieldError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.commons.lang3.StringUtils;

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
            throw buildException(violations, message);
        }
    }

    private static <E> CommandValidationException buildException(
        Set<ConstraintViolation<E>> constraintViolations, String message){
        Set<FieldError> fieldErrors = constraintViolations.stream()
            .map(CommandValidator::resolveFieldError)
            .collect(Collectors.toUnmodifiableSet());

        return new CommandValidationException(fieldErrors, message);
    }

    private static FieldError resolveFieldError(ConstraintViolation<?> constraintViolation){
        return new FieldError(
            constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
    }
}
