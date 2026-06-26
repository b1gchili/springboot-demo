# MySQL 初始化说明

1. 确认本机已启动 MySQL。
2. 使用有建库权限的账号执行 `schema.sql`：

```sql
SOURCE D:/javacode/springboot-fullstack-demo/backend/src/main/resources/db/schema.sql;
```

3. 修改 `backend/src/main/resources/application.yml` 中的数据库账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_demo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
```

默认初始化数据：

- 登录账号：`admin`
- 登录密码：`123456`
- 绑定手机号：`13800138000`
- 学生测试数据：5 条

手机验证码说明：

- 验证码有效期为 60 秒。
- 同一手机号 60 秒内不能重复获取验证码。
- 获取新验证码会让该手机号旧的未使用验证码失效。
- 验证码不会返回给前端。
- 当前项目不接入第三方短信服务，验证码会输出到后端控制台日志。

示例日志：

```text
手机验证码已生成: phone=13800138000, code=123456, expiresIn=60s
```

用户列表说明：

- `sys_user.last_login_time` 保存最后登录时间。
- 执行 `schema.sql` 会自动为已有 `sys_user` 表补充 `last_login_time` 字段和索引。
- 账号密码登录、手机验证码登录成功后都会更新最后登录时间。

登录日志说明：

- `login_log` 保存登录日志。
- 账号密码登录、手机验证码登录成功后都会写入一条日志。
- 日志字段包括：账号、姓名、手机号、登录 IP、登录时间。
