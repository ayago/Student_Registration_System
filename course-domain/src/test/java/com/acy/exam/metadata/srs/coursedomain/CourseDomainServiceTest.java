package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.FieldError;
import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import com.acy.exam.metadata.srs.commons.domain.StateValidationException;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.command.UpdateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.event.CourseUpdatedEvent;
import com.acy.exam.metadata.srs.coursedomain.event.NewCourseEvent;
import org.apache.commons.lang3.RandomStringUtils;
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

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
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
        when(courseDomainRepository.isNotYetUsed(eq("AXY111"), eq("First Subject"))).thenReturn(Mono.just(true));
        when(courseDomainRepository.save(any(CourseState.class))).thenReturn(Mono.empty());

        if(hasPublisher){
            when(courseEventPublisher.publish(any())).thenReturn(Mono.empty());
        }

        CourseDomainService courseDomainService = hasPublisher ?
            new CourseDomainService(courseDomainRepository, courseEventPublisher) :
            new CourseDomainService(courseDomainRepository);

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AXY111")
            .setName("First Subject")
            .setUnits(2);

        StepVerifier
            .create(courseDomainService.createNewCourse(courseCommand))
            .expectNext("AXY111")
            .verifyComplete();

        CourseState expectedState = CourseState.builder()
            .courseCode("AXY111")
            .name("First Subject")
            .units(2)
            .dateCreated(LocalDate.now())
            .build();

        verify(courseDomainRepository, times(1)).save(eq(expectedState));
        if (hasPublisher) {
            NewCourseEvent expectedEvent = NewCourseEvent.builder()
                .courseCode("AXY111")
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
    public void existingCourseWithCodeOrName(){
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);
        when(courseDomainRepository.isNotYetUsed(eq("AZZ106"), eq("First Subject")))
            .thenReturn(Mono.just(false));

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
                    () -> assertEquals(
                        "Course with code AZZ106 or name First Subject is already used", capturedException.getMessage())
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

        if(nonNull(courseCommand.getCourseCode()) || nonNull(courseCommand.getName())){
            when(courseDomainRepository.isNotYetUsed(anyString(), anyString())).thenReturn(Mono.just(true));
        }

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


        verifyNoInteractions(courseEventPublisher);
        if(nonNull(courseCommand.getCourseCode()) || nonNull(courseCommand.getName())){
            verify(courseDomainRepository, atMostOnce())
                .isNotYetUsed(eq(courseCommand.getCourseCode()), eq(courseCommand.getName()));
            verifyNoMoreInteractions(courseDomainRepository);
        } else {
            verifyNoInteractions(courseDomainRepository);
        }
    }

    @Test
    @DisplayName("When courseDomainRepository#save fails, event publisher must not be invoked")
    public void newCourseStatePersistenceFailed(){
        when(courseDomainRepository.isNotYetUsed(eq("AXY"), eq("First Subject"))).thenReturn(Mono.just(true));
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
            ),
            Arguments.of(
                new CreateCourseCommand()
                    .setName(RandomStringUtils.randomAlphanumeric(51))
                    .setCourseCode("ZAXY65431"),
                new CommandValidationException(
                    Set.of(
                        new FieldError("name", "length must not be greater than 50"),
                        new FieldError("units", "must not be null"),
                        new FieldError(
                            "courseCode", "must be prefixed with 3 capital letters followed by three integers")
                    ),
                    "Cannot create course due to validation errors"
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateCourse(boolean hasPublisher){

        //given
        CourseState courseState = CourseState.builder()
            .courseCode("AXY111")
            .name("First Subject")
            .units(3)
            .dateCreated(LocalDate.of(2022, 1, 23))
            .build();

        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.just(courseState));
        when(courseDomainRepository.nameIsNotYetUsedByOthers(anyString(), anyString()))
            .thenReturn(Mono.just(true));
        when(courseDomainRepository.save(any(CourseState.class))).thenReturn(Mono.empty());

        if(hasPublisher){
            when(courseEventPublisher.publish(any())).thenReturn(Mono.empty());
        }

        CourseDomainService courseDomainService = hasPublisher ?
            new CourseDomainService(courseDomainRepository, courseEventPublisher) :
            new CourseDomainService(courseDomainRepository);

        //when
        var courseCommand = new UpdateCourseCommand()
            .setName("First Subject New")
            .setUnits(2);

        StepVerifier
            .create(courseDomainService.updateCourse("AXY111", courseCommand))
        //then
            .expectNext("AXY111")
            .verifyComplete();

        CourseState expectedState = CourseState.builder()
            .courseCode("AXY111")
            .name("First Subject New")
            .units(2)
            .dateCreated(LocalDate.of(2022, 1, 23))
            .dateUpdated(LocalDate.now())
            .build();

        verify(courseDomainRepository, times(1)).save(eq(expectedState));
        verify(courseDomainRepository, times(1))
            .nameIsNotYetUsedByOthers(eq("AXY111"), eq("First Subject New"));
        verify(courseDomainRepository, times(1))
            .getCurrentStateOfCourse(eq("AXY111"));
        if (hasPublisher) {
            CourseUpdatedEvent expectedEvent = CourseUpdatedEvent.builder()
                .courseCode("AXY111")
                .name("First Subject New")
                .units(2)
                .dateUpdated(LocalDate.now())
                .build();

            verify(courseEventPublisher, times(1)).publish(eq(expectedEvent));
            verifyNoMoreInteractions(courseDomainRepository, courseEventPublisher);
        } else {
            verifyNoMoreInteractions(courseDomainRepository);
            verifyNoInteractions(courseEventPublisher);
        }
    }

    @Test
    public void updateNotExistingCourse(){
        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.empty());
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        UpdateCourseCommand courseCommand = new UpdateCourseCommand()
            .setName("New Name")
            .setUnits(5);

        StepVerifier
            .create(courseDomainService.updateCourse("AXY272", courseCommand))
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(RecordNotFoundException.class, capturedException.getClass()),
                    () -> assertEquals(
                        "Course AXY272 does not exist.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();


        verify(courseDomainRepository, times(1)).getCurrentStateOfCourse(eq("AXY272"));
        verifyNoMoreInteractions(courseDomainRepository);
        verifyNoInteractions(courseEventPublisher);
    }

    @ParameterizedTest
    @MethodSource("updateCourseWithInvalidStateParams")
    public void updateCourseWithInvalidState(CourseState givenInvalidState, Set<FieldError> expectedFieldErrors){

        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.just(givenInvalidState));

        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        UpdateCourseCommand courseCommand = new UpdateCourseCommand()
            .setName("New Name")
            .setUnits(5);

        StepVerifier
            .create(courseDomainService.updateCourse("AXY272", courseCommand))
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> {
                        assertEquals(StateValidationException.class, capturedException.getClass());

                        StateValidationException castException = (StateValidationException) capturedException;
                        assertEquals(expectedFieldErrors, castException.fieldErrors);
                    },
                    () -> assertEquals(
                        "Cannot instantiate Course Aggregate due to state validation errors",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();


        verify(courseDomainRepository, times(1)).getCurrentStateOfCourse(eq("AXY272"));
        verifyNoMoreInteractions(courseDomainRepository);
        verifyNoInteractions(courseEventPublisher);
    }

    private static Stream<Arguments> updateCourseWithInvalidStateParams(){
        return Stream.of(
            Arguments.of(
                CourseState.builder().build(),
                Set.of(
                    new FieldError("dateCreated", "must not be null"),
                    new FieldError("name", "must not be null"),
                    new FieldError("units", "must not be null"),
                    new FieldError("courseCode", "must not be null")
                )
            ),
            Arguments.of(
                CourseState.builder()
                    .name(RandomStringUtils.randomAlphanumeric(54))
                    .dateCreated(LocalDate.now())
                    .units(4)
                    .courseCode("ABC9DF8")
                    .build(),
                Set.of(
                    new FieldError("name", "length must not be greater than 50"),
                    new FieldError(
                        "courseCode", "must be prefixed with 3 capital letters followed by three integers")
                )
            )
        );
    }

    @Test
    public void updateUsingExistingName(){
        //given
        CourseState courseState = CourseState.builder()
            .courseCode("AXY272")
            .name("First Subject")
            .units(3)
            .dateCreated(LocalDate.of(2022, 1, 23))
            .build();

        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.just(courseState));
        when(courseDomainRepository.nameIsNotYetUsedByOthers(anyString(), anyString())).thenReturn(Mono.just(false));
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        //when
        UpdateCourseCommand courseCommand = new UpdateCourseCommand()
            .setName("New Name")
            .setUnits(5);

        StepVerifier
            .create(courseDomainService.updateCourse("AXY272", courseCommand))
        //then
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(CommandConflictException.class, capturedException.getClass()),
                    () -> assertEquals(
                        "Course name New Name is already used.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();


        verify(courseDomainRepository, times(1)).getCurrentStateOfCourse(eq("AXY272"));
        verify(courseDomainRepository, times(1))
            .nameIsNotYetUsedByOthers(eq("AXY272"), eq("New Name"));
        verifyNoMoreInteractions(courseDomainRepository);
        verifyNoInteractions(courseEventPublisher);
    }

    @ParameterizedTest
    @MethodSource("updateUsingInvalidCommandParams")
    public void updateUsingInvalidCommand(UpdateCourseCommand courseCommand, Set<FieldError> expectedFieldErrors){

        //given
        CourseState courseState = CourseState.builder()
            .courseCode("AXY272")
            .name("First Subject")
            .units(3)
            .dateCreated(LocalDate.of(2022, 1, 23))
            .build();

        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.just(courseState));
        if(nonNull(courseCommand.getName())){
            when(courseDomainRepository.nameIsNotYetUsedByOthers(anyString(), anyString())).thenReturn(Mono.just(true));
        }

        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        //when
        StepVerifier
            .create(courseDomainService.updateCourse("AXY272", courseCommand))
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
                        "Cannot update course due to validation errors",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();


        verifyNoInteractions(courseEventPublisher);
        verify(courseDomainRepository, times(1)).getCurrentStateOfCourse(eq("AXY272"));

        if(nonNull(courseCommand.getName())){
            verify(courseDomainRepository, times(1))
                .nameIsNotYetUsedByOthers(eq("AXY272"), eq(courseCommand.getName()));
            verifyNoMoreInteractions(courseDomainRepository);
        }

        verifyNoMoreInteractions(courseDomainRepository);
    }

    private static Stream<Arguments> updateUsingInvalidCommandParams(){
        return Stream.of(
            Arguments.of(
                new UpdateCourseCommand(),
                Set.of(
                    new FieldError("name", "must not be null"),
                    new FieldError("units", "must not be null")
                )
            ),
            Arguments.of(
                new UpdateCourseCommand()
                    .setName(RandomStringUtils.randomAlphanumeric(54))
                    .setUnits(4),
                Set.of(
                    new FieldError("name", "length must not be greater than 50")
                )
            )
        );
    }

    @Test
    @DisplayName("When courseDomainRepository#save fails on update, event publisher must not be invoked")
    public void updatedCourseStatePersistenceFailed(){
        //given
        CourseState courseState = CourseState.builder()
            .courseCode("AZZ106")
            .name("First Subject")
            .units(3)
            .dateCreated(LocalDate.of(2022, 1, 23))
            .build();

        when(courseDomainRepository.getCurrentStateOfCourse(anyString())).thenReturn(Mono.just(courseState));
        when(courseDomainRepository.nameIsNotYetUsedByOthers(anyString(), anyString())).thenReturn(Mono.just(true));
        when(courseDomainRepository.save(any(CourseState.class))).thenReturn(Mono.error(new RuntimeException()));

        var courseCommand = new UpdateCourseCommand()
            .setName("Minor Subject")
            .setUnits(2);

        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository, courseEventPublisher);

        //when
        StepVerifier
            .create(courseDomainService.updateCourse("AZZ106", courseCommand))
        //then
            .expectError(RuntimeException.class)
            .verify();

        verifyNoInteractions(courseEventPublisher);
    }
}
