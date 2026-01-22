FROM node:20-alpine AS frontend
WORKDIR /workspace/frontend

# Install frontend deps (cache-friendly)
COPY frontend/package.json ./
RUN npm install --no-audit --no-fund

# Build frontend
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# Copy gradle wrapper first to leverage Docker cache
COPY gradlew .
COPY gradle gradle

# Download gradle wrapper and cache it (this layer only changes when gradle version changes)
RUN chmod +x ./gradlew
RUN ./gradlew --version --no-daemon --quiet

# Copy project files for dependency resolution
COPY build.gradle .
COPY settings.gradle .

# Copy backend sources
COPY src src

# Copy built frontend assets into Spring Boot static resources
COPY --from=frontend /workspace/src/main/resources/static /workspace/app/src/main/resources/static

# Build the application (dependencies will be cached if build.gradle doesn't change)
RUN ./gradlew build -x test --no-daemon --parallel --configure-on-demand --max-workers=4

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
