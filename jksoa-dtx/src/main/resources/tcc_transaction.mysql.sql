CREATE TABLE `tcc_transaction` (
  `id` bigint(20) unsigned NOT NULL COMMENT '事务编号',
  `domain` varchar(100) DEFAULT NULL COMMENT '业务领域',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '父事务编号',
  `status` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '事务状态: 1 尝试中 2 确认中 3 取消中',
  `participants` varbinary(8000) DEFAULT NULL COMMENT '参与者',
  `retry_count` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '重试次数, 即事务恢复调用的次数',
  `created` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `updated` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  `version` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '版本',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
