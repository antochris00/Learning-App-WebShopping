/* product notification to admin/client(0 will notify all) */
CREATE TABLE IF NOT EXISTS productnotification (tenantid varchar(50) NOT NULL, notificationid double NOT NULL AUTO_INCREMENT PRIMARY KEY, productid double, notificationcontent varchar(250), adminid double DEFAULT 0, clientid double DEFAULT 0, CONSTRAINT FOREIGN KEY(tenantid) REFERENCES tenant(tenantid));