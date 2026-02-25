FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests \
    && mvn dependency:resolve
CMD ["mvn", "spring-boot:run"]
