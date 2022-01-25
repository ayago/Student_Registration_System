package com.acy.exam.metadata.srs.coursedomain.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class CreateCourseCommand {

    @NotEmpty
    @Size(max = 50, message = "length must not be greater than {max}")
    private String name;

    @NotNull
    @Min(value = 0L, message = "must not be less than {value}")
    private Integer units;

    @NotEmpty
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
