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
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Creates a {@link List} of policy output event's represented as {@link PolicyOutputEvent}.
 */
public class PolicyOutputEventListHandler implements ResultHandler<List<PolicyOutputEvent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyOutputEventListHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public List<PolicyOutputEvent> populate(final ResultSet resultSet) throws SQLException {
        final List<PolicyOutputEvent> policyOutputEvents = new ArrayList<>();

        while (resultSet.next()) {
            ProposedLoadBalancingQuanta lbq = null;
            try {
                lbq = GSON.fromJson(resultSet.getString(OptimizationsDbConstants.LBQ),
                        ProposedLoadBalancingQuanta.class);
            } catch (final JsonSyntaxException e) {
                LOGGER.error("Failed to parse Load Balancing Quanta", e);
            }
            final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);
            policyOutputEvent.setExecutionId(resultSet.getString(OptimizationsDbConstants.EXECUTION_ID));
            policyOutputEvent.setSectorId(resultSet.getLong(OptimizationsDbConstants.SECTOR_ID));
            policyOutputEvents.add(policyOutputEvent);
        }
        LOGGER.debug("Populated list of policy output events: {}", policyOutputEvents);
        return policyOutputEvents;
    }
}