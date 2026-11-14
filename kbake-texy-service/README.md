# KBake Texy Service

Docker container for the Texy markup processing service, with Maven integration for building, testing, and pushing to Docker Hub.

> **Note**: This is the canonical source for the Texy service Docker image. Other modules (e.g., `kbake-e2e-tests`) should use the pre-built `kbake/texy-service` image rather than maintaining duplicate Dockerfiles.

## Overview

This module provides:
- **Docker Image**: PHP-based Texy service ready to deploy
- **Maven Build**: Automated Docker image building
- **E2E Tests**: End-to-end tests using TestContainers
- **KBake Integration Tests**: Tests verifying KBake + Texy service integration
- **Docker Hub Push**: Automated publishing to Docker Hub

## Project Structure

```
kbake-texy-service/
├── Dockerfile                         # Docker image definition
├── pom.xml                            # Maven build configuration
├── src/
│   ├── main/
│   │   └── php/
│   │       └── texy-service.php       # Texy HTTP service implementation
│   └── test/
│       ├── kotlin/
│       │   └── org/jbake/texy/
│       │       ├── TexyServiceE2ETest.kt         # Service tests
│       │       └── JBakeTexyIntegrationTest.kt   # KBake integration tests
│       └── resources/
│           └── test-site/
│               ├── content/
│               │   └── test-post.texy           # Test content
│               └── jbake.properties             # Test configuration
```

## Building

### Build Docker Image

```bash
# Build the Docker image
mvn clean package

# Skip Docker build
mvn clean package -PskipDocker

# Build without tests
mvn clean package -DskipTests
```

This will create:
- `kbake/texy-service:5.0.0-rc1` (versioned)
- `kbake/texy-service:latest` (latest tag)

### Push to Docker Hub

```bash
# Set Docker Hub credentials
export DOCKERHUB_USERNAME=your-username
export DOCKERHUB_PASSWORD=your-password

# Build and push
mvn clean deploy -Pdocker-push

# Or push manually
docker push kbake/texy-service:5.0.0-rc1
docker push kbake/texy-service:latest
```

## Testing

### Run All Tests

```bash
mvn test
```

This will:
1. Build the Docker image
2. Start a container with the Texy service
3. Run end-to-end service tests
4. Run KBake integration tests
5. Verify service functionality



## Docker Image

### Image Details

- **Base**: `php:8.2-cli`
- **Port**: 8080
- **Endpoint**: `/texy` (POST)
- **Health Check**: `/` (GET)

### Running the Image

```bash
# Run locally
docker run -d -p 8080:8080 --name texy kbake/texy-service:latest

# Test the service
curl -X POST http://localhost:8080/texy \
  -H "Content-Type: text/plain" \
  -d "This is **bold** text"

# View logs
docker logs texy

# Stop and remove
docker stop texy && docker rm texy
```

### Environment Variables

None required. The service runs with default settings:
- Port: 8080
- No authentication
- UTF-8 encoding

## Maven Configuration

**Phases**:
- `package`: Build image and tag as latest
- `deploy`: Push image to Docker Hub

**Properties**:
- `docker.image.name`: Image name (default: `kbake/texy-service`)
- `docker.image.tag`: Image tag (default: project version)
- `skipDocker`: Skip Docker operations (default: false)


## Build Profiles & Properties

### Maven Profiles

Skip Docker operations: ```bash mvn clean install -PskipDocker```

Enable Docker Hub push during deploy: ```bash mvn clean deploy -Pdocker-push```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Texy Service

on:
  push:
    tags:
      - 'v*'

jobs:
  build-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v5

      - name: Set up JDK
        uses: actions/setup-java@v5
        with:
          java-version: '17'

      - name: Build and Push
        env:
          DOCKERHUB_USERNAME: ${{ secrets.DOCKERHUB_USERNAME }}
          DOCKERHUB_PASSWORD: ${{ secrets.DOCKERHUB_PASSWORD }}
        run: |
          cd kbake-texy-service
          mvn clean deploy -Pdocker-push
```

## Links

- [Texy Official Site](https://texy.info/)
- [TestContainers](https://www.testcontainers.org/)
- [Spotify Dockerfile Maven Plugin](https://github.com/spotify/dockerfile-maven)
