/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

/**
 * FLM database view access.
 */
public interface DatabaseViewDao {

    /**
     * Creates the database role to be used for external views and update view access.
     * <p>
     * If either of the environment variables "EXTERNAL_DB_USER" and "EXTERNAL_DB_PWD" are missing then this will not be done.
     */
    void createExternalDatabaseRoleAndGrantAccess();

}
