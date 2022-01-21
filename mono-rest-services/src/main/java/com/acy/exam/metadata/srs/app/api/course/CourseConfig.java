package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.coursedomain.CourseDomainRepository;
import com.acy.exam.metadata.srs.coursedomain.CourseDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseConfig {

    @Bean
    public CourseDomainService courseDomainService(CourseDomainRepository courseDomainRepository){
        return new CourseDomainService(courseDomainRepository);
    }
}
