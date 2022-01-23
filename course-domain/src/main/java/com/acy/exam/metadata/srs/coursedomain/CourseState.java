package com.acy.exam.metadata.srs.coursedomain;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class CourseState {
    public final String courseCode;
    public final String name;
    public final Integer units;
    public final LocalDate dateCreated;
    private final LocalDate dateUpdated;

    public Optional<LocalDate> getDateUpdated() {
        return Optional.ofNullable(dateUpdated);
    }
}
