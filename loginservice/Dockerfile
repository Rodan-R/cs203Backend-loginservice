# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the project JAR file to the container
COPY target/loginservice-0.0.1-SNAPSHOT.war /app/login-service.war

# Expose the port your Spring Boot application will run on
EXPOSE 8080

# Run the JAR file using Java
ENTRYPOINT ["java", "-jar", "login-service.war"]
