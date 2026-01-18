# Stage 1: Build the application
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Environment Variables (Default values, override at runtime)
ENV PORT=8080
ENV DB_URL=jdbc:mysql://localhost:3306/kairowan_ktor
ENV DB_USER=root
ENV DB_PASSWORD=password
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
