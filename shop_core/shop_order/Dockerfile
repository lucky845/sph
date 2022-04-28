FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-order.jar shop-order.jar
ENTRYPOINT ["java","-jar","/shop-order.jar", "&"]