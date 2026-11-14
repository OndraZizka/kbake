# JBake End-to-End Tests

This Maven module contains Testcontainers-based end-to-end tests for JBake. The tests exercise several template engines,
CLI options, and rendered output using fixtures under `/test-data/fixture`.

## Requirements

- JDK 17 and Maven 3.9.9+
- Docker, for container-based tests

The fixture location is controlled by `jbake.test.data.dir` and defaults to `../test-data/fixture`.

## Build and run

From the repository root, build the test module and prerequisites:

```bash
mvn -pl jbake-e2e-tests -am test
```

Run the fast fixture checks without Docker:

```bash
mvn -pl jbake-e2e-tests -am test -Dtest=TestResourcesVerificationTest
```

Build the JBake Docker image from the root module and run the complete container tests:

```bash
mvn -pl jbake-dist -am package -DskipTests
mvn -pl jbake-e2e-tests -am test
```

To use a different fixture directory:

```bash
mvn -pl jbake-e2e-tests -am test -Djbake.test.data.dir=/path/to/fixture
```

## Test coverage

- `JBakeEndToEndTest.kt` exercises Freemarker, Groovy, and Thymeleaf templates, CLI options, and generated HTML.
- `TestResourcesVerificationTest.kt` checks fixture structure and required test files.

The test fixtures are shared from `/test-data/fixture` and include content, templates, configuration, and static assets.
