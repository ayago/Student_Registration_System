package com.acy.exam.metadata.srs.studentdomain.entity;

import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.FieldError;
import com.acy.exam.metadata.srs.studentdomain.StudentState;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.NewStudentEvent;
import com.acy.exam.metadata.srs.studentdomain.exception.StudentAggregateException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentFactoryTest {

    @Mock
    Supplier<Mono<String>> idValueGenerator;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void createStudent(boolean generateEvent){
        //given
        StudentFactory studentFactory = new StudentFactory(idValueGenerator, generateEvent);
        when(idValueGenerator.get()).thenReturn(Mono.just("200810498"));

        RegisterStudentCommand command = new RegisterStudentCommand()
            .setFirstName("Baby")
            .setLastName("Maguire");

        //when
        StepVerifier.create(studentFactory.createStudent(command))
        //then
            .consumeNextWith(student -> {
                assertNotNull(student);
                StudentState expectedState = StudentState.builder()
                    .studentNumber("200810498")
                    .dateRegistered(LocalDate.now())
                    .firstName("Baby")
                    .lastName("Maguire")
                    .build();


                assertEquals(expectedState, student.toState());

                if(generateEvent){
                    NewStudentEvent expectedEvent = NewStudentEvent.builder()
                        .studentNumber("200810498")
                        .lastName("Maguire")
                        .firstName("Baby")
                        .dateRegistered(LocalDate.now())
                        .build();

                    assertEquals(expectedEvent, student.getLastUpdate().orElse(null));
                } else {
                    assertTrue(student.getLastUpdate().isEmpty());
                }
            })
            .verifyComplete();

        verify(idValueGenerator, times(1)).get();
        verifyNoMoreInteractions(idValueGenerator);
    }

    @ParameterizedTest
    @MethodSource("invalidStudentNumberOnRegisterParams")
    public void invalidStudentNumberOnRegister(Mono<String> erroneousResponse){
        //given
        StudentFactory studentFactory = new StudentFactory(idValueGenerator, false);
        when(idValueGenerator.get()).thenReturn(erroneousResponse);

        RegisterStudentCommand command = new RegisterStudentCommand()
            .setFirstName("Baby")
            .setLastName("Maguire");

        //when
        StepVerifier.create(studentFactory.createStudent(command))
        //then
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(StudentAggregateException.class, capturedException.getClass()),
                    () -> assertEquals(
                        "Cannot create student due to failed id value generation.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();

        verify(idValueGenerator, times(1)).get();
        verifyNoMoreInteractions(idValueGenerator);
    }

    private static Stream<Arguments> invalidStudentNumberOnRegisterParams(){
        return Stream.of(
            Arguments.of(Mono.error(new RuntimeException())),
            Arguments.of(Mono.empty())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRegisterCommandParams")
    public void invalidRegisterCommand(RegisterStudentCommand givenCommand, Set<FieldError> expectedFieldErrors){
        //given
        StudentFactory studentFactory = new StudentFactory(idValueGenerator, false);

        //when
        StepVerifier.create(studentFactory.createStudent(givenCommand))
        //then
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> {
                        assertEquals(CommandValidationException.class, capturedException.getClass());

                        CommandValidationException castException = (CommandValidationException) capturedException;
                        assertEquals(expectedFieldErrors, castException.fieldErrors);
                    },
                    () -> assertEquals(
                        "Cannot create student record due to validation errors.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();

        verifyNoInteractions(idValueGenerator);
    }

    private static Stream<Arguments> invalidRegisterCommandParams(){
        return Stream.of(
            Arguments.of(
                new RegisterStudentCommand(),
                Set.of(
                    new FieldError("firstName", "must not be empty"),
                    new FieldError("lastName", "must not be empty")
                )
            ),
            Arguments.of(
                new RegisterStudentCommand()
                    .setFirstName(RandomStringUtils.randomAlphanumeric(101))
                    .setLastName(RandomStringUtils.randomAlphanumeric(104)),
                Set.of(
                    new FieldError("firstName", "length must not be greater than 100"),
                    new FieldError("lastName", "length must not be greater than 100")
                )
            )
        );
    }
}
