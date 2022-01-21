package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.app.data.repository.CourseEntityRepository;
import com.acy.exam.metadata.srs.coursedomain.CourseDomainRepository;
import com.acy.exam.metadata.srs.coursedomain.CourseState;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CourseDomainRepositoryImpl implements CourseDomainRepository {

    private final CourseEntityRepository courseEntityRepository;

    public CourseDomainRepositoryImpl(CourseEntityRepository courseEntityRepository){
        this.courseEntityRepository = courseEntityRepository;
    }

    @Override
    public Mono<Boolean> isNotYetUsed(String courseCode) {
        return Mono.just(true);
    }

    @Override
    public Mono<Void> save(CourseState courseState) {
        return Mono.empty();
    }
}
