# ESTÁGIO 1: build (tem nome "build")
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
# aqui dentro existe: código fonte, Maven, JDK, target/api.jar

# ESTÁGIO 2: runtime (imagem final)
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar api.jar
# só copiei O ARQUIVO .jar do estágio "build" pra cá
# nada mais do estágio 1 existe aqui
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api.jar"]