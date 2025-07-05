# 1. Base Image: OpenJDK 사용
FROM openjdk:21-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. New Relic 에이전트 추가
RUN mkdir -p /usr/local/newrelic
COPY ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
COPY ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

# 5. 환경 변수를 ARG로 전달받아 ENV로 설정
ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# 6. 애플리케이션 실행 (New Relic 에이전트 포함)
CMD ["java", "-Duser.timezone=Asia/Seoul", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "-Dspring.profiles.active=prod", "-Xms256m", "-Xmx512m", "app.jar"]
