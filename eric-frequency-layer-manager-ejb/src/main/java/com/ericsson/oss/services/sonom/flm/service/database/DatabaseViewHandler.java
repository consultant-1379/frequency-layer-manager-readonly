/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.database;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;

import com.ericsson.oss.services.sonom.flm.database.DatabaseViewDao;
import com.ericsson.oss.services.sonom.flm.database.DatabaseViewDaoImpl;

/**
 * This class is responsible for creating the database views that will be exposed outside of the cluster.
 */
@Singleton
public class DatabaseViewHandler {

    private final DatabaseViewDao databaseViewDao = new DatabaseViewDaoImpl();

    /**
     * Creates the database role to be used for external views
     * and grant access for database views that will be exposed by flm.
     * <p>
     * If either of the environment variables "EXTERNAL_DB_USER" and "EXTERNAL_DB_PWD" are missing then this will not be done.
     * The method will retry a failed SQL view creation.
     */
    @Asynchronous
    public void createExternalDatabaseRoleAndGrantAccess() {
        databaseViewDao.createExternalDatabaseRoleAndGrantAccess();
    }

}
