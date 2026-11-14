# JBake E2E Tests: Quick Reference

Run the fast fixture checks:

```bash
mvn -pl jbake-e2e-tests -am test -Dtest=TestResourcesVerificationTest
```

Build the application image and run all Docker-based tests:

```bash
mvn -pl jbake-dist -am package -DskipTests
mvn -pl jbake-e2e-tests -am test
```

Override the shared fixture location with `-Djbake.test.data.dir=/path/to/fixture`.

The container tests use `jbake/jbake:latest` and the Freemarker, Groovy, and Thymeleaf fixtures in `test-data/fixture`.
