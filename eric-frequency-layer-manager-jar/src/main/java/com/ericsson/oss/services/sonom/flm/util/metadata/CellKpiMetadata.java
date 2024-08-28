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

package com.ericsson.oss.services.sonom.flm.util.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * CellKpiMetadata will hold all of the required metadata about a given cell kpi. Will be used by gson as a structure for json.
 */
public class CellKpiMetadata {

    private final String kpiName;
    private final String fdn;
    private final Integer ossId;
    private final List<String> listOfTimestampsForDegradedCellKpi;

    public CellKpiMetadata(final String kpiName, final String fdn, final Integer ossId, final List<String> listOfTimestampsForDegradedCellKpi) {
        this.kpiName = kpiName;
        this.fdn = fdn;
        this.ossId = ossId;
        this.listOfTimestampsForDegradedCellKpi = new ArrayList<>(listOfTimestampsForDegradedCellKpi);
    }

    public String getKpiName() {
        return kpiName;
    }

    public String getFdn() {
        return fdn;
    }

    public Integer getOssId() {
        return ossId;
    }

    public List<String> getListOfTimestampsForDegradedCellKpi() {
        return new ArrayList<>(listOfTimestampsForDegradedCellKpi);
    }
}
