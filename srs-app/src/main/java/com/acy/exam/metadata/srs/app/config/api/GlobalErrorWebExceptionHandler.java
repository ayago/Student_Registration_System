package com.acy.exam.metadata.srs.app.config.api;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private static final Map<Class<? extends Throwable>, HttpStatus> EXCEPTION_HTTP_STATUS_MAP = Map.of(
        CommandConflictException.class, HttpStatus.CONFLICT,
        CommandValidationException.class, HttpStatus.UNPROCESSABLE_ENTITY,
        RecordNotFoundException.class, HttpStatus.NOT_FOUND
    );

    public GlobalErrorWebExceptionHandler(
        ErrorAttributes errorAttributes,
        ApplicationContext applicationContext,
        ServerCodecConfigurer serverCodecConfigurer
    ) {
        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {

        return RouterFunctions.route(
            RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        Throwable throwable = getError(request);
        ErrorResponse response = resolveErrorResponse(throwable);
        HttpStatus httpStatus =
            EXCEPTION_HTTP_STATUS_MAP.getOrDefault(throwable.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ServerResponse.status(httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(response));
    }

    private static ErrorResponse resolveErrorResponse(Throwable throwable){
        if(throwable instanceof CommandValidationException){
            return new ErrorResponse((CommandValidationException) throwable);
        }

        return new ErrorResponse(throwable);
    }
}