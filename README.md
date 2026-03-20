# [LinktoFur 兽迷互联](https://www.linktofur.net/)

新后端 还在 recode

### 开发规范 (Coding Conventions)
- **缩进**: 4 个空格
- **命名**: 
  - 类名: PascalCase (如 `UserManager`
  - 方法/变量: camelCase (如 `getUserById`)
  - 常量: SCREAMING_SNAKE_CASE (如 `INSTANCE`, `SESSION_DURATION`)
- **框架**: 习惯使用 Lombok (`@Slf4j`, `@Builder`)、单例模式 (`INSTANCE`) 以及 `api.API` 基类
- **注释**: 类头包含 `@author` 和 `@date` 核心逻辑常用中文注释
