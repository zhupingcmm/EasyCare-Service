# ms-hr-maternity
ms for calculating maternity leave days and allowance supplements across 14 major Chinese cities.

## swagger

http://localhost:8080/swagger-ui/index.html

## Run Profiles

* __Default profile__: `dev`
* __Switch profile__: use Spring profile param to choose `dev` or `prod`.

Configs are split as:
* __Common__: `src/main/resources/application.properties` (spring.profiles.active=dev by default)
* __Dev overrides__: `src/main/resources/application-dev.properties`
  - Uses schema `hr-maternity-cn-dev`, verbose SQL logging
* __Prod overrides__: `src/main/resources/application-prod.properties`
  - Uses schema `hr-maternity-cn-prod`

## Run with Maven

```
# Dev (default)
mvn spring-boot:run

# Explicit dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Run built JAR

```
# Build
mvn -DskipTests package

# Dev (default)
java -jar target/ms-hr-maternity-*.jar

# Explicit dev
java -jar target/ms-hr-maternity-*.jar --spring.profiles.active=dev

# Prod
java -jar target/ms-hr-maternity-*.jar --spring.profiles.active=prod
```

## Environment variables (optional)

You can externalize DB credentials via environment variables and reference with placeholders in `application.properties`, e.g.:

```
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```

## Docker

Build and run using the provided `Dockerfile`.

```
# Build image
docker build -t ms-hr-maternity:latest .

# Run dev (default)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  --name ms-hr-maternity ms-hr-maternity:latest

# Run prod
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name ms-hr-maternity ms-hr-maternity:latest
```

## API Docs

* __Swagger UI__: `/swagger-ui.html`
* __OpenAPI JSON__: `/api-docs`
* __Docs__:
  - `docs/maternity-allowance-api.md`
  - `docs/maternity-leave-api.md`
  - `docs/support-api.md`

## 开发约定：自动记录 Prompts（强制）

本项目要求：所有执行的 Prompts 必须同步记录到 `docs/prompts.md`，并附上日期与输入人，确保可追溯。

已内置两类 Git 钩子，帮助自动化记录：

1. 预提交钩子（pre-commit）：`.githooks/pre-commit`
   - 当本次提交包含代码/文档等改动（`src/`、`docs/`、`pom.xml`、`Dockerfile`、`postman/`、`.github/workflows/`）且未手动更新 `docs/prompts.md` 时，会自动在 `docs/prompts.md` 末尾追加一条记录（包含日期与输入人占位），并自动 `git add docs/prompts.md`，以通过 CI 校验。

2. 提交信息钩子（commit-msg）：`.githooks/commit-msg`
   - 会将“提交说明的第一行”写入到 prompts 记录的 `Prompt` 字段，减少手动维护。

启用方式（本地执行一次）：

```bash
# 在仓库根目录启用自定义 hooks 目录
git config core.hooksPath .githooks

# 设置 Git 用户名（用于 prompts 记录中的“输入人”）
git config user.name "你的姓名"
# 可选：设置邮箱
git config user.email "你的邮箱"
```