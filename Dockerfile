# API 서버 이미지. 배포 구성은 deploy/README.md 참고.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# Windows에서 체크아웃한 경우 줄바꿈(CRLF)을 고친다.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home app \n    && mkdir -p /app/data/issue-images && chown -R app /app/data
COPY --from=build /app/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
