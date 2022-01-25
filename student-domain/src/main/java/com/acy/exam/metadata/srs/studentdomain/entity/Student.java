package com.acy.exam.metadata.srs.studentdomain.entity;

import com.acy.exam.metadata.srs.commons.domain.Aggregate;
import com.acy.exam.metadata.srs.commons.domain.Event;
import com.acy.exam.metadata.srs.studentdomain.StudentState;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.NewStudentEvent;

import java.time.LocalDate;
import java.util.Optional;

public class Student implements Aggregate {

    private final StudentNumber studentNumber;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateRegistered;
    private final LocalDate dateUpdated;

    private final Event lastUpdate;

    Student(RegisterStudentCommand command, StudentNumber studentNumber, boolean generateEvent){

        this.firstName = command.getFirstName();
        this.lastName = command.getLastName();
        this.dateRegistered = LocalDate.now();
        this.studentNumber = studentNumber;
        this.dateUpdated = null;

        lastUpdate = !generateEvent ? null: NewStudentEvent.builder()
            .studentNumber(studentNumber.value)
            .firstName(firstName)
            .lastName(lastName)
            .dateRegistered(dateRegistered)
            .build();
    }

    public StudentState toState(){
        return StudentState.builder()
            .firstName(firstName)
            .lastName(lastName)
            .studentNumber(studentNumber.value)
            .dateRegistered(dateRegistered)
            .dateUpdated(dateUpdated)
            .build();
    }

    @Override
    public Optional<Event> getLastUpdate() {
        return Optional.ofNullable(lastUpdate);
    }
}
