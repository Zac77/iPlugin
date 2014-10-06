SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `zac_ntxapp` ;
CREATE SCHEMA IF NOT EXISTS `zac_ntxapp` DEFAULT CHARACTER SET utf8 ;
USE `zac_ntxapp` ;

-- -----------------------------------------------------
-- Table `zac_ntxapp`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `zac_ntxapp`.`user` ;

CREATE TABLE IF NOT EXISTS `zac_ntxapp`.`user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(127) NULL,
  `auth` VARCHAR(127) NULL COMMENT 'Base64-encoded string, with the following format: username:password',
  `invitation_code` VARCHAR(127) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `zac_ntxapp`.`plugin`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `zac_ntxapp`.`plugin` ;

CREATE TABLE IF NOT EXISTS `zac_ntxapp`.`plugin` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `board_device_id` VARCHAR(127) NOT NULL,
  `sensor_id` VARCHAR(127) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `zac_ntxapp`.`device`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `zac_ntxapp`.`device` ;

CREATE TABLE IF NOT EXISTS `zac_ntxapp`.`device` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(127) NOT NULL,
  `brand` VARCHAR(127) NOT NULL,
  `model` VARCHAR(127) NOT NULL,
  `consume` INT UNSIGNED NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `zac_ntxapp`.`comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `zac_ntxapp`.`comment` ;

CREATE TABLE IF NOT EXISTS `zac_ntxapp`.`comment` (
  `user_id` INT UNSIGNED NOT NULL,
  `device_id` INT UNSIGNED NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  PRIMARY KEY (`user_id`, `device_id`),
  INDEX `fk_comment_device1_idx` (`device_id` ASC),
  CONSTRAINT `fk_comment_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `zac_ntxapp`.`user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_device1`
    FOREIGN KEY (`device_id`)
    REFERENCES `zac_ntxapp`.`device` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `zac_ntxapp`.`user_has_plugin`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `zac_ntxapp`.`user_has_plugin` ;

CREATE TABLE IF NOT EXISTS `zac_ntxapp`.`user_has_plugin` (
  `user_id` INT UNSIGNED NOT NULL,
  `plugin_id` INT UNSIGNED NOT NULL,
  `device_id` INT UNSIGNED NOT NULL,
  `nickname` VARCHAR(127) NOT NULL,
  PRIMARY KEY (`user_id`, `plugin_id`, `device_id`),
  INDEX `fk_user_has_plugin_plugin1_idx` (`plugin_id` ASC),
  INDEX `fk_user_has_plugin_user1_idx` (`user_id` ASC),
  INDEX `fk_user_has_plugin_device1_idx` (`device_id` ASC),
  CONSTRAINT `fk_user_has_plugin_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `zac_ntxapp`.`user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_has_plugin_plugin1`
    FOREIGN KEY (`plugin_id`)
    REFERENCES `zac_ntxapp`.`plugin` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_has_plugin_device1`
    FOREIGN KEY (`device_id`)
    REFERENCES `zac_ntxapp`.`device` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `zac_ntxapp`.`user`
-- -----------------------------------------------------
START TRANSACTION;
USE `zac_ntxapp`;
INSERT INTO `zac_ntxapp`.`user` (`id`, `email`, `auth`, `invitation_code`) VALUES (1, 'user@test.com', 'dXNlckB0ZXN0LmNvbTp0ZXN0dXNlcg%3D%3D', '0');
INSERT INTO `zac_ntxapp`.`user` (`id`, `email`, `auth`, `invitation_code`) VALUES (2, 'bob@gmail.com', '0', '0');

COMMIT;


-- -----------------------------------------------------
-- Data for table `zac_ntxapp`.`plugin`
-- -----------------------------------------------------
START TRANSACTION;
USE `zac_ntxapp`;
INSERT INTO `zac_ntxapp`.`plugin` (`id`, `board_device_id`, `sensor_id`) VALUES (1, '8c7e5475-3d2e-11e4-90ea-005056aa04c5', 'ae26.measure.value');
INSERT INTO `zac_ntxapp`.`plugin` (`id`, `board_device_id`, `sensor_id`) VALUES (2, '8c7e5475-3d2e-11e4-90ea-005056aa04c5', '32bc.measure.value');
INSERT INTO `zac_ntxapp`.`plugin` (`id`, `board_device_id`, `sensor_id`) VALUES (3, '8c7e5475-3d2e-11e4-90ea-005056aa04c5', 'af9e.measure.value');

COMMIT;


-- -----------------------------------------------------
-- Data for table `zac_ntxapp`.`device`
-- -----------------------------------------------------
START TRANSACTION;
USE `zac_ntxapp`;
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (1, 'AirConditioner', 'Honeywell', 'MN12CES', 800);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (2, 'AirConditioner', 'Honeywell', 'MM14CCS', 1000);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (3, 'AirConditioner', 'Honeywell', 'MF08CESWW', 1200);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (4, 'AirConditioner', 'LG', 'LP0814WNR ', 900);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (5, 'AirConditioner', 'LG', 'LP1414SHR', 1100);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (6, 'AirConditioner', 'LG', 'LP1311BXR', 1300);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (7, 'AirConditioner', 'Whynter ', 'ARC-14S', 1400);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (8, 'AirConditioner', 'Whynter ', 'ARC-10WB ', 1600);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (9, 'AirConditioner', 'Whynter ', 'ARC-131GD', 2000);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (10, 'Refrigerator', 'LG', 'LMX30995ST', 500);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (11, 'Refrigerator', 'LG', 'LFX31945 ', 600);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (12, 'Refrigerator', 'LG', 'LFX33975ST', 700);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (13, 'Refrigerator', 'Samsung', 'RF28HFEDBSR', 550);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (14, 'Refrigerator', 'Samsung', 'RF31FMESBSR', 650);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (15, 'Refrigerator', 'Samsung', 'RF32FMQDBSR', 750);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (16, 'Refrigerator', 'GE', 'CFE29TSDSS', 800);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (17, 'Refrigerator', 'GE', 'CZS25TSESS', 900);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (18, 'Refrigerator', 'GE', 'PZS25KSESS', 1000);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (19, 'Microwave', 'Electrolux', 'EI30SM55JS', 400);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (20, 'Microwave', 'Electrolux', 'E30MO75HPS', 600);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (21, 'Microwave', 'Electrolux', 'EI30BM55HS', 650);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (22, 'Microwave', 'GE', 'PNM9196SFSS', 700);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (23, 'Microwave', 'GE', 'CSA1201RSS ', 750);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (24, 'Microwave', 'GE', 'JNM7196SFSS ', 800);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (25, 'Microwave', 'BOSCH', 'HMD8451UC', 850);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (26, 'Microwave', 'BOSCH', 'HMC87151UC', 900);
INSERT INTO `zac_ntxapp`.`device` (`id`, `type`, `brand`, `model`, `consume`) VALUES (27, 'Microwave', 'BOSCH', 'HMC80151UC', 950);

COMMIT;


-- -----------------------------------------------------
-- Data for table `zac_ntxapp`.`comment`
-- -----------------------------------------------------
START TRANSACTION;
USE `zac_ntxapp`;
INSERT INTO `zac_ntxapp`.`comment` (`user_id`, `device_id`, `content`) VALUES (2, 1, 'This device is great to use!');

COMMIT;


-- -----------------------------------------------------
-- Data for table `zac_ntxapp`.`user_has_plugin`
-- -----------------------------------------------------
START TRANSACTION;
USE `zac_ntxapp`;
INSERT INTO `zac_ntxapp`.`user_has_plugin` (`user_id`, `plugin_id`, `device_id`, `nickname`) VALUES (1, 2, 1, 'Air Conditioner Example');
INSERT INTO `zac_ntxapp`.`user_has_plugin` (`user_id`, `plugin_id`, `device_id`, `nickname`) VALUES (1, 3, 10, 'Refrigerator Example');
INSERT INTO `zac_ntxapp`.`user_has_plugin` (`user_id`, `plugin_id`, `device_id`, `nickname`) VALUES (1, 1, 19, 'Microwave Example');

COMMIT;

