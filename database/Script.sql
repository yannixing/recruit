-- =============================================
-- 数据库: recruit (招聘管理系统)
-- =============================================

USE `recruit`;

-- =============================================
-- 表1: user (用户表)
-- 角色: 0-求职者, 1-HR, 2-管理员
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(加密存储)',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-求职者, 1-HR, 2-管理员',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 表2: company (企业信息表)
-- =============================================
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '企业ID',
    `hr_id` BIGINT NOT NULL COMMENT 'HR用户ID(关联user表)',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `short_name` VARCHAR(50) DEFAULT NULL COMMENT '企业简称',
    `industry` VARCHAR(50) DEFAULT NULL COMMENT '所属行业',
    `company_size` VARCHAR(20) DEFAULT NULL COMMENT '公司规模: 1-20人, 20-99人, 100-499人, 500-999人, 1000人以上',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '企业Logo URL',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '公司地址',
    `website` VARCHAR(100) DEFAULT NULL COMMENT '公司官网',
    `description` TEXT COMMENT '公司简介',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已注销, 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_hr_id` (`hr_id`),
    KEY `idx_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业信息表';

-- =============================================
-- 表3: job (职位表)
-- =============================================
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '职位ID',
    `company_id` BIGINT NOT NULL COMMENT '企业ID(关联company表)',
    `hr_id` BIGINT NOT NULL COMMENT '发布人ID(关联user表)',
    `title` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '职位类别: 技术/产品/运营/设计/市场/人事/财务/其他',
    `salary_min` INT DEFAULT NULL COMMENT '最低薪资(单位: 千/月)',
    `salary_max` INT DEFAULT NULL COMMENT '最高薪资(单位: 千/月)',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '工作城市',
    `work_experience` VARCHAR(20) DEFAULT NULL COMMENT '工作经验要求: 应届生/1-3年/3-5年/5-10年/10年以上',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '学历要求: 大专/本科/硕士/博士',
    `job_description` TEXT COMMENT '职位描述',
    `requirement` TEXT COMMENT '任职要求',
    `benefits` VARCHAR(255) DEFAULT NULL COMMENT '福利待遇: 五险一金/年终奖/双休等, 逗号分隔',
    `delivery_count` INT NOT NULL DEFAULT 0 COMMENT '投递人数(冗余统计字段)',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数(冗余统计字段)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过(在招), 2-已驳回, 3-已下架',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_hr_id` (`hr_id`),
    KEY `idx_city_status` (`city`, `status`),
    KEY `idx_category` (`category`),
    KEY `idx_status_create` (`status`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位表';

-- =============================================
-- 表4: resume (简历表)
-- =============================================
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '简历ID',
    `user_id` BIGINT NOT NULL COMMENT '求职者用户ID(关联user表)',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '现居地址',
    `education` VARCHAR(20) DEFAULT NULL COMMENT '最高学历: 大专/本科/硕士/博士',
    `school` VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `work_experience` TEXT COMMENT '工作经历',
    `project_experience` TEXT COMMENT '项目经验',
    `skill` VARCHAR(500) DEFAULT NULL COMMENT '技能标签, 逗号分隔',
    `self_evaluation` TEXT COMMENT '自我评价',
    `attachment` VARCHAR(255) DEFAULT NULL COMMENT '附件简历URL(PDF)',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为默认简历: 0-否, 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已删除, 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_education` (`education`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- =============================================
-- 表5: delivery (投递记录表) - 核心表
-- 投递状态机: 0-待沟通 -> 1-已查看 -> 2-邀约面试 -> 3-面试通过 -> 4-发送Offer -> 5-已入职
--            6-不合适(终止状态)  7-已拒绝(终止状态)
-- =============================================
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投递记录ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID(关联job表)',
    `user_id` BIGINT NOT NULL COMMENT '求职者用户ID(关联user表)',
    `resume_id` BIGINT NOT NULL COMMENT '使用的简历ID(关联resume表)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '投递状态: 0-待沟通, 1-已查看, 2-邀约面试, 3-面试通过, 4-发送Offer, 5-已入职, 6-不合适, 7-已拒绝',
    `status_remark` VARCHAR(255) DEFAULT NULL COMMENT '状态备注(如拒绝原因)',
    `delivery_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    `view_time` DATETIME DEFAULT NULL COMMENT 'HR查看时间',
    `interview_time` DATETIME DEFAULT NULL COMMENT '面试时间',
    `offer_time` DATETIME DEFAULT NULL COMMENT '发送Offer时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_user` (`job_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_status_delivery` (`status`, `delivery_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投递记录表';

-- =============================================
-- 表6: interview (面试邀约表)
-- =============================================
DROP TABLE IF EXISTS `interview`;
CREATE TABLE `interview` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试邀约ID',
    `delivery_id` BIGINT NOT NULL COMMENT '投递记录ID(关联delivery表)',
    `hr_id` BIGINT NOT NULL COMMENT 'HR用户ID(关联user表)',
    `interviewer` VARCHAR(50) DEFAULT NULL COMMENT '面试官姓名',
    `interview_time` DATETIME NOT NULL COMMENT '面试时间',
    `duration` INT DEFAULT 60 COMMENT '面试时长(分钟)',
    `location` VARCHAR(200) NOT NULL COMMENT '面试地点',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注(如: 请携带简历)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已确认(接受), 2-已拒绝, 3-已完成',
    `feedback` TEXT COMMENT '面试反馈',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_delivery_id` (`delivery_id`),
    KEY `idx_hr_id` (`hr_id`),
    KEY `idx_interview_time` (`interview_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试邀约表';

-- =============================================
-- 表7: notification (消息通知表)
-- =============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID(关联user表)',
    `type` TINYINT NOT NULL COMMENT '通知类型: 1-投递状态更新, 2-面试邀约, 3-职位审核结果, 4-系统通知',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `biz_id` BIGINT DEFAULT NULL COMMENT '业务关联ID(如delivery_id)',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 1. 插入用户数据 (密码统一用MD5加密: 123456 -> e10adc3949ba59abbe56e057f20f883e)
INSERT INTO `user` (`id`, `username`, `password`, `phone`, `email`, `role`, `status`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', 'admin@recruit.com', 2, 1),
(2, 'hr_zhang', 'e10adc3949ba59abbe56e057f20f883e', '13800000001', 'hr.zhang@tech.com', 1, 1),
(3, 'hr_wang', 'e10adc3949ba59abbe56e057f20f883e', '13800000002', 'hr.wang@tech.com', 1, 1),
(4, 'hr_li', 'e10adc3949ba59abbe56e057f20f883e', '13800000003', 'hr.li@finance.com', 1, 1),
(5, 'zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '13800000010', 'zhaoliu@email.com', 0, 1),
(6, 'qianqi', 'e10adc3949ba59abbe56e057f20f883e', '13800000011', 'qianqi@email.com', 0, 1),
(7, 'sunba', 'e10adc3949ba59abbe56e057f20f883e', '13800000012', 'sunba@email.com', 0, 1),
(8, 'zhoujiu', 'e10adc3949ba59abbe56e057f20f883e', '13800000013', 'zhoujiu@email.com', 0, 1),
(9, 'wushi', 'e10adc3949ba59abbe56e057f20f883e', '13800000014', 'wushi@email.com', 0, 1),
(10, 'zhengshi', 'e10adc3949ba59abbe56e057f20f883e', '13800000015', 'zhengshi@email.com', 0, 1);

-- 2. 插入企业数据
INSERT INTO `company` (`id`, `hr_id`, `company_name`, `short_name`, `industry`, `company_size`, `logo`, `address`, `website`, `description`) VALUES
(1, 2, '星云科技有限公司', '星云科技', '互联网/IT', '100-499人', '/logos/xingyun.png', '北京市朝阳区望京SOHO', 'https://www.xingyun.tech', '星云科技专注于企业级SaaS服务，致力于用AI赋能企业数字化转型。'),
(2, 3, '智慧云网络技术有限公司', '智慧云网络', '互联网/IT', '500-999人', '/logos/zhihuiyun.png', '上海市浦东新区张江高科技园区', 'https://www.zhihuiyun.com', '智慧云网络是中国领先的云计算服务提供商。'),
(3, 4, '华融金融信息服务有限公司', '华融金融', '金融', '1000人以上', '/logos/huarong.png', '深圳市南山区科技园', 'https://www.huarong.com', '华融金融是国内知名的综合性金融服务集团。');

-- 3. 插入职位数据
INSERT INTO `job` (`id`, `company_id`, `hr_id`, `title`, `category`, `salary_min`, `salary_max`, `city`, `work_experience`, `education`, `job_description`, `requirement`, `benefits`, `delivery_count`, `view_count`, `status`, `create_time`) VALUES
(1, 1, 2, 'Java后端开发工程师', '技术', 20, 35, '北京', '3-5年', '本科', '负责公司核心产品后端架构设计、开发与优化，参与高并发系统的建设。', '1. 计算机相关专业本科及以上学历；\n2. 3年以上Java开发经验，熟悉Spring Boot/Cloud框架；\n3. 熟悉MySQL、Redis等数据库和缓存技术；\n4. 有分布式系统、微服务架构经验者优先。', '五险一金,年终奖,双休,弹性工作', 3, 120, 1, '2026-08-01 10:00:00'),
(2, 1, 2, '前端开发工程师 (React)', '技术', 18, 30, '北京', '3-5年', '本科', '负责公司Web端产品的前端开发，使用React技术栈构建高性能用户界面。', '1. 本科及以上学历，3年以上前端开发经验；\n2. 精通HTML/CSS/JavaScript，熟悉React框架及生态；\n3. 有组件库开发经验、熟悉TypeScript者优先。', '五险一金,年终奖,双休', 2, 85, 1, '2026-08-05 14:30:00'),
(3, 2, 3, '高级产品经理', '产品', 30, 50, '上海', '5-10年', '本科', '负责云平台产品的规划、需求分析、产品设计及生命周期管理。', '1. 5年以上产品经理经验，有B端产品经验；\n2. 具备优秀的数据分析和逻辑思维能力；\n3. 有云计算、SaaS产品经验者优先。', '五险一金,年终奖,股票期权,双休', 5, 200, 1, '2026-08-10 09:00:00'),
(4, 3, 4, '金融数据分析师', '金融', 25, 40, '深圳', '3-5年', '硕士', '负责金融数据分析和建模，为投资决策提供数据支持。', '1. 金融、数学、统计学相关专业硕士及以上学历；\n2. 3年以上金融数据分析经验；\n3. 熟练使用Python、SQL，熟悉机器学习算法。', '五险一金,年终奖,补充公积金,双休', 1, 65, 1, '2026-08-12 11:00:00'),
(5, 1, 2, '运维开发工程师 (DevOps)', '技术', 22, 38, '北京', '3-5年', '本科', '负责公司云平台的运维自动化、监控体系建设及CI/CD流程优化。', '1. 计算机相关专业本科及以上学历；\n2. 3年以上运维开发经验，熟悉Linux系统；\n3. 熟悉Docker、Kubernetes、Jenkins等工具。', '五险一金,年终奖,双休', 0, 30, 0, '2026-08-15 16:00:00'),  -- 待审核
(6, 2, 3, 'UI/UX设计师', '设计', 15, 25, '上海', '1-3年', '本科', '负责公司产品界面设计和用户体验优化。', '1. 美术/设计相关专业本科及以上学历；\n2. 2年以上UI/UX设计经验；\n3. 熟练使用Figma、Sketch等设计工具。', '五险一金,年终奖,双休', 0, 42, 1, '2026-08-16 08:00:00'),
(7, 3, 4, '风控经理', '风控', 35, 55, '深圳', '5-10年', '本科', '负责公司信贷业务的风险管理和风控策略制定。', '1. 金融、经济、法律相关专业本科及以上学历；\n2. 5年以上金融风控经验；\n3. 熟悉银行信贷业务和风控模型。', '五险一金,年终奖,绩效奖金,双休', 0, 20, 1, '2026-08-16 10:00:00');

-- 4. 插入简历数据
INSERT INTO `resume` (`id`, `user_id`, `name`, `gender`, `birth_date`, `phone`, `email`, `address`, `education`, `school`, `major`, `work_experience`, `project_experience`, `skill`, `self_evaluation`, `is_default`) VALUES
(1, 5, '赵六', 1, '1995-03-15', '13800000010', 'zhaoliu@email.com', '北京市海淀区', '本科', '北京邮电大学', '计算机科学与技术', '3年Java开发经验，曾参与电商平台和金融系统开发。', '1. 电商平台订单系统开发；\n2. 金融风控数据平台开发。', 'Java,Spring Boot,MySQL,Redis,Docker', '热爱技术，善于学习，团队协作能力强。', 1),
(2, 6, '钱七', 2, '1997-07-20', '13800000011', 'qianqi@email.com', '上海市浦东新区', '本科', '上海交通大学', '软件工程', '2年前端开发经验，熟悉React和Vue框架。', '1. 企业管理系统前端开发；\n2. 可视化数据大屏项目。', 'React,Vue,TypeScript,Webpack', '对前端技术有热情，追求代码质量和用户体验。', 1),
(3, 7, '孙八', 1, '1993-11-08', '13800000012', 'sunba@email.com', '深圳市南山区', '硕士', '华中科技大学', '计算机科学与技术', '4年产品经理经验，负责过多个SaaS产品。', '1. 云平台产品设计和迭代；\n2. 数据可视化产品从0到1。', 'Axure,Sketch,数据分析,项目管理', '用户导向，逻辑清晰，具备出色的沟通能力。', 1),
(4, 8, '周九', 2, '1996-05-22', '13800000013', 'zhoujiu@email.com', '北京市朝阳区', '本科', '对外经济贸易大学', '金融学', '2年金融行业数据分析经验。', '1. 信贷数据分析报表开发；\n2. 风险指标监控体系建设。', 'Python,SQL,Excel,Tableau', '细心严谨，数据敏感度高。', 1),
(5, 9, '吴十', 1, '1994-09-12', '13800000014', 'wushi@email.com', '上海市徐汇区', '本科', '华东师范大学', '计算机科学与技术', '3年运维开发经验，擅长自动化运维。', '1. 容器化部署平台搭建；\n2. 监控报警系统开发。', 'Linux,Docker,Kubernetes,Python,Jenkins', '责任心强，喜欢钻研技术。', 1),
(6, 10, '郑十一', 2, '1998-02-28', '13800000015', 'zhengshi@email.com', '深圳市福田区', '本科', '中山大学', '设计学', '2年UI/UX设计经验。', '1. 金融App界面设计；\n2. 管理系统设计系统搭建。', 'Figma,Sketch,Photoshop,设计系统', '注重细节，追求美感与功能并存。', 1);

-- 5. 插入投递记录数据
INSERT INTO `delivery` (`id`, `job_id`, `user_id`, `resume_id`, `status`, `status_remark`, `delivery_time`, `view_time`, `interview_time`, `offer_time`) VALUES
(1, 1, 5, 1, 3, NULL, '2026-08-02 09:30:00', '2026-08-02 14:00:00', '2026-08-03 10:00:00', NULL),
(2, 1, 7, 3, 1, NULL, '2026-08-06 16:20:00', '2026-08-07 09:00:00', NULL, NULL),
(3, 1, 9, 5, 6, '技术能力与岗位匹配度不足', '2026-08-10 11:00:00', '2026-08-10 15:30:00', NULL, NULL),
(4, 2, 5, 1, 2, NULL, '2026-08-08 10:00:00', '2026-08-08 16:00:00', '2026-08-10 14:00:00', NULL),
(5, 2, 8, 4, 0, NULL, '2026-08-14 09:00:00', NULL, NULL, NULL),
(6, 3, 5, 1, 4, NULL, '2026-08-11 14:30:00', '2026-08-11 17:00:00', '2026-08-13 15:00:00', '2026-08-15 10:00:00'),
(7, 3, 6, 2, 3, NULL, '2026-08-12 08:00:00', '2026-08-12 11:00:00', '2026-08-14 09:00:00', NULL),
(8, 3, 8, 4, 0, NULL, '2026-08-15 13:00:00', NULL, NULL, NULL),
(9, 4, 9, 5, 1, NULL, '2026-08-13 10:30:00', '2026-08-13 16:00:00', NULL, NULL),
(10, 4, 10, 6, 0, NULL, '2026-08-16 09:00:00', NULL, NULL, NULL);

-- 6. 插入面试邀约数据
INSERT INTO `interview` (`id`, `delivery_id`, `hr_id`, `interviewer`, `interview_time`, `duration`, `location`, `address_detail`, `contact_person`, `contact_phone`, `remark`, `status`) VALUES
(1, 1, 2, '技术总监-陈总', '2026-08-03 10:00:00', 60, '星云科技北京总部', '北京市朝阳区望京SOHO T3 18层', '张HR', '13800000001', '请携带身份证和纸质简历', 3),
(2, 4, 2, '前端组长-刘工', '2026-08-10 14:00:00', 45, '星云科技北京总部', '北京市朝阳区望京SOHO T3 18层', '张HR', '13800000001', '准备个人作品集', 1),
(3, 6, 3, '产品总监-王总', '2026-08-13 15:00:00', 60, '智慧云网络上海总部', '上海市浦东新区张江高科技园区A座 20层', '王HR', '13800000002', NULL, 3),
(4, 7, 3, '产品总监-王总', '2026-08-14 09:00:00', 60, '智慧云网络上海总部', '上海市浦东新区张江高科技园区A座 20层', '王HR', '13800000002', '请准备产品分析报告', 0);

-- 7. 插入通知消息数据
INSERT INTO `notification` (`id`, `user_id`, `type`, `title`, `content`, `biz_id`, `is_read`, `create_time`) VALUES
(1, 5, 2, '面试邀约通知', '您投递的"Java后端开发工程师"职位已通过初筛，邀请您于2026-08-03 10:00参加面试。地点：星云科技北京总部。', 1, 1, '2026-08-02 14:00:00'),
(2, 7, 1, '投递进度更新', '您投递的"Java后端开发工程师"职位状态变更为"已查看"。', 2, 0, '2026-08-07 09:00:00'),
(3, 9, 1, '投递结果通知', '您投递的"Java后端开发工程师"职位未通过筛选，原因：技术能力与岗位匹配度不足。', 3, 0, '2026-08-10 15:30:00'),
(4, 5, 2, '面试邀约通知', '您投递的"前端开发工程师"职位邀请您于2026-08-10 14:00参加面试。', 4, 0, '2026-08-08 16:00:00'),
(5, 5, 1, '投递进度更新', '您投递的"高级产品经理"职位已发送Offer，请及时查收邮件确认。', 6, 0, '2026-08-15 10:00:00'),
(6, 6, 1, '投递进度更新', '您投递的"高级产品经理"职位状态变更为"面试通过"。', 7, 0, '2026-08-14 09:00:00');
