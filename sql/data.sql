﻿-- ============================================================
-- 健身房分布式项目 测试数据脚本
-- 可重复执行: 使用 INSERT ... ON DUPLICATE KEY UPDATE 保护
-- 数据分布与 sql/init.sql 的 8 库、Nacos 数据源完全一致
-- BCrypt 加密密码(原文123456): $2a$10$ngnCVMaR6aqcRHKIbBgJI./yKAwAGfOyAErgIxV.e1bInpQXWOHMa
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- 一、gym_user 库：用户 / 教练
-- ============================================================
USE gym_user;

-- 1) t_user (3个账号: admin ADMIN / coach COACH / user1 USER)
INSERT INTO t_user (id, username, password, nickname, phone, email, avatar, role, status, balance, point, create_time, update_time, create_by, update_by) VALUES
(1, 'admin', '$2a$10$ngnCVMaR6aqcRHKIbBgJI./yKAwAGfOyAErgIxV.e1bInpQXWOHMa', '超级管理员', '13800000001', 'admin@gym.com', '', 'ADMIN', 1, 10000.00, 0, NOW(), NOW(), NULL, NULL),
(2, 'coach', '$2a$10$ngnCVMaR6aqcRHKIbBgJI./yKAwAGfOyAErgIxV.e1bInpQXWOHMa', '王教练', '13800000002', 'coach@gym.com', '', 'COACH', 1, 2000.00, 0, NOW(), NOW(), NULL, NULL),
(3, 'user1', '$2a$10$ngnCVMaR6aqcRHKIbBgJI./yKAwAGfOyAErgIxV.e1bInpQXWOHMa', '健身会员小明', '13800000003', 'user1@gym.com', '', 'USER', 1, 5000.00, 0, NOW(), NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE
    password    = VALUES(password),
    nickname    = VALUES(nickname),
    phone       = VALUES(phone),
    email       = VALUES(email),
    role        = VALUES(role),
    status      = VALUES(status),
    balance     = VALUES(balance),
    update_time = NOW();

-- 2) t_coach (1条)
INSERT INTO t_coach (id, user_id, real_name, title, specialty, introduction, avatar, rating, years, create_time, update_time, create_by, update_by) VALUES
(1, 2, '王教练', '高级私教', '增肌/减脂', '10年从业经验，国家健美一级运动员，擅长增肌减脂塑形，帮助上千学员成功蜕变。', '', 5.0, 10, NOW(), NOW(), 1, 1)
ON DUPLICATE KEY UPDATE
    real_name    = VALUES(real_name),
    title        = VALUES(title),
    specialty    = VALUES(specialty),
    introduction = VALUES(introduction),
    rating       = VALUES(rating),
    years        = VALUES(years),
    update_time  = NOW();


-- ============================================================
-- 二、gym_goods 库：商品
-- ============================================================
USE gym_goods;

-- 3) t_goods_category (3类)
INSERT INTO t_goods_category (id, name, pid, sort, create_time) VALUES
(1, '运动补剂', 0, 1, NOW()),
(2, '健身器材', 0, 2, NOW()),
(3, '运动服饰', 0, 3, NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    pid  = VALUES(pid),
    sort = VALUES(sort);

-- 4) t_goods (7件)
INSERT INTO t_goods (id, category_id, name, cover, images, stock, price, description, sales, status, version, create_time) VALUES
(1, 1, '乳清蛋白粉 5磅', '', NULL, 100, 299.00, '进口高品质乳清蛋白，每勺含24g优质蛋白，增肌必备。',              0, 1, 0, NOW()),
(2, 1, '增肌粉 6磅',   '', NULL,  80, 199.00, '碳水+蛋白黄金配比，助力瘦体质快速增重增肌。',                0, 1, 0, NOW()),
(3, 1, '蛋白棒 可可味 12支装', '', NULL, 500, 29.90, '随身携带的高蛋白零食，每根20g蛋白，低糖低脂。',     0, 1, 0, NOW()),
(4, 2, '健身训练手套',   '', NULL, 200,  79.00, '透气防滑耐磨，保护手掌，适合器械训练和引体向上。',         0, 1, 0, NOW()),
(5, 3, 'TPE加厚瑜伽垫 10mm', '', NULL, 150, 49.00, '环保TPE材质，防滑加厚，附赠绑带和网袋。',              0, 1, 0, NOW()),
(6, 1, '蛋白威化饼干 24片装', '', NULL, 300, 39.90, '高蛋白威化，每片8g蛋白，训练后加餐首选。',            0, 1, 0, NOW()),
(7, 1, '蛋白能量饮 500ml*6瓶', '', NULL, 120, 59.90, '即饮型乳清蛋白饮，每瓶25g蛋白，开盖即饮。',           0, 1, 0, NOW())
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id),
    name        = VALUES(name),
    stock       = VALUES(stock),
    price       = VALUES(price),
    description = VALUES(description),
    status      = VALUES(status),
    version     = VALUES(version);


-- ============================================================
-- 三、gym_course 库：课程 / 排期
-- ============================================================
USE gym_course;

-- 5) t_course (3条)
INSERT INTO t_course (id, name, coach_id, category, capacity, duration, price, description, status, create_time) VALUES
(1, '动感单车',     1, '有氧',     20, 45,   39.00, '节奏感十足的室内单车有氧训练，高效燃脂，提升心肺功能。', 1, NOW()),
(2, '哈他瑜伽',     1, '柔韧',     15, 60,   49.00, '传统哈他瑜伽，舒缓身心，提升身体柔韧度与核心力量。',   1, NOW()),
(3, '一对一私教增肌', 1, '力量',   1,  60,  299.00, '资深教练一对一指导，科学增肌，定制化训练计划。',       1, NOW())
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    coach_id    = VALUES(coach_id),
    category    = VALUES(category),
    capacity    = VALUES(capacity),
    duration    = VALUES(duration),
    price       = VALUES(price),
    description = VALUES(description),
    status      = VALUES(status);

-- 6) t_course_schedule (每条课程 1 个 3 天后的排期)
INSERT INTO t_course_schedule (id, course_id, class_date, start_time, end_time, capacity, enrollment_count, status, create_time) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '19:00:00', '19:45:00', 20, 0, 1, NOW()),
(2, 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', '11:00:00', 15, 0, 1, NOW()),
(3, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00:00', '16:00:00', 1,  0, 1, NOW())
ON DUPLICATE KEY UPDATE
    start_time      = VALUES(start_time),
    end_time        = VALUES(end_time),
    capacity        = VALUES(capacity),
    enrollment_count= VALUES(enrollment_count),
    status          = VALUES(status);


-- ============================================================
-- 四、gym_member 库：会员卡
-- ============================================================
USE gym_member;

-- 7) t_member_card (4条)
INSERT INTO t_member_card (id, name, type, price, days, times, description, create_time) VALUES
(1, '会员月卡',  1,  199.00, 30,  NULL, '自开通起30天内有效，不限次入场健身。',                        NOW()),
(2, '会员季卡',  3,  499.00, 90,  NULL, '自开通起90天内有效，不限次入场健身，赠送体测一次。',          NOW()),
(3, '会员年卡',  12, 1599.00, 365, NULL, '自开通起365天内有效，不限次入场健身，赠送私教课程2节。',     NOW()),
(4, '次卡(50次)', 0,  999.00, NULL, 50,  '50次入场机会，有效期自开通起365天，适合非高频健身爱好者。', NOW())
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    type        = VALUES(type),
    price       = VALUES(price),
    days        = VALUES(days),
    times       = VALUES(times),
    description = VALUES(description);


-- ============================================================
-- 五、gym_food 库：餐饮
-- ============================================================
USE gym_food;

-- 8) t_food (3条)
INSERT INTO t_food (id, name, image, price, calories, protein, carbs, fat, description, status, create_time) VALUES
(1, '鸡胸肉沙拉 300g', '', 29.90, 280.0, 35.0, 12.0, 8.0,  '鲜嫩鸡胸肉搭配新鲜蔬菜，低脂高蛋白，增肌减脂好选择。',          1, NOW()),
(2, '牛肉减脂餐',     '', 59.90, 450.0, 42.0, 30.0, 15.0, '精选瘦牛肉配糙米饭西兰花，营养均衡饱腹感强。',                  1, NOW()),
(3, '蛋白藜麦饭',     '', 45.90, 380.0, 28.0, 45.0, 10.0, '超级谷物藜麦+双蛋白组合，低脂低GI，健身餐首选。',               1, NOW())
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    price       = VALUES(price),
    calories    = VALUES(calories),
    protein     = VALUES(protein),
    carbs       = VALUES(carbs),
    fat         = VALUES(fat),
    description = VALUES(description),
    status      = VALUES(status);


-- ============================================================
-- 六、gym_seckill 库：秒杀活动 / 商品
-- ============================================================
USE gym_seckill;

-- 9) t_seckill_activity (2个)
INSERT INTO t_seckill_activity (id, name, cover, start_time, end_time, status, total_stock, limit_count, create_time) VALUES
(1, '618秒杀-蛋白粉5折',     '', DATE_ADD(NOW(), INTERVAL 5 MINUTE),  DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 MINUTE), INTERVAL 2 HOUR),  0, 100, 1, NOW()),
(2, '超级会员日-年卡3折秒杀', '', DATE_ADD(NOW(), INTERVAL 1 DAY),     DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 1 DAY),      0,  50, 1, NOW())
ON DUPLICATE KEY UPDATE
    name        = VALUES(name),
    start_time  = VALUES(start_time),
    end_time    = VALUES(end_time),
    total_stock = VALUES(total_stock),
    limit_count = VALUES(limit_count);

-- 10) t_seckill_goods (活动①关联乳清蛋白粉 goods_id=1，秒杀价149)
INSERT INTO t_seckill_goods (id, activity_id, goods_id, seckill_price, stock, sold, sort, create_time) VALUES
(1, 1, 1, 149.00, 100, 0, 1, NOW())
ON DUPLICATE KEY UPDATE
    seckill_price = VALUES(seckill_price),
    stock         = VALUES(stock),
    sold          = VALUES(sold),
    sort          = VALUES(sort);


SET FOREIGN_KEY_CHECKS = 1;
