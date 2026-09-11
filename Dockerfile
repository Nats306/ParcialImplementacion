FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test

RUN JAR_FILE=$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1) \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /tmp/app.jar


FROM eclipse-temurin:25-jre

WORKDIR /app


COPY --from=build /tmp/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]