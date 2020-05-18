-- 数据库表
drop table if exists marmot_database;
create table marmot_database
(
    id        bigint auto_increment comment 'ID',
    name      varchar(1024) not null comment '名称',
    db_type   varchar(16)   not null comment '数据库类型',
    url       varchar(512)  not null comment '地址',
    user_name varchar(16)   not null comment '密码',
    password  varchar(128) default null comment '密码',
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uq_data_volume_name UNIQUE KEY (name)
) COMMENT ='数据库';

-- 数据集表
drop table if exists marmot_data_volume;
create table marmot_data_volume
(
    volume_id    bigint auto_increment comment 'ID',
    volume_name  varchar(1024) not null comment '名称',
    volume_code  varchar(512)  not null comment '编码',
    volume_type  varchar(16)   not null comment '类型',
    db_name      varchar(128)  not null comment '数据源名称',
    sql_script   text          not null comment 'sql脚本',
    volume_limit long          not null comment '数据集大小',
    content      varchar(512)  null comment '描述',
    CONSTRAINT `PRIMARY` PRIMARY KEY (volume_id),
    CONSTRAINT uq_data_volume_name UNIQUE KEY (volume_name),
    CONSTRAINT uq_data_volume_code UNIQUE KEY (volume_code)
) COMMENT ='数据集';
-- 字段数据集表
drop table if exists marmot_column_volume;
create table marmot_column_volume
(
    volume_id         bigint auto_increment comment 'ID',
    volume_name       varchar(1024) not null comment '名称',
    volume_code       varchar(512)  not null comment '数据集编码',
    volume_type       varchar(16)   not null comment '类型',
    column_code       varchar(512)  not null comment '字段编码',
    db_name           varchar(128)  not null comment '数据源名称',
    column_value_code varchar(512)  not null comment '字段值编码',
    column_show_code  varchar(512)  not null comment '字段展示编码',
    script            text          not null comment '脚本',
    content           varchar(512)  null comment '描述',
    CONSTRAINT `PRIMARY` PRIMARY KEY (volume_id),
    CONSTRAINT uq_column_volume_c_code UNIQUE KEY (column_code),
    CONSTRAINT uq_column_volume_name UNIQUE KEY (volume_name),
    CONSTRAINT uq_column_volume_code UNIQUE KEY (volume_code)
) COMMENT ='字段数据集';

-- 数据集字段表
drop table if exists marmot_data_column;
create table marmot_data_column
(
    column_id     bigint auto_increment comment '字段ID',
    volume_code   varchar(512) not null comment '数据集编码',
    column_order  int          not null comment '字段顺序',
    column_name   varchar(512) null comment '字段名称',
    column_code   varchar(512) not null comment '字段编码',
    column_type   varchar(18)  not null comment '字段类型',
    column_label  varchar(32) default null comment '字段标记',
    screen_column varchar(512) not null comment '筛选字段',
    column_filter boolean      not null comment '字段过滤',
    column_hidden boolean      not null comment '字段隐藏',
    column_escape boolean      not null comment '字段转义',
    column_mask   boolean      not null comment '字段掩码',
    data_format   varchar(512) null comment '数据格式',
    unit_value    double       null comment '单位换算',
    content       varchar(512) null comment '描述',
    CONSTRAINT `PRIMARY` PRIMARY KEY (column_id),
    CONSTRAINT uq_data_volume_column_code UNIQUE KEY (volume_code, column_code),
    CONSTRAINT uq_data_volume_column_name UNIQUE KEY (volume_code, column_name)
) COMMENT ='数据集字段';


-- 仪表盘
drop table if exists marmot_dash_board;
create table marmot_dash_board
(
    board_id     bigint auto_increment comment 'ID',
    volume_code  varchar(512) not null comment '数据集编码',
    board_name   varchar(128) not null comment '仪表盘名称',
    board_type   varchar(16)  not null comment '仪表盘类型',
    founder_id   varchar(512) not null comment '创建人ID',
    founder_name varchar(512) not null comment '创建人名称',
    content      varchar(512) null comment '描述',
    CONSTRAINT `PRIMARY` PRIMARY KEY (board_id),
    CONSTRAINT uq_dash_board_name UNIQUE KEY (founder_id, board_name),
    KEY dash_board_volume_code_index (volume_code),
    KEY dash_board_type_index (board_type)
) COMMENT ='数据仪表盘';

-- 图表设计
drop table if exists marmot_graphic_design;
create table marmot_graphic_design
(
    graphic_id   bigint auto_increment comment 'ID',
    graphic_name varchar(128) not null comment '图表名称',
    board_id     bigint       not null comment '仪表盘ID',
    graphic_type varchar(16)  not null comment '图表类型',
    graphic      text         not null comment '图表',
    CONSTRAINT uq_graphic_design_id UNIQUE KEY (graphic_id),
    CONSTRAINT uq_graphic_design_name UNIQUE KEY (board_id, graphic_name)
) COMMENT ='图表设计表';

DROP TABLE IF EXISTS `marmot_graphic_download`;
CREATE TABLE marmot_graphic_download
(
    download_id  bigint(20) auto_increment NOT NULL COMMENT '序列id',
    founder_id   varchar(64)               NOT NULL COMMENT '创建人ID',
    file_name    varchar(512)              NOT NULL COMMENT '文件名',
    volume_code  varchar(512) DEFAULT NULL COMMENT '数据集编码',
    graphic_name varchar(128) DEFAULT NULL COMMENT '图表名称',
    graphic_type varchar(16)  DEFAULT NULL COMMENT '图表类型',
    graphic      text         DEFAULT NULL COMMENT '图表',
    file_url     varchar(1024)             NOT NULL COMMENT '文件地址',
    download_url varchar(1024)             NOT NULL COMMENT '下载地址',
    status       varchar(32)               NOT NULL COMMENT '状态',
    memo         varchar(1024)             NULL COMMENT '说明',
    CONSTRAINT `PRIMARY` PRIMARY KEY (download_id),
    KEY no_graphic_download_f_n (founder_id, file_name)
) COMMENT ='图表下载';

DROP TABLE IF EXISTS `marmot_statistical_model`;
CREATE TABLE marmot_statistical_model
(
    model_id          bigint(20) auto_increment NOT NULL COMMENT '序列id',
    volume_code       varchar(512)              not null comment '数据集编码',
    model_name        varchar(128)              NOT NULL COMMENT '模型名称',
    db_name           varchar(128)              not null comment '数据源名称',
    fetch_sql         varchar(1024)             NOT NULL COMMENT '数据抓取sql',
    fetch_step        long                      NOT NULL COMMENT '数据抓取步长',
    running           boolean                   NOT null NULL COMMENT '模型是否运行中',
    calculated        boolean                   not null comment '模型是否已完成计算',
    offsetExpr        varchar(128)              not null comment '模型时间偏移量',
    time_column       varchar(512)              NOT NULL COMMENT '时间粒度字段',
    window_length     int(5)                    NOT NULL COMMENT '统计窗口长度',
    window_type       varchar(32)               NOT NULL COMMENT '统计窗口类型',
    window_unit       varchar(32)               NOT NULL COMMENT '统计庄口粒度',
    aggregate_columns text                      NOT NULL COMMENT '统计聚合字段',
    condition_columns text                               default NULL COMMENT '统计条件字段',
    group_columns     text                               default NULL COMMENT '统计分组字段',
    direction_columns text                               default NULL COMMENT '统计方向字段',
    memo              varchar(1024)                      default null COMMENT '说明',
    raw_update_time   timestamp                 not null default current_timestamp on update current_timestamp COMMENT '更新时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (model_id),
    UNIQUE KEY statistical_model_name (model_name),
    KEY statistical_volume_code (volume_code)
) COMMENT ='统计模型定义';

DROP TABLE IF EXISTS `marmot_statistical_task`;
CREATE TABLE marmot_statistical_task
(
    task_id         bigint(20) auto_increment NOT NULL COMMENT '序列id',
    model_name      varchar(128)              NOT NULL COMMENT '模型名称',
    scanned         boolean                   NOT NULL COMMENT '是否已扫描',
    start_index     long                      NOT NULL COMMENT '开始角标',
    end_index       long                      NOT NULL COMMENT '结束角标',
    raw_update_time timestamp                 not null default current_timestamp on update current_timestamp COMMENT '更新时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (`task_id`),
    UNIQUE KEY `statistical_task_model_name` (`model_name`)
) COMMENT ='统计模型统计任务';

DROP TABLE IF EXISTS `marmot_statistical_data`;
CREATE TABLE marmot_statistical_data
(
    data_id         bigint(20) auto_increment NOT NULL COMMENT '序列id',
    model_name      varchar(128)              NOT NULL COMMENT '模型名称',
    row_key         varchar(64)               NOT NULL COMMENT '统计数据行标识',
    aggregate_data  text                      NOT NULL COMMENT '聚合数据',
    group_columns   text                               default NULL COMMENT '分组字段',
    time_unit       datetime                           default null comment '时间粒度',
    raw_update_time timestamp                 not null default current_timestamp on update current_timestamp COMMENT '更新时间',
    CONSTRAINT `PRIMARY` PRIMARY KEY (data_id),
    UNIQUE KEY `statistical_data_row_key` (`row_key`) USING BTREE,
    KEY `statistical_data_time_unit` (`time_unit`) USING BTREE,
    KEY `statistical_data_m_r` (`model_name`, `row_key`) USING BTREE
) COMMENT ='统计模型聚合数据';

DROP TABLE IF EXISTS `marmot_statistical_distinct`;
CREATE TABLE marmot_statistical_distinct
(
    distinct_id     bigint(20) auto_increment NOT NULL COMMENT '序列id',
    model_name      varchar(128)              NOT NULL COMMENT '模型名称',
    row_key         varchar(64)               NOT NULL COMMENT '统计数据行标识',
    distinct_column varchar(512)              NOT NULL COMMENT '去重字段',
    distinct_data   text                      NOT NULL COMMENT '去重数据',
    CONSTRAINT `PRIMARY` PRIMARY KEY (distinct_id),
    UNIQUE KEY `statistical_distinct_r_d` (`row_key`, `distinct_column`) USING BTREE,
    KEY `statistical_distinct_m_d_r` (`model_name`, `distinct_column`, `row_key`) USING BTREE
) COMMENT ='统计模型去重数据';