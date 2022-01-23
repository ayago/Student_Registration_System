package com.acy.exam.metadata.srs.app.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

import static java.util.Objects.isNull;

@Getter
@Table("courses")
@EqualsAndHashCode
public class CourseEntity implements Persistable<String> {

    @Id
    private String courseCode;
    private String name;
    private Integer units;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;

    public CourseEntity setCourseCode(String courseCode) {
        this.courseCode = courseCode;
        return this;
    }

    public CourseEntity setName(String name) {
        this.name = name;
        return this;
    }

    public CourseEntity setUnits(Integer units) {
        this.units = units;
        return this;
    }

    public CourseEntity setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public CourseEntity setDateUpdated(LocalDate dateUpdated) {
        this.dateUpdated = dateUpdated;
        return this;
    }

    @Override
    public String getId() {
        return courseCode;
    }

    @Override
    public boolean isNew() {
        return isNull(dateUpdated);
    }
}
