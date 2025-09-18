FROM ghcr.io/graalvm/jdk-community:25.0.0 AS build
WORKDIR /build/app/stopgap
ENV GRADLE_USER_HOME=/build/.gradle
COPY gradlew ./
COPY gradle gradle
RUN sed -i 's@-all\.zip@-bin\.zip@' gradle/wrapper/gradle-wrapper.properties && \
    ./gradlew --status
COPY build.gradle settings.gradle gradle.properties  ./
RUN ./gradlew dependencies --refresh-dependencies
COPY src src
RUN ./gradlew jar


FROM ghcr.io/graalvm/jdk-community:25.0.0
WORKDIR /app/stopgap
COPY --from=build /build/app/stopgap/build/libs/libs libs
COPY --from=build /build/app/stopgap/build/libs/stopgap.jar ./
ENTRYPOINT ["java", "-jar", "stopgap.jar"]
