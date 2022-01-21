package com.acy.exam.metadata.srs.app.data.entity;

import lombok.Getter;
//import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Getter
public class CourseEntity {

//    @Id
    private String courseCode;
    private String name;
    private Integer units;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;
}
