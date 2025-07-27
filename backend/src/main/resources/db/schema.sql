-- 库房库存管理系统 - 本地开发环境数据库初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS warehouse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE warehouse_db;

-- 设置时区
SET time_zone = '+8:00';
