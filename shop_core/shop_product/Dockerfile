FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY ./target/shop-product.jar shop-product.jar
ENTRYPOINT ["java","-jar","/shop-product.jar", "&"]