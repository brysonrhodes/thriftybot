FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY thriftydbKey.json thrifty.key
ENTRYPOINT ["java", "-jar", "/app.jar", "-DTWILIO_TOKEN=**${TWILIO_TOKEN} -DTWILIO_SID=**${TWILIO_SID} -Dlogging.level.com.brysonrhodes=INFO -DADMIN_PHONE=4438343843 -DDEVELOPER_PHONE=9702372657 -DSERVICE_PHONE=2728881258 -DKEYFILE=thrifty.key"]
EXPOSE 8080