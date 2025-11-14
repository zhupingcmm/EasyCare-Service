---
trigger: always_on
---

# 命名规范
## 1 类命名
- 实体类（Entity）无后缀，使用 `@Entity`
- 领域对象（Domain）， 以 `DO` 结尾， 如`xxxDO`
- DTO 以 `Request`/`Response`/`DTO` 结尾
- Controller 以 `XxxController` 结尾，REST 路径用复数
- Service接口 以 `Service` 结尾, 如`xxxService`
- Service实现 以 `ServiceImpl` 结尾, 如 `xxxServiceImpl`
- Repository 以 `Repository` 结尾, 如`xxxRepository`
- 枚举类 以 `Enum` 结尾， 如 `xxxEnum`
- 配置类 以 `Config` 结尾, 如`xxxConfig`
- 策略接口 以 `Strategy` 结尾 ， `xxxStrategy`
- 常量类 以 `Constants` 结尾  `xxxConstants`
- PostgreSQL 表名全小写+下划线，字段小写+下划线
- API 响应结构统一为：{ code, message, data }

## 2 方法命名
- 查询单个  以 `findBy`, `get`开头，如 `findByXxx`, `getXxx` 
- 查询列表 以 `findAll`, `list`， 如`findAllXxx`, `listXxx`
- 保存  `saveXxx`, `createXxx` 
- 更新 `updateXxx`, `modify`
- 删除 以 `delete`, `remove` 开头，如 `deleteXxx`, `removeXxx`
- 计算 以 `calculate` 开头 如 `calculateXxx` 
- 验证 以 `validate`, `check` 开头, 如 `validateXxx`, `checkXxx`
- 转换 以 `convertTo`, `toXxx`， 如 `convertToXxx`, `toXxx` 

## 3 变量命名

- 布尔类型 - 使用 is, has, can, should 前缀
private Boolean isEnabled;
private Boolean hasBonus;
private Boolean canApply;

- 集合类型 - 使用复数形式或 List, Map 后缀
private List<City> cities;
private List<City> cityList;
private Map<String, City> cityMap;

- 常量 - 全大写+下划线
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_CITY_CODE = "SH";
public static final BigDecimal ZERO = BigDecimal.ZERO;

## 4 数据库命名
- 表名: 全小写+下划线
- 字段名: 全小写+下划线
- 索引: idx_表名_字段名
- 主键: 统一使用 id，类型为 UUID

