FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-payment.jar shop-payment.jar
ENTRYPOINT ["java","-jar","/shop-payment.jar", "&"]