package com.acy.exam.metadata.srs.studentdomain;

import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.command.UpdateStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.entity.Student;
import com.acy.exam.metadata.srs.studentdomain.entity.StudentFactory;
import reactor.core.publisher.Mono;

import static java.util.Objects.nonNull;

public class StudentDomainService {
    private final StudentDomainRepository repository;
    private final StudentEventPublisher eventPublisher;
    private final StudentFactory studentFactory;
    private final boolean generateEvent;

    public StudentDomainService(StudentDomainRepository repository) {
        this(repository, null);
    }

    public StudentDomainService(StudentDomainRepository repository, StudentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;

        this.generateEvent = nonNull(eventPublisher);
        this.studentFactory = new StudentFactory(repository::generateNextStudentNumber, generateEvent);
    }

    public Mono<String> registerNewStudent(RegisterStudentCommand command) {
        return studentFactory.createStudent(command)
            .flatMap(this::persistAndOptionallyPublish);
    }

    public Mono<String> updateStudentRecord(String studentNumber, UpdateStudentCommand command) {
        return repository.getStudentDetails(studentNumber)
            .switchIfEmpty(getNotFoundExceptionPublisher())
            .map(Student::new)
            .map(student -> student.update(command, generateEvent))
            .flatMap(this::persistAndOptionallyPublish);
    }

    public Mono<String> deactivateStudentRecord(String studentNumber) {
        return repository.getStudentDetails(studentNumber)
            .switchIfEmpty(getNotFoundExceptionPublisher())
            .map(Student::new)
            .map(student -> student.deactivate(generateEvent))
            .flatMap(this::persistAndOptionallyPublish);
    }

    private Mono<? extends String> persistAndOptionallyPublish(Student student) {
        return Mono.defer(() -> {
            StudentState studentState = student.toState();
            return repository.save(studentState)
                .then(Mono.defer(() -> student.getLastUpdate()
                    //event publisher can be null so we can't use method reference
                    .map(lastEvent -> eventPublisher.publish(lastEvent))
                    .orElse(Mono.empty()))
                )
                .then(Mono.just(studentState.studentNumber));
        });
    }

    private static <T> Mono<T> getNotFoundExceptionPublisher(){
        return Mono.error(
            () -> new RecordNotFoundException("Cannot perform operation on non existing student record."));
    }
}
