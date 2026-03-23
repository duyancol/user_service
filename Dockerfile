# Step 1: Build Stage
FROM maven:3.9.9 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code to the container
# Copy only pom.xml first to leverage layer caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# Step 2: Runtime Stage
FROM openjdk:22-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage to the runtime stage
COPY --from=build /app/target/user_service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]