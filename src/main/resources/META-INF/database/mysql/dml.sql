-- 数据库表
drop table if exists marmot_database;
create table marmot_database
(
    id        bigint(20) auto_increment comment 'id',
    name      varchar(1024) not null comment '名称',
    db_type   varchar(16)   not null comment '数据库类型',
    url       varchar(512)  not null comment '地址',
    user_name varchar(16)   not null comment '密码',
    password  varchar(128) default null comment '密码',
    constraint `primary` primary key (id),
    constraint uq_data_volume_name unique key (name)
) comment ='数据库';

-- 数据集表
drop table if exists marmot_data_volume;
create table marmot_data_volume
(
    volume_id    bigint(20) auto_increment comment 'id',
    volume_name  varchar(1024) not null comment '名称',
    volume_code  varchar(512)  not null comment '编码',
    volume_type  varchar(16)   not null comment '类型',
    db_name      varchar(128)  not null comment '数据源名称',
    sql_script   text          not null comment 'sql脚本',
    volume_limit long          not null comment '数据集大小',
    content      varchar(512)  null comment '描述',
    constraint `primary` primary key (volume_id),
    constraint uq_data_volume_name unique key (volume_name),
    constraint uq_data_volume_code unique key (volume_code)
) comment ='数据集';
-- 字段数据集表
drop table if exists marmot_column_volume;
create table marmot_column_volume
(
    volume_id         bigint(20) auto_increment comment 'id',
    volume_name       varchar(1024) not null comment '名称',
    volume_code       varchar(512)  not null comment '数据集编码',
    volume_type       varchar(16)   not null comment '类型',
    column_code       varchar(512)  not null comment '字段编码',
    db_name           varchar(128)  not null comment '数据源名称',
    column_value_code varchar(512)  not null comment '字段值编码',
    column_show_code  varchar(512)  not null comment '字段展示编码',
    script            text          not null comment '脚本',
    content           varchar(512)  null comment '描述',
    constraint `primary` primary key (volume_id),
    constraint uq_column_volume_c_code unique key (column_code),
    constraint uq_column_volume_name unique key (volume_name),
    constraint uq_column_volume_code unique key (volume_code)
) comment ='字段数据集';

-- 数据集字段表
drop table if exists marmot_data_column;
create table marmot_data_column
(
    column_id     bigint(20) auto_increment comment '字段id',
    volume_code   varchar(512) not null comment '数据集编码',
    column_order  int(10)      not null comment '字段顺序',
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
    constraint `primary` primary key (column_id),
    constraint uq_data_volume_column_code unique key (volume_code, column_code),
    constraint uq_data_volume_column_name unique key (volume_code, column_name)
) comment ='数据集字段';


-- 仪表盘
drop table if exists marmot_dash_board;
create table marmot_dash_board
(
    board_id     bigint(20) auto_increment comment 'id',
    volume_code  varchar(512) not null comment '数据集编码',
    board_name   varchar(128) not null comment '仪表盘名称',
    board_type   varchar(16)  not null comment '仪表盘类型',
    founder_id   varchar(512) not null comment '创建人id',
    founder_name varchar(512) not null comment '创建人名称',
    content      varchar(512) null comment '描述',
    constraint `primary` primary key (board_id),
    constraint uq_dash_board_name unique key (founder_id, board_name),
    key dash_board_volume_code_index (volume_code),
    key dash_board_type_index (board_type)
) comment ='数据仪表盘';

-- 图表设计
drop table if exists marmot_graphic_design;
create table marmot_graphic_design
(
    graphic_id   bigint(20) auto_increment comment 'id',
    graphic_name varchar(128) not null comment '图表名称',
    board_id     bigint(20)   not null comment '仪表盘id',
    graphic_type varchar(16)  not null comment '图表类型',
    graphic      text         not null comment '图表',
    constraint uq_graphic_design_id unique key (graphic_id),
    constraint uq_graphic_design_name unique key (board_id, graphic_name)
) comment ='图表设计表';

drop table if exists `marmot_graphic_download`;
create table marmot_graphic_download
(
    download_id  bigint(20) auto_increment not null comment '序列id',
    founder_id   varchar(64)               not null comment '创建人id',
    file_name    varchar(512)              not null comment '文件名',
    volume_code  varchar(512) default null comment '数据集编码',
    graphic_name varchar(128) default null comment '图表名称',
    graphic_type varchar(16)  default null comment '图表类型',
    graphic      text         default null comment '图表',
    file_url     varchar(1024)             not null comment '文件地址',
    download_url varchar(1024)             not null comment '下载地址',
    status       varchar(32)               not null comment '状态',
    memo         varchar(1024)             null comment '说明',
    constraint `primary` primary key (download_id),
    key no_graphic_download_f_n (founder_id, file_name)
) comment ='图表下载';

drop table if exists `marmot_statistical_model`;
create table marmot_statistical_model
(
    model_id          bigint(20) auto_increment not null comment '序列id',
    volume_code       varchar(512)              not null comment '数据集编码',
    model_name        varchar(128)              not null comment '模型名称',
    db_name           varchar(128)              not null comment '数据源名称',
    fetch_sql         varchar(1024)             not null comment '数据抓取sql',
    fetch_step        long                      not null comment '数据抓取步长',
    running           boolean                   not null null comment '模型是否运行中',
    calculated        boolean                   not null comment '模型是否已完成计算',
    offset_expr       varchar(128)              not null comment '模型时间偏移量',
    time_column       varchar(512)              not null comment '时间粒度字段',
    window_length     int(5)                    not null comment '统计窗口长度',
    window_type       varchar(32)               not null comment '统计窗口类型',
    window_unit       varchar(32)               not null comment '统计庄口粒度',
    aggregate_columns text                      not null comment '统计聚合字段',
    condition_columns text                               default null comment '统计条件字段',
    group_columns     text                               default null comment '统计分组字段',
    direction_columns text                               default null comment '统计方向字段',
    memo              varchar(1024)                      default null comment '说明',
    raw_update_time   timestamp                 not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint `primary` primary key (model_id),
    unique key statistical_model_name (model_name),
    key statistical_volume_code (volume_code)
) comment ='统计模型定义';

drop table if exists `marmot_statistical_task`;
create table marmot_statistical_task
(
    task_id         bigint(20) auto_increment not null comment '序列id',
    model_name      varchar(128)              not null comment '模型名称',
    scanned         boolean                   not null comment '是否已扫描',
    start_index     long                      not null comment '开始角标',
    end_index       long                      not null comment '结束角标',
    raw_update_time timestamp                 not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint `primary` primary key (`task_id`),
    unique key `statistical_task_model_name` (`model_name`)
) comment ='统计模型统计任务';

drop table if exists `marmot_statistical_data`;
create table marmot_statistical_data
(
    data_id         bigint(20) auto_increment not null comment '序列id',
    model_name      varchar(128)              not null comment '模型名称',
    row_key         varchar(64)               not null comment '统计数据行标识',
    aggregate_data  text                      not null comment '聚合数据',
    group_columns   text                               default null comment '分组字段',
    time_unit       datetime                           default null comment '时间粒度',
    raw_update_time timestamp                 not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint `primary` primary key (data_id),
    unique key `statistical_data_row_key` (`row_key`) using btree,
    key `statistical_data_time_unit` (`time_unit`) using btree,
    key `statistical_data_m_r` (`model_name`, `row_key`) using btree
) comment ='统计模型聚合数据';

drop table if exists `marmot_statistical_distinct`;
create table marmot_statistical_distinct
(
    distinct_id     bigint(20) auto_increment not null comment '序列id',
    model_name      varchar(128)              not null comment '模型名称',
    row_key         varchar(64)               not null comment '统计数据行标识',
    distinct_column varchar(512)              not null comment '去重字段',
    distinct_data   text                      not null comment '去重数据',
    constraint `primary` primary key (distinct_id),
    unique key `statistical_distinct_r_d` (`row_key`, `distinct_column`) using btree,
    key `statistical_distinct_m_d_r` (`model_name`, `distinct_column`, `row_key`) using btree
) comment ='统计模型去重数据';