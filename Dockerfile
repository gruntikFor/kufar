FROM gradle:7.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM amazoncorretto:17.0.18-al2023
COPY --from=build /app/build/libs /app
WORKDIR /app
run chmod +x kufar.jar
ENTRYPOINT ["java", "-jar", "kufar.jar", "--spring.profiles.active=docker"]