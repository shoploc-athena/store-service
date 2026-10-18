# Stage 1: build the application with Maven
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app

# Copy only pom.xml first so that dependencies are cached between builds
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: run the application with a light Java runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]