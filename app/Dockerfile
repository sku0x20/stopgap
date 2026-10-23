FROM ghcr.io/graalvm/jdk-community:25i-ol10 AS build
WORKDIR /project/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY ./ ./
# If gradlew download hangs, pin a reachable IP (185.199.108/110/111.133): docker build --add-host release-assets.githubusercontent.com:185.199.110.133
RUN --mount=type=cache,target=${GRADLE_USER_HOME} ./gradlew --no-daemon :app:jar


FROM ghcr.io/graalvm/jdk-community:25i-ol10
WORKDIR /app/stopgap
COPY --from=build /project/stopgap/build/app/libs/libs libs
COPY --from=build /project/stopgap/build/app/libs/stopgap-app.jar ./
# Keep in sync with otel-instrumentation in libs.versions.toml (RFC-0002).
ADD --checksum=sha256:f787eb6c7f3d18e69a431e108a15278d25ee37f83d68b678f621e063f3988f82 \
    https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/2.32.0/opentelemetry-javaagent-2.32.0.jar \
    otel/opentelemetry-javaagent.jar
COPY app/otel.properties otel/
ENTRYPOINT ["java", "-javaagent:otel/opentelemetry-javaagent.jar", "-Dotel.javaagent.configuration-file=otel/otel.properties", "-jar", "stopgap-app.jar"]
