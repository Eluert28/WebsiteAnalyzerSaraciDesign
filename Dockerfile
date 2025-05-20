FROM maven:3.8.6-openjdk-18-slim AS build

WORKDIR /app

# Kopiere nur die POM-Datei, um die Abhängigkeiten in einem separaten Layer zu cachen
COPY pom.xml .
RUN mvn dependency:go-offline

# Kopiere den Quellcode und baue die Anwendung
COPY src/ /app/src/
RUN mvn package -DskipTests

# Verwende ein schlankeres Basis-Image für die Laufzeit
FROM openjdk:18-slim

WORKDIR /app

# Kopiere das gebaute JAR aus dem Build-Stage
COPY --from=build /app/target/website-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

# Erstelle Verzeichnisse für die Anwendungsdaten und lege Berechtigungen fest
RUN mkdir -p /app/data /app/reports && \
    chmod -R 777 /app/data /app/reports

# Setze den Einstiegspunkt
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Exponiere den Standard-Port
EXPOSE 8080