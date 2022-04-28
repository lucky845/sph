FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-user.jar shop-user.jar
ENTRYPOINT ["java","-jar","/shop-user.jar", "&"]