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
package com.ericsson.oss.services.sonom.flm.util;

import java.util.List;

import com.ericsson.oss.services.sonom.flm.util.metadata.CellKpiMetadata;
import com.ericsson.oss.services.sonom.flm.util.metadata.SectorKpiMetadata;

public class FlmMetadataVerifier {

    private FlmMetadataVerifier() {
    }

    public static boolean verifyListOfAffectedCellKpisWithTimestampsContains(final String cellKpiName,
            final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps) {
        return listOfAffectedCellKpisWithTimestamps.stream().anyMatch(cellKpiMetadata -> cellKpiName.equals(cellKpiMetadata.getKpiName()));
    }

    public static boolean verifyListOfAffectedSectorKpisWithTimestampsContains(final String sectorKpiName,
            final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps) {
        return listOfAffectedSectorKpisWithTimestamps.stream().anyMatch(sectorKpiMetadata -> sectorKpiName.equals(sectorKpiMetadata.getKpiName()));
    }

    public static boolean verifyListOfAffectedCellKpisWithTimestampsContainsFdn(final String fdn,
            final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps) {
        return listOfAffectedCellKpisWithTimestamps.stream().anyMatch(cellKpiMetadata -> fdn.equals(cellKpiMetadata.getFdn()));
    }
}
