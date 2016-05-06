DROP ALL OBJECTS;

CREATE TABLE `user` (
  `id`    INT(11)      NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(128) NOT NULL,
  `phone` VARCHAR(16)           DEFAULT NULL,
  UNIQUE KEY email (`email`),
  PRIMARY KEY (`id`)
);
INSERT INTO user VALUES (0, 'system', 'system');

CREATE TABLE `account` (
  `id`      INT(20)        NOT NULL AUTO_INCREMENT,
  `amount`  DECIMAL(12, 2) NOT NULL,
  `type`    VARCHAR(255)   NOT NULL DEFAULT 'CURRENT',
  `user_id` INT(20)                 DEFAULT NULL,
  `version` INT(20)                 DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `user_fk1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
);
INSERT INTO account VALUES (0, 0, 'SYSTEM_DEPOSIT', 0, 0);

CREATE TABLE `transaction` (
  `id`             INT(20)        NOT NULL AUTO_INCREMENT,
  `corr_id`        INT(20),
  `amount`         DECIMAL(12, 2) NOT NULL,
  `date`           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_account_id` INT(20)        NOT NULL,
  `dst_account_id` INT(20)        NOT NULL,
  `state`          VARCHAR(31)    NOT NULL,
  `comment`        VARCHAR(255)            DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `account_fk1` FOREIGN KEY (`src_account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `account_fk2` FOREIGN KEY (`dst_account_id`) REFERENCES `account` (`id`)
)

