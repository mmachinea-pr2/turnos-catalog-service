FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -ntp package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --gid 10001 catalog && useradd --uid 10001 --gid catalog --no-create-home catalog
COPY --from=build /workspace/target/turnos-catalog-service-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
