FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-web.jar shop-web.jar
ENTRYPOINT ["java","-jar","/shop-web.jar", "&"]