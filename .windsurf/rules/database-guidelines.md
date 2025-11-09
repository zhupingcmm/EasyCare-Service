---
trigger: always_on
---

# 代码规范

## 1 注解使用
```java
// Controller 层
@Slf4j                                    // 日志
@RestController                           // REST控制器
@RequestMapping("/api/maternity-leave")   // 路径映射
@RequiredArgsConstructor                  // Lombok构造器注入
@Tag(name = "产假计算")                    // Swagger文档

// Service 层
@Slf4j
@Service
@RequiredArgsConstructor

// Repository 层
@Repository

// Entity 层
@Entity
@Table(name = "city")
@Data
@EntityListeners(AuditingEntityListener.class)

// DTO 层
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
```
## 2 API响应结构

统一使用 ApiResponse 结构：
```json
{
  "code": 0,           // 0表示成功，非0表示失败
  "message": "OK",     // 提示信息
  "data": {...}        // 数据载体
}
```

## 3 REST API 路径规范
基础路径: /api/{resource}（如 /api/maternity-leave）
资源名: 使用复数形式（如 /cities, /holidays）
操作:
- GET /api/cities - 查询列表
- GET /api/cities/{id} - 查询单个
- POST /api/cities - 创建
- PUT /api/cities/{id} - 更新
- DELETE /api/cities/{id} - 删除
- POST /api/maternity-leave/calculate - 特殊操作

## 4 异常处理
```java
// 参数校验
if (param == null) {
    throw new IllegalArgumentException("参数不能为空");
}

// 业务异常
if (!isValid) {
    throw new BusinessException("业务规则校验失败");
}

// 使用 @Valid 进行 DTO 校验
public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
    // ...
}
```
## 5 日志规范
```java
// 方法入口
log.info("开始计算产假，请求参数: {}", request);

// 关键业务逻辑
log.debug("计算结果: 基础产假={}天, 奖励假={}天", baseLeave, bonusLeave);

// 异常情况
log.error("计算产假失败", e);

// 警告信息
log.warn("城市代码不存在: {}", cityCode);
```
