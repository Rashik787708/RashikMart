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

# Default to an embedded H2 database. On hosts with an ephemeral filesystem
# (e.g. Render free tier) use IN-MEMORY H2: DB_CLOSE_DELAY=-1 keeps it alive for
# the whole JVM, and the schema + catalog are recreated/seeded on every boot.
# Data written at runtime (users, orders) is NOT persisted across restarts.
#
# Anything set in the Render dashboard overrides these values, so to switch back
# to PostgreSQL or a disk-backed H2, set DB_TYPE / DB_URL / DATABASE_URL there.
ENV DB_TYPE=h2 \
    DB_URL="jdbc:h2:mem:rashikmart;DB_CLOSE_DELAY=-1" \
    DB_USER=sa \
    DB_PASSWORD=WE

# Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]