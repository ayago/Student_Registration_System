package com.acy.exam.metadata.srs.coursedomain.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class CreateCourseCommand {

    @Getter
    @NotNull
    @Size(max = 50, message = "length must not be greater than {max}")
    private String name;

    @Getter
    @NotNull
    private Integer units;

    @Getter
    @NotNull
    @Pattern(
        regexp = "^[A-Z]{3}[0-9]{3}$", message = "must be prefixed with 3 capital letters followed by three integers")
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
