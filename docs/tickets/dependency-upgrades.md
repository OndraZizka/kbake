# Ticket: Review and upgrade project dependencies

## Summary

Review the dependency and build-plugin updates reported by Maven Versions Plugin, upgrade them in small, testable batches, and address compatibility changes where required.

Version candidates below came from `mvn versions:display-dependency-updates` and `mvn versions:display-plugin-updates` across the Maven reactor (report run 2026-11-15). The plugin reports newer versions, not guaranteed drop-in replacements; its dependency-management results can include artifacts that are not used by a module.

## Work items

- [x] Tag the `5.0.0-rc1` base before changes.
- [x] Set the reactor version to `5.0.0-rc2-SNAPSHOT` for ongoing development/release-plugin preparation.
- [x] Upgrade Kotlin from `2.2.21` to `2.4.21` (shared Kotlin BOM and Maven plugin property).
- [x] Upgrade the requested dependencies and run the reactor tests after each listed group.
- [ ] Review and upgrade the remaining build plugins.

## Dependency candidates

### Logging and Jackson

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Logback | 1.5.21 | 1.6.7 | |
| Jackson BOM/core/databind | 2.20/2.20.1 | 2.23/2.23.0 | Align the separately pinned annotations version. |
| SLF4J | 2.0.17 | 2.0.20 | Align bridges and test bindings. |

### Libraries and databases

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Caffeine | 3.2.3 | 3.3.1 | Managed transitive override. |
| Gson | 2.13.2 | 2.15.0 | |
| OrientDB | 3.2.47 | 3.2.58 | Exercise database operations and persisted-store compatibility. |
| Neo4j | 5.26.0 | 2026.10.2 | Its Java 21-only annotation processor required disabling processor discovery in `kbake-maven-plugin` (which uses no JSR-269 processors) to keep Java 17 compilation. |

### Templates

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Thymeleaf | 3.1.3.RELEASE | 3.1.7.RELEASE | Exercise template rendering. |
| FreeMarker | 2.3.34 | 2.3.35 | Exercise template rendering. |

### Other runtime dependencies

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Commons IO | 2.21.0 | 2.22.0 | |
| Commons Configuration | 2.13.0 | 2.15.1 | |
| Commons Lang | 3.20.0 | 3.22.0 | |
| jsoup | 1.21.2 | 1.24.1 | |
| SnakeYAML | 2.5 | 2.7 | |
| JGit | 7.4.0.202509020913-r | 7.8.1.202611041320-r | Exercise Git-backed content handling. |
| AsciidoctorJ Diagram | 3.1.0 | 3.2.1 | Exercise diagram rendering. |

### Test dependencies

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Kotest | 6.0.7 | 6.2.7 | |
| MockK | 1.14.7 | 1.14.12 | |
| JUnit | 6.0.1 | 6.1.3 | Keep BOM and JUnit modules aligned. |
| Testcontainers | 2.0.2 | 2.0.5 | Run Docker-backed E2E and Texy tests. |
| Docker Java | 3.7.0 | 3.7.1 | Used by E2E tests. |
| JUnit system extensions | 1.2.2 | 1.2.3 | |
| HttpClient 5 | 5.5.1 | 5.6.5 | Used by Texy service tests. |

Other candidates to handle separately: Pug4j `2.4.1 → 3.1.1`, Pebble `4.0.0 → 4.1.3`, Groovy `5.0.3 → 6.0.0`, Jetty `11.0.26 → 12.1.15`, JFFI `1.3.14 → 1.4.7`, JNR POSIX `3.1.21 → 3.2.8`, Maven core/API `3.9.11 → 3.10.0`, and Maven plugin annotations `3.15.2 → 3.16.0`. JCommander `1.85 → 3.0` was reported in dependency management; verify actual use before upgrading.

## Build plugin candidates

| Plugin | Current | Reported update |
|---|---:|---:|
| Jib | 3.5.1 | 3.5.2 |
| Maven Compiler | 3.14.1 | 3.16.0 |
| Maven Deploy | 3.1.4 | 3.2.0 |
| Maven Enforcer | 3.6.2 | 3.7.0 |
| Maven Failsafe | 3.5.4 | 3.6.1 |
| Maven Install | 3.1.4 | 3.2.0 |
| Maven Jar | 3.5.0 | 3.5.1 |
| Maven Plugin | 3.15.2 | 3.16.0 |
| Maven Release | 3.3.0 | 3.3.1 |
| Maven Resources | 3.4.0 | 3.5.0 |
| Maven Shade | 3.6.1 | 3.6.2 |
| Maven Surefire | 3.5.4 | 3.6.1 |
| Maven Toolchains | 3.2.0 | 3.3.0 |
| Build Helper | 3.6.1 | 3.8.0 |
| Flatten | 1.7.3 | 1.9.0 |
| Versions Maven Plugin | 2.20.1 | 2.25.2 |
| Maven Invoker | 3.9.1 | 3.10.1 |
| Modernizer | 3.2.0 | 3.5.0 |
| JaCoCo | 0.8.14 | 0.8.16 |
| Dokka | 2.1.0 | 2.3.0 |
| Central Publishing | 0.9.0 | 0.12.0 |

## Verification

- [x] Run `mvn test` after each requested dependency group.
- [x] Run final `mvn verify` after the HttpClient 5 and JUnit system extensions group.
- [ ] Re-run the KBake Docker E2E tests when the local `kbake/kbake:5.0.0-rc2-SNAPSHOT` image is available. Testcontainers connected successfully, but the earlier run tried the non-snapshot `5.0.0-rc2` image, which was missing; the snapshot image is not present in this worktree's Docker daemon yet.
- For embedded databases, test both fresh stores and any supported existing-store upgrade path; back up persistent data before a production upgrade.

The final `mvn verify` passed and included the distribution Failsafe tests; a subsequent `mvn test` also passed with the `5.0.0-rc2-SNAPSHOT` reactor version. The 12 Texy service tests passed when explicitly invoked after packaging and copying that module's test resources. The distribution build still reports duplicate classes/resources, including Lucene classes from Neo4j; review those warnings separately.
