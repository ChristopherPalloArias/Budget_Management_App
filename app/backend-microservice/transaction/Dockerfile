# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set permissions for the non-root user
RUN chown spring:spring /app/app.jar

# Switch to the non-root user
USER spring

# Expose port 8081
EXPOSE 8081

# Environment variables (to be overridden at runtime)
ENV DB_USERNAME="" \
    DB_PASSWORD=""

# Healthcheck to ensure the service is alive
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || wget -q --spider http://localhost:8081/ || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
