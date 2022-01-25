package com.acy.exam.metadata.srs.studentdomain;

import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.entity.Student;
import com.acy.exam.metadata.srs.studentdomain.entity.StudentFactory;
import reactor.core.publisher.Mono;

import static java.util.Objects.nonNull;

public class StudentDomainService {
    private final StudentDomainRepository repository;
    private final StudentEventPublisher eventPublisher;
    private final StudentFactory studentFactory;

    public StudentDomainService(StudentDomainRepository repository) {
        this(repository, null);
    }

    public StudentDomainService(StudentDomainRepository repository, StudentEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;

        boolean generateEvent = nonNull(eventPublisher);
        this.studentFactory = new StudentFactory(repository, generateEvent);
    }

    public Mono<String> registerNewStudent(RegisterStudentCommand command) {
        return studentFactory.createStudent(command)
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
}
