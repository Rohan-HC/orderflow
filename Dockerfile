FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests package


FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /builder

COPY --from=builder /workspace/target/*.jar application.jar

RUN java \
    -Djarmode=tools \
    -jar application.jar \
    extract \
    --layers \
    --destination extracted


FROM eclipse-temurin:21-jre-alpine

WORKDIR /application

RUN addgroup -S orderflow \
    && adduser -S orderflow -G orderflow

COPY --from=extractor /builder/extracted/dependencies/ ./
COPY --from=extractor /builder/extracted/spring-boot-loader/ ./
COPY --from=extractor /builder/extracted/snapshot-dependencies/ ./
COPY --from=extractor /builder/extracted/application/ ./

USER orderflow

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]