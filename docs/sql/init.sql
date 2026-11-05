DROP DATABASE IF EXISTS `flight_db`;
CREATE DATABASE `flight_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `flight_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- (1) t_user 用户表
CREATE TABLE `t_user` (
                          `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                          `username` VARCHAR(50) NOT NULL COMMENT '登录名',
                          `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密存储',
                          `nickname` VARCHAR(50) COMMENT '昵称',
                          `phone` VARCHAR(20) COMMENT '手机号',
                          `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
                          `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
                          `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          UNIQUE KEY `uk_username` (`username`)
) COMMENT '用户表';

INSERT INTO `t_user` VALUES
                         (1, 'admin', '$2a$10$bw9pqR/I2EF/4xZV.jLXEex3YSBu0vIfhywBjijhzxrT5ztRgt4Hq', '系统管理员', '13800138000', 'ADMIN', 1, NOW(), NOW()),
                         (2, 'testuser', '$2a$10$bw9pqR/I2EF/4xZV.jLXEex3YSBu0vIfhywBjijhzxrT5ztRgt4Hq', '张三', '110101199505058888', 'USER', 1, NOW(), NOW());

-- (2) t_airport 机场/城市表
CREATE TABLE `t_airport` (
                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                             `airport_code` VARCHAR(10) NOT NULL COMMENT '机场三字码, 如 PEK',
                             `airport_name` VARCHAR(50) NOT NULL,
                             `city_name` VARCHAR(30) NOT NULL COMMENT '城市名, 查询按城市走',
                             `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY `uk_code` (`airport_code`),
                             KEY `idx_city` (`city_name`)
) COMMENT '机场/城市表';

INSERT INTO `t_airport` VALUES
                            (1, 'PEK', '北京首都国际机场', '北京', NOW()),
                            (2, 'PKX', '北京大兴国际机场', '北京', NOW()),
                            (3, 'SHA', '上海虹桥国际机场', '上海', NOW()),
                            (4, 'PVG', '上海浦东国际机场', '上海', NOW()),
                            (5, 'CAN', '广州白云国际机场', '广州', NOW()),
                            (6, 'CTU', '成都双流国际机场', '成都', NOW());

-- (3) t_flight 航班表
CREATE TABLE `t_flight` (
                            `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                            `flight_no` VARCHAR(20) NOT NULL COMMENT '航班号, 如 CA1234',
                            `airline` VARCHAR(30) COMMENT '航空公司',
                            `from_airport_id` BIGINT NOT NULL COMMENT '起飞机场ID',
                            `to_airport_id` BIGINT NOT NULL COMMENT '到达机场ID',
                            `from_city` VARCHAR(30) NOT NULL COMMENT '出发城市 (冗余字段, 加速查询)',
                            `to_city` VARCHAR(30) NOT NULL COMMENT '到达城市',
                            `depart_date` DATE NOT NULL COMMENT '航班日期',
                            `depart_time` TIME NOT NULL COMMENT '起飞时刻',
                            `arrive_time` TIME NOT NULL COMMENT '到达时刻',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1在售 0停售',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            KEY `idx_route_date` (`from_city`, `to_city`, `depart_date`)
) COMMENT '航班表';

INSERT INTO `t_flight` VALUES
                           (1001, 'CA1234', '中国国际航空', 1, 3, '北京', '上海', '2026-11-05', '08:30:00', '10:45:00', 1, NOW(), NOW()),
                           (1002, 'MU5108', '东方航空', 2, 3, '北京', '上海', '2026-11-05', '11:00:00', '13:20:00', 1, NOW(), NOW()),
                           (1003, 'CZ3901', '南方航空', 2, 4, '北京', '上海', '2026-11-05', '14:15:00', '16:35:00', 1, NOW(), NOW()),
                           (1004, 'HU7601', '海南航空', 1, 3, '北京', '上海', '2026-11-05', '17:30:00', '19:50:00', 1, NOW(), NOW()),
                           (1005, 'MU5101', '东方航空', 3, 1, '上海', '北京', '2026-11-05', '09:00:00', '11:20:00', 1, NOW(), NOW()),
                           (1006, 'CA1832', '中国国际航空', 3, 1, '上海', '北京', '2026-11-05', '15:30:00', '17:45:00', 1, NOW(), NOW());

-- (4) t_flight_seat 航班舱位库存表 (防超卖核心表)
CREATE TABLE `t_flight_seat` (
                                 `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 `flight_id` BIGINT NOT NULL COMMENT '航班ID',
                                 `seat_type` VARCHAR(20) NOT NULL COMMENT '舱位: Y经济舱/J公务舱/F头等舱',
                                 `price` INT NOT NULL COMMENT '票价(分)',
                                 `total_count` INT NOT NULL COMMENT '总座位数',
                                 `remain_count` INT NOT NULL COMMENT '可售余票数',
                                 `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 KEY `idx_flight_seat` (`flight_id`, `seat_type`)
) COMMENT '航班舱位库存表';

INSERT INTO `t_flight_seat` VALUES
                                (1, 1001, 'Y', 85000, 120, 5, 0, NOW(), NOW()),   -- CA1234 经济舱余票 5 张，用于 200 并发压测
                                (2, 1001, 'J', 188000, 24, 8, 0, NOW(), NOW()),
                                (3, 1001, 'F', 320000, 8, 2, 0, NOW(), NOW()),
                                (4, 1002, 'Y', 79000, 150, 85, 0, NOW(), NOW()),
                                (5, 1002, 'J', 165000, 30, 12, 0, NOW(), NOW()),
                                (6, 1003, 'Y', 82000, 200, 60, 0, NOW(), NOW()),
                                (7, 1003, 'J', 178000, 32, 16, 0, NOW(), NOW()),
                                (8, 1004, 'Y', 76000, 120, 42, 0, NOW(), NOW()),
                                (9, 1005, 'Y', 78000, 150, 90, 0, NOW(), NOW()),
                                (10, 1006, 'Y', 86000, 200, 75, 0, NOW(), NOW());

-- (5) t_order 订单表
CREATE TABLE `t_order` (
                           `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                           `order_no` VARCHAR(32) NOT NULL COMMENT '订单号(业务主键)',
                           `user_id` BIGINT NOT NULL,
                           `flight_id` BIGINT NOT NULL,
                           `seat_id` BIGINT NOT NULL,
                           `passenger_name` VARCHAR(50) NOT NULL COMMENT '乘机人',
                           `id_card` VARCHAR(20) NOT NULL COMMENT '证件号',
                           `amount` INT NOT NULL COMMENT '支付金额(分)',
                           `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已出票 3已取消',
                           `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                           `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           UNIQUE KEY `uk_order_no` (`order_no`),
                           KEY `idx_user_id` (`user_id`)
) COMMENT '订单表';

-- (6) t_payment 支付流水表 (幂等凭证)
CREATE TABLE `t_payment` (
                             `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                             `pay_no` VARCHAR(32) NOT NULL COMMENT '支付流水号(幂等键)',
                             `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
                             `user_id` BIGINT NOT NULL,
                             `amount` INT NOT NULL COMMENT '支付金额(分)',
                             `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败',
                             `pay_channel` VARCHAR(20) DEFAULT 'MOCK' COMMENT '支付渠道',
                             `pay_time` DATETIME COMMENT '支付完成时间',
                             `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY `uk_pay_no` (`pay_no`),
                             UNIQUE KEY `uk_order_no` (`order_no`)
) COMMENT '支付流水表';

-- (7) t_ticket 电子客票表
CREATE TABLE `t_ticket` (
                            `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                            `ticket_no` VARCHAR(32) NOT NULL COMMENT '票号',
                            `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
                            `flight_id` BIGINT NOT NULL,
                            `passenger_name` VARCHAR(50) NOT NULL,
                            `id_card` VARCHAR(20) NOT NULL,
                            `seat_no` VARCHAR(10) COMMENT '座位号',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1有效 0作废',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE KEY `uk_ticket_no` (`ticket_no`),
                            KEY `idx_order_no` (`order_no`)
) COMMENT '电子客票表';

SET FOREIGN_KEY_CHECKS = 1;