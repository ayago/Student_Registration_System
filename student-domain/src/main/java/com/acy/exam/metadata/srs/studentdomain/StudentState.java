package com.acy.exam.metadata.srs.studentdomain;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class StudentState {

    public final String studentNumber;
    public final String firstName;
    public final String lastName;
    public final LocalDate dateRegistered;

    private final LocalDate dateUpdated;

    public Optional<LocalDate> getDateUpdated(){
        return Optional.ofNullable(dateUpdated);
    }
}
