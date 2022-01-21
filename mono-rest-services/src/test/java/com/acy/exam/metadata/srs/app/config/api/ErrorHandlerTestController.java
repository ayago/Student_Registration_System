package com.acy.exam.metadata.srs.app.config.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class ErrorHandlerTestController {

    private final ErrorHandlerTestService errorHandlerTestService;

    ErrorHandlerTestController(ErrorHandlerTestService errorHandlerTestService) {
        this.errorHandlerTestService = errorHandlerTestService;
    }

    @PostMapping
    Mono<String> post(){
        return errorHandlerTestService.someEndpoint();
    }

    @GetMapping
    Mono<String> get(){
        return errorHandlerTestService.someEndpoint();
    }

    @PutMapping
    Mono<String> put(){
        return errorHandlerTestService.someEndpoint();
    }

    @DeleteMapping
    Mono<String> delete(){
        return errorHandlerTestService.someEndpoint();
    }


    public interface ErrorHandlerTestService {
        Mono<String> someEndpoint();
    }
}
