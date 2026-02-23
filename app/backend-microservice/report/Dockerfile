# Stage 1: Build
FROM eclipse-temurin:17.0.10_7-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17.0.10_7-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built jar from the build stage
COPY --from=build /app/target/report-0.0.1-SNAPSHOT.jar app.jar

# Set permissions for the non-root user
RUN chown spring:spring /app/app.jar

# Switch to the non-root user
USER spring

# Expose port 8082
EXPOSE 8082

# Environment variables (to be overridden at runtime)
ENV DB_USERNAME="" \
    DB_PASSWORD=""

# Healthcheck to ensure the service is alive (basic check since Actuator is missing)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8082/ || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]