-- ============================================================
-- 校园食堂菜品营养分析与推荐系统 建库脚本
-- 数据库：canteen_nutrition      适配：MySQL 5.7
-- 说明：不使用外键约束（业务层保证关联，便于演示删除），
--       排序规则统一 utf8mb4_general_ci（5.7 不支持 0900_ai_ci）
-- ============================================================

DROP DATABASE IF EXISTS canteen_nutrition;
CREATE DATABASE canteen_nutrition DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE canteen_nutrition;

-- ------------------------------------------------------------
-- 1. 用户表（学生 / 食堂商户 / 管理员）
-- ------------------------------------------------------------
CREATE TABLE `user` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt密文）',
  `nickname`    VARCHAR(64)  NOT NULL COMMENT '昵称',
  `role`        TINYINT      NOT NULL DEFAULT 1 COMMENT '角色：1学生，2食堂商户，3管理员',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `merchant_id` INT          DEFAULT NULL COMMENT '所属商户ID（商户账号必填，用于数据隔离）',
  `health_goal` VARCHAR(32)  DEFAULT NULL COMMENT '健康目标：减脂/增肌/均衡饮食',
  `preference`  VARCHAR(255) DEFAULT NULL COMMENT '饮食偏好',
  `taboo`       VARCHAR(255) DEFAULT NULL COMMENT '忌口信息，多个用顿号分隔',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_merchant` (`merchant_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '用户表';

-- ------------------------------------------------------------
-- 2. 食堂商户表
-- ------------------------------------------------------------
CREATE TABLE `merchant` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(64)  NOT NULL COMMENT '商户名称',
  `contact`     VARCHAR(64)  DEFAULT NULL COMMENT '联系人',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `address`     VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '商户介绍',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常，0停业',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '食堂商户表';

-- ------------------------------------------------------------
-- 3. 档口表
-- ------------------------------------------------------------
CREATE TABLE `stall` (
  `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id`    INT          NOT NULL COMMENT '所属商户ID',
  `name`           VARCHAR(64)  NOT NULL COMMENT '档口名称',
  `location`       VARCHAR(255) DEFAULT NULL COMMENT '档口位置',
  `business_hours` VARCHAR(64)  DEFAULT NULL COMMENT '营业时间',
  `description`    VARCHAR(500) DEFAULT NULL COMMENT '档口介绍',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1营业，0关闭',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_stall_merchant` (`merchant_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '档口表';

-- ------------------------------------------------------------
-- 4. 菜品分类表
-- ------------------------------------------------------------
CREATE TABLE `dish_category` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(64)  NOT NULL COMMENT '分类名称',
  `sort_num`    INT          DEFAULT 0 COMMENT '排序号',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '菜品分类表';

-- ------------------------------------------------------------
-- 5. 菜品表
-- ------------------------------------------------------------
CREATE TABLE `dish` (
  `id`           INT           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stall_id`     INT           NOT NULL COMMENT '所属档口ID',
  `category_id`  INT           NOT NULL COMMENT '菜品分类ID',
  `name`         VARCHAR(64)   NOT NULL COMMENT '菜品名称',
  `price`        DECIMAL(10, 2) NOT NULL COMMENT '价格（元）',
  `ingredients`  VARCHAR(500)  NOT NULL COMMENT '主要食材，顿号分隔',
  `image`        VARCHAR(500)  DEFAULT NULL COMMENT '菜品图片地址',
  `description`  VARCHAR(500)  DEFAULT NULL COMMENT '菜品介绍',
  `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '上架状态：1上架，0下架',
  `audit_status` TINYINT       NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2驳回',
  `audit_remark` VARCHAR(255)  DEFAULT NULL COMMENT '审核意见',
  `sales`        INT           NOT NULL DEFAULT 0 COMMENT '累计销量',
  `stock`        INT           DEFAULT NULL COMMENT '当前库存，NULL表示不限',
  `daily_limit`  INT           DEFAULT NULL COMMENT '每日限量，NULL表示不限',
  `create_time`  DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time`  DATETIME      DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_dish_stall` (`stall_id`),
  KEY `idx_dish_category` (`category_id`),
  KEY `idx_dish_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '菜品表';

-- ------------------------------------------------------------
-- 6. 营养分析表（AI大模型估算结果）
-- ------------------------------------------------------------
CREATE TABLE `nutrition` (
  `id`             INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dish_id`        INT            NOT NULL COMMENT '菜品ID',
  `calorie`        DECIMAL(10, 2) NOT NULL COMMENT '热量（kcal）',
  `protein`        DECIMAL(10, 2) NOT NULL COMMENT '蛋白质（g）',
  `fat`            DECIMAL(10, 2) NOT NULL COMMENT '脂肪（g）',
  `carbohydrate`   DECIMAL(10, 2) DEFAULT NULL COMMENT '碳水化合物（g）',
  `fiber`          DECIMAL(10, 2) DEFAULT NULL COMMENT '膳食纤维（g）',
  `sodium`         DECIMAL(10, 2) DEFAULT NULL COMMENT '钠（mg）',
  `analysis_text`  TEXT           COMMENT 'AI分析说明',
  `source`         VARCHAR(128)   NOT NULL COMMENT '数据来源说明',
  `model_name`     VARCHAR(64)    DEFAULT NULL COMMENT '生成该结果的模型/规则标识',
  `create_time`    DATETIME       DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME       DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nutrition_dish` (`dish_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '菜品营养分析表';

-- ------------------------------------------------------------
-- 7. 订单主表（order 为保留字，表名使用 orders）
-- ------------------------------------------------------------
CREATE TABLE `orders` (
  `id`            INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no`      VARCHAR(32)    NOT NULL COMMENT '订单编号',
  `user_id`       INT            NOT NULL COMMENT '下单学生ID',
  `merchant_id`   INT            NOT NULL COMMENT '所属商户ID',
  `total_amount`  DECIMAL(10, 2) NOT NULL COMMENT '订单总金额（元）',
  `total_calorie` DECIMAL(10, 2) DEFAULT NULL COMMENT '订单估算总热量（kcal）',
  `order_type`    VARCHAR(16)    NOT NULL DEFAULT 'PICKUP' COMMENT '订单类型：PICKUP点餐，RESERVE预订',
  `status`        TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1待接单，2已接单/制作中，5待取餐，3已完成，4已取消',
  `pickup_code`   VARCHAR(8)     DEFAULT NULL COMMENT '取餐码',
  `pay_time`      DATETIME       DEFAULT NULL COMMENT '支付时间',
  `accept_time`   DATETIME       DEFAULT NULL COMMENT '接单时间',
  `finish_time`   DATETIME       DEFAULT NULL COMMENT '完成时间',
  `remark`        VARCHAR(255)   DEFAULT NULL COMMENT '备注',
  `create_time`   DATETIME       DEFAULT NULL COMMENT '下单时间',
  `update_time`   DATETIME       DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_no` (`order_no`),
  KEY `idx_orders_user` (`user_id`),
  KEY `idx_orders_merchant` (`merchant_id`),
  KEY `idx_orders_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '订单主表';

-- ------------------------------------------------------------
-- 8. 订单明细表
-- ------------------------------------------------------------
CREATE TABLE `order_item` (
  `id`        INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id`  INT            NOT NULL COMMENT '订单ID',
  `dish_id`   INT            NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(64)    NOT NULL COMMENT '下单时菜品名称快照',
  `quantity`  INT            NOT NULL COMMENT '数量',
  `price`     DECIMAL(10, 2) NOT NULL COMMENT '下单时单价快照',
  `amount`    DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_item_order` (`order_id`),
  KEY `idx_item_dish` (`dish_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '订单明细表';

-- ------------------------------------------------------------
-- 9. 评价表
-- ------------------------------------------------------------
CREATE TABLE `review` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          NOT NULL COMMENT '评价学生ID',
  `dish_id`     INT          NOT NULL COMMENT '菜品ID',
  `order_id`    INT          DEFAULT NULL COMMENT '关联订单ID',
  `rating`      TINYINT      NOT NULL COMMENT '评分：1-5',
  `content`     VARCHAR(500) NOT NULL COMMENT '评价内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常，0已屏蔽',
  `create_time` DATETIME     DEFAULT NULL COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_dish` (`dish_id`),
  KEY `idx_review_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '菜品评价表';

-- ------------------------------------------------------------
-- 10. 推荐记录表
-- ------------------------------------------------------------
CREATE TABLE `recommendation` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          NOT NULL COMMENT '学生ID',
  `dish_id`     INT          NOT NULL COMMENT '推荐菜品ID',
  `reason`      VARCHAR(500) DEFAULT NULL COMMENT '推荐理由',
  `score`       DECIMAL(10, 2) DEFAULT NULL COMMENT '推荐得分',
  `health_goal` VARCHAR(32)  DEFAULT NULL COMMENT '生成时依据的健康目标',
  `create_time` DATETIME     DEFAULT NULL COMMENT '生成时间',
  PRIMARY KEY (`id`),
  KEY `idx_rec_user` (`user_id`),
  KEY `idx_rec_dish` (`dish_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '个性化推荐记录表';

-- ------------------------------------------------------------
-- 11. 系统公告表（管理员系统管理）
-- ------------------------------------------------------------
CREATE TABLE `announcement` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title`       VARCHAR(128) NOT NULL COMMENT '公告标题',
  `content`     TEXT         COMMENT '公告内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1发布，0下线',
  `create_time` DATETIME     DEFAULT NULL COMMENT '发布时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '系统公告表';

-- ------------------------------------------------------------
-- 12. AI 调用日志表（异常处理与运行监控依据）
-- ------------------------------------------------------------
CREATE TABLE `ai_call_log` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dish_id`     INT          DEFAULT NULL COMMENT '菜品ID',
  `call_type`   VARCHAR(32)  NOT NULL COMMENT '调用类型：NUTRITION/RECOMMEND',
  `model_name`  VARCHAR(64)  DEFAULT NULL COMMENT '模型名称',
  `success`     TINYINT      NOT NULL COMMENT '是否成功：1成功，0失败',
  `cost_ms`     BIGINT       DEFAULT NULL COMMENT '耗时（毫秒）',
  `error_msg`   VARCHAR(1000) DEFAULT NULL COMMENT '异常信息',
  `raw_content` TEXT         COMMENT '模型原始返回内容',
  `create_time` DATETIME     DEFAULT NULL COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_dish` (`dish_id`),
  KEY `idx_log_success` (`success`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT 'AI接口调用日志表';

-- ------------------------------------------------------------
-- 13. 公告已读记录表（支撑消息提醒的红点，去掉"公告提醒去不掉"的假象）
-- ------------------------------------------------------------
CREATE TABLE `announcement_read` (
  `id`              INT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `announcement_id` INT      NOT NULL COMMENT '公告ID',
  `user_id`         INT      NOT NULL COMMENT '阅读者用户ID',
  `read_time`       DATETIME DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_read_announce_user` (`announcement_id`, `user_id`),
  KEY `idx_read_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '公告已读记录表';

-- ============================================================
-- 以下为功能扩展 v2 新增表（营养追踪 / 推荐升级 / 交易运营 / 后台）
-- ============================================================

CREATE TABLE `health_profile` (
  `id`             INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`        INT            NOT NULL COMMENT '学生用户ID',
  `gender`         TINYINT        DEFAULT NULL COMMENT '性别：1男，2女',
  `birthday`       DATE           DEFAULT NULL COMMENT '出生日期',
  `height_cm`      DECIMAL(5, 1)  DEFAULT NULL COMMENT '身高（cm）',
  `weight_kg`      DECIMAL(5, 1)  DEFAULT NULL COMMENT '当前体重（kg）',
  `activity_level` TINYINT        DEFAULT 2 COMMENT '活动量：1久坐，2轻度，3中度，4重度',
  `health_goal`    VARCHAR(16)    DEFAULT NULL COMMENT '健康目标：减脂/增肌/均衡',
  `bmr`            DECIMAL(7, 1)  DEFAULT NULL COMMENT '基础代谢率（kcal）',
  `tdee`           DECIMAL(7, 1)  DEFAULT NULL COMMENT '每日总消耗（kcal）',
  `target_calorie` DECIMAL(7, 1)  DEFAULT NULL COMMENT '每日目标热量（kcal）',
  `target_protein` DECIMAL(6, 1)  DEFAULT NULL COMMENT '每日目标蛋白质（g）',
  `target_fat`     DECIMAL(6, 1)  DEFAULT NULL COMMENT '每日目标脂肪（g）',
  `target_carb`    DECIMAL(6, 1)  DEFAULT NULL COMMENT '每日目标碳水（g）',
  `create_time`    DATETIME       DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME       DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_profile_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '学生身体档案与营养目标';

CREATE TABLE `diet_record` (
  `id`           INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`      INT            NOT NULL COMMENT '学生用户ID',
  `dish_id`      INT            DEFAULT NULL COMMENT '关联菜品ID，手动记录可空',
  `dish_name`    VARCHAR(64)    DEFAULT NULL COMMENT '菜品名称快照',
  `record_date`  DATE           NOT NULL COMMENT '记录日期',
  `meal_type`    VARCHAR(8)     DEFAULT NULL COMMENT '餐段：早餐/午餐/晚餐/加餐',
  `calorie`      DECIMAL(7, 1)  DEFAULT NULL COMMENT '热量（kcal）',
  `protein`      DECIMAL(6, 1)  DEFAULT NULL COMMENT '蛋白质（g）',
  `fat`          DECIMAL(6, 1)  DEFAULT NULL COMMENT '脂肪（g）',
  `carbohydrate` DECIMAL(6, 1)  DEFAULT NULL COMMENT '碳水化合物（g）',
  `sodium`       DECIMAL(7, 1)  DEFAULT NULL COMMENT '钠（mg）',
  `source`       VARCHAR(16)    NOT NULL DEFAULT 'manual' COMMENT '来源：order下单，manual手动',
  `order_item_id` INT           DEFAULT NULL COMMENT '关联订单明细ID',
  `create_time`  DATETIME       DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_diet_user_date` (`user_id`, `record_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '每日饮食摄入记录';

CREATE TABLE `weight_log` (
  `id`          INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT            NOT NULL COMMENT '学生用户ID',
  `weight_kg`   DECIMAL(5, 1)  NOT NULL COMMENT '体重（kg）',
  `record_date` DATE           NOT NULL COMMENT '记录日期',
  `note`        VARCHAR(255)   DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME       DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_weight_user_date` (`user_id`, `record_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '体重趋势记录';

CREATE TABLE `meal_plan` (
  `id`              INT            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         INT            NOT NULL COMMENT '学生用户ID',
  `plan_date`       DATE           NOT NULL COMMENT '计划日期',
  `meal_type`       VARCHAR(8)     NOT NULL COMMENT '餐段：早餐/午餐/晚餐/加餐',
  `dish_id`         INT            NOT NULL COMMENT '菜品ID',
  `dish_name`       VARCHAR(64)    DEFAULT NULL COMMENT '菜品名称快照',
  `planned_calorie` DECIMAL(7, 1)  DEFAULT NULL COMMENT '计划热量（kcal）',
  `adopted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '是否已下单采用：1是，0否',
  `create_time`     DATETIME       DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME       DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_plan_user_date` (`user_id`, `plan_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '周期餐计划';

CREATE TABLE `allergen` (
  `id`   INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(32)  NOT NULL COMMENT '英文编码',
  `name` VARCHAR(32)  NOT NULL COMMENT '中文名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_allergen_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '过敏原字典';

CREATE TABLE `dish_allergen` (
  `id`          INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dish_id`     INT NOT NULL COMMENT '菜品ID',
  `allergen_id` INT NOT NULL COMMENT '过敏原ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dish_allergen` (`dish_id`, `allergen_id`),
  KEY `idx_da_allergen` (`allergen_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '菜品-过敏原关系';

CREATE TABLE `ai_chat_message` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          NOT NULL COMMENT '学生用户ID',
  `session_id`  VARCHAR(40)  NOT NULL COMMENT '会话ID',
  `role`        VARCHAR(16)  NOT NULL COMMENT '角色：user/assistant',
  `content`     TEXT         COMMENT '消息内容',
  `model_name`  VARCHAR(64)  DEFAULT NULL COMMENT '生成回复的模型标识',
  `success`     TINYINT      DEFAULT NULL COMMENT '本次调用是否成功',
  `cost_ms`     INT          DEFAULT NULL COMMENT '耗时（毫秒）',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_user_session` (`user_id`, `session_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT 'AI营养问答对话记录';

CREATE TABLE `recommend_feedback` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          NOT NULL COMMENT '学生用户ID',
  `dish_id`     INT          NOT NULL COMMENT '菜品ID',
  `action`      TINYINT      NOT NULL COMMENT '反馈：1赞，-1踩',
  `reason`      VARCHAR(255) DEFAULT NULL COMMENT '反馈原因',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fb_user_dish` (`user_id`, `dish_id`),
  KEY `idx_fb_dish` (`dish_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '个性化推荐反馈';

CREATE TABLE `favorite` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT      NOT NULL COMMENT '学生用户ID',
  `dish_id`     INT      NOT NULL COMMENT '菜品ID',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav_user_dish` (`user_id`, `dish_id`),
  KEY `idx_fav_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '学生菜品收藏';

CREATE TABLE `notification` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          NOT NULL COMMENT '接收用户ID',
  `type`        VARCHAR(16)  NOT NULL COMMENT '类型：order/audit/announcement/system',
  `title`       VARCHAR(128) NOT NULL COMMENT '标题',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '内容',
  `biz_id`      INT          DEFAULT NULL COMMENT '关联业务ID（订单/公告等）',
  `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：1已读，0未读',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notify_user_read` (`user_id`, `is_read`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '站内消息通知';

CREATE TABLE `operation_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     INT          DEFAULT NULL COMMENT '操作人ID',
  `username`    VARCHAR(64)  DEFAULT NULL COMMENT '操作人账号',
  `module`      VARCHAR(32)  DEFAULT NULL COMMENT '业务模块',
  `operation`   VARCHAR(64)  DEFAULT NULL COMMENT '操作描述',
  `method`      VARCHAR(200) DEFAULT NULL COMMENT '目标方法',
  `params`      VARCHAR(1000) DEFAULT NULL COMMENT '入参摘要',
  `ip`          VARCHAR(64)  DEFAULT NULL COMMENT '来源IP',
  `cost_ms`     INT          DEFAULT NULL COMMENT '耗时（毫秒）',
  `success`     TINYINT      DEFAULT NULL COMMENT '是否成功',
  `error_msg`   VARCHAR(500) DEFAULT NULL COMMENT '异常信息',
  `create_time` DATETIME     DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_op_time` (`create_time`),
  KEY `idx_op_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '操作日志';

-- 过敏原字典种子
INSERT INTO `allergen` (`code`, `name`) VALUES
  ('gluten', '麸质'), ('nuts', '坚果'), ('peanut', '花生'), ('dairy', '乳制品'),
  ('seafood', '海鲜'), ('egg', '蛋'), ('soy', '大豆');
