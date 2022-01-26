package com.acy.exam.metadata.srs.studentdomain.entity;

import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.FieldError;
import com.acy.exam.metadata.srs.commons.domain.RecordDeactivatedException;
import com.acy.exam.metadata.srs.commons.domain.StateValidationException;
import com.acy.exam.metadata.srs.studentdomain.StudentState;
import com.acy.exam.metadata.srs.studentdomain.command.UpdateStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.StudentDeactivatedEvent;
import com.acy.exam.metadata.srs.studentdomain.event.StudentUpdatedEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StudentTest {

    @ParameterizedTest
    @MethodSource("instantiateFromStateParams")
    public void instantiateFromState(StudentState givenState, Set<FieldError> expectedFieldErrors){

        StateValidationException capturedException =
            assertThrows(StateValidationException.class, () -> new Student(givenState));

        assertNotNull(capturedException);
        assertAll(
            () -> assertEquals(
                "Cannot instantiate Student Aggregate due to state validation errors.",
                capturedException.getMessage()
            ),
            () -> assertEquals(expectedFieldErrors, capturedException.fieldErrors)
        );
    }

    private static Stream<Arguments> instantiateFromStateParams(){
        return Stream.of(
            Arguments.of(
                StudentState.builder().build(),
                Set.of(
                    new FieldError("studentNumber", "must not be empty"),
                    new FieldError("firstName", "must not be empty"),
                    new FieldError("lastName", "must not be empty"),
                    new FieldError("recordDeactivated", "must not be null"),
                    new FieldError("dateRegistered", "must not be null")
                )
            ),
            Arguments.of(
                StudentState.builder()
                    .studentNumber("200810548")
                    .lastName(randomAlphanumeric(104))
                    .firstName(randomAlphanumeric(101))
                    .recordDeactivated(false)
                    .dateRegistered(LocalDate.now())
                    .build(),
                Set.of(
                    new FieldError("firstName", "length must not be greater than 100"),
                    new FieldError("lastName", "length must not be greater than 100")
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void update(boolean generateEvent){
        //given
        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName(randomAlphanumeric(25))
            .recordDeactivated(false)
            .firstName(randomAlphanumeric(25))
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .build();

        Student student = new Student(studentState);

        //when
        var command = new UpdateStudentCommand()
            .setFirstName("Adrian")
            .setLastName("Yago");

        Student updatedStudent = student.update(command, generateEvent);

        //then
        assertNotNull(updatedStudent);

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810548")
            .recordDeactivated(false)
            .lastName("Yago")
            .firstName("Adrian")
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .dateUpdated(LocalDate.now())
            .build();

        assertEquals(expectedState, updatedStudent.toState());

        assertEquals(generateEvent, updatedStudent.getLastUpdate().isPresent());
        updatedStudent.getLastUpdate().ifPresent(event -> {
            StudentUpdatedEvent expectedEvent = StudentUpdatedEvent.builder()
                .dateUpdated(LocalDate.now())
                .studentNumber("200810548")
                .lastName("Yago")
                .firstName("Adrian")
                .build();

            assertEquals(expectedEvent, event);
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void updateDeactivated(boolean generateEvent){
        //given
        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName(randomAlphanumeric(25))
            .recordDeactivated(true)
            .firstName(randomAlphanumeric(25))
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .build();

        Student student = new Student(studentState);

        //when
        var command = new UpdateStudentCommand()
            .setFirstName("Adrian")
            .setLastName("Yago");

        RecordDeactivatedException capturedException = assertThrows(
            RecordDeactivatedException.class, () -> student.update(command, generateEvent));

        //then
        assertNotNull(capturedException);
        assertEquals("Cannot perform operation on non existing student record.", capturedException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("updateValidationParams")
    public void updateValidation(UpdateStudentCommand givenCommand, Set<FieldError> expectedFieldErrors){
        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName(randomAlphanumeric(25))
            .firstName(randomAlphanumeric(25))
            .dateRegistered(LocalDate.now())
            .recordDeactivated(false)
            .build();

        CommandValidationException capturedException =
            assertThrows(CommandValidationException.class, () -> new Student(studentState).update(givenCommand, false));

        assertNotNull(capturedException);
        assertAll(
            () -> assertEquals(
                "Cannot update Student due to validation errors.",
                capturedException.getMessage()
            ),
            () -> assertEquals(expectedFieldErrors, capturedException.fieldErrors)
        );
    }

    private static Stream<Arguments> updateValidationParams(){
        return Stream.of(
            Arguments.of(
                new UpdateStudentCommand(),
                Set.of(
                    new FieldError("firstName", "must not be empty"),
                    new FieldError("lastName", "must not be empty")
                )
            ),
            Arguments.of(
                new UpdateStudentCommand()
                    .setLastName(randomAlphanumeric(104))
                    .setFirstName(randomAlphanumeric(101)),
                Set.of(
                    new FieldError("firstName", "length must not be greater than 100"),
                    new FieldError("lastName", "length must not be greater than 100")
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void deactivate(boolean generateEvent){
        //given
        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName("Yago")
            .recordDeactivated(false)
            .firstName("Adrian")
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .build();

        Student student = new Student(studentState);

        //when
        Student updatedStudent = student.deactivate(generateEvent);

        //then
        assertNotNull(updatedStudent);

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810548")
            .recordDeactivated(true)
            .lastName("Yago")
            .firstName("Adrian")
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .dateUpdated(LocalDate.now())
            .build();

        assertEquals(expectedState, updatedStudent.toState());

        assertEquals(generateEvent, updatedStudent.getLastUpdate().isPresent());
        updatedStudent.getLastUpdate().ifPresent(event -> {
            StudentDeactivatedEvent expectedEvent = new StudentDeactivatedEvent("200810548", LocalDate.now());

            assertEquals(expectedEvent, event);
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void deactivateDeactivated(boolean generateEvent){
        //given
        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName(randomAlphanumeric(25))
            .recordDeactivated(true)
            .firstName(randomAlphanumeric(25))
            .dateRegistered(LocalDate.of(2021, 1, 12))
            .build();

        Student student = new Student(studentState);

        //when
        RecordDeactivatedException capturedException = assertThrows(
            RecordDeactivatedException.class, () -> student.deactivate(generateEvent));

        //then
        assertNotNull(capturedException);
        assertEquals("Cannot perform operation on non existing student record.", capturedException.getMessage());
    }
}
