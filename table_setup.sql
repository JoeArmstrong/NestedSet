/* 
 * mysql -u root -p devportal < this-file
 * or from within mysql:
 * source table_setup.sql
 */

DROP TABLE nested_extended_hierarchy;

/*
 * The nested heirarchy table
 */
CREATE TABLE nested_extended_hierarchy (
 id VARCHAR(40) PRIMARY KEY,
 name VARCHAR(60) DEFAULT NULL,
 display_name VARCHAR(60) DEFAULT NULL,
 level INT NOT NULL,
 assetId VARCHAR(40) DEFAULT NULL UNIQUE,
 lft INT NOT NULL,
 rgt INT NOT NULL,
 create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
 modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

/* For some reason, DATETIME does not get initialized with CURRENT_TIMESTAMP */
/* Needed to create the trigger below to update create_date, modified_date */

create trigger before_insert_nested_extended_hierarchy
 before insert on nested_extended_hierarchy
 for each row set
    new.id = REPLACE(uuid(), '-',''),
    new.create_date = now(),
    new.modified_date = now()
    ;