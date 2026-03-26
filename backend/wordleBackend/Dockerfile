# ---------- Stage 1: Build JAR using Maven ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests


# ---------- Stage 2: Run JAR on Java 17 JRE ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy built JAR (safe pattern)
COPY --from=build /app/target/*jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]