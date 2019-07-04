#app
CREATE TABLE `app` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#service
CREATE TABLE `service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `app_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_appId` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#trace
CREATE TABLE `trace` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `duration` int(11) DEFAULT NULL,
  `service_id` int(11) NOT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#span
CREATE TABLE `span` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `trace_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `service_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#annotation
CREATE TABLE `annotation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(128) DEFAULT NULL,
  `value` varchar(2048) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `port` int(11) DEFAULT 0,
  `timestamp` bigint(20) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `span_id` bigint(128) DEFAULT NULL,
  `trace_id` bigint(128) DEFAULT NULL,
  `service_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
