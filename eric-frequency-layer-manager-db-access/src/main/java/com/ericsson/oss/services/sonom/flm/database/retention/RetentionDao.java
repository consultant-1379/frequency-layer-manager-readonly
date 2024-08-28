/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database.retention;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Interface defining the <code>RetentionDao</code>.
 */
public interface RetentionDao {

    /**
     * Method to clean up executions table.
     *
     * @param retentionDate The LocalDateTime where older tuples before this LocalDateTime will be deleted
     * @param retentionExecutionCount the number of executions that should be kept in table
     * @throws SQLException thrown if there is an error in executing the {@link PreparedStatement}
     */
    void cleanUpFlmExecutionsTable(LocalDateTime retentionDate, Integer retentionExecutionCount) throws SQLException;

    /**
     * Method to clean up historical cell configuration.
     *
     * @param retentionDate The LocalDateTime where older tuples before this LocalDateTime will be deleted
     * @param retentionExecutionCount the number of executions that should be kept in table
     * @throws SQLException thrown if there is an error in executing the {@link PreparedStatement}
     */
    void cleanUpHistoricalCellConfigurationTable(LocalDateTime retentionDate, Integer retentionExecutionCount) throws SQLException;

}
