package com.asiainfo.comm;

import com.avaje.ebean.config.dbplatform.DbPlatformName;
import com.avaje.ebean.dbmigration.DbMigration;

import java.io.IOException;

public class EbeanDbMigration {

    /**
     * Generate the next "DB schema DIFF" migration.
     * <p>
     * These migration are typically run using FlywayDB, Liquibase
     * or Ebean's own built in migration runner.
     * </p>
     */
    public static void main(String[] args) throws IOException {
    	
        // optionally specify the version and name
        System.setProperty("ddl.migration.version", "1.0");
        System.setProperty("ddl.migration.name", "initial release");

        // generate a migration using drops from a prior version
        //System.setProperty("ddl.migration.pendingDropsFor", "1.2");

        DbMigration dbMigration = new DbMigration();
        dbMigration.setPlatform(DbPlatformName.ORACLE);
        // generate the migration ddl and xml
        // ... with EbeanServer in "offline" mode
        dbMigration.generateMigration();
    }

}
