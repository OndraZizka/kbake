# Ticket: Review and upgrade project dependencies

## Summary

Review the updates reported by Maven Versions Plugin, upgrade them in small testable batches, and address compatibility changes where required. Candidate versions were reported by `mvn versions:display-dependency-updates` and `mvn versions:display-plugin-updates` across the reactor on 2026-11-15. These reports can include managed artifacts that are not used by a module; newer versions are candidates, not guaranteed drop-in replacements.

## Work items

- [x] Tag the `5.0.0-rc1` base before changes.
- [x] Set the reactor version to `5.0.0-rc2-SNAPSHOT` for ongoing development/release-plugin preparation.
- [x] Upgrade Kotlin from `2.2.21` to `2.4.21` (shared Kotlin BOM and Maven plugin property).
- [x] Upgrade the requested dependencies and run the reactor tests after each listed group.
- [ ] Review and upgrade the remaining build plugins.

## Dependency candidates

| Group | Dependency | Current | Reported update | Migration notes |
|---|---|---:|---:|---|
| Logging/Jackson | Logback | 1.5.21 | 1.6.7 | |
| Logging/Jackson | Jackson BOM/core/databind | 2.20/2.20.1 | 2.23/2.23.0 | Align the separately pinned annotations version. |
| Logging/Jackson | SLF4J | 2.0.17 | 2.0.20 | Align bridges and test bindings. |
| Databases/libraries | Caffeine | 3.2.3 | 3.3.1 | Managed transitive override. |
| Databases/libraries | Gson | 2.13.2 | 2.15.0 | |
| Databases/libraries | OrientDB | 3.2.47 | 3.2.58 | Exercise database operations and persisted-store compatibility. |
| Databases/libraries | Neo4j | 5.26.0 | 2026.10.2 | Major version-stream change. Its Java 21-only service annotation processor required disabling processor discovery in `kbake-maven-plugin` (no JSR-269 processors are used there) to retain Java 17 compilation. |
| Templates | Thymeleaf | 3.1.3.RELEASE | 3.1.7.RELEASE | Exercise template rendering. |
| Templates | FreeMarker | 2.3.34 | 2.3.35 | Exercise template rendering. |
| Other runtime | Commons IO | 2.21.0 | 2.22.0 | |
| Other runtime | Commons Configuration | 2.13.0 | 2.15.1 | |
| Other runtime | Commons Lang | 3.20.0 | 3.22.0 | |
| Other runtime | jsoup | 1.21.2 | 1.24.1 | |
| Other runtime | SnakeYAML | 2.5 | 2.7 | |
| Other runtime | JGit | 7.4.0.202509020913-r | 7.8.1.202611041320-r | Exercise Git-backed content handling. |
| Other runtime | AsciidoctorJ Diagram | 3.1.0 | 3.2.1 | Exercise diagram rendering. |
| Testing | Kotest | 6.0.7 | 6.2.7 | |
| Testing | MockK | 1.14.7 | 1.14.12 | |
| Testing | JUnit | 6.0.1 | 6.1.3 | Keep BOM and JUnit modules aligned. |
| Testing | Testcontainers | 2.0.2 | 2.0.5 | Run Docker-backed E2E and Texy tests. |
| Testing | Docker Java | 3.7.0 | 3.7.1 | Used by E2E tests. |
| Testing | JUnit system extensions | 1.2.2 | 1.2.3 | |
| Testing | HttpClient 5 | 5.5.1 | 5.6.5 | Used by Texy service tests. |

Other candidates from the report include Pug4j `2.4.1 → 3.1.1`, Pebble `4.0.0 → 4.1.3`, Groovy `5.0.3 → 6.0.0`, Jetty `11.0.26 → 12.1.15`, JFFI `1.3.14 → 1.4.7`, JNR POSIX `3.1.21 → 3.2.8`, and Maven core/API `3.9.11 → 3.10.0`. Review these separately because they may require compatibility work. JCommander `1.85 → 3.0` was also reported in dependency management; verify actual use before upgrading.

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
- [ ] Re-run the KBake Docker E2E tests when the local `kbake/kbake:5.0.0-rc2-SNAPSHOT` image is available. The attempt against the old `5.0.0-rc2` tag failed because that local image was missing; Testcontainers itself connected successfully.
- For embedded databases, test both fresh stores and any supported existing-store upgrade path; back up persistent data before a production upgrade.

The final `mvn verify` passed and included the distribution Failsafe tests. The 12 Texy service tests passed when explicitly invoked after packaging and copying that module's test resources. The distribution build still reports duplicate classes/resources, including Lucene classes from Neo4j; review those warnings separately.
