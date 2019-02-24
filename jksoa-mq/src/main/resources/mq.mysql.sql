-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '消息编号',
  `topic` varchar(255) NOT NULL COMMENT '主题',
  `group` varchar(255) NOT NULL COMMENT '分组',
  `data` text DEFAULT NULL COMMENT '数据',
  `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '状态：0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)',
  `tryTimes` int(11) NOT NULL DEFAULT '0' COMMENT '尝试次数',
  `created` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `remark` text DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_pull` (`topic`,`group`,`status`, `created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息';