-- 优惠券库
-- create database if not exists tcc_cpn;
-- use tcc_cpn;
create table if not exists `cpn_coupon` (
  `id` int(11) unsigned not null auto_increment comment '编号',
  `money` int(11) unsigned not null default '0' comment '金额',
  `uid` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '用户编号',
  `biz_order_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '业务订单编号',
  `status` tinyint(4) unsigned not null default '0' comment '状态： 1 未使用 2 尝试使用 3 已使用',
  primary key (`id`)
)comment='红包表' collate='utf8_general_ci' engine = innodb;