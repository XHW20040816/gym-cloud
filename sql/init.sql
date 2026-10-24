-- ============================================================
-- 健身房分布式项目 初始化建库建表脚本 (MySQL 8.0+)
-- 字符集: utf8mb4   引擎: InnoDB
--
-- 【唯一口径】本脚本创建的 8 个库与 Nacos 中各服务数据源保持一致：
--   gym_user(用户/教练) gym_goods(商品) gym_order(订单/支付流水外)
--   gym_payment(支付)   gym_course(课程) gym_food(餐饮)
--   gym_member(会员卡)  gym_seckill(秒杀)
-- 每个库均含 Seata AT 模式所需的 undo_log 表。
--
-- 可重复执行：建库 IF NOT EXISTS + 建表 DROP TABLE IF EXISTS
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、gym_user 库（用户 / 教练 域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_user;

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    username        VARCHAR(32)     NOT NULL                    COMMENT '用户名',
    password        VARCHAR(128)    NOT NULL                    COMMENT '密码(BCrypt)',
    nickname        VARCHAR(64)     DEFAULT NULL                COMMENT '昵称',
    phone           VARCHAR(20)     DEFAULT NULL                COMMENT '手机号',
    email           VARCHAR(64)     DEFAULT NULL                COMMENT '邮箱',
    avatar          VARCHAR(256)    DEFAULT NULL                COMMENT '头像URL',
    role            VARCHAR(16)     NOT NULL DEFAULT 'USER'     COMMENT 'USER/COACH/ADMIN',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1正常 0禁用',
    balance         DECIMAL(12,2)   NOT NULL DEFAULT 0.00       COMMENT '账户余额',
    point           INT             NOT NULL DEFAULT 0          COMMENT '积分',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS t_coach;
CREATE TABLE t_coach (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    user_id         BIGINT          NOT NULL                    COMMENT '关联用户ID',
    real_name       VARCHAR(32)     DEFAULT NULL                COMMENT '真实姓名',
    title           VARCHAR(64)     DEFAULT NULL                COMMENT '职称',
    specialty       VARCHAR(128)    DEFAULT NULL                COMMENT '专长',
    introduction    TEXT            DEFAULT NULL                COMMENT '个人介绍',
    avatar          VARCHAR(256)    DEFAULT NULL                COMMENT '头像',
    rating          DECIMAL(2,1)    NOT NULL DEFAULT 5.0        COMMENT '评分',
    years           INT             NOT NULL DEFAULT 0          COMMENT '从业年限',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 二、gym_goods 库（商品域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_goods DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_goods;

DROP TABLE IF EXISTS t_goods_category;
CREATE TABLE t_goods_category (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    name            VARCHAR(64)     NOT NULL                    COMMENT '分类名称',
    pid             BIGINT          NOT NULL DEFAULT 0          COMMENT '父分类ID(0表示顶级)',
    sort            INT             NOT NULL DEFAULT 0          COMMENT '排序',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

DROP TABLE IF EXISTS t_goods;
CREATE TABLE t_goods (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    category_id     BIGINT          NOT NULL                    COMMENT '分类ID',
    name            VARCHAR(128)    NOT NULL                    COMMENT '商品名称',
    cover           VARCHAR(256)    DEFAULT NULL                COMMENT '封面图',
    images          TEXT            DEFAULT NULL                COMMENT '多图(JSON)',
    stock           INT             NOT NULL DEFAULT 0          COMMENT '库存',
    price           DECIMAL(10,2)   NOT NULL                    COMMENT '价格',
    description     TEXT            DEFAULT NULL                COMMENT '商品描述',
    sales           INT             NOT NULL DEFAULT 0          COMMENT '销量',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1上架 0下架',
    version         INT             NOT NULL DEFAULT 0          COMMENT '乐观锁版本号',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 三、gym_order 库（订单域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_order;

DROP TABLE IF EXISTS t_orders;
CREATE TABLE t_orders (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    order_no        VARCHAR(64)     NOT NULL                    COMMENT '订单号',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    total_amount    DECIMAL(12,2)   NOT NULL                    COMMENT '订单总金额',
    pay_amount      DECIMAL(12,2)   NOT NULL                    COMMENT '实付金额',
    status          TINYINT         NOT NULL DEFAULT 0          COMMENT '0待付款 1已付款 2已发货 3已完成 4已取消 5已退款',
    pay_type        TINYINT         DEFAULT NULL                COMMENT '支付方式: 1微信 2支付宝 3余额',
    pay_time        DATETIME        DEFAULT NULL                COMMENT '支付时间',
    delivery_info   VARCHAR(512)    DEFAULT NULL                COMMENT '配送信息(JSON)',
    remark          VARCHAR(512)    DEFAULT NULL                COMMENT '备注',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time),
    KEY idx_status (status),
    KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    order_id        VARCHAR(64)     NOT NULL                    COMMENT '订单号',
    goods_id        BIGINT          NOT NULL                    COMMENT '商品ID',
    goods_name      VARCHAR(128)    NOT NULL                    COMMENT '商品名称快照',
    goods_cover     VARCHAR(256)    DEFAULT NULL                COMMENT '商品封面快照',
    price           DECIMAL(10,2)   NOT NULL                    COMMENT '单价快照',
    quantity        INT             NOT NULL                    COMMENT '数量',
    subtotal        DECIMAL(12,2)   NOT NULL                    COMMENT '小计',
    item_type       TINYINT         NOT NULL DEFAULT 1          COMMENT '1商品 2课程 3会员卡 4餐饮',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 四、gym_payment 库（支付域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_payment DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_payment;

DROP TABLE IF EXISTS t_payment;
CREATE TABLE t_payment (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    payment_no      VARCHAR(64)     NOT NULL                    COMMENT '支付流水号',
    order_no        VARCHAR(64)     NOT NULL                    COMMENT '业务订单号',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    amount          DECIMAL(12,2)   NOT NULL                    COMMENT '支付金额',
    pay_type        TINYINT         NOT NULL                    COMMENT '支付方式: 1微信 2支付宝 3余额',
    status          TINYINT         NOT NULL DEFAULT 0          COMMENT '0待支付 1支付成功 2支付失败 3已关闭 4已退款',
    transaction_id  VARCHAR(128)    DEFAULT NULL                COMMENT '第三方交易号',
    pay_time        DATETIME        DEFAULT NULL                COMMENT '支付时间',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    UNIQUE KEY uk_order_pay (order_no, pay_type),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 五、gym_course 库（课程 / 排期 / 预约 域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_course DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_course;

DROP TABLE IF EXISTS t_course;
CREATE TABLE t_course (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    name            VARCHAR(64)     NOT NULL                    COMMENT '课程名称',
    coach_id        BIGINT          NOT NULL                    COMMENT '教练ID',
    category        VARCHAR(32)     DEFAULT NULL                COMMENT '分类',
    capacity        INT             NOT NULL                    COMMENT '容量(人数)',
    duration        INT             DEFAULT NULL                COMMENT '时长(分钟)',
    price           DECIMAL(10,2)   NOT NULL                    COMMENT '价格',
    description     TEXT            DEFAULT NULL                COMMENT '描述',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1启用 0禁用',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_coach_id (coach_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

DROP TABLE IF EXISTS t_course_schedule;
CREATE TABLE t_course_schedule (
    id                  BIGINT          NOT NULL                COMMENT '主键ID',
    course_id           BIGINT          NOT NULL                COMMENT '课程ID',
    class_date          DATE            NOT NULL                COMMENT '上课日期',
    start_time          TIME            DEFAULT NULL            COMMENT '开始时间',
    end_time            TIME            DEFAULT NULL            COMMENT '结束时间',
    capacity            INT             NOT NULL                COMMENT '容量',
    enrollment_count    INT             NOT NULL DEFAULT 0      COMMENT '已报名人数',
    status              TINYINT         NOT NULL DEFAULT 1      COMMENT '1正常 0取消',
    create_time         DATETIME        DEFAULT NULL            COMMENT '创建时间',
    update_time         DATETIME        DEFAULT NULL            COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL            COMMENT '创建人ID',
    update_by           BIGINT          DEFAULT NULL            COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_class_date (class_date),
    KEY idx_course_date (course_id, class_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程排期表';

DROP TABLE IF EXISTS t_course_enrollment;
CREATE TABLE t_course_enrollment (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    schedule_id     BIGINT          NOT NULL                    COMMENT '排期ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    enroll_time     DATETIME        DEFAULT NULL                COMMENT '报名时间',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1已报名 2已取消 3已完成',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_user (schedule_id, user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程报名表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 六、gym_food 库（餐饮域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_food DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_food;

DROP TABLE IF EXISTS t_food;
CREATE TABLE t_food (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    name            VARCHAR(128)    NOT NULL                    COMMENT '名称',
    image           VARCHAR(256)    DEFAULT NULL                COMMENT '图片',
    price           DECIMAL(10,2)   NOT NULL                    COMMENT '价格',
    calories        DOUBLE          NOT NULL DEFAULT 0          COMMENT '卡路里(kcal)',
    protein         DOUBLE          NOT NULL DEFAULT 0          COMMENT '蛋白质(g)',
    carbs           DOUBLE          NOT NULL DEFAULT 0          COMMENT '碳水(g)',
    fat             DOUBLE          NOT NULL DEFAULT 0          COMMENT '脂肪(g)',
    description     TEXT            DEFAULT NULL                COMMENT '描述',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1上架 0下架',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐饮表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 七、gym_member 库（会员卡域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_member DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_member;

DROP TABLE IF EXISTS t_member_card;
CREATE TABLE t_member_card (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    name            VARCHAR(64)     NOT NULL                    COMMENT '卡名称',
    type            TINYINT         NOT NULL                    COMMENT '类型: 0次卡 1月卡 3季卡 12年卡',
    price           DECIMAL(10,2)   NOT NULL                    COMMENT '价格',
    days            INT             DEFAULT NULL                COMMENT '有效期天数(次卡为NULL)',
    times           INT             DEFAULT NULL                COMMENT '次卡次数(非次卡为NULL)',
    description     TEXT            DEFAULT NULL                COMMENT '描述',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员卡表';

DROP TABLE IF EXISTS t_user_member;
CREATE TABLE t_user_member (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    card_id         BIGINT          NOT NULL                    COMMENT '会员卡ID',
    start_time      DATETIME        DEFAULT NULL                COMMENT '开始时间',
    end_time        DATETIME        DEFAULT NULL                COMMENT '结束时间',
    remain_times    INT             DEFAULT NULL                COMMENT '剩余次数',
    status          TINYINT         NOT NULL DEFAULT 1          COMMENT '1正常 2过期 0冻结',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_card (user_id, card_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员卡关联表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';


-- ============================================================
-- 八、gym_seckill 库（秒杀域）
-- ============================================================
CREATE DATABASE IF NOT EXISTS gym_seckill DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE gym_seckill;

DROP TABLE IF EXISTS t_seckill_activity;
CREATE TABLE t_seckill_activity (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    name            VARCHAR(128)    NOT NULL                    COMMENT '活动名称',
    cover           VARCHAR(256)    DEFAULT NULL                COMMENT '活动封面',
    start_time      DATETIME        NOT NULL                    COMMENT '开始时间',
    end_time        DATETIME        NOT NULL                    COMMENT '结束时间',
    status          TINYINT         NOT NULL DEFAULT 0          COMMENT '0未开始 1进行中 2已结束',
    total_stock     INT             NOT NULL DEFAULT 0          COMMENT '活动总库存',
    limit_count     INT             NOT NULL DEFAULT 1          COMMENT '每人限购数',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

DROP TABLE IF EXISTS t_seckill_goods;
CREATE TABLE t_seckill_goods (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    activity_id     BIGINT          NOT NULL                    COMMENT '活动ID',
    goods_id        BIGINT          NOT NULL                    COMMENT '商品ID',
    seckill_price   DECIMAL(10,2)   NOT NULL                    COMMENT '秒杀价',
    stock           INT             NOT NULL DEFAULT 0          COMMENT '秒杀库存',
    sold            INT             NOT NULL DEFAULT 0          COMMENT '已售数量',
    sort            INT             NOT NULL DEFAULT 0          COMMENT '排序',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_activity (activity_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 每人每商品限购 1 件：唯一键与 Redis Lua 限购口径（seckill:user:{goodsId}）保持一致
DROP TABLE IF EXISTS t_seckill_order;
CREATE TABLE t_seckill_order (
    id              BIGINT          NOT NULL                    COMMENT '主键ID',
    order_no        VARCHAR(64)     NOT NULL                    COMMENT '秒杀订单号',
    activity_id     BIGINT          NOT NULL                    COMMENT '活动ID',
    goods_id        BIGINT          NOT NULL                    COMMENT '商品ID',
    user_id         BIGINT          NOT NULL                    COMMENT '用户ID',
    amount          DECIMAL(10,2)   NOT NULL                    COMMENT '订单金额',
    status          TINYINT         NOT NULL DEFAULT 0          COMMENT '0待付款 1已付款 2已取消 3已完成',
    create_time     DATETIME        DEFAULT NULL                COMMENT '创建时间',
    update_time     DATETIME        DEFAULT NULL                COMMENT '更新时间',
    create_by       BIGINT          DEFAULT NULL                COMMENT '创建人ID',
    update_by       BIGINT          DEFAULT NULL                COMMENT '更新人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_user_goods (user_id, goods_id),
    KEY idx_order (order_no),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                    COMMENT 'branch transaction id',
    xid             VARCHAR(100)    NOT NULL                    COMMENT 'global transaction id',
    context         VARCHAR(128)    NOT NULL                    COMMENT 'undo_log context, such as serialization',
    rollback_info   LONGBLOB        NOT NULL                    COMMENT 'rollback info',
    log_status      INT             NOT NULL                    COMMENT '0: normal, 1: defense',
    log_created     DATETIME        NOT NULL                    COMMENT 'create datetime',
    log_modified    DATETIME        NOT NULL                    COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

SET FOREIGN_KEY_CHECKS = 1;
