package com.acy.exam.metadata.srs.coursedomain.event;

import com.acy.exam.metadata.srs.commons.domain.Event;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Builder
@EqualsAndHashCode
public class NewCourseEvent implements Event {
    public final String name;
    public final Integer units;
    public final LocalDate dateCreated;
    public final String courseCode;
}
