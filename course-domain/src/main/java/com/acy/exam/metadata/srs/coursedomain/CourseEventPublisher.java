package com.acy.exam.metadata.srs.coursedomain;

import com.acy.exam.metadata.srs.commons.domain.Event;
import reactor.core.publisher.Mono;

public interface CourseEventPublisher {
    Mono<Void> publish(Event event);
}
