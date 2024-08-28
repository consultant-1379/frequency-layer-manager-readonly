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

package com.ericsson.oss.services.sonom.flm.database.pa.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradationStatus;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Creates a {@link List} of {@link PaPolicyOutputEvent} from the {@link ResultSet}.
 */
public class PAOutputEventHandler implements ResultHandler<List<PaPolicyOutputEvent>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAOutputEventHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public List<PaPolicyOutputEvent> populate(final ResultSet resultSet) throws SQLException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = new ArrayList<>();
        while (resultSet.next()) {
            DegradationStatus degradationStatus = null;
            try {
                degradationStatus = GSON.fromJson(resultSet.getString(PAExecutionDbConstants.DEGRADATION_STATUS),
                        DegradationStatus.class);
            } catch (final JsonSyntaxException e) {
                LOGGER.error("Failed to parse Degradation Status", e);
            }
            final PaPolicyOutputEvent paPolicyOutputEvent = new PaPolicyOutputEvent(
                    resultSet.getString(PAExecutionDbConstants.FLM_EXECUTION_ID),
                    resultSet.getString(PAExecutionDbConstants.PA_EXECUTION_ID),
                    resultSet.getInt(PAExecutionDbConstants.PA_WINDOW),
                    resultSet.getString(PAExecutionDbConstants.SECTOR_ID),
                    degradationStatus);
            paPolicyOutputEvents.add(paPolicyOutputEvent);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} PA output events retrieved: {}", paPolicyOutputEvents.size(), paPolicyOutputEvents);
        }
        return paPolicyOutputEvents;
    }
}
