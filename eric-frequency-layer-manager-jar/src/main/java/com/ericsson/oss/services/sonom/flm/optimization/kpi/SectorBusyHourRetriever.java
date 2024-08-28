/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorExternalDaoImpl;
import com.ericsson.oss.services.sonom.flm.optimization.OptimizationExecutor;

/**
 * Class for retrieving sector busy hours.
 */
public class SectorBusyHourRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizationExecutor.class);
    private static final int GET_SECTOR_BUSY_HOUR_MAX_RETRY_ATTEMPTS = 10;
    private static final int GET_SECTOR_BUSY_HOUR_WAIT_PERIOD_IN_SECONDS = 30;
    private static final KpiSectorExternalDao SECTOR_DAO = new KpiSectorExternalDaoImpl(GET_SECTOR_BUSY_HOUR_MAX_RETRY_ATTEMPTS,
                                                                                        GET_SECTOR_BUSY_HOUR_WAIT_PERIOD_IN_SECONDS);

    /**
     * Populates all Sector Busy Hours for the {@link TopologySector}s provided.
     *
     * @param sectors
     *            The sectors for which to retrieve the Sector Busy Hours
     * @param queryDate
     *            The query date for which to retrieve the Sector Busy Hours
     * @return A {@link Map} of Sector Id's represented as {@link Long} to Sector Busy Hour.
     * @throws SQLException
     *             thrown if an error occurred retrieving the sector busy hours.
     */
    public Map<Long, String> populateSectorIdToBusyHour(final String queryDate, final Collection<TopologySector> sectors)
            throws SQLException {
        try {
            return SECTOR_DAO.getSectorBusyHourForSectorIds(queryDate, TopologyIdentifierUtils.getAllSectorIdsAsList(sectors));
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve sector busy hours from '{}'", queryDate);
            throw e;
        }
    }

}
