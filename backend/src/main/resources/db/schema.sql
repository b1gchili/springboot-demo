CREATE DATABASE IF NOT EXISTS student_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE student_demo;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id VARCHAR(64) NOT NULL UNIQUE COMMENT '业务用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '登录密码。演示项目暂存明文，生产环境必须存哈希',
  phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  display_name VARCHAR(50) NOT NULL COMMENT '展示名称',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  last_login_time DATETIME NULL COMMENT '最后登录时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_sys_user_phone (phone),
  INDEX idx_sys_user_last_login_time (last_login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

SET @column_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'last_login_time'
);
SET @sql = IF(
  @column_exists = 0,
  'ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME NULL COMMENT ''最后登录时间''',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND INDEX_NAME = 'idx_sys_user_last_login_time'
);
SET @sql = IF(
  @index_exists = 0,
  'CREATE INDEX idx_sys_user_last_login_time ON sys_user(last_login_time)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id VARCHAR(64) NOT NULL COMMENT '业务用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  display_name VARCHAR(50) NOT NULL COMMENT '姓名',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  login_ip VARCHAR(64) NOT NULL COMMENT '登录IP',
  login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  INDEX idx_login_log_user_id (user_id),
  INDEX idx_login_log_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS sms_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  code VARCHAR(6) NOT NULL COMMENT '验证码',
  expires_at DATETIME NOT NULL COMMENT '过期时间',
  used TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用：1已使用，0未使用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_sms_code_phone_time (phone, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';

CREATE TABLE IF NOT EXISTS student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  student_id VARCHAR(32) NOT NULL UNIQUE COMMENT '学号',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  age INT NOT NULL COMMENT '年龄',
  gender VARCHAR(10) NOT NULL COMMENT '性别',
  description VARCHAR(200) NULL COMMENT '描述',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_student_name (name),
  INDEX idx_student_age (age),
  INDEX idx_student_gender (gender),
  INDEX idx_student_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

INSERT INTO sys_user (user_id, username, password, phone, display_name)
SELECT '1', 'admin', '123456', '13800138000', '管理员'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO student (student_id, name, age, gender, description)
SELECT 'STU20260101000001', '张三', 20, '男', '计算机科学专业学生'
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_id = 'STU20260101000001');

INSERT INTO student (student_id, name, age, gender, description)
SELECT 'STU20260101000002', '李四', 21, '女', '数学专业学生'
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_id = 'STU20260101000002');

INSERT INTO student (student_id, name, age, gender, description)
SELECT 'STU20260101000003', '王五', 19, '男', '物理专业学生'
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_id = 'STU20260101000003');

INSERT INTO student (student_id, name, age, gender, description)
SELECT 'STU20260101000004', '赵六', 22, '女', '化学专业学生'
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_id = 'STU20260101000004');

INSERT INTO student (student_id, name, age, gender, description)
SELECT 'STU20260101000005', '钱七', 20, '男', '生物专业学生'
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_id = 'STU20260101000005');
