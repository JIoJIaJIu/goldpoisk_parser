CREATE TABLE IF NOT EXISTS `goldpoisk_entity` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `article` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `material` varchar(255) DEFAULT NULL,
    `category` varchar(255) DEFAULT NULL,
    `weight` varchar(255) DEFAULT NULL,
    `url` varchar(255) DEFAULT NULL,
    `type` varchar(255) DEFAULT NULL,
    `proba` varchar(255) DEFAULT NULL,
    `price` varchar(255) DEFAULT NULL,
    `description` text,
    `old_price` varchar(255) DEFAULT NULL,
    `discount` varchar(255) DEFAULT NULL,
    `count` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

CREATE TABLE IF NOT EXISTS `goldpoisk_update_entity` {
    `id` int(11) NOT NULL PRIMARY KEY,
    `article` varchar(255) NOT NULL,
    `field_name` varchar(255) NOT NULL,
    `field_value` varchar(255) NOT NULL
}

CREATE TABLE IF NOT EXISTS `goldpoisk_entity_images` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `article` varchar(255) NOT NULL,
  `image` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=24 ;


CREATE TABLE IF NOT EXISTS `goldpoisk_kamni` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `article` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `weight` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=4 ;
