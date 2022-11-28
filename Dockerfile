FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "-DTWILIO_TOKEN=**${TWILIO_TOKEN} -DTWILIO_SID=**${TWILIO_SID} -Dlogging.level.com.brysonrhodes=INFO"]
EXPOSE 8080