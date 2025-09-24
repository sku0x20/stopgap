FROM ghcr.io/graalvm/jdk-community:25.0.0 AS build
WORKDIR /build/app/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY gradlew ./
COPY gradle gradle
RUN ./gradlew --status
COPY build.gradle.kts settings.gradle.kts gradle.properties  ./
RUN ./gradlew dependencies --refresh-dependencies
COPY src src
RUN ./gradlew jar


FROM ghcr.io/graalvm/jdk-community:25.0.0
WORKDIR /app/stopgap
COPY --from=build /build/app/stopgap/build/libs/libs libs
COPY --from=build /build/app/stopgap/build/libs/stopgap.jar ./
ENTRYPOINT ["java", "-jar", "stopgap.jar"]
