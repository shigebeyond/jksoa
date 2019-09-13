-- 订单库
-- create database if not exists tcc_ord;
-- use tcc_ord;
create table if not exists `ord_product` (
    `id` int(11) unsigned not null auto_increment comment '商品编号',
    `seller_uid` int(11) unsigned not null default '0' comment '卖家编号',
    `seller_uname` varchar(50) not null default '' comment '卖家名称',

    `name` varchar(255) not null default '' comment '商品名',
    `price` int(11) unsigned not null default '0.00' comment '售价, 单位:分',
    `quantity` int(11) unsigned null default '0' comment '库存',
    `remain_quantity` int(11) unsigned null default '0' comment '剩余库存',
    primary key (`id`)
)comment='商品表' collate='utf8_general_ci' engine=innodb;

create table if not exists `ord_order` (
  `id` bigint(20) unsigned not null auto_increment comment '编号',
  `seller_uid` int(11) unsigned not null default '0' comment '卖家编号',
  `seller_uname` varchar(50) not null default '' comment '卖家名称',

  `buyer_uid` int(11) unsigned not null default '0' comment '买家编号',
  `buyer_uname` varchar(50) not null default '' comment '买家名称',

  `coupon_id` int(11) unsigned not null default '0' comment '红包id',
  `coupon_money` int(11) unsigned not null default '0' comment '红包支付的金额, 单位:分',
  `pay_money` int(11) unsigned not null default '0' comment '要支付的金额, 单位:分',
  `total_money` int(11) unsigned not null default '0' comment '总金额, 单位:分',

  `status` tinyint(4) unsigned not null default '0' comment '订单状态： 0 草稿 1 待支付 2 已支付 3 支付失败',
  `created` int(11) unsigned not null default '0' comment '创建时间',
  `pay_time` int(11) unsigned not null default '0' comment '支付时间',

  primary key (`id`)
)comment='订单表' collate='utf8_general_ci' engine = innodb;

create table if not exists `ord_order_item` (
  `id` int(11) unsigned not null auto_increment comment '编号',
  `order_id` bigint(20) unsigned not null default '0' comment '订单编号',
  `product_id` int(11) unsigned not null default '0' comment '商品编号',
  `product_name` varchar(255) not null default '' comment '商品名',
  `product_quantity` int(11) unsigned not null default '0' comment '数量',
  `product_price` int(11) unsigned not null default '0' comment '价格, 单位:分',
  primary key (`id`)
)comment='订单项目表' collate='utf8_general_ci' engine = innodb;