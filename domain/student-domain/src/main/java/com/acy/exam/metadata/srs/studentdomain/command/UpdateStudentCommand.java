package com.acy.exam.metadata.srs.studentdomain.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UpdateStudentCommand {

    @NotEmpty
    @Size(max = 100, message = "length must not be greater than {max}")
    private String firstName;

    @NotEmpty
    @Size(max = 100, message = "length must not be greater than {max}")
    private String lastName;

    public UpdateStudentCommand setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UpdateStudentCommand setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
}
