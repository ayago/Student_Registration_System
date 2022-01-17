package com.acy.exam.metadata.srs.coursedomain.entity;

import com.acy.exam.metadata.srs.commons.domain.CommandValidator;
import com.acy.exam.metadata.srs.commons.domain.Event;
import com.acy.exam.metadata.srs.coursedomain.CourseState;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.event.NewCourseEvent;

import java.time.LocalDate;
import java.util.Optional;

public class Course {

    private final CourseCode courseCode;
    public final String name;
    public final Integer units;
    public final LocalDate dateCreated;
    public final LocalDate dateUpdated;

    private final Event lastUpdate;

    public Course(CreateCourseCommand command, boolean generateEvent) {
        CommandValidator.validateCommand(command, "Cannot create course due to validation errors");

        this.courseCode = new CourseCode(command.getCourseCode());
        this.name = command.getName();
        this.units = command.getUnits();
        this.dateCreated = LocalDate.now();
        this.dateUpdated = LocalDate.now();

        if (generateEvent) {
            this.lastUpdate = NewCourseEvent.builder()
                .courseCode(courseCode.value)
                .dateCreated(dateCreated)
                .units(units)
                .name(name)
                .build();
        } else {
            this.lastUpdate = null;
        }
    }

    public Optional<Event> getLastUpdate() {
        return Optional.ofNullable(lastUpdate);
    }

    public CourseState toState() {
        return CourseState.builder()
            .courseCode(courseCode.value)
            .dateCreated(dateCreated)
            .dateUpdated(dateUpdated)
            .units(units)
            .name(name)
            .build();
    }
}
