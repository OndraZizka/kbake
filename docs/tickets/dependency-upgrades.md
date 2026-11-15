# Ticket: Review and upgrade project dependencies

## Summary

Review the dependency and build-plugin updates reported by Maven Versions Plugin, upgrade them in small, testable batches, and address compatibility changes where required.

Version candidates below came from `mvn versions:display-dependency-updates` and `mvn versions:display-plugin-updates` across the Maven reactor (report run 2026-11-15). The plugin reports newer versions, not guaranteed drop-in replacements; its dependency-management results can include artifacts that are not used by a module.

## Work items

- [x] Upgrade Kotlin from `2.2.21` to `2.4.21` in the parent property shared by the Kotlin BOM and Kotlin Maven plugin.
- [ ] Upgrade remaining dependencies after review and compatibility testing.
- [ ] Upgrade Maven build plugins after review and build verification.

## Dependency candidates

### Runtime and application dependencies

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Logback | 1.5.21 | 1.6.7 | |
| Jackson family (BOM / managed artifacts) | 2.20 / 2.20.1 | 2.23 / 2.23.0 | Align the separately pinned Jackson annotations dependency with the BOM. |
| Caffeine | 3.2.3 | 3.3.1 | Managed transitive override. |
| Docker Java family | 3.7.0 | 3.7.1 | Used by E2E tests. |
| JFFI | 1.3.14 | 1.4.7 | Used for Java 17+ compatibility. |
| JNR POSIX | 3.1.21 | 3.2.8 | Used for Java 17+ compatibility. |
| Gson | 2.13.2 | 2.15.0 | |
| OrientDB | 3.2.47 | 3.2.58 | Exercise database operations and persisted-store compatibility. |
| Commons IO | 2.21.0 | 2.22.0 | |
| Commons Configuration | 2.13.0 | 2.15.1 | |
| Commons Lang | 3.20.0 | 3.22.0 | |
| Pug4j | 2.4.1 | 3.1.1 | Major update; review the Pug template adapter and render fixtures. |
| Pebble | 4.0.0 | 4.1.3 | Exercise template rendering. |
| Groovy family | 5.0.3 | 6.0.0 | Major update; test Groovy and markup template rendering. |
| AsciidoctorJ Diagram | 3.1.0 | 3.2.1 | Exercise diagram rendering. |
| Jetty server | 11.0.26 | 12.1.15 | Major update; review embedded server/handler APIs and run server tests. |
| JGit | 7.4.0.202509020913-r | 7.8.1.202611041320-r | Exercise Git-backed content handling. |
| FreeMarker | 2.3.34 | 2.3.35 | Exercise template rendering. |
| JCommander | 1.85 | 3.0 | Appears in dependency management; verify it is actually used before upgrading. |
| jsoup | 1.21.2 | 1.24.1 | |
| Neo4j | 5.26.0 | 2026.10.2 | Major version stream change; validate APIs and existing database upgrade behavior. |
| SLF4J | 2.0.17 | 2.0.20 | Keep bridges and bindings aligned. |
| Thymeleaf | 3.1.3.RELEASE | 3.1.7.RELEASE | Exercise template rendering. |
| SnakeYAML | 2.5 | 2.7 | |

### Test dependencies and project APIs

| Dependency | Current | Reported update | Notes |
|---|---:|---:|---|
| Kotest family | 6.0.7 | 6.2.7 | |
| MockK | 1.14.7 | 1.14.12 | |
| JUnit family | 6.0.1 | 6.1.3 | Keep the JUnit BOM and modules aligned. |
| JUnit 5 system extensions | 1.2.2 | 1.2.3 | |
| Testcontainers family | 2.0.2 | 2.0.5 | Run Docker-backed E2E and Texy tests. |
| Maven core / plugin API | 3.9.11 | 3.10.0 | Rebuild and run Maven-plugin integration tests. |
| Maven plugin annotations | 3.15.2 | 3.16.0 | |
| HttpClient 5 | 5.5.1 | 5.6.5 | Used by Texy service tests. |

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

## Migration and verification plan

1. Apply patch/minor updates in small batches, keeping BOM-managed families aligned.
2. Handle the Kotlin, Groovy, Pug4j, Jetty, Neo4j, and Maven API upgrades separately and inspect compile/test failures before making source changes.
3. Run `mvn verify`; include Docker-backed E2E and Texy integration tests, template-engine fixture tests, and Maven plugin integration tests.
4. For embedded databases, test both fresh stores and any supported existing-store upgrade path; back up persistent data before a production upgrade.

## Scope note

Only Kotlin is upgraded in the change that creates this ticket. The other versions above remain candidates for subsequent work, and should be rechecked with the Versions Plugin when each batch is scheduled.
