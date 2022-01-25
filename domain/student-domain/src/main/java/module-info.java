module srs.studentdomain {
    requires transitive srs.commons.domain;
    requires lombok;
    requires jakarta.validation;
    requires reactor.core;
    exports com.acy.exam.metadata.srs.studentdomain;
    exports com.acy.exam.metadata.srs.studentdomain.command;
    exports com.acy.exam.metadata.srs.studentdomain.event;
    exports com.acy.exam.metadata.srs.studentdomain.exception;
}