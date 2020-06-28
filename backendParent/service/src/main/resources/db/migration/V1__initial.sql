CREATE TABLE IF NOT EXISTS TENANT (TENANTID varchar(50) NOT NULL PRIMARY KEY, TENANTUNIQUENAME varchar(50) NOT NULL UNIQUE, ACTIVE BOOL DEFAULT TRUE);
CREATE TABLE IF NOT EXISTS TENANTCORSMAPPING (TENANTID varchar(50) NOT NULL, ORIGIN varchar(300), CONSTRAINT FOREIGN KEY(TENANTID) REFERENCES TENANT(TENANTID));
CREATE TABLE IF NOT EXISTS FEATURETOGGLE(FEATUREID int NOT NULL AUTO_INCREMENT PRIMARY KEY, FEATURENAME varchar(30) NOT NULL UNIQUE, ACTIVE BOOL DEFAULT FALSE);
