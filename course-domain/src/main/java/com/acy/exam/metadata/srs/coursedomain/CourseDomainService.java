package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.entity.Course;

import static java.util.Objects.nonNull;

public class CourseDomainService {
    private final CourseDomainRepository repository;
    private final CourseEventPublisher eventPublisher;
    private final boolean generateEvent;

    public CourseDomainService(CourseDomainRepository repository) {
        this(repository, null);
    }

    public CourseDomainService(CourseDomainRepository repository, CourseEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.generateEvent = nonNull(eventPublisher);
    }

    public void createNewCourse(CreateCourseCommand command) {
        ensureCourseCodeIsUnique(command);

        Course course = new Course(command, generateEvent);
        if(course.getLastUpdate().isPresent()){
            eventPublisher.publish(course.getLastUpdate().get());
        }

        CourseState courseState = course.toState();
        repository.save(courseState);
    }

    private void ensureCourseCodeIsUnique(CreateCourseCommand command){
        String courseCode = command.getCourseCode();
        if(nonNull(courseCode) && !repository.isNotYetUsed(courseCode)){
            throw new CommandConflictException("Course code "+command.getCourseCode()+" is already used");
        }
    }
}
