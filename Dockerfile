#
# Build stage
#
# Wir wechseln von jdk25 auf jdk17, damit es zu deiner build.gradle passt
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

#
# Package stage
#
# Auch hier nutzen wir die stabile Java 17 Version
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /home/gradle/src/build/libs/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]