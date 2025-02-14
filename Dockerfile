# 1. Base Image: OpenJDK 사용
FROM openjdk:21-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/*.jar app.jar

# 4. 환경 변수를 ARG로 전달받아 ENV로 설정
ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# 5. 애플리케이션 실행
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "-Xms256m", "-Xmx512m", "app.jar"]