create table if not exists exec_file
(
    id                 int auto_increment,
    name               varchar               not null comment '脚本名称',
    link               varchar               not null comment '文件路径',
    create_timestamp   numeric(13) default 0 not null comment '创建时间',
    last_use_timestamp numeric(13) default 0 not null comment '最后使用时间',
    constraint pk_exec_file primary key (id)
);
comment on table exec_file is '可执行程序';

delete from func_setting where func_code = 'JdkVersion';
insert into func_setting (func_code, func_name, enable_flag)
values ('JdkVersion', 'JdkVersion', true);

create table if not exists env_var
(
    id               int auto_increment,
    name             varchar               not null comment '变量名称',
    val              varchar               not null comment '变量值',
    desc             varchar               null comment '描述',
    create_timestamp numeric(13) default 0 not null comment '创建时间',
    mod_timestamp    numeric(13) default 0 not null comment '最后修改时间',
    constraint pk_env_var primary key (id),
    unique (name, val)
);
comment on table rtp_linklist is '环境变量';