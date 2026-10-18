# Etapa 1: Construcción (Build) usando Maven y Java 17
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el pom y descargamos las dependencias primero (optimiza el tiempo)
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run) solo con el JRE para que la imagen sea más ligera
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiamos el archivo .jar generado en la etapa 1
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto de tu ms-order
EXPOSE 8081
# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]