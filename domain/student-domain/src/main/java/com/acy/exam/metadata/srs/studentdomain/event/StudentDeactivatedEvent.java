package com.acy.exam.metadata.srs.studentdomain.event;

import com.acy.exam.metadata.srs.commons.domain.Event;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode
public class StudentDeactivatedEvent implements Event {
    public final String studentNumber;
    public final LocalDate dateUpdated;

    public StudentDeactivatedEvent(String studentNumber, LocalDate dateUpdated) {
        this.studentNumber = studentNumber;
        this.dateUpdated = dateUpdated;
    }
}
