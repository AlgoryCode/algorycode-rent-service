FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw \
  && ./mvnw -B -DskipTests package \
  && JAR="$(ls target/*.jar | grep -v 'original' | grep -v 'sources' | grep -v 'javadoc' | head -n1)" \
  && test -n "$JAR" \
  && cp "$JAR" /app/application.jar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

USER spring:spring

COPY --from=build --chown=spring:spring /app/application.jar .

EXPOSE 8090

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar application.jar"]
