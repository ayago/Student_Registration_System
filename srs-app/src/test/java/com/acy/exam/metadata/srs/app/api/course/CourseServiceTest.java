package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.CourseDomainService;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    private static final CreateCourseCommand TEST_CREATE_COMMAND = new CreateCourseCommand()
        .setCourseCode("0NF")
        .setUnits(2)
        .setName("Sample Course");

    @InjectMocks
    CourseService courseService;

    @Mock
    CourseDomainService courseDomainService;

    @Test
    public void processCreateCommand(){
        String returnedCourseCode = TEST_CREATE_COMMAND.getCourseCode();

        when(courseDomainService.createNewCourse(any(CreateCourseCommand.class)))
            .thenReturn(Mono.just(returnedCourseCode));

        StepVerifier
            .create(courseService.processCreateCommand(TEST_CREATE_COMMAND))
            .expectNext(returnedCourseCode)
            .verifyComplete();

        verify(courseDomainService, times(1)).createNewCourse(eq(TEST_CREATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }

    @Test
    public void processCreateCommandNull(){
        when(courseDomainService.createNewCourse(any(CreateCourseCommand.class))).thenReturn(Mono.empty());

        StepVerifier
            .create(courseService.processCreateCommand(TEST_CREATE_COMMAND))
            .expectComplete();

        verify(courseDomainService, times(1)).createNewCourse(eq(TEST_CREATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }

    @Test
    public void processCreateCommandReturnsError(){
        when(courseDomainService.createNewCourse(any(CreateCourseCommand.class)))
            .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier
            .create(courseService.processCreateCommand(TEST_CREATE_COMMAND))
            .expectError(RuntimeException.class)
            .verify();

        verify(courseDomainService, times(1)).createNewCourse(eq(TEST_CREATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }
}
