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
package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.Cell;

/**
 * A POJO to represent a sector passed to the FLM performance assurance policy.
 */
public class Sector implements Serializable {

    private static final long serialVersionUID = 5548695188422648967L;
    private final String sectorId;
    private final Map<String, String> settings;
    private final Map<String, SectorLevelKpi> kpis;
    private final List<Cell> cells;

    public Sector(final String sectorId, final Map<String, String> settings,
            final Map<String, SectorLevelKpi> kpis,
            final List<Cell> cells) {

        this.sectorId = sectorId;
        this.settings = settings;
        this.kpis = kpis;
        this.cells = new ArrayList<>(cells);
    }

    public Sector(final String sectorId) {
        this(sectorId, new HashMap<>(), new HashMap<>(), new ArrayList<>());
    }

    public String getSectorId() {
        return sectorId;
    }

    public Long getSectorIdAsLong() {
        return Long.parseLong(sectorId);
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public Map<String, SectorLevelKpi> getKpis() {
        return kpis;
    }

    public List<Cell> getCells() {
        return new ArrayList<>(cells);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Sector sector = (Sector) o;
        return Objects.equals(sectorId, sector.sectorId) &&
                Objects.equals(settings, sector.settings) &&
                Objects.equals(kpis, sector.kpis) &&
                Objects.equals(cells, sector.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectorId, settings, kpis, cells);
    }

    @Override
    public String toString() {
        return String.format("%s:: { sectorId: '%s', settings: '%s', kpis: '%s', cells: '%s' }",
                getClass().getSimpleName(), sectorId, settings, kpis, cells);
    }
}
