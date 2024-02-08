FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY target/userservice-pub-0.0.1.jar /app/userservice-pub-0.0.1.jar
COPY src/main/resources/MOCK_DATA.csv /app/data/MOCK_DATA.csv
ENTRYPOINT ["java","-jar","/app/userservice-pub-0.0.1.jar"]