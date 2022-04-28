FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-cart.jar shop-cart.jar
ENTRYPOINT ["java","-jar","/shop-cart.jar", "&"]