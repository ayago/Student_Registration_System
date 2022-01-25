package com.acy.exam.metadata.srs.studentdomain;

import com.acy.exam.metadata.srs.commons.domain.Event;
import reactor.core.publisher.Mono;

public interface StudentEventPublisher {
    Mono<Void> publish(Event event);
}
