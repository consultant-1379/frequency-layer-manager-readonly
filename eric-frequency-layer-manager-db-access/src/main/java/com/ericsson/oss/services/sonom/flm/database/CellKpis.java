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

package com.ericsson.oss.services.sonom.flm.database;

import java.util.Arrays;
import java.util.List;

/**
 * Object to store cell KPIs.
 */
public class CellKpis {
    private final double connectedUsers;
    private final double subscriptionRatio;
    private final long pmIdleModeRelDistrHighLoad;
    private final long pmIdleModeRelDistrMediumHighLoad;
    private final long pmIdleModeRelDistrMediumLoad;
    private final long pmIdleModeRelDistrLowMediumLoad;
    private final long pmIdleModeRelDistrLowLoad;

    public CellKpis(final double connectedUsers, final double subscriptionRatio, final long pmIdleModeRelDistrHighLoad,
                    final long pmIdleModeRelDistrMediumHighLoad, final long pmIdleModeRelDistrMediumLoad,
                    final long pmIdleModeRelDistrLowMediumLoad, final long pmIdleModeRelDistrLowLoad) {
        this.connectedUsers = connectedUsers;
        this.subscriptionRatio = subscriptionRatio;
        this.pmIdleModeRelDistrHighLoad = pmIdleModeRelDistrHighLoad;
        this.pmIdleModeRelDistrMediumHighLoad = pmIdleModeRelDistrMediumHighLoad;
        this.pmIdleModeRelDistrMediumLoad = pmIdleModeRelDistrMediumLoad;
        this.pmIdleModeRelDistrLowMediumLoad = pmIdleModeRelDistrLowMediumLoad;
        this.pmIdleModeRelDistrLowLoad = pmIdleModeRelDistrLowLoad;
    }

    /**
     * Transforms the IdleModeRelDistr kpis to a List. The order of the list is from the lowest to the highest.
     * @return a list of long.
     */
    public List<Long> getPmIdleModeReleaseCounters() {
        return Arrays.asList(
                pmIdleModeRelDistrLowLoad,
                pmIdleModeRelDistrLowMediumLoad,
                pmIdleModeRelDistrMediumLoad,
                pmIdleModeRelDistrMediumHighLoad,
                pmIdleModeRelDistrHighLoad);
    }

    public double getConnectedUsers() {
        return connectedUsers;
    }

    public double getSubscriptionRatio() {
        return subscriptionRatio;
    }

    public long getPmIdleModeRelDistrHighLoad() {
        return pmIdleModeRelDistrHighLoad;
    }

    public long getPmIdleModeRelDistrMediumHighLoad() {
        return pmIdleModeRelDistrMediumHighLoad;
    }

    public long getPmIdleModeRelDistrMediumLoad() {
        return pmIdleModeRelDistrMediumLoad;
    }

    public long getPmIdleModeRelDistrLowMediumLoad() {
        return pmIdleModeRelDistrLowMediumLoad;
    }

    public long getPmIdleModeRelDistrLowLoad() {
        return pmIdleModeRelDistrLowLoad;
    }

}
