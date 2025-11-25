FROM ghcr.io/graalvm/jdk-community:25.0.0 AS build
WORKDIR /project/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY gradlew ./
COPY gradle gradle
COPY settings.gradle.kts gradle.properties  ./
COPY app/build.gradle.kts app/
COPY app/src/main app/src/main
RUN --mount=type=cache,target=${GRADLE_USER_HOME} ./gradlew --no-daemon :app:jar


FROM ghcr.io/graalvm/jdk-community:25.0.0
WORKDIR /app/stopgap
COPY --from=build /project/stopgap/build/app/libs/libs libs
COPY --from=build /project/stopgap/build/app/libs/stopgap-app.jar ./
ENTRYPOINT ["java", "-jar", "stopgap-app.jar"]
