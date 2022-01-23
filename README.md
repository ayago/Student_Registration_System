# Student Registration Application

This project contains different modules used to deploy a REST API service
to manage operations related to Student registration.

## Prerequisites

* Docker, preferably Docker desktop
* docker-compose is installed

## Execution

On root folder execute `sh srs-app-run.sh ${MYSQL_ROOT_PASSWORD}` where `MYSQL_ROOT_PASSWORD` is the 
password of root account of the containerized mysql (See packaging);

## Modules

The following modules are conformal to Java 9 Jigzaw modules. Each are also gradle sub-modules of the
project.

* domains-common - contains common domain error handling logic
* course-domain - enforces transaction rules and domain boundary for courses
* srs-app - a Spring Boot Reactive app that exposes different REST Endpoints that is equivalent for
each of Student Registration System (SRS) operations. It uses MySQL as it's underlying data storage and
  fully exploits reactive operations across layers. On startup, it executes liquibase based db history
  starting state to fully bootstrap the application. Dockerized.
  
## Packaging

* srs-app-packaging - contains the the docker-compose file that contains instructions to synchronize deployment
of mysql 8 container and srs-app
  
## Implementation Details

SRS uses Domain Driven Design concepts to clearly define the domain concepts of this application.
It maintains two modules for the two aggregates of this application: Course and Student. These modules
are framework agnostic and leaves infrastructure implementation to the application layer.