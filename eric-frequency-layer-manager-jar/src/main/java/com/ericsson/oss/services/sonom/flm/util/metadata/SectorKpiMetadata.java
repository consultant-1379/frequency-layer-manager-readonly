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
 * SectorKpiMetadata will hold all of the required metadata about a given sector kpi. Will be used by gson as a structure for json.
 */
public class SectorKpiMetadata {

    private final String kpiName;
    private final String sectorId;
    private final List<String> listOfTimestampsForDegradedSectorKpi;

    public SectorKpiMetadata(final String kpiName, final String sectorId, final List<String> listOfTimestampsForDegradedSectorKpi) {
        this.kpiName = kpiName;
        this.sectorId = sectorId;
        this.listOfTimestampsForDegradedSectorKpi = new ArrayList<>(listOfTimestampsForDegradedSectorKpi);
    }

    public String getKpiName() {
        return kpiName;
    }

    public String getSectorId() {
        return sectorId;
    }

    public List<String> getListOfTimestampsForDegradedSectorKpi() {
        return new ArrayList<>(listOfTimestampsForDegradedSectorKpi);
    }
}
