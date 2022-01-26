package com.acy.exam.metadata.srs.studentdomain.event;

import com.acy.exam.metadata.srs.commons.domain.Event;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Builder
@EqualsAndHashCode
public class StudentUpdatedEvent implements Event {
    public final String studentNumber;
    public final String firstName;
    public final String lastName;
    private final LocalDate dateUpdated;
}
