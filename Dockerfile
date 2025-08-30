FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Xmx256m","-jar","/app.jar"]
