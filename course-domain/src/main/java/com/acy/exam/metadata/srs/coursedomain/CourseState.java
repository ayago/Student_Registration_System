package com.acy.exam.metadata.srs.coursedomain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CourseState {
    String courseCode;
    String name;
    Integer units;
    LocalDate dateCreated;
    LocalDate dateUpdated;

    @Value
    public static class CourseCodeState {
        String value;
    }
}
