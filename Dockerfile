# --- Build Stage ---
# Use a Maven image with Java 21 (as specified in your pom.xml) to build your Spring Boot application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build # Changed to JRE 21

# Set the working directory inside the container for the build process
WORKDIR /app

# Copy the Maven project file (pom.xml) first to leverage Docker cache
COPY pom.xml .

# Copy your source code
COPY src ./src

# Build your Spring Boot application into an executable JAR file
# -DskipTests is used to skip running tests during the build
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
# Use a smaller JRE (Java Runtime Environment) 21 image for the runtime
FROM eclipse-temurin:21-jre-alpine # Changed to JRE 21

# Set the working directory for the application inside the runtime container
WORKDIR /app

# Copy the executable JAR file from the 'build' stage into the 'runtime' stage
# This line now correctly references 'Pro-Tracker.jar' based on your pom.xml's finalName
COPY --from=build /app/target/Pro-Tracker.jar app.jar

# Expose the port your Spring Boot application listens on (default is 8084 in your case)
EXPOSE 8084

# Define the command to run the application when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]