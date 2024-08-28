/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDao;

/**
 * Finds the sector Id's that have had their reference cell deleted.
 */
public class SectorsWithRefCellDeletedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectorsWithRefCellDeletedHandler.class);

    private final CmSectorCellStore cmSectorCellStore;
    private final KpiSectorDao kpiSectorDao;

    public SectorsWithRefCellDeletedHandler(final CmSectorCellStore cmSectorCellStore, final KpiSectorDao kpiSectorDao) {
        this.cmSectorCellStore = cmSectorCellStore;
        this.kpiSectorDao = kpiSectorDao;
    }

    /**
     * Find the {@link Set} of sectors whose reference cell has been deleted.
     *
     * @return A {@link Set} of sectorId's
     * @throws SQLException
     *             Will be thrown in case of error finding the deleted cells.
     */
    public Set<Long> find() throws SQLException {
        LOGGER.info("Retrieving list of cells from CM Topology");

        final Collection<Cell> cells = cmSectorCellStore.getAllMediatedCells();
        final Map<Long, String> kpiSectorIdsAndRefCell = kpiSectorDao.getSectorIdsAndRefCell();

        LOGGER.info("Number of cells retrieved from CM Topology: {}", cells.size());
        LOGGER.debug("Retrieved list of cells from CM Topology: {}", cells);

        final Set<String> refCellsWhichHaveBeenDeleted = getRefCellsWhichHaveBeenDeleted(cells, kpiSectorIdsAndRefCell);

        LOGGER.info("Number of reference cells which have been deleted: {}", refCellsWhichHaveBeenDeleted.size());
        LOGGER.debug("Reference cells which have been deleted: {}", refCellsWhichHaveBeenDeleted);

        return getSectorsWithRefCellDeleted(kpiSectorIdsAndRefCell, refCellsWhichHaveBeenDeleted);
    }

    private static Set<String> getRefCellsWhichHaveBeenDeleted(final Collection<Cell> cells, final Map<Long, String> kpiSectorIdsAndRefCell) {
        final Set<String> cellFdn = cells.stream().map(Cell::getFdn).collect(Collectors.toSet());
        final Set<String> kpiCellFdns = new HashSet<>(kpiSectorIdsAndRefCell.values());
        kpiCellFdns.removeAll(cellFdn);

        return kpiCellFdns;
    }

    private static Set<Long> getSectorsWithRefCellDeleted(final Map<Long, String> kpiSectorIdsAndRefCell,
            final Set<String> refCellsWhichHaveBeenDeleted) {
        final Set<Long> kpiSectors = kpiSectorIdsAndRefCell.keySet();
        // Remove a sector if its reference cell hasn't been deleted
        kpiSectors.removeIf(sector -> !refCellsWhichHaveBeenDeleted.contains(kpiSectorIdsAndRefCell.get(sector)));

        LOGGER.info("Number of sectors with reference cell deleted that need recalculation: {}", kpiSectors.size());
        LOGGER.debug("Sectors with reference cell deleted that need recalculation: {}", kpiSectors);

        return kpiSectors;
    }
}
