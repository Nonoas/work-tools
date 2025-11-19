create table if not exists page_params
(
    PARAM_CODE         VARCHAR               not null,
    ID                 INT auto_increment,
    PARAM_VAL          VARCHAR,
    LAST_USE_TIMESTAMP NUMERIC(13) default 0 not null,
    constraint filepathpaneform_pk
        primary key (ID)
);

alter table page_params
    add column if not exists last_use_timestamp numeric(13) default 0 not null;

-- 系统参数表
create table if not exists sys_param
(
    param_code varchar not null comment '参数编码',
    param_val  varchar not null comment '参数值',
    constraint pk_sys_param primary key (param_code)
);
comment on table sys_param is '系统参数表';

create table if not exists rtp_linklist
(
    id                 int auto_increment,
    name               varchar               not null comment '文件名',
    link               varchar               not null comment '文件路径',
    last_use_timestamp numeric(13) default 0 not null comment '最后使用时间',
    constraint pk_rtp_linklist primary key (id)
);
comment on table rtp_linklist is '最近链接表';

-- 功能配置表
create table if not exists func_setting
(
    func_code   varchar not null comment '菜单编码',
    func_name   varchar not null comment '菜单名称',
    enable_flag boolean not null default true comment '启用标志',
    constraint pk_func_setting primary key (func_code)
);
comment on table func_setting is '功能配置表';

truncate table func_setting;
insert into func_setting (func_code, func_name, enable_flag)
values ('ExecFile', '快速启动', true);
insert into func_setting (func_code, func_name, enable_flag)
values ('TextReplace', '文本替换', true);
insert into func_setting (func_code, func_name, enable_flag)
values ('RecentTouch', '最近', true);
insert into func_setting (func_code, func_name, enable_flag)
values ('SQLExtraction', 'SQL提取', true);
insert into func_setting (func_code, func_name, enable_flag)
values ('FileEncode', '文件编码', true);
insert into func_setting (func_code, func_name, enable_flag)
values ('TodoList', '待办事项', true);


insert into func_setting(func_code, func_name, enable_flag)
select 'ClassExtract', 'class提取', true
from dual
where not exists(select 1 from func_setting where func_code = 'ClassExtract');


insert into func_setting(func_code, func_name, enable_flag)
select 'SQLTransfer', 'sql转换', true
from dual
where not exists(select 1 from func_setting where func_code = 'SQLTransfer');



