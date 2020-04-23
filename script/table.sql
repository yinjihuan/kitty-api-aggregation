create table api_metadata(
    id bigint(20) not null auto_increment,
    api_name varchar(20) not null comment 'API名称',
    metadata text not null comment '元数据',
    status tinyint(1) not null DEFAULT 1 comment '状态 (0:禁用 1:正常)',
    add_time datetime not null DEFAULT CURRENT_TIMESTAMP comment '添加时间',
    update_time datetime not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 comment='API元数据表';