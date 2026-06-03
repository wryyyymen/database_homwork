/*
 Navicat Premium Dump SQL

 Source Server         : aaaa
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : hospital_register_system

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 03/06/2026 12:34:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `admin_id` int NOT NULL AUTO_INCREMENT COMMENT '管理员编号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '管理员真实姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'admin' COMMENT '角色：admin/super_admin',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `dept_id` int NOT NULL AUTO_INCREMENT COMMENT '科室编号',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '科室名称',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '科室位置',
  PRIMARY KEY (`dept_id`) USING BTREE,
  UNIQUE INDEX `dept_name`(`dept_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '科室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `doctor_id` int NOT NULL AUTO_INCREMENT COMMENT '医生编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '医生姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别：男/女',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称：主任医师/副主任医师/主治医师等',
  `dept_id` int NOT NULL COMMENT '所属科室编号',
  `fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '挂号费',
  PRIMARY KEY (`doctor_id`) USING BTREE,
  INDEX `fk_doctor_department`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `fk_doctor_department` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for medical_record
-- ----------------------------
DROP TABLE IF EXISTS `medical_record`;
CREATE TABLE `medical_record`  (
  `record_id` int NOT NULL AUTO_INCREMENT COMMENT '就诊记录编号',
  `reg_id` int NOT NULL COMMENT '挂号编号',
  `symptom` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '症状描述',
  `diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '诊断结果',
  `prescription` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '处方',
  `advice` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '医嘱',
  `visit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '就诊时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE INDEX `reg_id`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_medical_record_registration` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '就诊记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `patient_id` int NOT NULL AUTO_INCREMENT COMMENT '患者编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '患者姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别：男/女',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `id_card` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`patient_id`) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '患者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `payment_id` int NOT NULL AUTO_INCREMENT COMMENT '缴费编号',
  `reg_id` int NOT NULL COMMENT '挂号编号',
  `amount` decimal(10, 2) NOT NULL COMMENT '缴费金额',
  `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式：现金/微信/支付宝/银行卡/医保',
  `pay_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '未支付' COMMENT '支付状态：未支付/已支付/已退款',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`payment_id`) USING BTREE,
  INDEX `fk_payment_registration`(`reg_id` ASC) USING BTREE,
  CONSTRAINT `fk_payment_registration` FOREIGN KEY (`reg_id`) REFERENCES `registration` (`reg_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_payment_amount` CHECK (`amount` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '缴费表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for registration
-- ----------------------------
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration`  (
  `reg_id` int NOT NULL AUTO_INCREMENT COMMENT '挂号编号',
  `patient_id` int NOT NULL COMMENT '患者编号',
  `schedule_id` int NOT NULL COMMENT '排班编号',
  `reg_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '已挂号' COMMENT '状态：已挂号/已取消/已就诊/已退号',
  `fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '挂号费用',
  PRIMARY KEY (`reg_id`) USING BTREE,
  UNIQUE INDEX `uq_patient_schedule`(`patient_id` ASC, `schedule_id` ASC) USING BTREE,
  INDEX `fk_registration_schedule`(`schedule_id` ASC) USING BTREE,
  CONSTRAINT `fk_registration_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`patient_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_registration_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '挂号记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for schedule
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule`  (
  `schedule_id` int NOT NULL AUTO_INCREMENT COMMENT '排班编号',
  `doctor_id` int NOT NULL COMMENT '医生编号',
  `work_date` date NOT NULL COMMENT '出诊日期',
  `time_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '时间段：上午/下午/晚上',
  `total_num` int NOT NULL COMMENT '总号源数量',
  `remain_num` int NOT NULL COMMENT '剩余号源数量',
  PRIMARY KEY (`schedule_id`) USING BTREE,
  UNIQUE INDEX `uq_schedule_doctor_time`(`doctor_id` ASC, `work_date` ASC, `time_period` ASC) USING BTREE,
  CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_schedule_remain_num` CHECK ((`remain_num` >= 0) and (`remain_num` <= `total_num`)),
  CONSTRAINT `chk_schedule_total_num` CHECK (`total_num` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '医生排班表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
