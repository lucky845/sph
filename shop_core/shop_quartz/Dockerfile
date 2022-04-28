FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-quartz.jar shop-quartz.jar
ENTRYPOINT ["java","-jar","/shop-quartz.jar", "&"]