# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src

# 의존성 캐시 최적화 (pom 변경 없을 때 다운로드 재사용)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests


# ===== Runtime stage =====
FROM tomcat:9.0-jdk11-temurin-jammy

# 기본 샘플 앱 제거
RUN rm -rf "$CATALINA_HOME/webapps/"* "$CATALINA_HOME/webapps.dist"

# WAR을 ROOT로 배포 → 컨텍스트 경로 "/" 로 접근
COPY --from=build /src/target/dokseosil.war "$CATALINA_HOME/webapps/ROOT.war"

# Railway가 주는 $PORT에 Tomcat을 바인딩하는 시작 스크립트
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080
CMD ["/entrypoint.sh"]
