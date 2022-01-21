package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.CourseDomainService;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CourseService {

    private final CourseDomainService courseDomainService;

    public CourseService(CourseDomainService courseDomainService) {
        this.courseDomainService = courseDomainService;
    }

    public Mono<String> processCreateCommand(CreateCourseCommand command){
        return courseDomainService.createNewCourse(command);
    }
}
