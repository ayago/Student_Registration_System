package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException.FieldError;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.event.NewCourseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseDomainServiceTest {

    @Mock
    CourseEventPublisher courseEventPublisher;

    @Mock
    CourseDomainRepository courseDomainRepository;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createNewCourse(boolean hasPublisher) {
        when(courseDomainRepository.isNotYetUsed(eq("AXY"))).thenReturn(Mono.just(true));
        when(courseDomainRepository.save(any(CourseState.class))).thenReturn(Mono.empty());

        if(hasPublisher){
            when(courseEventPublisher.publish(any())).thenReturn(Mono.empty());
        }

        CourseDomainService courseDomainService = hasPublisher ?
            new CourseDomainService(courseDomainRepository, courseEventPublisher) :
            new CourseDomainService(courseDomainRepository);

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AXY")
            .setName("First Subject")
            .setUnits(2);

        StepVerifier
            .create(courseDomainService.createNewCourse(courseCommand))
            .expectNext("AXY")
            .verifyComplete();

        CourseState expectedState = CourseState.builder()
            .courseCode("AXY")
            .name("First Subject")
            .units(2)
            .dateCreated(LocalDate.now())
            .dateUpdated(LocalDate.now())
            .build();

        verify(courseDomainRepository, times(1)).save(eq(expectedState));
        if (hasPublisher) {
            NewCourseEvent expectedEvent = NewCourseEvent.builder()
                .courseCode("AXY")
                .name("First Subject")
                .units(2)
                .dateCreated(LocalDate.now())
                .build();

            verify(courseEventPublisher, times(1)).publish(eq(expectedEvent));
            verifyNoMoreInteractions(courseDomainRepository, courseEventPublisher);
        } else {
            verifyNoMoreInteractions(courseDomainRepository);
            verifyNoInteractions(courseEventPublisher);
        }
    }

    @Test
    @DisplayName("When courseDomainRepository#isNotYetUsed returns false, " +
        "courseDomainRepository#save and event publisher must not be invoked")
    public void existingCustomerCode(){
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);
        when(courseDomainRepository.isNotYetUsed(eq("AZZ106"))).thenReturn(Mono.just(false));

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AZZ106")
            .setName("First Subject")
            .setUnits(2);

        StepVerifier
            .create(courseDomainService.createNewCourse(courseCommand))
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(CommandConflictException.class, capturedException.getClass()),
                    () -> assertEquals("Course code AZZ106 is already used", capturedException.getMessage())
                );
            })
            .verify();

        verifyNoMoreInteractions(courseDomainRepository);
        verifyNoInteractions(courseEventPublisher);
    }

    @ParameterizedTest
    @MethodSource("newCourseCommandValidationParams")
    public void newCourseCommandValidation(
        CreateCourseCommand courseCommand, CommandValidationException expectedException){

        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        StepVerifier
            .create(courseDomainService.createNewCourse(courseCommand))
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> {
                        assertEquals(CommandValidationException.class, capturedException.getClass());

                        CommandValidationException castException = (CommandValidationException) capturedException;
                        assertEquals(expectedException.fieldErrors, castException.fieldErrors);
                    },
                    () -> assertEquals(expectedException.getMessage(), capturedException.getMessage())
                );
            })
            .verify();


        verifyNoInteractions(courseDomainRepository, courseEventPublisher);
    }

    @Test
    @DisplayName("When courseDomainRepository#save fails, event publisher must not be invoked")
    public void newCourseStatePersistenceFailed(){
        when(courseDomainRepository.isNotYetUsed(eq("AXY"))).thenReturn(Mono.just(true));
        when(courseDomainRepository.save(any(CourseState.class)))
            .thenReturn(Mono.error(new RuntimeException()));

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AZZ106")
            .setName("First Subject")
            .setUnits(2);

        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository, courseEventPublisher);

        StepVerifier
            .create(courseDomainService.createNewCourse(courseCommand))
            .expectError(RuntimeException.class)
            .verify();

        verifyNoInteractions(courseEventPublisher);
    }

    private static Stream<Arguments> newCourseCommandValidationParams(){
        return Stream.of(
            Arguments.of(
                new CreateCourseCommand(),
                new CommandValidationException(
                    Set.of(
                        new FieldError("name", "must not be null"),
                        new FieldError("units", "must not be null"),
                        new FieldError("courseCode", "must not be null")
                    ),
                    "Cannot create course due to validation errors"
                )
            )
        );
    }
}
