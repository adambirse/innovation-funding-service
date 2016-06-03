UPDATE `competition` SET `status`='COMPETITION_SETUP_FINISHED' WHERE true=true;

-- Add Competition Types
INSERT INTO `competition_type` (`name`) VALUES ('Technology Inspired');
INSERT INTO `competition_type` (`name`) VALUES ('Additive Manufacturing');


-- ADD INNOVATION_SECTOR
INSERT INTO `category` (id, `name`, `type`) VALUES (1, 'Health and life sciences', 'INNOVATION_SECTOR');
INSERT INTO `category` (id, `name`, `type`) VALUES (2, 'Materials and manufacturing', 'INNOVATION_SECTOR');
INSERT INTO `category` (id, `name`, `type`) VALUES (3, 'Emerging and enabling', 'INNOVATION_SECTOR');
INSERT INTO `category` (id, `name`, `type`) VALUES (4, 'Infrastructure', 'INNOVATION_SECTOR');

-- ADD INNOVATION_AREA
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Transport', 'INNOVATION_AREA', '3');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Energy', 'INNOVATION_AREA', '3');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Urban living and built', 'INNOVATION_AREA', '3');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Health and care', 'INNOVATION_AREA', '1');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Agriculture and food', 'INNOVATION_AREA', '1');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Biosciences', 'INNOVATION_AREA', '1');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('High value manufacturing', 'INNOVATION_AREA', '2');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Resource efficiency', 'INNOVATION_AREA', '2');
INSERT INTO `category` (`name`, `type`, `parent_id`) VALUES ('Advanced materials', 'INNOVATION_AREA', '2');


