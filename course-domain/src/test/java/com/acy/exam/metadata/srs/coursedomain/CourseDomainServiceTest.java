package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException.FieldError;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.event.NewCourseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(courseDomainRepository.isNotYetUsed(eq("AXY"))).thenReturn(true);

        CourseDomainService courseDomainService = hasPublisher ?
            new CourseDomainService(courseDomainRepository, courseEventPublisher) :
            new CourseDomainService(courseDomainRepository);

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AXY")
            .setName("First Subject")
            .setUnits(2);

        courseDomainService.createNewCourse(courseCommand);

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
    public void existingCustomerCode(){
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);
        when(courseDomainRepository.isNotYetUsed(eq("AZZ106"))).thenReturn(false);

        var courseCommand = new CreateCourseCommand()
            .setCourseCode("AZZ106")
            .setName("First Subject")
            .setUnits(2);

        CommandConflictException capturedException =
            assertThrows(CommandConflictException.class, () -> courseDomainService.createNewCourse(courseCommand));

        assertNotNull(capturedException);
        assertEquals("Course code AZZ106 is already used", capturedException.getMessage());

        verifyNoMoreInteractions(courseDomainRepository);
        verifyNoInteractions(courseEventPublisher);
    }

    @ParameterizedTest
    @MethodSource("newCourseCommandValidationParams")
    public void newCourseCommandValidation(
        CreateCourseCommand givenCommand, CommandValidationException expectedException){
        CourseDomainService courseDomainService = new CourseDomainService(courseDomainRepository);

        CommandValidationException capturedException =
            assertThrows(CommandValidationException.class, () -> courseDomainService.createNewCourse(givenCommand));

        assertNotNull(capturedException);
        assertAll(
            () -> assertEquals(expectedException.getMessage(), capturedException.getMessage()),
            () -> assertEquals(expectedException.fieldErrors, capturedException.fieldErrors)
        );

        verifyNoInteractions(courseDomainRepository, courseEventPublisher);
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
