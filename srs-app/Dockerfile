FROM bellsoft/liberica-openjdk-alpine-musl:11.0.14-9
VOLUME /tmp
ARG DEPENDENCY=build/dependencies
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.acy.exam.metadata.srs.app.StudentRegistrationSystemApplication"]