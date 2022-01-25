package com.acy.exam.metadata.srs.studentdomain.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class StudentNumber {
    public final String value;

    StudentNumber(String value) {
        this.value = value;
    }
}
