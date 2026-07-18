# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the jar file, skipping tests for faster deployment on Render
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime Environment
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the builder stage
COPY --from=builder /app/target/berry-0.0.1-SNAPSHOT.jar app.jar

# Expose the default port (Render dynamically assigns $PORT)
EXPOSE 8080

# ==========================================
# Expert JVM Tuning for Render Free Tier (512MB RAM, 0.1 vCPU)
# ==========================================
# -XX:+UseSerialGC: Ideal for single-core (0.1 CPU). Uses lowest footprint for Garbage Collection.
# -Xmx300m: Hard limit max heap to 300MB (leaves 212MB for OS/Off-heap).
# -XX:MaxMetaspaceSize=100m: Prevents off-heap memory leaks from loaded classes.
# -Xss256k: Shrinks OS thread stack size to conserve RAM.
# -XX:+ExitOnOutOfMemoryError: Forces container crash on OOM so Render auto-restarts it.
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -Xmx250m -XX:MaxMetaspaceSize=150m -Xss256k -XX:+ExitOnOutOfMemoryError -Dserver.port=${PORT:8080} -jar app.jar"]
