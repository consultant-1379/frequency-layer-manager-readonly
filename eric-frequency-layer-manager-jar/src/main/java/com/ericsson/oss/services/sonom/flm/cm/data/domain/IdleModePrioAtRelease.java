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
package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import java.util.List;
import java.util.Set;

/**
 * This class represent an LBDAR profile. A {@link DistributionInfo} class is used to represent distribution and frequency info for thresholds
 */
public class IdleModePrioAtRelease extends GenericIdleModePrioAtRelease<GenericIdleModePrioAtRelease.DistributionInfo> {
    public IdleModePrioAtRelease(final long id, final String fdn, final int ossId, final String name, final List<Integer> thresholds,
                                 final List<DistributionInfo> distributionInfos, final Set<String> reservedBy) {
        super(id, fdn, ossId, name, thresholds, distributionInfos, reservedBy);
    }
}
