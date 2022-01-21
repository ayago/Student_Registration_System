package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> createNewCourse(@RequestBody CreateCourseCommand command){
        return courseService.processCreateCommand(command)
            .map(courseCode -> {
                URI location = URI.create(String.format("/courses/%s", courseCode));
                return ResponseEntity.created(location).build();
            });
    }
}
