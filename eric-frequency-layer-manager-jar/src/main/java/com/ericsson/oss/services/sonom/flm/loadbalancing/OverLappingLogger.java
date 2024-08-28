/*
 * -----------------------------------------------------------------------------
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

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo.OverlappingFlag.OVERLAP_LOG_NEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The class is used to log the Overlapping sectors.
 */
public class OverLappingLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverLappingLogger.class);

    private OverLappingLogger() {

    }

    /**
     * Log the overlapping sectors.
     * @param pair is a Pair of {@link PolicyOutputEvent} and {@link OverlapInfo}
     */
    public static void logOverlappingSectors(final Pair<PolicyOutputEvent, OverlapInfo> pair) {
        final OverlapInfo overlapInfo = pair.getRight();
        final PolicyOutputEvent policyOutputEvent = pair.getLeft();
        if (OVERLAP_DROP_NEEDED.equals(overlapInfo.getOverlappingFlag())) {
            final String message = String.format("Sector is dropped as it is overlapping with other executions: %s ",
                    pair.getRight().getOverlappingExecutions());
            logFilteredSector(pair.getLeft().getExecutionId(), String.valueOf(pair.getLeft().getSectorId()), message);
        } else if (OVERLAP_LOG_NEEDED.equals(overlapInfo.getOverlappingFlag())) {
            LOGGER.info("Sector {} is overlapping with other executions: {}, dropping is not needed",
                    policyOutputEvent.getSectorId(), overlapInfo.getOverlappingExecutions());
        }
    }

}
