FROM ghcr.io/graalvm/jdk-community:25i-ol10 AS build
WORKDIR /project/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY ./ ./
# If gradlew download hangs, pin a reachable IP (185.199.108/110/111.133): docker build --add-host release-assets.githubusercontent.com:185.199.110.133
# The build has no git; the host passes the commit, which ends up as service.version.
ARG GIT_SHA=unknown
RUN --mount=type=cache,target=${GRADLE_USER_HOME} ./gradlew --no-daemon -PgitSha=${GIT_SHA} :app:jar :app:copyOtelAgent


FROM ghcr.io/graalvm/jdk-community:25i-ol10
# Owned before VOLUME, so Docker seeds new volumes with this owner and the stopgap user can write its dumps there.
RUN groupadd --system stopgap && useradd --system --gid stopgap --no-create-home stopgap \
    && mkdir /diagnostics && chown stopgap:stopgap /diagnostics
WORKDIR /app/stopgap
COPY --from=build /project/stopgap/build/app/libs/libs libs
COPY --from=build /project/stopgap/build/app/libs/stopgap-app.jar ./
COPY --from=build /project/stopgap/build/app/otel otel
COPY app/otel.properties app/pyroscope.properties otel/
COPY --from=ghcr.io/amacneil/dbmate:2.37.0 /usr/local/bin/dbmate /usr/local/bin/
COPY app/db/migrations db/migrations
COPY --chmod=755 app/entrypoint.sh ./
# Heap dumps, hs_err logs and JFR dumps; see entrypoint.sh.
VOLUME /diagnostics
USER stopgap
ENTRYPOINT ["./entrypoint.sh"]
