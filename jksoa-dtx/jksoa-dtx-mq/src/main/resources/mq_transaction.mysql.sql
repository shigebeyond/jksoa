CREATE TABLE IF NOT EXISTS `mq_transaction` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '消息编号',
  `topic` VARCHAR(255) NOT NULL COMMENT '消息主题',
  `msg` BLOB NOT NULL COMMENT '消息内容',
  `biz_type` VARCHAR(30) NOT NULL COMMENT '业务类型',
  `biz_id` VARCHAR(255) NOT NULL COMMENT '业务主体编号',
  `created` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间戳, 单位秒',
  `next_send_time` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '下次发送时间戳, 单位秒',
  `try_count` INT unsigned NOT NULL DEFAULT 0 COMMENT '重试次数',
  PRIMARY KEY (`id`),
  KEY `idx_next_send_time` (`next_send_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息事务表';