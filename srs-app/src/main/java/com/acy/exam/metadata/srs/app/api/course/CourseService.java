package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.CourseDomainService;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.command.UpdateCourseCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class CourseService {

    private final CourseDomainService courseDomainService;

    public CourseService(CourseDomainService courseDomainService) {
        this.courseDomainService = courseDomainService;
    }

    public Mono<String> processCreateCommand(CreateCourseCommand command){
        return courseDomainService.createNewCourse(command);
    }

    public Mono<String> processUpdateCommand(String courseCode, UpdateCourseCommand command){
        return courseDomainService.updateCourse(courseCode, command);
    }
}
