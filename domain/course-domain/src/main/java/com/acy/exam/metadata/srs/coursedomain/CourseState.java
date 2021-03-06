package com.acy.exam.metadata.srs.coursedomain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class CourseState {

    @NotEmpty
    @Pattern(
        regexp = "^[A-Z]{3}[0-9]{3}$", message = "must be prefixed with 3 capital letters followed by three integers")
    public final String courseCode;

    @NotEmpty
    @Size(max = 50, message = "length must not be greater than {max}")
    public final String name;

    @NotNull
    @Min(value = 0L, message = "must not be less than {value}")
    public final Integer units;

    @NotNull
    public final LocalDate dateCreated;
    private final LocalDate dateUpdated;

    public Optional<LocalDate> getDateUpdated() {
        return Optional.ofNullable(dateUpdated);
    }
}
