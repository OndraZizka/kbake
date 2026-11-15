# Releasing KBake to Maven Central

This document describes how to release KBake artifacts to Maven Central under the `ch.zizka.kbake` groupId.

Published artifacts use the `ch.zizka.kbake` namespace.


## Quick Start

Run the Maven Release Plugin only after the working tree is clean, release commits are pushed, and CI passes. The release uploads to Central for validation; publication remains a separate manual action in the Portal.

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
mvn release:prepare -Prelease -DreleaseVersion=5.0.0
mvn release:perform -Prelease
```

## Prerequisites

### 1. Java 17

KBake requires Java 17 for building:

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Verify it's Java 17
```

### 2. Maven Central Portal Account

- Portal: https://central.sonatype.com/
- Sign in and verify the account is authorized to publish to `ch.zizka.kbake`.

### 3. Maven Settings (~/.m2/settings.xml)

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>

</settings>
```

To get credentials:
1. Go to https://central.sonatype.com/account
2. Click "Generate User Token"
3. Copy the credentials to settings.xml

### 4. GPG Key

Your GPG key must be uploaded to public keyservers.

#### To generate a new key, if needed:

```bash
gpg --full-generate-key
# Key type: RSA and RSA (default)
# Key size: 4096
# Expiration: 0 (never expires)
# Name: Ondrej Zizka
# Email: ...
# Passphrase: (optional, can be empty)
```

#### Upload public key to keyservers:

```bash
# Get your key ID
gpg --list-secret-keys --keyid-format=long

# Upload (replace KEY_ID with actual ID)
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

## Release Procedure

The Maven Release Plugin updates versions and creates the SCM release commit and tag. Review and confirm the next development version when prompted.

```bash
export JAVA_HOME=$HOME/.jdks/temurin-17.0.17
export PATH=$JAVA_HOME/bin:$PATH
export GPG_TTY=$(tty)
mvn release:prepare -Prelease -DreleaseVersion=5.0.0
mvn release:perform -Prelease
```

The Central Publishing Maven Plugin uploads the release and waits for validation. With `autoPublish=false`, it does not publish automatically. Review the validated deployment at https://central.sonatype.com/publishing and click **Publish** when publication is authorized.

The release profile excludes the Texy module from the Central bundle and suppresses its Docker Hub push goals while still allowing the image build needed by its integration tests.

## After Deployment

### Timeline

- **Upload**: Immediate
- **Validation**: 1-5 minutes
- **Maven Central**: 15-30 minutes
- **Search Index**: 2-24 hours

### Verification

1. **Central Portal** (immediate):
   https://central.sonatype.com/publishing

2. **Maven Central Repository** (15-30 min):
   https://repo1.maven.org/maven2/ch/zizka/kbake/

3. **Maven Search** (few hours):
   https://search.maven.org/search?q=g:ch.zizka.kbake

## Modules Published

- `ch.zizka.kbake:kbake-base` - Parent POM
- `ch.zizka.kbake:kbake-core` - Core library (with Kotlin KDoc documentation)
- `ch.zizka.kbake:kbake-dist` - Distribution JAR
- `ch.zizka.kbake:kbake-maven-plugin` - Maven plugin

`kbake-e2e-tests` and `kbake-texy-service` are excluded from the Central bundle using the Central plugin's `skipPublishing` option.

## Project Configuration

### GPG Signing

All artifacts are signed with GPG in the release profile:

- Uses `gpg-agent` and the system Pinentry program; no passphrase is stored in Maven configuration.
- Set `GPG_TTY` in the interactive shell before running Maven so Pinentry can use the terminal.
- The public signing key must be available from a keyserver supported by Central.
- Select the intended signing key explicitly if multiple keys are available.
- Do not store a GPG passphrase in the POM or plaintext Maven settings; use the GPG agent interactively or a protected environment variable in automation.

## Troubleshooting

### Error: GPG signing failed: No secret key

The selected GPG secret key is unavailable to the signing process.

**Solution**:
Check `gpg --list-secret-keys` and import or create a key if necessary.

### Error: GPG signing failed: Inappropriate ioctl for device

GPG_TTY is not set. Use `export GPG_TTY=$(tty)`.

### Error: Invalid signature - Could not find public key

Your GPG public key is not on keyservers.

**Solution**:
```bash
# Get key ID
gpg --list-secret-keys --keyid-format=long

# Upload to keyservers
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```


## Manual Review Before Publishing

The release profile already has `autoPublish=false`. To review artifacts before publication:

1. Run the Maven Release Plugin procedure above; `release:perform` uploads and validates the bundle.
2. Review the deployment and its artifacts at https://central.sonatype.com/publishing.
3. Click **Publish** in the Portal when publication is authorized.

## Reference

- Maven Central Portal: https://central.sonatype.com/
- Portal Documentation: https://central.sonatype.org/publish/publish-portal-maven/
- GPG Documentation: https://www.gnupg.org/documentation/S
