/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.util.Objects;

/**
 * Class representing a Cell and its KPIs.
 */
public class CellKpi {

    private final String fdn;
    private final int ossId;
    private final String localTimestamp;

    public CellKpi(final String fdn, final int ossId, final String localTimestamp) {
        this.fdn = fdn;
        this.ossId = ossId;
        this.localTimestamp = localTimestamp;
    }

    public String getFdn() {
        return fdn;
    }

    public int getOssId() {
        return ossId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CellKpi cellKpi = (CellKpi) o;
        return Objects.equals(fdn, cellKpi.fdn) &&
                Objects.equals(ossId, cellKpi.ossId) &&
                Objects.equals(localTimestamp, cellKpi.localTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fdn, ossId, localTimestamp);
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {fdn: '%s', ossId: %d, localTimestamp: '%s'}",
                getClass().getSimpleName(), fdn, ossId, localTimestamp);
    }
}
