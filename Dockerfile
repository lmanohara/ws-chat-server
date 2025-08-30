# ---------- Build Stage ----------
FROM gradle:8.10-jdk23 AS build

# Set working directory inside container
WORKDIR /app

# Copy Gradle wrapper and build files first to leverage caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the project (produces a JAR)
RUN gradle clean build --no-daemon

# ---------- Runtime Stage ----------
FROM openjdk:23-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/app-1.0.0.jar app.jar

# Expose port (adjust to your app)
EXPOSE 8081

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]