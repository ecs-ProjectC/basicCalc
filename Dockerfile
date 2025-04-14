# Use a base image with Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR (adjust if filename is different)
COPY target/basicCalc-1.0-SNAPSHOT.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
