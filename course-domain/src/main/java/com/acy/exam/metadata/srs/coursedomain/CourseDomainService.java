package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.entity.Course;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;
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

    public Mono<String> createNewCourse(CreateCourseCommand command) {

        return isUniqueCourseCode(command)
            .flatMap(passedCheck -> passedCheck ?
                Mono.just(command) :
                Mono.error(() ->
                    new CommandConflictException("Course code "+command.getCourseCode()+" is already used"))
            )
            .flatMap(this::createCourseAggregate)
            .flatMap(course -> {
                CourseState courseState = course.toState();
                return repository.save(courseState)
                    .then(Mono.defer(() -> course.getLastUpdate()

                        //event publisher can be null so we can't use method reference
                        .map(lastEvent -> eventPublisher.publish(lastEvent))
                        .orElse(Mono.empty()))
                    )
                    .then(Mono.just(courseState.getCourseCode()));
            });
    }

    private Mono<Course> createCourseAggregate(CreateCourseCommand command){
        try {
            Course course = new Course(command, generateEvent);
            return Mono.just(course);
        } catch (CommandValidationException ex){
            return Mono.error(ex);
        }
    }

    private Mono<Boolean> isUniqueCourseCode(CreateCourseCommand command){
        return Mono.defer(() -> {
            String courseCode = command.getCourseCode();
            if(isNull(courseCode)) {
                return Mono.just(true);
            }

            return repository.isNotYetUsed(courseCode);
        });
    }
}
