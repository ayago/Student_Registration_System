package com.acy.exam.metadata.srs.app.config.api;

import com.acy.exam.metadata.srs.app.config.api.ErrorHandlerTestController.ErrorHandlerTestService;
import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.FieldError;
import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ErrorHandlerTestController.class, GlobalErrorWebExceptionHandler.class})
public class GlobalErrorWebExceptionHandlerTest {

    @MockBean
    ErrorHandlerTestService errorHandlerTestService;

    @Autowired
    WebTestClient testClient;

    @ParameterizedTest
    @MethodSource("handlingOnPostEndpointParams")
    void handlingOnPostEndpoint(
        Throwable error,
        HttpStatus expectedStatus,
        JSONObject expectedResponse
    ){
        assertExceptionHandling(testClient.post(), error, expectedStatus, expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("handlingOnPostEndpointParams")
    void handlingOnPutEndpoint(
        Throwable error,
        HttpStatus expectedStatus,
        JSONObject expectedResponse
    ){
        assertExceptionHandling(testClient.put(), error, expectedStatus, expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("handlingOnPostEndpointParams")
    void handlingOnGetEndpoint(
        Throwable error,
        HttpStatus expectedStatus,
        JSONObject expectedResponse
    ){
        assertExceptionHandling(testClient.get(), error, expectedStatus, expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("handlingOnPostEndpointParams")
    void handlingOnDeleteEndpoint(
        Throwable error,
        HttpStatus expectedStatus,
        JSONObject expectedResponse
    ){
        assertExceptionHandling(testClient.delete(), error, expectedStatus, expectedResponse);
    }

    private void assertExceptionHandling(
        WebTestClient.UriSpec<? extends WebTestClient.RequestHeadersSpec<?>> testSpec,
        Throwable error,
        HttpStatus expectedStatus,
        JSONObject expectedResponse
    ){
        when(errorHandlerTestService.someEndpoint()).thenReturn(Mono.error(error));

        testSpec
            .uri("/")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().json(expectedResponse.toString());
    }

    private static Stream<Arguments> handlingOnPostEndpointParams() throws JSONException {
        return Stream.of(
            Arguments.of(
                new CommandConflictException("Command conflict"),
                HttpStatus.CONFLICT,
                new JSONObject().put("message", "Command conflict")
            ),
            Arguments.of(
                new RuntimeException("Unhandled error"),
                HttpStatus.INTERNAL_SERVER_ERROR,
                new JSONObject().put("message", "Unhandled error")
            ),
            Arguments.of(
                new RuntimeException(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                new JSONObject().put("message", null)
            ),
            Arguments.of(
                new CommandValidationException(
                    Set.of(
                        new FieldError("someField", "test Message"),
                        new FieldError("anotherField", "another message")
                    ),
                    "Command validation error"
                ),
                HttpStatus.UNPROCESSABLE_ENTITY,
                new JSONObject()
                    .put("message", "Command validation error")
                    .put("fieldErrors", new JSONArray()
                        .put(new JSONObject()
                            .put("field", "anotherField")
                            .put("message", "another message")
                        )
                        .put(new JSONObject()
                            .put("field", "someField")
                            .put("message", "test Message")
                        )
                    )
            ),
            Arguments.of(
                new RecordNotFoundException("Record 2019 not found"),
                HttpStatus.NOT_FOUND,
                new JSONObject().put("message", "Record 2019 not found")
            )
        );
    }
}
