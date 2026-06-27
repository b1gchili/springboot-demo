CREATE DATABASE IF NOT EXISTS student_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE student_demo;

CREATE TABLE IF NOT EXISTS user_points (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  available_points BIGINT NOT NULL DEFAULT 0 COMMENT '可用积分',
  total_earned_points BIGINT NOT NULL DEFAULT 0 COMMENT '累计获得积分',
  total_used_points BIGINT NOT NULL DEFAULT 0 COMMENT '累计使用积分',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_points_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户表';

CREATE TABLE IF NOT EXISTS points_flow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  flow_no VARCHAR(64) NOT NULL COMMENT '积分流水号',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  change_points BIGINT NOT NULL COMMENT '积分变动值，正数表示增加，负数表示扣减',
  before_points BIGINT NOT NULL COMMENT '变动前可用积分',
  after_points BIGINT NOT NULL COMMENT '变动后可用积分',
  flow_type VARCHAR(20) NOT NULL COMMENT '流水类型：EARN获得，USE使用，FREEZE冻结，UNFREEZE解冻，EXPIRE过期，ADJUST调整',
  source_type VARCHAR(50) NOT NULL COMMENT '来源类型：SIGN签到，TASK任务，EXCHANGE兑换，ADMIN后台等',
  source_biz_id VARCHAR(64) NULL COMMENT '来源业务ID',
  reason VARCHAR(255) NOT NULL COMMENT '积分变动原因',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流水创建时间',
  UNIQUE KEY uk_points_flow_flow_no (flow_no),
  KEY idx_points_flow_user_time (user_id, create_time),
  KEY idx_points_flow_source (source_type, source_biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

CREATE TABLE IF NOT EXISTS points_sign_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  sign_date DATE NOT NULL COMMENT '签到日期',
  reward_points BIGINT NOT NULL DEFAULT 0 COMMENT '签到奖励积分',
  continuous_days INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_points_sign_user_date (user_id, sign_date),
  KEY idx_points_sign_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日签到记录表';

CREATE TABLE IF NOT EXISTS points_task_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  task_code VARCHAR(64) NOT NULL COMMENT '任务编码',
  task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  reward_points BIGINT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  repeatable TINYINT NOT NULL DEFAULT 0 COMMENT '是否可重复奖励：0不可重复，1可重复',
  reward_limit_type VARCHAR(20) NOT NULL DEFAULT 'ONCE' COMMENT '奖励限制类型：ONCE一次性，DAILY每日，UNLIMITED不限次',
  daily_limit INT NOT NULL DEFAULT 1 COMMENT '每日奖励次数上限',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  description VARCHAR(255) NULL COMMENT '任务说明',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_points_task_config_code (task_code),
  KEY idx_points_task_config_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分任务配置表';

SET @column_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'points_task_config'
    AND COLUMN_NAME = 'repeatable'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE points_task_config ADD COLUMN repeatable TINYINT NOT NULL DEFAULT 0 COMMENT ''是否可重复奖励：0不可重复，1可重复'' AFTER reward_points',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS points_task_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  task_code VARCHAR(64) NOT NULL COMMENT '任务编码',
  biz_id VARCHAR(64) NOT NULL COMMENT '业务ID，用于同一任务下的幂等控制',
  reward_points BIGINT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  reward_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '奖励状态：SUCCESS成功，FAILED失败',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_points_task_user_task_biz (user_id, task_code, biz_id),
  KEY idx_points_task_record_user_time (user_id, create_time),
  KEY idx_points_task_record_task_code (task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分任务奖励记录表';

CREATE TABLE IF NOT EXISTS coupon_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  template_code VARCHAR(64) NOT NULL COMMENT '优惠券模板编码',
  template_name VARCHAR(100) NOT NULL COMMENT '优惠券模板名称',
  coupon_type VARCHAR(20) NOT NULL COMMENT '优惠券类型：AMOUNT满减券，DISCOUNT折扣券',
  face_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '面值或折扣值',
  threshold_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
  exchange_points BIGINT NOT NULL DEFAULT 0 COMMENT '兑换所需积分',
  stock INT NOT NULL DEFAULT 0 COMMENT '剩余库存，扣库存时使用 stock > 0 条件更新',
  total_stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
  valid_days INT NOT NULL DEFAULT 30 COMMENT '领取后有效天数',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_coupon_template_code (template_code),
  KEY idx_coupon_template_enabled (enabled),
  KEY idx_coupon_template_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

CREATE TABLE IF NOT EXISTS user_coupon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  coupon_no VARCHAR(64) NOT NULL COMMENT '用户优惠券编号',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  template_code VARCHAR(64) NOT NULL COMMENT '优惠券模板编码',
  coupon_name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  coupon_type VARCHAR(20) NOT NULL COMMENT '优惠券类型：AMOUNT满减券，DISCOUNT折扣券',
  face_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '面值或折扣值',
  threshold_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
  status VARCHAR(20) NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED未使用，USED已使用，EXPIRED已过期',
  receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  use_time DATETIME NULL COMMENT '使用时间',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_coupon_no (coupon_no),
  KEY idx_user_coupon_user_status (user_id, status),
  KEY idx_user_coupon_template_code (template_code),
  KEY idx_user_coupon_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

CREATE TABLE IF NOT EXISTS points_exchange_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  exchange_no VARCHAR(64) NOT NULL COMMENT '兑换单号',
  request_no VARCHAR(64) NOT NULL COMMENT '客户端请求号，用于幂等控制',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  template_code VARCHAR(64) NOT NULL COMMENT '优惠券模板编码',
  coupon_no VARCHAR(64) NULL COMMENT '兑换成功后的用户优惠券编号',
  used_points BIGINT NOT NULL DEFAULT 0 COMMENT '消耗积分',
  exchange_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '兑换状态：SUCCESS成功，FAILED失败',
  fail_reason VARCHAR(255) NULL COMMENT '失败原因',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_points_exchange_no (exchange_no),
  UNIQUE KEY uk_points_exchange_user_request (user_id, request_no),
  KEY idx_points_exchange_user_time (user_id, create_time),
  KEY idx_points_exchange_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

INSERT INTO points_task_config (
  task_code,
  task_name,
  reward_points,
  repeatable,
  reward_limit_type,
  daily_limit,
  enabled,
  description
)
SELECT
  'COMPLETE_PROFILE',
  '完善个人资料',
  20,
  0,
  'ONCE',
  1,
  1,
  '用户首次完善个人资料后奖励积分'
WHERE NOT EXISTS (
  SELECT 1 FROM points_task_config WHERE task_code = 'COMPLETE_PROFILE'
);

INSERT INTO points_task_config (
  task_code,
  task_name,
  reward_points,
  repeatable,
  reward_limit_type,
  daily_limit,
  enabled,
  description
)
SELECT
  'DAILY_SIGN_IN',
  '每日签到',
  10,
  1,
  'DAILY',
  1,
  1,
  '用户每日签到奖励积分'
WHERE NOT EXISTS (
  SELECT 1 FROM points_task_config WHERE task_code = 'DAILY_SIGN_IN'
);

-- 优惠券模板扣库存示例：业务代码执行时需要带 stock > 0 条件，避免超卖。
-- UPDATE coupon_template
-- SET stock = stock - 1
-- WHERE template_code = ? AND enabled = 1 AND stock > 0;
