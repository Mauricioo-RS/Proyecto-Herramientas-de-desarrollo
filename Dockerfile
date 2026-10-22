# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY --chmod=0755 mvnw ./mvnw
COPY pom.xml ./pom.xml
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp verify

FROM eclipse-temurin:21-jre-jammy AS runtime
RUN command -v curl \
    && groupadd --gid 10001 minimarket \
    && useradd --uid 10001 --gid minimarket --no-create-home minimarket
WORKDIR /app
COPY --from=build --chown=minimarket:minimarket /workspace/target/minimarket-0.0.1-SNAPSHOT.jar app.jar
USER minimarket
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
