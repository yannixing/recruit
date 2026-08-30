# recruit 招聘管理系统后端

本项目借鉴苍穹外卖的 Spring Boot、MyBatis、Redis、JWT、统一返回结构、全局异常处理和审计字段自动填充等通用能力。

## 当前实现

- 项目 Maven 坐标已调整为 `com.recruit:recruit`，业务包名为 `com.recruit`。
- 登录接口为 `POST /auth/login`。当前复用原项目的 MD5 账号密码认证：
  - `role = 0` 的求职者使用 `phone` 作为账号。
  - `role = 1` 的 HR 使用 `email` 作为账号。
  - 登录成功后的 JWT 包含 `userId` 和 `role`。
- `/hr/**` 仅允许 HR Token 访问，`/candidate/**` 仅允许求职者 Token 访问。当前用户 ID 会写入 `BaseContext`。
- HR 职位管理：
  - `POST /hr/jobs` 发布职位。
  - `PUT /hr/jobs` 编辑本人职位。
  - `POST /hr/jobs/{id}/status?status=0|1` 下架或重新上架职位。
  - `DELETE /hr/jobs?ids=1&ids=2` 删除本人且没有投递记录的职位。
  - `GET /hr/jobs/page` 分页查询本人职位。
  - `GET /hr/jobs/{id}` 查看本人职位详情。
- 求职者职位浏览：
  - `GET /candidate/jobs` 支持 `keyword`、`city`、`salaryMin`、`salaryMax`、`page`、`pageSize` 搜索。
  - `GET /candidate/jobs/{id}` 仅返回在招职位详情。
  - `GET /candidate/jobs/hot?city=北京` 返回某城市前 20 个热门职位，Redis Key 为 `job:hot:{city}`，有效期 30 分钟。
- 职位新增、编辑、上下架、删除后会清理 `job:hot:*` 缓存。
- `JobMapper.xml` 已实现 `job` 和 `delivery` 的左关联、`group by` 与投递人数统计。

## 数据库约定

代码不会创建或修改数据库。请在 [application-dev.yml](recruit-server/src/main/resources/application-dev.yml) 中填写本地 MySQL 和 Redis 配置。

当前 Mapper 假定：

- `user` 表包含：`id`、`phone`、`email`、`password`、`role`。
- `job` 表包含：`id`、`hr_id`、`company_name`、`title`、`salary_min`、`salary_max`、`city`、`description`、`requirements`、`status`、`create_time`、`update_time`、`create_user`、`update_user`。
- `delivery` 表包含至少：`id`、`job_id`。

其中 `job` 的四个审计字段来自原项目自动填充能力；若你的表没有这些字段，请提供建表 SQL，再调整 [JobMapper.xml](recruit-server/src/main/resources/mapper/JobMapper.xml)。

## 尚未实现

- 手机或邮箱验证码发送、Redis 验证码校验和验证码登录。
- 简历中照片接口的上传图片需求。

## 运行

安装 Maven 后，在项目根目录执行：

```powershell
mvn clean package
mvn -pl recruit-server spring-boot:run
```

默认服务地址为 `http://localhost:8080`。
