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
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Creates a {@link List} of policy output event's represented as {@link PolicyOutputEvent}
 * paired with it's overlapping information represented as {@link OverlapInfo}.
 */
public class OverlapInfoHandler implements ResultHandler<List<Pair<PolicyOutputEvent, OverlapInfo>>> {

    @Override
    public List<Pair<PolicyOutputEvent, OverlapInfo>> populate(final ResultSet resultSet) throws SQLException {
        final List<Pair<PolicyOutputEvent, OverlapInfo>> overlapInfoPairs = new ArrayList<>();
        final PolicyOutputEventListHandler policyOutputEventListHandler = new PolicyOutputEventListHandler();
        final List<PolicyOutputEvent> policyOutputEvents = policyOutputEventListHandler.populate(resultSet);
        resultSet.beforeFirst();

        int i = 0;
        while (resultSet.next()) {
            final OverlapInfo overlapInfo = OverlapInfo.of(resultSet.getString("overlap"),
                    resultSet.getString("overlapped_executions"));
            overlapInfoPairs.add(new ImmutablePair<>(policyOutputEvents.get(i), overlapInfo));
            i++;
        }
        return overlapInfoPairs;
    }
}
