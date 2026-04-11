# Stage 1: Build the JAR file
FROM bellsoft/liberica-openjdk-alpine:21 AS builder

# Install Maven using apk (Alpine's package manager)
RUN apk update && apk add maven

# Set the working directory in the container
WORKDIR /build

# Copy the pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy the source code into the container
COPY src ./src

# Build the application with explicit encoding
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Stage 2: Create the final image
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/api-gateway-service.jar /app/

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "api-gateway-service.jar"]