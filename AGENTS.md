# KBake Agent Instructions

## Release

- Use the Maven Release Plugin; do not hand-edit release versions or publish with a plain `mvn deploy`.
- Before preparing a release, ensure the working tree is clean, the release commits are pushed, and CI passes.
- Prepare the 5.0.0 release with:

  ```sh
  mvn release:prepare -Prelease -DreleaseVersion=5.0.0
  ```

  Review and confirm the next development version when prompted. The release plugin updates the reactor POMs and creates the SCM release commit/tag.
- Perform the release with:

  ```sh
  mvn release:perform -Prelease
  ```

- `release:perform` uploads through the Maven Central Publishing Plugin with `autoPublish=false`. Central validates the deployment, but it must then be reviewed and manually published in the Central Portal. Do not run `release:perform` or publish in the Portal unless the user has explicitly authorized those actions.
- The release profile keeps the Texy image build available for its integration tests but disables all Docker Hub push executions.
- Configure a Maven Central Portal token under server ID `central` in `~/.m2/settings.xml` and an available GPG signing key before release. Set `GPG_TTY` in interactive shells. Never expose or commit credentials or private keys.
- Follow `docs/releasing/RELEASING.md` for credential and signing setup details.
