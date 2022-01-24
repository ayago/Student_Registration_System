package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.CommandConflictException;
import com.acy.exam.metadata.srs.commons.domain.CommandValidationException;
import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.command.UpdateCourseCommand;
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

        return isUniqueCourseCodeOrName(command)
            .flatMap(passedCheck -> passedCheck ?
                Mono.just(command) :
                Mono.error(() -> {
                    String message = String.format(
                        "Course with code %s or name %s is already used", command.getCourseCode(), command.getName());
                    return new CommandConflictException(message);
                })
            )
            .flatMap(this::createCourseAggregate)
            .flatMap(this::persistAndOptionallyPublish);
    }

    private Mono<Course> createCourseAggregate(CreateCourseCommand command){
        try {
            Course course = new Course(command, generateEvent);
            return Mono.just(course);
        } catch (CommandValidationException ex){
            return Mono.error(ex);
        }
    }

    private Mono<Boolean> isUniqueCourseCodeOrName(CreateCourseCommand command){
        return Mono.defer(() -> {
            String courseCode = command.getCourseCode();
            String name = command.getName();
            if(isNull(courseCode) && isNull(name)) {
                return Mono.just(true);
            }

            return repository.isNotYetUsed(courseCode, name);
        });
    }

    public Mono<String> updateCourse(String courseCodeStr, UpdateCourseCommand command){
        return repository.getCurrentStateOfCourse(courseCodeStr)
            .switchIfEmpty(Mono.error(() -> {
                String errorMessage = String.format("Course %s does not exist.", courseCodeStr);
                return new RecordNotFoundException(errorMessage);
            }))
            .map(Course::new)
            .flatMap(course -> applyUpdateToCourse(command, course))
            .flatMap(this::persistAndOptionallyPublish);
    }

    private Mono<? extends String> persistAndOptionallyPublish(Course course) {
        return Mono.defer(() -> {
            CourseState courseState = course.toState();
            return repository.save(courseState)
                .then(Mono.defer(() -> course.getLastUpdate()
                    //event publisher can be null so we can't use method reference
                    .map(lastEvent -> eventPublisher.publish(lastEvent))
                    .orElse(Mono.empty()))
                )
                .then(Mono.just(courseState.courseCode));
        });
    }

    private Mono<Course> applyUpdateToCourse(UpdateCourseCommand command, Course course) {
        return isNameNotYetUsedByOther(course, command.getName())
            .flatMap(passedCheck -> passedCheck ?
                Mono.defer(() -> Mono.just(course.update(command, generateEvent))) :
                Mono.error(() -> {
                    String message = String.format("Course name %s is already used.", command.getName());
                    return new CommandConflictException(message);
                })
            );
    }

    private Mono<Boolean> isNameNotYetUsedByOther(Course course, String newName){
        return Mono.defer(() -> {
            if(isNull(newName)) {
                return Mono.just(true);
            }

            return repository.nameIsNotYetUsedByOthers(course.courseCode.value, newName);
        });
    }
}
