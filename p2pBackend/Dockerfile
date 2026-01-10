# STAGE 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiem și descărcăm dependințele (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiem sursele și compilăm
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiem jar-ul generat
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]