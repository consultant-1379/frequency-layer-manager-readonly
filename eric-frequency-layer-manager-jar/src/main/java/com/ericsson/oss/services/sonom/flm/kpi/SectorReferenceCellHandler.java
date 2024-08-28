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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDao;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;

/**
 * Class for retrieving sectors that require recalculation of their reference cells.
 */
public class SectorReferenceCellHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SectorReferenceCellHandler.class);

    private static final int GET_KPI_SECTOR_IDS_MAX_RETRY_ATTEMPTS = 10;
    private static final int GET_KPI_SECTOR_IDS_WAIT_PERIOD_IN_SECONDS = 30;
    private static final int FIVE_DAYS_UNAVAILABILITY = 5;

    private final CmStore cmStore;
    private final KpiSectorDao kpiSectorDao;
    private Set<Long> sectorsForReferenceCellCalculation;

    /**
     * Creates a new {@link SectorReferenceCellHandler} with default {@link CmStore}, {@link CmCellGroupRetriever} and {@link KpiSectorDao}.
     *
     * @param cmStore
     *            A reference to the {@link CmStore} cache.
     */
    public SectorReferenceCellHandler(final CmStore cmStore) {
        this.cmStore = cmStore;
        kpiSectorDao = new KpiSectorDaoImpl(GET_KPI_SECTOR_IDS_MAX_RETRY_ATTEMPTS, GET_KPI_SECTOR_IDS_WAIT_PERIOD_IN_SECONDS);
    }

    /**
     * Creates a new {@link SectorReferenceCellHandler} with given {@link CmStore} and {@link KpiSectorDao}.
     *
     * @param kpiSectorDao
     *            the {@link KpiSectorDao} to be used
     * @param cmStore
     *            A reference to the {@link CmStore} cache.
     */
    public SectorReferenceCellHandler(final KpiSectorDao kpiSectorDao,
            final CmStore cmStore) {
        this.cmStore = cmStore;
        this.kpiSectorDao = kpiSectorDao;
    }

    /**
     * Retrieves a {@link Set} of {@link Long}'s representing sectors that require new calculation of their reference cell.
     *
     * @return the {@link Set} to be used.
     * @throws SQLException
     *             thrown if the retrieval fails.
     * @throws FlmAlgorithmException
     *             if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public Set<Long> getSectorIdsRequiringReferenceCellRecalculation() throws SQLException, FlmAlgorithmException {
        if (Objects.isNull(sectorsForReferenceCellCalculation)) {
            sectorsForReferenceCellCalculation = new HashSet<>();
            sectorsForReferenceCellCalculation.addAll(getSectorsWithoutRefCell());
            sectorsForReferenceCellCalculation.addAll(getSectorsWithRefCellNeedingRecalculation());
            sectorsForReferenceCellCalculation.addAll(getSectorsWithRefCellDeleted());
        }
        return Collections.unmodifiableSet(sectorsForReferenceCellCalculation);
    }

    private Set<Long> getSectorsWithoutRefCell() throws SQLException, FlmAlgorithmException {
        final Set<Long> kpiSectorsWithRefCell = kpiSectorDao.getKpiSectorIdsWithRefCell();
        final Collection<TopologySector> cmSectors = cmStore.getCmSectorCellStore().getFullSectors();

        LOGGER.info("{} sectors retrieved from CM", cmSectors.size());
        LOGGER.debug("Following sectors have been retrieved from CM: '{}'", cmSectors);

        final Set<Long> sectorsWithoutRefCell = cmSectors.parallelStream().map(TopologySector::getSectorId)
                .filter(sectorId -> !kpiSectorsWithRefCell.contains(sectorId))
                .collect(Collectors.toSet());
        LOGGER.info("{} sectors without reference cells", sectorsWithoutRefCell.size());
        LOGGER.debug("Following sectors have no reference cell: '{}'", sectorsWithoutRefCell);

        return sectorsWithoutRefCell;
    }

    private Set<Long> getSectorsWithRefCellNeedingRecalculation() throws SQLException {
        final LocalDateTime todayMidnight = getMidnightToday();
        final Set<Long> sectorsWithRefCellUnavailable = kpiSectorDao
                .getKpiSectorIdsWithUnavailableRefCell(todayMidnight.minusDays(FIVE_DAYS_UNAVAILABILITY));

        LOGGER.info("{} sectors with reference cell unavailable for {} days", sectorsWithRefCellUnavailable.size(), FIVE_DAYS_UNAVAILABILITY);
        LOGGER.debug("Following sectors have a reference cell unavailable for {} days: '{}'", FIVE_DAYS_UNAVAILABILITY,
                sectorsWithRefCellUnavailable);

        return sectorsWithRefCellUnavailable;
    }

    private static LocalDateTime getMidnightToday() {
        final LocalTime midnight = LocalTime.MIDNIGHT;
        final LocalDate today = LocalDate.now();
        return LocalDateTime.of(today, midnight);
    }

    private Set<Long> getSectorsWithRefCellDeleted() throws SQLException, FlmAlgorithmException {
        final SectorsWithRefCellDeletedHandler sectorsWithRefCellDeletedHandler = new SectorsWithRefCellDeletedHandler(cmStore.getCmSectorCellStore(),
                kpiSectorDao);
        return sectorsWithRefCellDeletedHandler.find();
    }
}
