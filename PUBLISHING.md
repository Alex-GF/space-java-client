# Publishing to Maven Central

This guide explains how to publish the space-java-client package to Maven Central.

## Prerequisites

1. **Maven Central Account**: Create an account at [Sonatype JIRA](https://issues.sonatype.org/)
2. **GPG Keys**: Generate GPG keys for signing artifacts
3. **Maven Configuration**: Configure your `~/.m2/settings.xml`

## Step 1: Generate GPG Keys

```bash
# Generate a new key pair
gpg --gen-key

# List your keys
gpg --list-keys

# Distribute your public key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

## Step 2: Configure Maven Settings

Create or edit `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-username</username>
      <password>your-jira-password</password>
    </server>
  </servers>
  
  <profiles>
    <profile>
      <id>release</id>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

## Step 3: Create a Sonatype JIRA Ticket

1. Go to [Sonatype JIRA](https://issues.sonatype.org/)
2. Create a new issue for "New Project"
3. Provide:
   - Group ID: `io.github.isa-group`
   - Project URL: `https://github.com/Alex-GF/space-java-client`
   - SCM URL: `https://github.com/Alex-GF/space-java-client.git`

Wait for approval (usually 1-2 business days).

## Step 4: Prepare Your Release

```bash
# Ensure everything is committed
git status

# Update version in pom.xml (remove -SNAPSHOT for release)
# Current version: 0.3.0

# Build and verify
mvn clean verify
```

## Step 5: Deploy to Maven Central

```bash
# Deploy to staging repository
mvn clean deploy -P release

# Or use the release plugin
mvn release:prepare
mvn release:perform
```

## Step 6: Release from Staging

1. Go to [Nexus Repository Manager](https://s01.oss.sonatype.org/)
2. Login with your Sonatype credentials
3. Click on "Staging Repositories"
4. Find your repository (io.github.isa-group-...)
5. Select it and click "Close"
6. Wait for validation
7. Click "Release"

## Step 7: Verify Publication

After 10-15 minutes:
- Check [Maven Central Search](https://search.maven.org/)
- Search for: `io.github.isa-group:space-java-client`

It may take 2-4 hours for the package to sync completely.

## Continuous Deployment

For automated releases, consider using:
- GitHub Actions
- Maven Release Plugin
- Semantic Versioning

Example GitHub Action workflow:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
          server-id: ossrh
          server-username: MAVEN_USERNAME
          server-password: MAVEN_PASSWORD
          gpg-private-key: ${{ secrets.GPG_PRIVATE_KEY }}
          gpg-passphrase: MAVEN_GPG_PASSPHRASE
      
      - name: Publish to Maven Central
        run: mvn clean deploy -P release
        env:
          MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
          MAVEN_GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

## Version Management

Follow semantic versioning:
- **Major**: Breaking changes (1.0.0 → 2.0.0)
- **Minor**: New features (0.3.0 → 0.4.0)
- **Patch**: Bug fixes (0.3.0 → 0.3.1)

## Testing Before Publishing

Always test your package locally:

```bash
# Install locally
mvn clean install

# Test in another project
# Add to other project's pom.xml:
<dependency>
    <groupId>io.github.isa-group</groupId>
    <artifactId>space-java-client</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Troubleshooting

**GPG Issues:**
```bash
# If GPG agent fails
export GPG_TTY=$(tty)
```

**Build Failures:**
```bash
# Run with debug
mvn clean deploy -P release -X
```

**Staging Issues:**
- Check validation errors in Nexus
- Ensure all required files are present (jar, sources, javadoc, pom)
- Verify GPG signatures

## Resources

- [Maven Central Guide](https://central.sonatype.org/publish/publish-guide/)
- [GPG Documentation](https://central.sonatype.org/publish/requirements/gpg/)
- [Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/)
