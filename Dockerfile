# 使用轻量 JDK 21 运行环境
FROM eclipse-temurin:21-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl

WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN addgroup -g 1001 -S deployuser && \
    adduser -S deployuser -u 1001 -G deployuser

# 拷贝已经打好的 jar
COPY target/*.jar app.jar

# 修改文件权限
RUN chown deployuser:deployuser app.jar

# 切换到非 root 用户
USER deployuser:deployuser

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数和时区
ENV JAVA_OPTS="-Xmx512m -Xms256m -Duser.timezone=Asia/Shanghai"
ENV TZ=Asia/Shanghai

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
