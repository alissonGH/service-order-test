# Etapa 1: build da aplicação
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: imagem final com JAR gerado
FROM amazoncorretto:21-alpine
WORKDIR /app

# Copia o JAR compilado
COPY --from=builder /app/target/service-order*.jar app.jar

# Expõe a porta do Spring Boot
EXPOSE 8080

# Define o comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
