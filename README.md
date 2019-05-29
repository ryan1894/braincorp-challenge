# Brain Corp Challenge

## Abstract

The purpose of this application is to expose both `/etc/passwd` and `/etc/group/` files via a simple HTTP service.

## Design

## Considerations

### userId / groupId

Although it is possible for both to be represented by an unsigned 32 bit integer, for simplicity, I have chosen to represent
both with signed java integers. For the purposes of this challenge, it should suffice.

### Exception Handling

I have decided to simply filter out rows that aren't colon separated into 4 fields (in group) and 7 fields (in passwd)

For the purposes of this exercise, this seems sufficient. Validation of both files could be performed if this task were to be extended.

## Execution

### Configuration

`passwd` and `group` file locations are specified in `src/main/resources/application.yml`. This can be overridden when
packaged in a jar in various methods as described [https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html](here),
including overriding environment variable `spring.config.location`, or e.g. setting environment variable `FILES_LOCATION_PASSWD` and `FILES_LOCATION_GROUP`.

### Running locally

`./gradlew clean bootRun`

### Testing

`./gradlew clean test`

### Packaging

`./gradlew clean bootJar` will produce a fat JAR with all dependencies included at the `build/libs/braincorp-1.0-SNAPSHOT.jar`
