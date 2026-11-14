# KBake E2E Tests: Quick Reference

Run the fast fixture checks:

```bash
mvn -pl kbake-e2e-tests -am test -Dtest=TestResourcesVerificationTest
```

Build the application image and run all Docker-based tests:

```bash
mvn -pl kbake-dist -am package -DskipTests
mvn -pl kbake-e2e-tests -am test
```

Override the shared fixture location with `-Dkbake.test.data.dir=/path/to/fixture`.

The container tests use `kbake/kbake:latest` and the Freemarker, Groovy, and Thymeleaf fixtures in `test-data/fixture`.
