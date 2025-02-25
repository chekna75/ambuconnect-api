# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# Stage 2: Runtime
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Configuration de la base de données
ENV QUARKUS_DATASOURCE_USERNAME=postgres
ENV QUARKUS_DATASOURCE_PASSWORD=toNkzKAbXtdHGkmBYiNZMpCDTNskQcKt
ENV QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres.railway.internal:5432/railway

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]