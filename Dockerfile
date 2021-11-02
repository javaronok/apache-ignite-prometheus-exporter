FROM java:8-jre
MAINTAINER Dmirtiy Gorchakov <javaronok.gda@gmail.com>

ADD ./build/libs/apache-ignite-prometheus-exporter.jar /app/apache-ignite-prometheus-exporter.jar
ADD ./deploy /app

ENV IGNITE_REST_URL="localhost:8080"
ENV IGNITE_REGION_FILTER="Default_Region"
ENV METRICS_NODE_ENABLED="true"
ENV METRICS_QUERY_ENABLED="true"

WORKDIR /app

CMD ["java", "-Xmx200m", "-jar", "apache-ignite-prometheus-exporter.jar", "--spring.config.location=file:./application.properties"]

EXPOSE 9000