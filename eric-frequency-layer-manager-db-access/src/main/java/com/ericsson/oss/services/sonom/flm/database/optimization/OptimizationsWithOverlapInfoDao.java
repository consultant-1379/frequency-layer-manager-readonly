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
package com.ericsson.oss.services.sonom.flm.database.optimization;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Dao interface for {@link PolicyOutputEvent}s with Overlapping info. Can be used to filter out overlapping executions.
 */
public interface OptimizationsWithOverlapInfoDao {
    /**
     * Give the {@link PolicyOutputEvent}s paired with the list of executions which have overlapping sectors with the given execution.
     * @param executionId the id of execution which should be used in the query.
     * @return the {@link PolicyOutputEvent}s paired with the list of overlapping executions for given execution.
     * @throws SQLException thrown if an error occurred executing the query.
     */
    List<Pair<PolicyOutputEvent, OverlapInfo>> getOptimizationsWithOverlapInfo(String executionId) throws SQLException;
}
