FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
# Copy gradle wrapper and related files first to cache dependencies download
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
# We don't have dependencies explicitly to download via a separate task easily without src,
# so we will just copy src and run installDist which will download them.
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew installDist

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/install/LinkToFur-Server .

# Expose the application port
EXPOSE 2778
ENTRYPOINT ["bin/LinkToFur-Server"]
