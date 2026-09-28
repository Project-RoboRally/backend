# backend

## Prerequisites

[Java Zulu-25](https://www.azul.com/downloads/?version=java-25-lts&package=jdk-fx#zulu)

[Gradle 9.x.x](https://gradle.org/releases/)

## Get started

Run the following command to create a runnable distribution of the application.
```bash
gradle clean installDist
```

If you specifically need the Spring Boot distribution, run:
```bash
gradle clean installBootDist
```

### Resolve dependencies
Run the following command to build the project, resolve its dependencies, run the tests, and create the application artifacts:
```bash
gradle clean build
```

### Format the base
The project is configured such that it will not compile and run if the code is
not correctly formatted. 

To automatically format the code, run:

```bash
gradle spotlessApply
```

## Development

### Run project
To run the project, use the following command

```bash
gradle run
```

### Run Unit tests
To run unit tests using JUnit 4 run

```bash
gradle test
```
