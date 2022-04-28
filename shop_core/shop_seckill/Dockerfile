FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-seckill.jar shop-seckill.jar
ENTRYPOINT ["java","-jar","/shop-seckill.jar", "&"]