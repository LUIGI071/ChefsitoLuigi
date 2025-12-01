# ============================
#  Etapa 1: Build con Maven
# ============================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos la config de Maven y el código
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto (sale el .jar en target/)
RUN mvn -q -DskipTests package

# ============================
#  Etapa 2: Imagen final ligera
# ============================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el .jar generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto interno de la app
EXPOSE 8080

# Arranque de la app Spring Boot
ENTRYPOINT ["java","-jar","/app/app.jar"]
