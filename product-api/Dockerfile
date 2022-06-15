FROM maven
COPY ./. /
WORKDIR .
RUN mvn compile clean install -DskipTests
EXPOSE 8081
CMD ["mvn", "spring-boot:run"]

