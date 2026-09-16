# ---- Run ----
# Jar đã được CI build sẵn ở stage 'Build' (mvn package). Ở đây chỉ đóng gói runtime.
# Build context = thư mục product-service  =>  target/*.jar tồn tại sau khi mvn package.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# tự lấy jar bất kể tên gì (bỏ qua *.jar.original của spring-boot repackage)
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
