# ---------------------------------------------------------------
# IdaMart backend image (used by Render, works with any Docker host)
#
# Stage 1 builds the .jar with Maven; stage 2 runs it on a small Java runtime image.
# Multi-stage = the final image doesn't carry Maven or the source code.
# ---------------------------------------------------------------

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Download dependencies first, in their own layer, so later builds reuse the cache
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Stage 2: run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Don't run as root inside the container
RUN useradd --system --create-home idamart
USER idamart

COPY --from=build /app/target/*.jar app.jar

# Render tells the app which port to use through the PORT variable (see application.properties)
EXPOSE 8080

# Memory settings sized for a 512 MB free instance:
#   heap capped at 280 MB, small thread stacks, the simple serial garbage collector,
#   and faster start-up (TieredStopAtLevel=1) because free instances sleep and wake often.
ENTRYPOINT ["java", \
  "-Xmx280m", "-Xss512k", "-XX:MaxMetaspaceSize=140m", \
  "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", \
  "-jar", "app.jar"]
