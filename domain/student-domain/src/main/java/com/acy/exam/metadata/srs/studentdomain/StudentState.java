package com.acy.exam.metadata.srs.studentdomain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class StudentState {

    @NotEmpty
    public final String studentNumber;

    @NotEmpty
    @Size(max = 100, message = "length must not be greater than {max}")
    public final String firstName;

    @NotEmpty
    @Size(max = 100, message = "length must not be greater than {max}")
    public final String lastName;

    @NotNull
    public final LocalDate dateRegistered;

    private final LocalDate dateUpdated;

    @NotNull
    public final Boolean recordDeactivated;

    public Optional<LocalDate> getDateUpdated(){
        return Optional.ofNullable(dateUpdated);
    }
}
