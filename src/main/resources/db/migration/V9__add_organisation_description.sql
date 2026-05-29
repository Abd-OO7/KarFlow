-- V9: Add description column to organisation table
ALTER TABLE organisation ADD COLUMN description VARCHAR(2000) NULL AFTER address;
