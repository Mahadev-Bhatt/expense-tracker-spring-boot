# --- Build Stage ---
# Use a Maven image with Java 21 (as specified in your pom.xml) to build your Spring Boot application
# --- Stage 1: Build the Spring Boot Application ---
# We use a Maven image that includes a JDK (Java Development Kit) for compilation.
# '3.9.6-eclipse-temurin-22-alpine' is a good choice for Maven with Java 22 on an Alpine Linux base.
FROM maven:3.9.6-eclipse-temurin-22-alpine AS build

# Set the working directory inside this build container
WORKDIR /app

# Copy the Maven project file (pom.xml) first. This allows Docker to cache this step.
# If pom.xml doesn't change, Docker can reuse this layer, speeding up subsequent builds.
COPY pom.xml .

# Copy your application's source code
COPY src ./src

# Build your Spring Boot application into an executable JAR file.
# 'mvn clean package -DskipTests' will clean, compile, and package your app, skipping tests for faster build.
RUN mvn clean package -DskipTests

# --- Stage 2: Create the Final, Lightweight Runtime Image ---
# We now use a smaller JRE (Java Runtime Environment) image. This image only contains what's needed to run Java apps,
# resulting in a significantly smaller final Docker image compared to a full JDK image.
FROM eclipse-temurin:22-jre-alpine

# Set the working directory for your application inside the runtime container
WORKDIR /app

# Copy the executable JAR file from the 'build' stage into this final runtime image.
# IMPORTANT: You need to confirm the exact name of the JAR file produced by your Maven build.
# Based on a typical Spring Boot project with artifactId 'Tracker' and version '0.0.1-SNAPSHOT',
# the JAR name is usually 'Tracker-0.0.1-SNAPSHOT.jar'.
# We rename it to 'app.jar' inside the Docker image for simplicity.
COPY --from=build /app/target/Pro-Tracker.jar app.jar
# If your JAR file has a different name, please adjust 'Tracker-0.0.1-SNAPSHOT.jar' in the line above!

# Expose the port your Spring Boot application listens on.
# Your application.properties previously indicated port 8084.
EXPOSE 8084

# Define the command to run your Spring Boot application when the container starts.
# This will execute the 'app.jar' using Java.
ENTRYPOINT ["java", "-jar", "app.jar"]