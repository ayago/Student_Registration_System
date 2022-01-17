package com.acy.exam.metadata.srs.coursedomain;

public interface CourseDomainRepository {
    boolean isNotYetUsed(String courseCode);

    void save(CourseState courseState);
}
