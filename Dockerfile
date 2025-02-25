## Étape 1: Build de l'application (utilise Maven et JDK 21)
FROM maven:3.9.6-eclipse-temurin-21-alpine as build

# Définir le répertoire de travail
WORKDIR /app

# Copier d'abord les fichiers pom.xml pour tirer parti du cache des dépendances
COPY pom.xml mvnw* ./
COPY src ./src

# Construction avec Maven
RUN mvn -B package -DskipTests

## Étape 2: Création de l'image d'exécution
FROM eclipse-temurin:21-jre-alpine

# Créer un utilisateur non-root pour l'exécution de l'application
RUN addgroup -S ambuconnect && adduser -S ambuconnect -G ambuconnect

# Définir le répertoire où l'application sera déployée (correspond au startCommand dans railway.toml)
WORKDIR /deployments

# Copier l'application construite depuis l'étape de build
COPY --from=build /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /app/target/quarkus-app/*.jar /deployments/
COPY --from=build /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /deployments/quarkus/

# Changer le propriétaire des fichiers pour l'utilisateur non-root
RUN chown -R ambuconnect:ambuconnect /deployments

# Définir l'utilisateur pour l'exécution de l'application
USER ambuconnect

# Exposer le port sur lequel l'application s'exécute (défini dans railway.toml)
EXPOSE 8080

# Configuration de la JVM
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Point d'entrée pour démarrer l'application
ENTRYPOINT java $JAVA_OPTS -jar /deployments/quarkus-run.jar 