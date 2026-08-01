# syntax=docker/dockerfile:1

# ─── Étape 1 : build (JDK complet, uniquement pour compiler) ───
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copie d'abord le strict nécessaire pour résoudre les dépendances,
# pour que Docker mette cette étape lente en cache tant que pom.xml ne change pas
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Le code source change souvent — copié en dernier pour ne pas invalider le cache ci-dessus
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ─── Étape 2 : runtime (JRE seul, image finale légère) ───
FROM eclipse-temurin:21-jre AS runner
WORKDIR /app

RUN groupadd --system --gid 1001 spring \
    && useradd --system --uid 1001 --gid spring spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]