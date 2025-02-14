FROM openjdk:21
RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone
ARG JAR_FILE=web-api/build/libs/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /usr/local/newrelic
ADD newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml
CMD ["java", "-Dspring.profiles.active=prod","-javaagent:/usr/local/newrelic/newrelic.jar","-jar","app.jar"]
