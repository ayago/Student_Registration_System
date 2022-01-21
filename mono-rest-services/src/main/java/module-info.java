module com.acy.exam.metadata.srs.app {
    requires lombok;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.starter.webflux;
    requires spring.webflux;
    requires spring.web;
    requires spring.context;
    requires spring.core;
    requires reactor.core;
    requires srs.coursedomain;
    exports com.acy.exam.metadata.srs.app;
}