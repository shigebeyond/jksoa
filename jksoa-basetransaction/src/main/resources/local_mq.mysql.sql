CREATE TABLE IF NOT EXISTS `local_mq` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '消息编号',
  `biz_type` VARCHAR(30) NOT NULL COMMENT '业务类型',
  `biz_id` VARCHAR(255) NOT NULL COMMENT '业务主体编号',
  `topic` VARCHAR(255) NOT NULL COMMENT '消息主题',
  `msg` BLOB NOT NULL COMMENT '消息内容',
  `created` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间戳',
  `try_times` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='本地消息表';