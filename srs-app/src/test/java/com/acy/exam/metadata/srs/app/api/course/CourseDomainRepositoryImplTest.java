package com.acy.exam.metadata.srs.app.api.course;

import com.acy.exam.metadata.srs.app.data.entity.CourseEntity;
import com.acy.exam.metadata.srs.app.data.repository.CourseEntityRepository;
import com.acy.exam.metadata.srs.coursedomain.CourseState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseDomainRepositoryImplTest {

    @InjectMocks
    CourseDomainRepositoryImpl courseDomainRepository;

    @Mock
    CourseEntityRepository courseEntityRepository;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void isNotYetUsed(boolean expected){
        when(courseEntityRepository.existsByCourseCodeOrName(anyString(), anyString()))
            .thenReturn(Mono.just(!expected));

        StepVerifier.create(courseDomainRepository.isNotYetUsed("TEST12", "Subject Alpha"))
            .expectNext(expected)
            .verifyComplete();

        verify(courseEntityRepository, times(1))
            .existsByCourseCodeOrName(eq("TEST12"), eq("Subject Alpha"));
        verifyNoMoreInteractions(courseEntityRepository);
    }

    @Test
    public void isNotYetUsedError(){
        when(courseEntityRepository.existsByCourseCodeOrName(anyString(), anyString()))
            .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(courseDomainRepository.isNotYetUsed("TEST12", "Subject Alpha"))
            .expectError(RuntimeException.class)
            .verify();

        verify(courseEntityRepository, times(1))
            .existsByCourseCodeOrName(eq("TEST12"), eq("Subject Alpha"));
        verifyNoMoreInteractions(courseEntityRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void save(boolean isAnUpdate){
        LocalDate presentDay = LocalDate.of(2020, 1, 12);

        CourseEntity expectedEntity = new CourseEntity()
            .setCourseCode("JAN021")
            .setName("Some Course")
            .setUnits(5)
            .setDateCreated(presentDay)
            .setDateUpdated(isAnUpdate ? presentDay : null);

        CourseState givenCourseState = CourseState.builder()
            .courseCode("JAN021")
            .name("Some Course")
            .units(5)
            .dateCreated(presentDay)
            .dateUpdated(isAnUpdate ? presentDay : null)
            .build();

        when(courseEntityRepository.save(any(CourseEntity.class))).thenReturn(Mono.just(expectedEntity));

        StepVerifier.create(courseDomainRepository.save(givenCourseState))
            .expectComplete()
            .verify();

        verify(courseEntityRepository, times(1)).save(eq(expectedEntity));
        verifyNoMoreInteractions(courseEntityRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void saveError(boolean isAnUpdate){
        LocalDate presentDay = LocalDate.of(2020, 1, 12);

        CourseEntity expectedEntity = new CourseEntity()
            .setCourseCode("JAN021")
            .setName("Some Course")
            .setUnits(5)
            .setDateCreated(presentDay)
            .setDateUpdated(isAnUpdate ? presentDay : null);

        CourseState givenCourseState = CourseState.builder()
            .courseCode("JAN021")
            .name("Some Course")
            .units(5)
            .dateCreated(presentDay)
            .dateUpdated(isAnUpdate ? presentDay : null)
            .build();

        when(courseEntityRepository.save(any(CourseEntity.class))).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(courseDomainRepository.save(givenCourseState))
            .expectError(RuntimeException.class)
            .verify();

        verify(courseEntityRepository, times(1)).save(eq(expectedEntity));
        verifyNoMoreInteractions(courseEntityRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void nameIsNotYetUsedByOthers(boolean expected){
        when(courseEntityRepository.existsByNameAndCourseCodeNot(anyString(), anyString()))
            .thenReturn(Mono.just(!expected));

        StepVerifier.create(courseDomainRepository.nameIsNotYetUsedByOthers("TEST12", "Subject Alpha"))
            .expectNext(expected)
            .verifyComplete();

        verify(courseEntityRepository, times(1))
            .existsByNameAndCourseCodeNot(eq("Subject Alpha"), eq("TEST12"));
        verifyNoMoreInteractions(courseEntityRepository);
    }

    @Test
    public void getCurrentStateOfCourse(){
        CourseEntity courseEntity = new CourseEntity()
            .setName("Sample Subject")
            .setCourseCode("SHS123")
            .setUnits(1)
            .setDateCreated(LocalDate.of(2022, 1, 24))
            .setDateUpdated(LocalDate.of(2022, 1, 25));

        when(courseEntityRepository.findById(anyString())).thenReturn(Mono.just(courseEntity));

        CourseState expected = CourseState.builder()
            .name("Sample Subject")
            .courseCode("SHS123")
            .units(1)
            .dateCreated(LocalDate.of(2022, 1, 24))
            .dateUpdated(LocalDate.of(2022, 1, 25))
            .build();

        StepVerifier.create(courseDomainRepository.getCurrentStateOfCourse("SHS123"))
            .expectNext(expected)
            .verifyComplete();

        verify(courseEntityRepository, times(1)).findById(eq("SHS123"));
        verifyNoMoreInteractions(courseEntityRepository);
    }
}
