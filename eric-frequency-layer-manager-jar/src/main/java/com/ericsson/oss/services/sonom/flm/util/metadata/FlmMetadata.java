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
 * FlmMetadata will hold all of the required metadata. Will be used by gson as a structure for json.
 */
public class FlmMetadata {

    private final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps;
    private final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps;
    private final Integer paWindow;

    public FlmMetadata(final Integer paWindow, final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps,
            final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps) {
        this.listOfAffectedCellKpisWithTimestamps = new ArrayList<>(listOfAffectedCellKpisWithTimestamps);
        this.listOfAffectedSectorKpisWithTimestamps = new ArrayList<>(listOfAffectedSectorKpisWithTimestamps);
        this.paWindow = paWindow;
    }

    public FlmMetadata(final Integer paWindow) {
        this.paWindow = paWindow;
        listOfAffectedCellKpisWithTimestamps = new ArrayList<>();
        listOfAffectedSectorKpisWithTimestamps = new ArrayList<>();
    }

    public void addToListOfAffectedCellKpisWithTimestamps(final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps) {
        this.listOfAffectedCellKpisWithTimestamps.addAll(listOfAffectedCellKpisWithTimestamps);
    }

    public void addToListOfAffectedSectorKpisWithTimestamps(final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps) {
        this.listOfAffectedSectorKpisWithTimestamps.addAll(listOfAffectedSectorKpisWithTimestamps);
    }

    public List<CellKpiMetadata> getListOfAffectedCellKpisWithTimestamps() {
        return new ArrayList<>(listOfAffectedCellKpisWithTimestamps);
    }

    public List<SectorKpiMetadata> getListOfAffectedSectorKpisWithTimestamps() {
        return new ArrayList<>(listOfAffectedSectorKpisWithTimestamps);
    }

    public Integer getPaWindow() {
        return paWindow;
    }

}
