package com.acy.exam.metadata.srs.coursedomain.command;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class CreateCourseCommand {

    @Getter
    @NotNull
    private String name;

    @Getter
    @NotNull
    private Integer units;

    @Getter
    @NotNull
    private String courseCode;

    public CreateCourseCommand setName(String name) {
        this.name = name;
        return this;
    }

    public CreateCourseCommand setUnits(Integer units) {
        this.units = units;
        return this;
    }

    public CreateCourseCommand setCourseCode(String courseCode) {
        this.courseCode = courseCode;
        return this;
    }
}
