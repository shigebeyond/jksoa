-- 支付库
-- create database if not exists tcc_pay;
-- use tcc_pay;
create table if not exists `pay_account` (
  `uid` int(11) unsigned not null auto_increment comment '用户编号',
  `balance` int(11) unsigned not null default '0' comment '余额, 单位:分',
  primary key (`uid`)
)comment='支付账号表' collate='utf8_general_ci' engine = innodb;

create table if not exists `pay_order` (
  `id` int(11) unsigned not null auto_increment comment '编号',
  `from_uid` int(11) unsigned NOT NULL DEFAULT '0' comment '转出用户编号',
  `from_uname` varchar(200) NOT NULL DEFAULT '' COMMENT '转出用户名',
  `to_uid` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '转入用户编号',
  `to_uname` varchar(200) NOT NULL DEFAULT '' COMMENT '转入用户名',
  `money` int(11) unsigned not null default '0' comment '金额',
  `biz_order_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '业务订单编号',
  `status` tinyint(4) unsigned not null default '0' comment '订单状态： 1 尝试中 2 已确认 3 已取消',
  primary key (`id`),
  unique key `uk_biz_order_id` (`biz_order_id`)
)comment='支付订单表' collate='utf8_general_ci' engine = innodb;

