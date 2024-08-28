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

package com.ericsson.oss.services.sonom.flm.loadbalancing.retriever;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsWithOverlapInfoDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsWithOverlapInfoDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Implementation of {@link PolicyOutputEventRetriever}.
 */
public class PolicyOutputEventRetrieverImpl implements PolicyOutputEventRetriever {
    private static final int OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS = 30;
    private static final int OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS = 10;
    private final OptimizationsWithOverlapInfoDao optimizationsDao;

    public PolicyOutputEventRetrieverImpl() {
        optimizationsDao = new OptimizationsWithOverlapInfoDaoImpl(OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS,
                OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS);
    }

    @Override
    public List<Pair<PolicyOutputEvent, OverlapInfo>> getPolicyOutputEvents(final String executionId) throws SQLException {
        return optimizationsDao.getOptimizationsWithOverlapInfo(executionId);
    }
}
