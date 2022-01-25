package com.acy.exam.metadata.srs.commons.domain;

import java.util.Optional;

public interface Aggregate {
    Optional<Event> getLastUpdate();
}
