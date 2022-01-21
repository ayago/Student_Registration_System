package com.acy.exam.metadata.srs.commons.domain;

import com.acy.exam.metadata.srs.commons.domain.CommandValidationException.FieldError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandValidatorTest{

    @Test
    public void validateCommand(){
        var testObject = new TestValidateableObject();

        testObject.childObjects = List.of(
            new TestValidateableChildObject()
        );

        var capturedException = assertThrows(
            CommandValidationException.class, () -> CommandValidator.validateCommand(testObject, "Error Message"));

        assertNotNull(capturedException);
        assertAll(
            () -> assertEquals("Error Message", capturedException.getMessage()),
            () -> assertEquals(expectedErrors(), capturedException.fieldErrors)
        );
    }

    private static Set<FieldError> expectedErrors(){
        return Set.of(
            new FieldError("stringField", "must not be null"),
            new FieldError("childObjects", "size must be between 2 and 3"),
            new FieldError("childObjects[0].intField", "Custom null message")
        );
    }

    private static class TestValidateableObject {

        @NotNull
        private String stringField;

        @Valid
        @Size(min = 2, max = 3)
        private List<TestValidateableChildObject> childObjects;
    }

    private static class TestValidateableChildObject {
        @NotNull(message = "Custom null message")
        private Integer intField;
    }
}
