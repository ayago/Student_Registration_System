module com.acy.exam.metadata.srs.app {
    requires lombok;
    requires spring.context;
    requires spring.core;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.boot.starter.webflux;
    requires spring.webflux;
    requires reactor.core;
    requires spring.tx;
    requires spring.data.commons;
    requires spring.data.relational;
    requires srs.coursedomain;
    exports com.acy.exam.metadata.srs.app;
}