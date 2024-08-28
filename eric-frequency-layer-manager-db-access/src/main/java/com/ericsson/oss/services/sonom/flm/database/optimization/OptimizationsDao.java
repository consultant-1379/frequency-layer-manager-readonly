/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.database.optimization;

import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * LoadBalancingQuantum is stored for every sector for Executions.
 */
public interface OptimizationsDao {

    /**
     * Give the number of sectors for a given execution.
     *
     * @param executionId the id of execution which should be used in the query
     * @return the number of sectors for a given execution
     * @throws SQLException thrown if an error occurred executing the query.
     */
    Integer getNumberOfPolicyOutputEvents(String executionId) throws SQLException;

    /**
     * Retrieve list of PolicyOutputEvents for a given execution.
     *
     * @param executionId the id of execution which should be used in the query
     * @return a list of {@link PolicyOutputEvent}
     * @throws SQLException thrown if an error occurred executing the query.
     */
    List<PolicyOutputEvent> getOptimizations(String executionId) throws SQLException;

    /**
     * Retrieve filtered list of PolicyOutputEvents for a given execution.
     * Empty LBQs will be filtered out from the result
     *
     * @param executionId the id of execution which should be used in the query
     * @return a list of {@link PolicyOutputEvent}
     * @throws SQLException thrown if an error occurred executing the query.
     */
    List<PolicyOutputEvent> getOptimizationsFiltered(String executionId) throws SQLException;

    /**
     * Persist the given PolicyOutputEvent into the database.
     *
     * @param policyOutputEvent the object to persist.
     * @return The number of records inserted after query execution
     * @throws SQLException thrown if an error occurred executing the query.
     */
    Integer insertOptimization(PolicyOutputEvent policyOutputEvent) throws SQLException;

}
