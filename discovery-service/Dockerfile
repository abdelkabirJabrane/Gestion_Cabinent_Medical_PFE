# Stage 1: Build the JAR file
FROM bellsoft/liberica-openjdk-alpine:21 as builder

# Install Maven using apk (Alpine's package manager)
RUN apk update && apk add maven

# Set the working directory in the container
WORKDIR /build

# Copy the pom.xml and the source code into the container
COPY pom.xml .
COPY src ./src

# Build the application (this will generate the JAR in the target folder)
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/discovery-service.jar /app/

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "discovery-service.jar"]