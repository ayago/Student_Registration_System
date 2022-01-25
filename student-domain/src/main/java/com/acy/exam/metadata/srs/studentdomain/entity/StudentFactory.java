package com.acy.exam.metadata.srs.studentdomain.entity;

import com.acy.exam.metadata.srs.commons.domain.CommandValidator;
import com.acy.exam.metadata.srs.studentdomain.StudentDomainRepository;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.exception.StudentAggregateException;
import reactor.core.publisher.Mono;

public class StudentFactory {
    private static final String STUDENT_AGGREGATION_EXCEPTION_MESSAGE =
        "Cannot create student due to failed id value generation.";
    private final boolean generateEvent;
    private final StudentDomainRepository studentRepository;

    public StudentFactory(StudentDomainRepository studentRepository, boolean generateEvent) {
        this.studentRepository = studentRepository;
        this.generateEvent = generateEvent;
    }

    public Mono<Student> createStudent(RegisterStudentCommand command){
        return Mono.defer(() -> {
            CommandValidator.validateCommand(command, "Cannot create student record due to validation errors.");

            return studentRepository.generateNextStudentNumber()
                .switchIfEmpty(Mono.error(() ->
                    new StudentAggregateException(STUDENT_AGGREGATION_EXCEPTION_MESSAGE)))
                .onErrorResume(e ->
                    Mono.error(new StudentAggregateException(STUDENT_AGGREGATION_EXCEPTION_MESSAGE, e)))
                .map(StudentNumber::new)
                .map(studentNumber -> new Student(command, studentNumber, generateEvent));
        });

    }
}
