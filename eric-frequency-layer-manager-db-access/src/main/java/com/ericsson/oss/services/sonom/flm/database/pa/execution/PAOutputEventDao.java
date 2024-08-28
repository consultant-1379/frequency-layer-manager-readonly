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

package com.ericsson.oss.services.sonom.flm.database.pa.execution;

import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * Interface defining the {@link PAOutputEventDao}.
 */
public interface PAOutputEventDao {

    /**
     * This method inserts the PA Policy Output event in the database in table pa_output_events.
     *
     * @param paPolicyOutputEvent
     *            a {@link PaPolicyOutputEvent } containing the paExecution which is to be added.
     * @return {@link String} containing paExecutionId of newly created record.
     * @throws SQLException
     *             SqlException thrown when there is issue updating database.
     */
    String insertPaPolicyOutputEvent(PaPolicyOutputEvent paPolicyOutputEvent) throws SQLException;

    /**
     * This method gets the {@link List} PA Policy Output events in the database in table pa_output_events by the paExecutionId.
     *
     * @param paExecutionId
     *            a {@link String } containing the paExecutionId which is to be queried.
     * @return {@link PaPolicyOutputEvent} with the given paExecutionId.
     * @throws SQLException
     *             SqlException thrown when there is issue updating database.
     */
    List<PaPolicyOutputEvent> getPaPolicyOutputEventById(String paExecutionId) throws SQLException;

}
