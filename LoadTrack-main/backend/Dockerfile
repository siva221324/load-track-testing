# LoadTrack backend — Spring Boot 3.5 on Java 21
# Multi-stage build keeps the final image small (~280 MB)

# ============================================================
# Stage 1 — build the jar
# ============================================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy maven wrapper + descriptor first so Docker caches dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable (Render Linux containers need this even on Windows-pushed repos)
RUN chmod +x mvnw

# Resolve dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Now copy source + build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ============================================================
# Stage 2 — runtime
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Render injects $PORT dynamically (usually 10000); fall back to 8080 for local docker runs
EXPOSE 8080

# Copy only the fat jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Use shell form so ${PORT} is expanded at container start
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080}"]
