# ===== Build Stage =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Build WAR without running tests
RUN mvn clean package -DskipTests

# ===== Tomcat Stage =====
FROM tomcat:9.0-jdk17

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy your WAR as the ROOT application
COPY --from=build /app/target/RashikMart.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]