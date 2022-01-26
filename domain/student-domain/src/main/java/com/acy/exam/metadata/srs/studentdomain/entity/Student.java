package com.acy.exam.metadata.srs.studentdomain.entity;

import com.acy.exam.metadata.srs.commons.domain.Aggregate;
import com.acy.exam.metadata.srs.commons.domain.CommandValidator;
import com.acy.exam.metadata.srs.commons.domain.Event;
import com.acy.exam.metadata.srs.commons.domain.RecordDeactivatedException;
import com.acy.exam.metadata.srs.studentdomain.StudentState;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.command.UpdateStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.NewStudentEvent;
import com.acy.exam.metadata.srs.studentdomain.event.StudentDeactivatedEvent;
import com.acy.exam.metadata.srs.studentdomain.event.StudentUpdatedEvent;

import java.time.LocalDate;
import java.util.Optional;

public class Student implements Aggregate {

    private final StudentNumber studentNumber;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateRegistered;
    private final LocalDate dateUpdated;
    private final boolean recordDeactivated;

    private final Event lastUpdate;

    Student(RegisterStudentCommand command, StudentNumber studentNumber, boolean generateEvent){

        this.firstName = command.getFirstName();
        this.lastName = command.getLastName();
        this.dateRegistered = LocalDate.now();
        this.studentNumber = studentNumber;
        this.dateUpdated = null;
        this.recordDeactivated = false;

        lastUpdate = !generateEvent ? null: NewStudentEvent.builder()
            .studentNumber(studentNumber.value)
            .firstName(firstName)
            .lastName(lastName)
            .dateRegistered(dateRegistered)
            .build();
    }

    public Student(StudentState studentState){
        CommandValidator.validateState(
            studentState, "Cannot instantiate Student Aggregate due to state validation errors.");

        this.firstName = studentState.firstName;
        this.lastName = studentState.lastName;
        this.dateRegistered = studentState.dateRegistered;
        this.studentNumber = new StudentNumber(studentState.studentNumber);
        this.dateUpdated = studentState.getDateUpdated().orElse(null);
        this.recordDeactivated = studentState.recordDeactivated;

        this.lastUpdate = null;
    }

    //special constructor for deactivation
    private Student(
        StudentNumber studentNumber,
        String firstName,
        String lastName,
        LocalDate dateRegistered,
        boolean generateEvent
    ){
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateRegistered = dateRegistered;
        this.dateUpdated = LocalDate.now();
        this.recordDeactivated = true;

        lastUpdate = generateEvent ? new StudentDeactivatedEvent(studentNumber.value, dateUpdated) : null;
    }

    private Student(
        UpdateStudentCommand command,
        StudentNumber studentNumber,
        LocalDate dateRegistered,
        boolean generateEvent
    ){
        this.firstName = command.getFirstName();
        this.lastName = command.getLastName();
        this.dateRegistered = dateRegistered;
        this.studentNumber = studentNumber;
        this.dateUpdated = LocalDate.now();

        lastUpdate = !generateEvent ? null: StudentUpdatedEvent.builder()
            .studentNumber(studentNumber.value)
            .firstName(firstName)
            .lastName(lastName)
            .dateUpdated(dateUpdated)
            .build();

        this.recordDeactivated = false;
    }

    public Student update(UpdateStudentCommand command, boolean generateEvent){
        assertIsActive();
        CommandValidator.validateCommand(command, "Cannot update Student due to validation errors.");

        return new Student(command, studentNumber, dateRegistered, generateEvent);
    }

    public Student deactivate(boolean generateEvent){
        assertIsActive();
        return new Student(studentNumber, firstName, lastName, dateRegistered, generateEvent);
    }

    private void assertIsActive() {
        if (recordDeactivated) {
            throw new RecordDeactivatedException("Cannot perform operation on non existing student record.");
        }
    }

    public StudentState toState(){
        return StudentState.builder()
            .firstName(firstName)
            .lastName(lastName)
            .studentNumber(studentNumber.value)
            .dateRegistered(dateRegistered)
            .dateUpdated(dateUpdated)
            .recordDeactivated(recordDeactivated)
            .build();
    }

    @Override
    public Optional<Event> getLastUpdate() {
        return Optional.ofNullable(lastUpdate);
    }
}
