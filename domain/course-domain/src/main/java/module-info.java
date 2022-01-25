module srs.coursedomain {
    requires transitive srs.commons.domain;
    requires lombok;
    requires jakarta.validation;
    requires reactor.core;
    exports com.acy.exam.metadata.srs.coursedomain;
    exports com.acy.exam.metadata.srs.coursedomain.command;
    exports com.acy.exam.metadata.srs.coursedomain.event;
}