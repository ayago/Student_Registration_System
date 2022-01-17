package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.Event;

public interface CourseEventPublisher {
    void publish(Event event);
}
