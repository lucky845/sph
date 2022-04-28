FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-search.jar shop-search.jar
ENTRYPOINT ["java","-jar","/shop-search.jar", "&"]