FROM maven:3.6.3-jdk-11
COPY ./. /
WORKDIR .
RUN mvn compile clean install -DskipTests
EXPOSE 8081
CMD ["mvn", "spring-boot:run"]

