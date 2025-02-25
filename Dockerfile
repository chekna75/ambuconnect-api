# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Configuration du répertoire de travail
WORKDIR /build

# Copie du pom.xml et téléchargement des dépendances
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copie des sources et build
COPY src src
RUN ./mvnw package -DskipTests

# Stage 2: Runtime
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

# Configuration de l'environnement
ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Configuration de la base de données
ENV QUARKUS_DATASOURCE_USERNAME=postgres
ENV QUARKUS_DATASOURCE_PASSWORD=toNkzKAbXtdHGkmBYiNZMpCDTNskQcKt
ENV QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres.railway.internal:5432/railway

# Copie des fichiers de l'application
COPY --from=build --chown=185 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /build/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]