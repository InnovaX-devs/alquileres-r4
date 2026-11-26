# Imagen del backend para el deploy en Render (issue #90).
# Etapa 1: compila el jar con Maven. Los tests se corren antes, en local (mvn test).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: solo el JRE y el jar, para que la imagen sea liviana.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Perfil prod: lee la configuración de variables de entorno (application-prod.properties)
ENV SPRING_PROFILES_ACTIVE=prod
# El plan Free de Render tiene 512 MB: la JVM usa como mucho el 75 %
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
