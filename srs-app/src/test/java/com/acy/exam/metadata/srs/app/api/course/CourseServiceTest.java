package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.CourseDomainService;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.command.UpdateCourseCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    private static final String TEST_COURSE_CODE = "0NF";

    private static final CreateCourseCommand TEST_CREATE_COMMAND = new CreateCourseCommand()
        .setCourseCode(TEST_COURSE_CODE)
        .setUnits(2)
        .setName("Sample Course");

    private static final UpdateCourseCommand TEST_UPDATE_COMMAND = new UpdateCourseCommand()
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

    @Test
    public void processUpdateCommand(){

        when(courseDomainService.updateCourse(anyString(), any(UpdateCourseCommand.class)))
            .thenReturn(Mono.just(TEST_COURSE_CODE));

        StepVerifier
            .create(courseService.processUpdateCommand(TEST_COURSE_CODE, TEST_UPDATE_COMMAND))
            .expectNext(TEST_COURSE_CODE)
            .verifyComplete();

        verify(courseDomainService, times(1))
            .updateCourse(eq(TEST_COURSE_CODE), eq(TEST_UPDATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }

    @Test
    public void processUpdayteCommandNull(){
        when(courseDomainService.updateCourse(anyString(), any(UpdateCourseCommand.class)))
            .thenReturn(Mono.empty());

        StepVerifier
            .create(courseService.processUpdateCommand(TEST_COURSE_CODE, TEST_UPDATE_COMMAND))
            .expectComplete();

        verify(courseDomainService, times(1))
            .updateCourse(eq(TEST_COURSE_CODE), eq(TEST_UPDATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }

    @Test
    public void processUpdateCommandReturnsError(){
        when(courseDomainService.updateCourse(anyString(), any(UpdateCourseCommand.class)))
            .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier
            .create(courseService.processUpdateCommand(TEST_COURSE_CODE, TEST_UPDATE_COMMAND))
            .expectError(RuntimeException.class)
            .verify();

        verify(courseDomainService, times(1))
            .updateCourse(eq(TEST_COURSE_CODE), eq(TEST_UPDATE_COMMAND));
        verifyNoMoreInteractions(courseDomainService);
    }
}
