FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/report-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENV DB_USERNAME="" \
    DB_PASSWORD=""
ENTRYPOINT ["java", "-jar", "app.jar"]