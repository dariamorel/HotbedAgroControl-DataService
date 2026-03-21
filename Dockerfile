FROM gradle:9-jdk21 AS builder
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

RUN gradle bootJar --no-daemon -x test

RUN JAR=$(ls build/libs/*-SNAPSHOT.jar 2>/dev/null | head -1) \
    && test -n "$JAR" \
    && cp "$JAR" /workspace/app.jar

# Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
