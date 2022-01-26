package com.acy.exam.metadata.srs.studentdomain;

import com.acy.exam.metadata.srs.commons.domain.RecordNotFoundException;
import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.command.UpdateStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.NewStudentEvent;
import com.acy.exam.metadata.srs.studentdomain.event.StudentDeactivatedEvent;
import com.acy.exam.metadata.srs.studentdomain.event.StudentUpdatedEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentDomainServiceTest {

    private static final RegisterStudentCommand VALID_REGISTER_COMMAND = new RegisterStudentCommand()
            .setFirstName("Baby")
            .setLastName("Maguire");

    private static final UpdateStudentCommand VALID_UPDATE_COMMAND = new UpdateStudentCommand()
        .setFirstName("Andre")
        .setLastName("Garfield");

    @Mock
    StudentDomainRepository repository;

    @Mock
    StudentEventPublisher eventPublisher;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void registerNewStudent(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        when(repository.generateNextStudentNumber()).thenReturn(Mono.just("200810498"));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.empty());
        if(hasPublisher){
            when(eventPublisher.publish(any(NewStudentEvent.class))).thenReturn(Mono.empty());
        }

        //when
        StepVerifier.create(domainService.registerNewStudent(VALID_REGISTER_COMMAND))
        //then
            .expectNext("200810498")
            .verifyComplete();

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810498")
            .lastName(VALID_REGISTER_COMMAND.getLastName())
            .firstName(VALID_REGISTER_COMMAND.getFirstName())
            .dateRegistered(LocalDate.now())
            .recordDeactivated(false)
            .build();

        verify(repository, times(1)).save(eq(expectedState));
        if(hasPublisher){
            var expectedEvent = NewStudentEvent.builder()
                .studentNumber("200810498")
                .lastName(VALID_REGISTER_COMMAND.getLastName())
                .firstName(VALID_REGISTER_COMMAND.getFirstName())
                .dateRegistered(LocalDate.now())
                .build();

            verify(eventPublisher, times(1)).publish(eq(expectedEvent));
            verifyNoMoreInteractions(repository, eventPublisher);
        } else {
            verifyNoInteractions(eventPublisher);
            verifyNoMoreInteractions(repository);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void idGenerationFailedOnRegisterNewStudent(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        when(repository.generateNextStudentNumber()).thenReturn(Mono.error(new RuntimeException()));

        //when
        StepVerifier.create(domainService.registerNewStudent(VALID_REGISTER_COMMAND))
            //then
            .expectError(RuntimeException.class)
            .verify();

        verify(repository, times(1)).generateNextStudentNumber();
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void statePersistenceFailedOnRegisterNewStudent(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        when(repository.generateNextStudentNumber()).thenReturn(Mono.just("200810498"));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.error(new RuntimeException()));

        //when
        StepVerifier.create(domainService.registerNewStudent(VALID_REGISTER_COMMAND))
            //then
            .expectError(RuntimeException.class)
            .verify();

        verify(repository, times(1)).generateNextStudentNumber();

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810498")
            .lastName(VALID_REGISTER_COMMAND.getLastName())
            .firstName(VALID_REGISTER_COMMAND.getFirstName())
            .dateRegistered(LocalDate.now())
            .recordDeactivated(false)
            .build();

        verify(repository, times(1)).save(eq(expectedState));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateStudentRecord(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName("Tausi")
            .firstName("Itim")
            .dateRegistered(LocalDate.of(2022, 1, 15))
            .recordDeactivated(false)
            .build();

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.just(studentState));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.empty());
        if(hasPublisher){
            when(eventPublisher.publish(any(StudentUpdatedEvent.class))).thenReturn(Mono.empty());
        }

        //when
        StepVerifier.create(domainService.updateStudentRecord("200810548", VALID_UPDATE_COMMAND))
        //then
            .expectNext("200810548")
            .verifyComplete();

        verify(repository, times(1)).getStudentDetails(eq("200810548"));

        StudentState expectedPersistedState = StudentState.builder()
            .studentNumber("200810548")
            .lastName(VALID_UPDATE_COMMAND.getLastName())
            .firstName(VALID_UPDATE_COMMAND.getFirstName())
            .dateRegistered(LocalDate.of(2022, 1, 15))
            .recordDeactivated(false)
            .dateUpdated(LocalDate.now())
            .build();

        verify(repository, times(1)).save(eq(expectedPersistedState));

        verifyNoMoreInteractions(repository);

        if(hasPublisher){
            StudentUpdatedEvent event = StudentUpdatedEvent.builder()
                .studentNumber("200810548")
                .lastName(VALID_UPDATE_COMMAND.getLastName())
                .firstName(VALID_UPDATE_COMMAND.getFirstName())
                .dateUpdated(LocalDate.now())
                .build();

            verify(eventPublisher, times(1)).publish(eq(event));
            verifyNoMoreInteractions(eventPublisher);
        } else {
            verifyNoInteractions(eventPublisher);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void updateStudentThatDoesNotExist(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.empty());

        //when
        StepVerifier.create(domainService.updateStudentRecord("200810498", VALID_UPDATE_COMMAND))
        //then
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(RecordNotFoundException.class, capturedException.getClass()),
                    () -> assertEquals(
                        "Cannot perform operation on non existing student record.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();

        verify(repository, times(1)).getStudentDetails(eq("200810498"));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void statePersistenceFailedOnUpdateStudent(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        StudentState givenState = StudentState.builder()
            .studentNumber("200810498")
            .lastName("Tausi")
            .firstName("Mingming")
            .dateRegistered(LocalDate.of(2021, 1, 14))
            .recordDeactivated(false)
            .build();

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.just(givenState));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.error(new RuntimeException()));

        //when
        StepVerifier.create(domainService.updateStudentRecord("200810498", VALID_UPDATE_COMMAND))
        //then
            .expectError(RuntimeException.class)
            .verify();

        verify(repository, times(1)).getStudentDetails(eq("200810498"));

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810498")
            .dateRegistered(LocalDate.of(2021, 1, 14))
            .lastName(VALID_UPDATE_COMMAND.getLastName())
            .firstName(VALID_UPDATE_COMMAND.getFirstName())
            .recordDeactivated(false)
            .dateUpdated(LocalDate.now())
            .build();

        verify(repository, times(1)).save(eq(expectedState));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deactivateStudentRecord(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        StudentState studentState = StudentState.builder()
            .studentNumber("200810548")
            .lastName("Tausi")
            .firstName("Itim")
            .dateRegistered(LocalDate.of(2022, 1, 15))
            .recordDeactivated(false)
            .build();

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.just(studentState));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.empty());
        if(hasPublisher){
            when(eventPublisher.publish(any(StudentDeactivatedEvent.class))).thenReturn(Mono.empty());
        }

        //when
        StepVerifier.create(domainService.deactivateStudentRecord("200810548"))
        //then
            .expectNext("200810548")
            .verifyComplete();

        verify(repository, times(1)).getStudentDetails(eq("200810548"));

        StudentState expectedPersistedState = StudentState.builder()
            .studentNumber("200810548")
            .lastName("Tausi")
            .firstName("Itim")
            .dateRegistered(LocalDate.of(2022, 1, 15))
            .dateUpdated(LocalDate.now())
            .recordDeactivated(true)
            .build();

        verify(repository, times(1)).save(eq(expectedPersistedState));

        verifyNoMoreInteractions(repository);

        if(hasPublisher){
            StudentDeactivatedEvent event = new StudentDeactivatedEvent("200810548", LocalDate.now());

            verify(eventPublisher, times(1)).publish(eq(event));
            verifyNoMoreInteractions(eventPublisher);
        } else {
            verifyNoInteractions(eventPublisher);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deactivateStudentThatDoesNotExist(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.empty());

        //when
        StepVerifier.create(domainService.deactivateStudentRecord("200810498"))
            //then
            .expectErrorSatisfies(capturedException -> {
                assertNotNull(capturedException);
                assertAll(
                    () -> assertEquals(RecordNotFoundException.class, capturedException.getClass()),
                    () -> assertEquals(
                        "Cannot perform operation on non existing student record.",
                        capturedException.getMessage()
                    )
                );
            })
            .verify();

        verify(repository, times(1)).getStudentDetails(eq("200810498"));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void statePersistenceFailedOnDeactivateStudent(boolean hasPublisher){
        //given
        StudentDomainService domainService = hasPublisher ?
            new StudentDomainService(repository, eventPublisher) : new StudentDomainService(repository);

        StudentState givenState = StudentState.builder()
            .studentNumber("200810498")
            .lastName("Tausi")
            .firstName("Mingming")
            .dateRegistered(LocalDate.of(2021, 1, 14))
            .recordDeactivated(false)
            .build();

        when(repository.getStudentDetails(anyString())).thenReturn(Mono.just(givenState));
        when(repository.save(any(StudentState.class))).thenReturn(Mono.error(new RuntimeException()));

        //when
        StepVerifier.create(domainService.deactivateStudentRecord("200810498"))
            //then
            .expectError(RuntimeException.class)
            .verify();

        verify(repository, times(1)).getStudentDetails(eq("200810498"));

        StudentState expectedState = StudentState.builder()
            .studentNumber("200810498")
            .dateRegistered(LocalDate.of(2021, 1, 14))
            .lastName("Tausi")
            .firstName("Mingming")
            .dateUpdated(LocalDate.now())
            .recordDeactivated(true)
            .build();

        verify(repository, times(1)).save(eq(expectedState));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }
}
