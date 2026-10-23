FROM ghcr.io/graalvm/jdk-community:25i-ol10 AS build
WORKDIR /project/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY ./ ./
# If gradlew download hangs, pin a reachable IP (185.199.108/110/111.133): docker build --add-host release-assets.githubusercontent.com:185.199.110.133
RUN --mount=type=cache,target=${GRADLE_USER_HOME} ./gradlew --no-daemon :app:jar :app:copyOtelAgent


FROM ghcr.io/graalvm/jdk-community:25i-ol10
WORKDIR /app/stopgap
COPY --from=build /project/stopgap/build/app/libs/libs libs
COPY --from=build /project/stopgap/build/app/libs/stopgap-app.jar ./
COPY --from=build /project/stopgap/build/app/otel otel
COPY app/otel.properties otel/
ENTRYPOINT ["java", \
    "-javaagent:otel/opentelemetry-javaagent.jar", \
    "-Dotel.javaagent.configuration-file=otel/otel.properties", \
    "-jar", "stopgap-app.jar"]
