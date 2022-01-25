package com.acy.exam.metadata.srs.coursedomain.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class UpdateCourseCommand {

    @NotEmpty
    @Size(max = 50, message = "length must not be greater than {max}")
    private String name;

    @NotNull
    @Min(value = 0L, message = "must not be less than {value}")
    private Integer units;

    public UpdateCourseCommand setName(String name) {
        this.name = name;
        return this;
    }

    public UpdateCourseCommand setUnits(Integer units) {
        this.units = units;
        return this;
    }
}
