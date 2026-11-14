# Upstream JBake changes relevant to KBake

This summary compares KBake's February 10, 2026 snapshot with the checked-out JBake repository at `~/uw/jbake`. It covers committed history only; uncommitted files in that separate checkout were not included.

## Since February 10, 2026

- **May 1, 2026 — `4cb4c05e` — “Some deps upgrades”**: updated Kotlin to 2.3.21, Groovy to 5.0.5, Jackson to 2.21.3, JUnit 5 to 6.0.3, and Kotest to 6.1.11. The Jackson annotations override moved to 2.21 as well. This commit changes the Maven dependency versions; it does not contain application-code changes.

## Already present in the KBake snapshot

- **December 15, 2025 — timezone handling for date conversion** appears on JBake `master` as `484ec83a`. KBake already contains the corresponding change as `92bad11b`; it is not additional work to port.
- The preceding late-2025 refactors and date/time fixes (including the Freemarker template-model cleanup and Groovy `OffsetDateTime` handling) are also represented in the KBake history.

## Release context

The inspected JBake checkout is on the `kotlin-upgraded` branch at `4cb4c05e`, one commit ahead of its configured remote-tracking branch. Its listed release tags are legacy `v2.x` tags; this history does not show a later tagged 5.0.0 release.
