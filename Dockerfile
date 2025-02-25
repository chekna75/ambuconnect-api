FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

ENV LANGUAGE='en_US:en'
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENV PORT=8080

# Configuration de la base de données
ENV QUARKUS_DATASOURCE_USERNAME=postgres
ENV QUARKUS_DATASOURCE_PASSWORD=toNkzKAbXtdHGkmBYiNZMpCDTNskQcKt
ENV QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres.railway.internal:5432/railway

# Configuration additionnelle pour la robustesse
ENV QUARKUS_HTTP_PORT=5432
ENV QUARKUS_HTTP_ACCESS_LOG_ENABLED=true
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+ExitOnOutOfMemoryError"
ENV CI_PROJECT_NAME=e45ec9ae-0b48-4942-9ea3-0cd191708816




# Copie des fichiers de l'application
COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE ${PORT}
USER 185

# Ajout d'un healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:${PORT}/q/health || exit 1

