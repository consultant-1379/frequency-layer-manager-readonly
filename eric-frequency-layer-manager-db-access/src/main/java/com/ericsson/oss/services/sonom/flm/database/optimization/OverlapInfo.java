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

import java.util.Locale;

/**
 * Information class about the overlapping sectors.
 */
public class OverlapInfo {
    private final OverlappingFlag overlappingFlag;
    private final String overlappingExecutions;

    /**
     * Defines the types of overlaps for executions.
     */
    public enum OverlappingFlag {
        NOT_OVERLAPPING,
        OVERLAP_LOG_NEEDED,
        OVERLAP_DROP_NEEDED
    }

    private OverlapInfo(final OverlappingFlag overlappingFlag, final String overlappingExecutions) {
        this.overlappingFlag = overlappingFlag;
        this.overlappingExecutions = overlappingExecutions;
    }

    /**
     * Get {@link OverlapInfo} with given overlappingFlag and overlappingExecution.
     * @param overlappingFlag the type of overlap between the executions.
     * @param overlappingExecutions the list of overlapping executions, divided by commas.
     * @return {@link OverlapInfo} containing the list of executions and the type of overlap.
     */
    public static OverlapInfo of(final OverlappingFlag overlappingFlag, final String overlappingExecutions) {
        return new OverlapInfo(overlappingFlag, overlappingExecutions);
    }

    /**
     * Get {@link OverlapInfo} with given overlappingFlag and overlappingExecution.
     * @param overlappingFlag the name of the type of overlap between the executions.
     * @param overlappingExecutions the list of overlapping executions, divided by commas.
     * @return {@link OverlapInfo} containing the list of executions and the type of overlap.
     */
    public static OverlapInfo of(final String overlappingFlag, final String overlappingExecutions) {
        return OverlapInfo.of(OverlapInfo.OverlappingFlag.valueOf(overlappingFlag.toUpperCase(Locale.ROOT)),
                overlappingExecutions);
    }

    public OverlappingFlag getOverlappingFlag() {
        return overlappingFlag;
    }

    public String getOverlappingExecutions() {
        return overlappingExecutions;
    }
}
