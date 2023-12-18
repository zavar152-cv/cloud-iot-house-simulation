FROM openjdk:17-jdk
VOLUME /tmp
#RUN microdnf update && microdnf install wget && \
#    mkdir -p ~/.postgresql && \
#    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
#         --output-document ~/.postgresql/root.crt && \
#    chmod 0600 ~/.postgresql/root.crt
ADD target/*.jar app.jar
ENV JAVA_OPTS=""

EXPOSE  8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar" ]