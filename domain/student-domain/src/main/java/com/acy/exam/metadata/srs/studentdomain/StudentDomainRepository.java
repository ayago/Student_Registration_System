package com.acy.exam.metadata.srs.studentdomain;

import reactor.core.publisher.Mono;

public interface StudentDomainRepository {
    Mono<String> generateNextStudentNumber();

    Mono<Void> save(StudentState studentState);

    Mono<StudentState> getStudentDetails(String studentNumber);
}
