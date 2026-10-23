FROM ghcr.io/graalvm/jdk-community:25i-ol10 AS build
WORKDIR /project/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY ./ ./
# If gradlew download hangs, pin a reachable IP (185.199.108/110/111.133): docker build --add-host release-assets.githubusercontent.com:185.199.110.133
# The build has no git; the host passes the commit, which ends up as service.version.
ARG GIT_SHA=unknown
RUN --mount=type=cache,target=${GRADLE_USER_HOME} ./gradlew --no-daemon -PgitSha=${GIT_SHA} :app:jar :app:copyOtelAgent


FROM ghcr.io/graalvm/jdk-community:25i-ol10
WORKDIR /app/stopgap
COPY --from=build /project/stopgap/build/app/libs/libs libs
COPY --from=build /project/stopgap/build/app/libs/stopgap-app.jar ./
COPY --from=build /project/stopgap/build/app/otel otel
COPY app/otel.properties app/pyroscope.properties otel/
COPY --chmod=755 app/entrypoint.sh ./
# Heap dumps, hs_err logs and JFR dumps; see entrypoint.sh.
VOLUME /diagnostics
# otel.semconv-stability.opt-in is a system property: the JDBC library reads it directly, not from otel.properties.
ENTRYPOINT ["./entrypoint.sh", \
    "-javaagent:otel/opentelemetry-javaagent.jar", \
    "-Dotel.javaagent.configuration-file=otel/otel.properties", \
    "-Dotel.javaagent.extensions=otel/extensions", \
    "-Dpyroscope.configuration.file=otel/pyroscope.properties", \
    "-Dotel.semconv-stability.opt-in=database", \
    "-jar", "stopgap-app.jar"]
