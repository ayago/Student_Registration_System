package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = CourseController.class)
public class CourseControllerTest {

    @Autowired
    WebTestClient testClient;

    @MockBean
    CourseService courseService;

    @Test
    public void createNewCourse() throws JSONException {
        when(courseService.processCreateCommand(any(CreateCourseCommand.class))).thenReturn(Mono.just("SXE"));

        JSONObject request = new JSONObject()
            .put("courseCode", "0NF")
            .put("name", "Sample Course")
            .put("units", 2);

        testClient.post()
            .uri("/courses")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().location("/courses/SXE");

        CreateCourseCommand expectedRequest = new CreateCourseCommand()
            .setCourseCode("0NF")
            .setUnits(2)
            .setName("Sample Course");

        verify(courseService, times(1)).processCreateCommand(eq(expectedRequest));
        verifyNoMoreInteractions(courseService);
    }
}
