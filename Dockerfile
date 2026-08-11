# ESTÁGIO 1: build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ESTÁGIO 2: runtime
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api.jar"]