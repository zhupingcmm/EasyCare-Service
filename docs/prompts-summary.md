# 产假津贴计算系统 - 后端重构提示词总结

## 项目概述

基于需求文档重新设计完整的产假津贴计算系统后端，采用Spring Boot 3.2.10 + Java 21技术栈，实现四大核心功能模块：
1. 个人产假计算
2. 个人津贴补差计算  

## 技术架构选型

**核心框架：**
- Spring Boot 3.2.10
- Spring Data JPA
- Spring Validation
- Spring Web

**数据库：**
- PostgreSQL（主数据库）
- Redis（缓存）

**文档处理：**
- Apache POI（Excel处理）
- iText PDF（PDF生成）

**设计模式：**
- 策略模式（城市计算规则）
- 工厂模式（计算器创建）
- 建造者模式（复杂对象构建）
- 模板方法模式（通用计算流程）

---

## 第一阶段：基础架构搭建

### 提示词 1：项目结构重构
```
作为资深Spring Boot架构师，重构产假津贴计算系统的项目结构：

1. **包结构设计**：
   - com.ocbc.ms.easy.care.controller：REST API控制器
   - com.ocbc.ms.easy.care.service：业务逻辑服务层
   - com.ocbc.ms.easy.care.repository：数据访问层
   - com.ocbc.ms.easy.care.entity：JPA实体类
   - com.ocbc.ms.easy.care.dto：数据传输对象
   - com.ocbc.ms.easy.care.enums：枚举类型
   - com.ocbc.ms.easy.care.config：配置类
   - com.ocbc.ms.easy.care.strategy：策略模式实现
   - com.ocbc.ms.easy.care.common：通用工具类
   - com.ocbc.ms.easy.care.exception：异常处理

2. **核心配置类**：
   - WebConfig：Web配置
   - DatabaseConfig：数据库配置
   - RedisConfig：Redis配置
   - ValidationConfig：参数校验配置
   - SwaggerConfig：API文档配置

3. **全局异常处理**：
   - GlobalExceptionHandler
   - 自定义业务异常类
   - 统一响应格式

要求：
- 遵循Spring Boot最佳实践
- 清晰的分层架构
- 完整的异常处理机制
- 统一的响应格式
```

### 提示词 2：数据模型设计
```
设计产假津贴计算系统的完整数据模型，包括JPA实体类和数据库表结构：

1. **核心实体类**：
   - Employee：员工基础信息
   - MaternityLeaveRecord：产假记录
   - AllowanceRecord：津贴计算记录
   - CityPolicy：城市政策配置
   - CalculationHistory：计算历史记录
   - BatchProcessRecord：批量处理记录

2. **实体关系**：
   - Employee 1:N MaternityLeaveRecord
   - Employee 1:N AllowanceRecord
   - CityPolicy 1:N MaternityLeaveRecord
   - Employee 1:N CalculationHistory

3. **数据库设计要求**：
   - 使用PostgreSQL
   - 合理的索引设计
   - 审计字段（创建时间、更新时间、创建人、更新人）
   - 软删除支持
   - 数据版本控制

4. **JPA注解使用**：
   - @Entity, @Table
   - @Id, @GeneratedValue
   - @Column, @JoinColumn
   - @OneToMany, @ManyToOne
   - @Enumerated
   - @CreationTimestamp, @UpdateTimestamp
   - @EntityListeners(AuditingEntityListener.class)

要求：
- 完整的实体关系映射
- 合理的字段类型和约束
- 支持审计和软删除
- 数据完整性保证
```

### 提示词 3：枚举类型定义
```
定义产假津贴计算系统所需的所有枚举类型：

1. **城市枚举（CityEnum）**：
   - 14个城市：上海、深圳、广州、天津、绍兴、厦门、成都、苏州、青岛、北京、重庆、珠海、佛山、武汉
   - 包含城市代码、中文名称、是否需要公司垫付

2. **生育方式枚举（DeliveryTypeEnum）**：
   - 顺产（NORMAL）
   - 难产（DIFFICULT）
   - 流产（MISCARRIAGE）

3. **胞胎数量枚举（MultipleEnum）**：
   - 单胎、双胞胎、三胞胎、四胞胎

4. **流产类型枚举（MiscarriageTypeEnum）**：
   - 妊娠未满4个月流产（15天）
   - 妊娠满4个月流产（42天）

5. **难产类型枚举（DifficultDeliveryTypeEnum）**：
   - 剖腹产、会阴III度破裂、吸引产、钳产、臀位牵引产

6. **计算状态枚举（CalculationStatusEnum）**：
   - 计算中、计算成功、计算失败

7. **批量处理状态枚举（BatchStatusEnum）**：
   - 待处理、处理中、处理成功、处理失败

要求：
- 每个枚举包含code、description字段
- 提供根据code查找的静态方法
- 支持JSON序列化/反序列化
- 完整的JavaDoc文档
```

---

## 第二阶段：核心业务逻辑实现

### 提示词 4：产假计算策略模式重构
```
重构产假计算的策略模式实现，支持14个城市的不同计算规则：

1. **策略接口设计**：
   - MaternityLeaveCalculationStrategy：产假计算策略接口
   - 方法：calculateMaternityLeave(MaternityLeaveRequest request)
   - 返回：MaternityLeaveResponse

2. **城市策略实现**：
   - 每个城市一个策略实现类
   - 基础产假98天 + 城市特殊规则
   - 难产假、多胞胎假、奖励假的不同计算
   - 流产假的特殊处理

3. **计算规则**：
   - 总产假天数 = 基础产假(98天) + 奖励假 + 难产假 + 多胞胎假(15天×(胎数-1))
   - 享受津贴天数 = 基础产假(98天) + 难产假 + 多胞胎假 + 奖励假(享受津贴)
   - 绍兴特殊规则：一孩额外处理
   - 广州难产类型细分处理

4. **策略工厂**：
   - StrategyFactory：根据城市代码获取对应策略
   - 使用Spring的@Component注解自动注册
   - 支持策略的动态扩展

5. **DTO设计**：
   - MaternityLeaveRequest：包含所有输入参数
   - MaternityLeaveResponse：包含计算结果和详细构成

要求：
- 策略模式的标准实现
- 支持城市规则的灵活配置
- 完整的参数校验
- 详细的计算过程记录
```

### 提示词 5：津贴补差计算策略重构
```
重构津贴补差计算的策略模式实现，支持不同城市的计算规则：

1. **策略接口设计**：
   - AllowanceCalculationStrategy：津贴计算策略接口
   - 方法：calculateAllowance(AllowanceRequest request)
   - 返回：AllowanceResponse

2. **计算规则实现**：
   - 产假期间不发工资城市：MAX(A,B,C) 或 MAX(B,C)
   - 产假期间发工资城市：MAX(B,C,D)
   - 上海特殊公式：A = 单位申报工资 / 30 × 享受津贴天数
   - 通用公式：B = 员工12个月均工资 / 30 × 享受津贴天数

3. **跨4月调薪处理**：
   - 发工资城市：按时间段分别计算D值
   - 不发工资城市：影响返岗当月日薪计算
   - 跨年场景：个税社保直接相加

4. **跨7月社保基数调整处理**：
   - 社保基数调整时间：每年7月1日生效
   - 产假跨7月场景：按调整前后基数分段计算
   - 个人社保计算：7月前用旧基数，7月后用新基数
   - 公积金基数同步调整：与社保基数保持一致
   - 返还金额影响：重新计算跨7月期间的个人社保和公积金
   - 详细记录：在计算详情中分别显示调整前后的基数和金额

5. **公司垫付计算**（天津、绍兴、厦门、上海、青岛）：
   - 公司垫付 = 整个产假期间(个人社保+个税+弹性福利+ESPP-奖励)
   - 返岗工资 = 返岗当月工作日数 × 日薪
   - 员工返还 = MAX(0, 公司垫付 - 返岗工资)

6. **DTO设计**：
   - AllowanceRequest：薪资信息、垫付信息
   - AllowanceResponse：补差金额、计算详情、返还金额

要求：
- 精确的金额计算
- 详细的计算过程记录
- 支持跨月跨年计算
- 支持跨7月社保基数调整计算
- 完整的业务规则覆盖
```

### 提示词 6：服务层架构设计
```
设计完整的服务层架构，实现业务逻辑的清晰分离：

1. **核心服务接口**：
   - MaternityLeaveService：产假计算服务
   - AllowanceService：津贴计算服务
   - EmployeeService：员工管理服务
   - HistoryService：历史记录服务
   - BatchProcessService：批量处理服务
   - ExportService：导出服务

2. **服务实现要求**：
   - 使用@Service注解
   - 依赖注入Repository和Strategy
   - 完整的事务管理
   - 异常处理和日志记录
   - 缓存策略应用

3. **业务逻辑流程**：
   - 参数校验 → 策略选择 → 计算执行 → 结果保存 → 响应返回
   - 支持计算结果的持久化
   - 支持计算历史的查询

4. **缓存策略**：
   - 城市政策配置缓存
   - 计算结果缓存（基于参数hash）
   - 员工信息缓存

5. **事务管理**：
   - 计算过程的事务一致性
   - 批量处理的事务控制
   - 异常回滚机制

要求：
- 清晰的服务边界
- 完整的事务控制
- 合理的缓存策略
- 详细的日志记录
```

---

## 第三阶段：API接口设计

### 提示词 7：REST API控制器设计
```
设计完整的REST API控制器，提供标准的RESTful接口：

1. **产假计算API**：
   - POST /api/maternity-leave/calculate：计算产假
   - GET /api/maternity-leave/history/{employeeId}：查询计算历史
   - GET /api/maternity-leave/export/{recordId}：导出计算结果

2. **津贴补差API**：
   - POST /api/allowance/calculate：计算津贴补差
   - GET /api/allowance/history/{employeeId}：查询计算历史
   - GET /api/allowance/export/{recordId}：导出计算结果

3. **系统配置API**：
   - GET /api/config/cities：获取城市列表
   - GET /api/config/policies/{cityCode}：获取城市政策

要求：
- 标准的RESTful设计
- 完整的参数校验
- 统一的响应格式
- 详细的API文档
- 合理的HTTP状态码
```

---

## 第四阶段：数据持久化和缓存

### 提示词 8：Repository层设计
```
设计完整的数据访问层，使用Spring Data JPA：

1. **Repository接口**：
   - EmployeeRepository：员工数据访问
   - MaternityLeaveRecordRepository：产假记录访问
   - AllowanceRecordRepository：津贴记录访问
   - CityPolicyRepository：城市政策访问
   - CalculationHistoryRepository：计算历史访问
   - BatchProcessRecordRepository：批量处理记录访问

2. **自定义查询方法**：
   - 基于员工ID的历史记录查询
   - 基于时间范围的记录查询
   - 基于城市的政策查询
   - 复杂条件的分页查询

3. **JPA查询优化**：
   - @Query注解自定义SQL
   - 关联查询优化
   - 分页查询实现
   - 索引设计建议

4. **事务管理**：
   - @Transactional注解使用
   - 事务传播机制
   - 只读事务优化
   - 异常回滚策略

5. **审计功能**：
   - 自动审计字段填充
   - 数据变更历史记录
   - 软删除实现

要求：
- 高效的查询性能
- 完整的事务控制
- 合理的关联查询
- 数据完整性保证
```

### 提示词 9：Redis缓存策略设计
```
设计Redis缓存策略，提升系统性能：

1. **缓存内容设计**：
   - 城市政策配置缓存（长期有效）
   - 计算结果缓存（基于参数hash，短期有效）
   - 员工基础信息缓存（中期有效）
   - 批量处理状态缓存（临时有效）

2. **缓存策略**：
   - Cache-Aside模式：手动管理缓存
   - Write-Through模式：同步写入缓存和数据库
   - 缓存穿透、击穿、雪崩防护

3. **缓存配置**：
   - 不同数据类型的过期时间设置
   - 缓存key命名规范
   - 序列化策略配置
   - 连接池配置优化

4. **技术实现**：
   - Spring Cache抽象
   - @Cacheable、@CacheEvict、@CachePut注解
   - RedisTemplate自定义操作
   - 缓存监控和统计

要求：
- 合理的缓存策略
- 高效的序列化机制
- 完善的缓存失效策略
- 缓存性能监控
```

---

## 第五阶段：系统集成和测试

### 提示词 10：单元测试和集成测试
```
设计完整的测试体系，确保代码质量：

1. **单元测试**：
   - 策略类测试：各城市计算规则测试
   - 服务类测试：业务逻辑测试
   - 工具类测试：计算工具方法测试
   - Repository测试：数据访问测试

2. **集成测试**：
   - API接口测试：完整的请求响应测试
   - 数据库集成测试：事务和数据一致性测试
   - 缓存集成测试：缓存读写测试
   - 文件处理测试：Excel/PDF生成测试

3. **测试工具和框架**：
   - JUnit 5：单元测试框架
   - Mockito：Mock对象框架
   - TestContainers：集成测试容器
   - Spring Boot Test：Spring集成测试

4. **测试数据管理**：
   - 测试数据准备和清理
   - 测试数据库配置
   - Mock数据生成
   - 测试环境隔离

5. **测试覆盖率**：
   - 代码覆盖率要求（>80%）
   - 关键业务逻辑100%覆盖
   - 异常场景测试覆盖

要求：
- 完整的测试用例设计
- 高测试覆盖率
- 可靠的测试数据
- 自动化测试执行
```

### 提示词 11：配置管理和部署
```
设计生产级别的配置管理和部署方案：

1. **配置文件管理**：
   - application.yml多环境配置
   - 数据库连接配置
   - Redis缓存配置
   - 日志配置
   - 文件存储配置

2. **环境配置**：
   - 开发环境（dev）
   - 测试环境（test）
   - 生产环境（prod）
   - 配置参数外部化

3. **Docker容器化**：
   - Dockerfile编写
   - docker-compose.yml配置
   - 多阶段构建优化
   - 健康检查配置

4. **监控和日志**：
   - Actuator健康检查端点
   - Logback日志配置
   - 应用性能监控
   - 错误日志收集

5. **安全配置**：
   - 敏感信息加密
   - API访问控制
   - 文件上传安全
   - CORS跨域配置

要求：
- 生产级别的配置管理
- 容器化部署支持
- 完善的监控体系
- 安全性保障
```

---

## 总结

本提示词总结涵盖了产假津贴计算系统的完整后端重构方案，包括：

1. **基础架构**：项目结构、数据模型、枚举定义
2. **核心业务**：策略模式实现、服务层设计
3. **API设计**：RESTful接口、批量处理、文件导出
4. **数据层**：Repository设计、缓存策略
5. **系统集成**：测试体系、配置管理、部署方案

**技术栈特点**：
- Spring Boot 3.2.10 + Java 21现代化技术栈
- 策略模式支持多城市规则扩展
- 完整的缓存和性能优化
- 生产级别的监控和部署方案

**实施建议**：
- 按阶段逐步实施，确保每个阶段的质量
- 重点关注业务规则的准确性和可扩展性
- 建立完善的测试体系保证代码质量
- 做好性能优化和监控准备
