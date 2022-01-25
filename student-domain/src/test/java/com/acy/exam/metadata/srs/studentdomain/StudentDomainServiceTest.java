package com.acy.exam.metadata.srs.studentdomain;

import com.acy.exam.metadata.srs.studentdomain.command.RegisterStudentCommand;
import com.acy.exam.metadata.srs.studentdomain.event.NewStudentEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
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
            .build();

        verify(repository, times(1)).save(eq(expectedState));
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(eventPublisher);
    }
}
