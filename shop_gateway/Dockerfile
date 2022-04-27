FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-gateway.jar shop-gateway.jar
ENTRYPOINT ["java","-jar","/shop-gateway.jar", "&"]