package com.acy.exam.metadata.srs.coursedomain.entity;

import com.acy.exam.metadata.srs.commons.domain.Aggregate;
import com.acy.exam.metadata.srs.commons.domain.CommandValidator;
import com.acy.exam.metadata.srs.commons.domain.Event;
import com.acy.exam.metadata.srs.coursedomain.CourseState;
import com.acy.exam.metadata.srs.coursedomain.command.CreateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.command.UpdateCourseCommand;
import com.acy.exam.metadata.srs.coursedomain.event.CourseUpdatedEvent;
import com.acy.exam.metadata.srs.coursedomain.event.NewCourseEvent;

import java.time.LocalDate;
import java.util.Optional;

public class Course implements Aggregate {

    public final CourseCode courseCode;
    private final String name;
    private final Integer units;
    private final LocalDate dateCreated;
    private final LocalDate dateUpdated;

    private final Event lastUpdate;

    public Course(CreateCourseCommand command, boolean generateEvent) {
        CommandValidator.validateCommand(command, "Cannot create course due to validation errors");

        this.courseCode = new CourseCode(command.getCourseCode());
        this.name = command.getName();
        this.units = command.getUnits();
        this.dateCreated = LocalDate.now();
        this.dateUpdated = null;

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

    public Course(CourseState courseState){
        CommandValidator.validateState(
            courseState, "Cannot instantiate Course Aggregate due to state validation errors");

        this.courseCode = new CourseCode(courseState.courseCode);
        this.name = courseState.name;
        this.units = courseState.units;
        this.dateCreated = courseState.dateCreated;
        this.dateUpdated = courseState.getDateUpdated().orElse(null);

        this.lastUpdate = null;
    }

    private Course(
        CourseCode courseCode,
        LocalDate dateCreated,
        UpdateCourseCommand command,
        boolean generateEvent
    ){
        this.courseCode = courseCode;
        this.name = command.getName();
        this.units = command.getUnits();
        this.dateCreated = dateCreated;
        this.dateUpdated = LocalDate.now();

        if (generateEvent) {
            this.lastUpdate = CourseUpdatedEvent.builder()
                .courseCode(courseCode.value)
                .dateUpdated(dateUpdated)
                .units(units)
                .name(name)
                .build();
        } else {
            this.lastUpdate = null;
        }
    }

    public Course update(UpdateCourseCommand command, boolean generateEvent){
        CommandValidator.validateCommand(command, "Cannot update course due to validation errors");
        return new Course(this.courseCode, this.dateCreated, command, generateEvent);
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
