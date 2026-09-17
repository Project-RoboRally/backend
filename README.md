# backend

## Prerequirements

[Java Zulu-25](https://www.azul.com/downloads/?version=java-25-lts&package=jdk-fx#zulu)

[Gradle 9.x.x](https://gradle.org/releases/)

## Get started

run the following command to install all deppendencies
```bash
gradle clean install
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

### Format the base

The project is configured such that it will not compile and run if the code is
not correctly formatted.

```bash
gralde spotlessApply
```
