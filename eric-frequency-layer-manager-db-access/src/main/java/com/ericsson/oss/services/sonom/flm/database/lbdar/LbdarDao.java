/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database.lbdar;

import java.sql.SQLException;
import java.util.Set;

/**
 * LBDAR data access interface.
 */
public interface LbdarDao {

    /**
     * Retrieve the set of leakage cells for given execution and sector.
     *
     * @param executionId
     *            the execution id which should be used in the query.
     * @param sectorId
     *            the sector id within the execution which should be used in the query.
     * @return a {@link Set} of {@link LeakageCell}
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    Set<LeakageCell> getLeakageCells(String executionId, Long sectorId) throws SQLException;

    /**
     * Persist the given leakage cells into the database for the execution and sector.
     *
     * @param executionId
     *            the execution id which the leakage cells should be inserted for.
     * @param sectorId
     *            the sector id which the leakage cells should be inserted for.
     * @param leakageCells
     *            as {@link Set} of {@link LeakageCell}s to be persisted.
     * @return The number of records inserted after query execution.
     * @throws SQLException
     *             thrown if an error occurred executing the insert.
     */
    Integer insertLeakageCells(String executionId, Long sectorId, Set<LeakageCell> leakageCells) throws SQLException;

}
