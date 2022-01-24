package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.app.data.entity.CourseEntity;
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
    public Mono<Boolean> isNotYetUsed(String courseCode, String name) {
        return courseEntityRepository.existsByCourseCodeOrName(courseCode, name).map(exists -> !exists);
    }

    @Override
    public Mono<Boolean> nameIsNotYetUsedByOthers(String courseCode, String name) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> save(CourseState courseState) {
        return Mono.defer(() -> {
            CourseEntity entity = new CourseEntity()
                .setCourseCode(courseState.courseCode)
                .setName(courseState.name)
                .setUnits(courseState.units)
                .setDateCreated(courseState.dateCreated)
                .setDateUpdated(courseState.getDateUpdated().orElse(null));

            return courseEntityRepository.save(entity);
        }).flatMap(e -> Mono.empty());
    }

    @Override
    public Mono<CourseState> getCurrentStateOfCourse(String courseCode) {
        return Mono.empty();
    }
}
