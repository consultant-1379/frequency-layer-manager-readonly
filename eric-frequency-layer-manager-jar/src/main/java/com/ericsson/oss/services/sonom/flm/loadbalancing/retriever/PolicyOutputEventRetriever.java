/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.retriever;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The implementation of PolicyOutputEventRetriever should retrieve a list of Pairs {@link PolicyOutputEvent} amd {@link OverlapInfo}.
 * of an {@link Execution}.
 * It works in a lazy way, the data should be loaded only if it is requested.
 */
public interface PolicyOutputEventRetriever {
    List<Pair<PolicyOutputEvent, OverlapInfo>> getPolicyOutputEvents(String executionId) throws SQLException;
}
