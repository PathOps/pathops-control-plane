# --- Build stage ---
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

RUN apt-get update && apt-get install -y maven

COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# --- Runtime stage ---
FROM eclipse-temurin:25-jre
WORKDIR /app

ARG GIT_COMMIT=unknown
ARG GIT_BRANCH=unknown
ENV GIT_COMMIT=${GIT_COMMIT}
ENV GIT_BRANCH=${GIT_BRANCH}

COPY --from=builder /build/target/pathops-control-plane-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
