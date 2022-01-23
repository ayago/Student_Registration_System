package com.acy.exam.metadata.srs.app.data.repository;

import com.acy.exam.metadata.srs.app.data.entity.CourseEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CourseEntityRepository extends ReactiveCrudRepository<CourseEntity, String> {
    Mono<Boolean> existsByCourseCodeOrName(String courseCode, String name);
}
