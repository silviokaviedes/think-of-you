FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# Copy gradle wrapper first to leverage Docker cache
COPY gradlew .
COPY gradle gradle

# Download gradle wrapper and cache it (this layer only changes when gradle version changes)
RUN chmod +x ./gradlew
RUN ./gradlew --version --no-daemon --quiet

# Copy project files
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build the application (dependencies will be cached if build.gradle doesn't change)
RUN ./gradlew build -x test --no-daemon --parallel --configure-on-demand --max-workers=4

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
