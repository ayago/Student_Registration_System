package com.acy.exam.metadata.srs.coursedomain;

import reactor.core.publisher.Mono;

public interface CourseDomainRepository {
    Mono<Boolean> isNotYetUsed(String courseCode, String name);

    Mono<Void> save(CourseState courseState);
}
