/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmDao;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDao;
import com.ericsson.oss.services.sonom.flm.database.KpiCellFlmExternalDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.KpiCombinedViewDao;
import com.ericsson.oss.services.sonom.flm.database.KpiCombinedViewDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

import io.vavr.Tuple2;

/**
 * Gets Cell KPIs per FLM execution.
 */
public class CellFlmKpiRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellFlmKpiRetriever.class);
    private static final int GET_BUSY_HOUR_KPIS_MAX_RETRY_ATTEMPTS = 10;
    private static final int GET_BUSY_HOUR_KPIS_WAIT_PERIOD_IN_SECONDS = 30;
    private final KpiCellFlmExternalDao cellFlmDao;
    private final KpiCellFlmDao cellFlmInternalDao;
    private final KpiCombinedViewDao kpiCombinedViewDao;

    public CellFlmKpiRetriever() {
        cellFlmDao = new KpiCellFlmExternalDaoImpl(GET_BUSY_HOUR_KPIS_MAX_RETRY_ATTEMPTS, GET_BUSY_HOUR_KPIS_WAIT_PERIOD_IN_SECONDS);
        cellFlmInternalDao = new KpiCellFlmDaoImpl(GET_BUSY_HOUR_KPIS_MAX_RETRY_ATTEMPTS, GET_BUSY_HOUR_KPIS_WAIT_PERIOD_IN_SECONDS);
        kpiCombinedViewDao = new KpiCombinedViewDaoImpl(GET_BUSY_HOUR_KPIS_MAX_RETRY_ATTEMPTS, GET_BUSY_HOUR_KPIS_WAIT_PERIOD_IN_SECONDS);
    }

    // required for Mockito JUnit
    public CellFlmKpiRetriever(final KpiCellFlmExternalDao kpiCellFlmExternalDao, final KpiCellFlmDao kpiCellFlmDao,
            final KpiCombinedViewDao kpiCombinedViewDao) {
        cellFlmDao = kpiCellFlmExternalDao;
        cellFlmInternalDao = kpiCellFlmDao;
        this.kpiCombinedViewDao = kpiCombinedViewDao;
    }

    /**
     * Retrieve all sector busy hour related KPIs for the {@link TopologySector}s and execution ID provided.
     *
     * @param startDateTime
     *            fetch the KPIs with a time stamp greater or equal to this timestamp
     * @param endDateTime
     *            fetch the KPIs with a time stamp less than this timestamp
     * @param sectorList
     *            the sectors for which to retrieve the Busy Hour KPIs
     * @param executionId
     *            the FLM execution ID for which to retrieve KPIs
     * @return A {@link Map} of KPIs which holds a {@link Map} of {@link CellIdentifier} which holds a {@link Map} of Hourly Timestamps further linked
     *         to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred executing the query.
     */
    public Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> retrieveAllKpis(final String startDateTime,
            final String endDateTime, final Collection<TopologySector> sectorList, final String executionId) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(2);
        for (final CellGuid60Kpis kpi : CellGuid60Kpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        for (final BusyHourCellFlmKpis kpi : BusyHourCellFlmKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return kpiCombinedViewDao.getKpis(TopologyIdentifierUtils.getAllSectorIdsAsList(sectorList), executionId,
                    startDateTime, endDateTime, kpiNames);
        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to retrieve all sector busy hour related KPIs for execution Id '{}'", executionId, e);
            }
            throw e;
        }
    }

    /**
     * Retrieve all busy hour KPIs for the execution ID provided.
     *
     * @param executionId
     *            The FLM execution ID for which to retrieve KPIs
     * @return A {@link Map} of {@link CellIdentifier} which holds to a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellIdentifier, Map<String, Map<String, Object>>> retrieveBusyHourCellFlmKpis(final String executionId) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(2);
        for (final BusyHourCellFlmKpis kpi : BusyHourCellFlmKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmDao.getCellHourlyFlmKpis(executionId, kpiNames);
        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, "Failed to retrieve cell FLM KPIs"), e);
            }
            throw e;
        }
    }

    /**
     * Retrieve all Coverage Balance KPIs for the {@link TopologySector}s provided on the given date.
     *
     * @param startDateTime
     *            current date minus one day
     * @param sectorList
     *            list of TopologySector object.
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, Map<String, Object>> retrieveCoverageBalanceFlmKpis(final Collection<TopologySector> sectorList, final String startDateTime)
            throws SQLException {
        final List<String> kpiNames = new ArrayList<>(1);
        for (final CellSectorDailyKpis kpi : CellSectorDailyKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmDao.getCoverageBalanceKpis(startDateTime, TopologyIdentifierUtils.getAllSectorIdsAsList(sectorList), kpiNames);
        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            LOGGER.warn("Failed to retrieve Coverage Balance cell FLM KPIs for sectorIdList", e);
            throw e;
        }
    }

    /**
     * Retrieve daily cell KPIs for the given date.
     *
     * @param executionDay
     *            the date on which to fetch the KPIs for
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, Map<String, Object>> retrieveCellDailyKpis(final String executionDay)
            throws SQLException {
        final List<String> kpiNames = new ArrayList<>(7);
        for (final CellDailyKpis kpi : CellDailyKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmDao.getCellDailyKpis(executionDay, kpiNames);
        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            LOGGER.warn("Failed to retrieve cell daily KPIs for execution day '{}'", executionDay, e);
            throw e;
        }
    }

    /**
     * Retrieve non-visible hourly cell KPIs for the given time.
     *
     * @param localDateTime
     *            the local date time of the KPIs to retrieve
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, CellKpis> retrieveNotVisibleCellHourlyKpis(final String localDateTime) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(7);
        for (final BusyHourCellKpis kpi : BusyHourCellKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmInternalDao.getNotVisibleCellHourlyKpis(kpiNames, localDateTime);
        } catch (final SQLException e) { // NOSONAR Exception suitably logged
            LOGGER.error("Failed to retrieve not visible cell hourly KPIs for the localDateTime: '{}'", localDateTime, e);
            throw e;
        }
    }

    /**
     * Retrieve hourly cell KPIs for the given time.
     *
     * @param startDateTime
     *            the date on which to fetch the KPIs for
     * @param endDateTime
     *            fetch the KPIs with a time stamp less than this timestamp
     * @return A {@link Map} of {@link CellIdentifier} which holds to a {@link Map} of Hourly Timestamps further linked to a {@link Map} of KPI values
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellIdentifier, Map<String, Map<String, Object>>> retrieveCellHourlyKpis(final String startDateTime, final String endDateTime)
            throws SQLException {
        final List<String> kpiNames = new ArrayList<>(9);
        for (final CellGuid60Kpis kpi : CellGuid60Kpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmDao.getCellHourlyKpis(startDateTime, endDateTime, kpiNames);
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve cell hourly KPIs for execution day '{}'", startDateTime, e);
            throw e;
        }
    }

    /**
     * Retrieve the non-visible daily cell KPIs for the given date.
     *
     * @param executionDay
     *            execution day
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, Map<String, Object>> retrieveNotVisibleCellDailyKpis(final String executionDay) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(2);
        for (final KpisCellGuid1440 kpi : KpisCellGuid1440.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmInternalDao.getNotVisibleCellDailyKpis(executionDay, kpiNames);
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve not visible cell daily KPIs for execution day '{}'", executionDay, e);
            throw e;
        }
    }

    /**
     * Retrieve the non-visible daily cell sector KPIs for the given execution ID.
     *
     * @param executionId
     *            The Execution ID for which to retrieve the KPIs
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, Map<String, Object>> retrieveNotVisibleCellSectorFlmKpis(final String executionId) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(CellSectorFlmKpis.values().length);
        for (final CellSectorFlmKpis kpi : CellSectorFlmKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmInternalDao.getNotVisibleCellSectorDailyFlmKpis(executionId, kpiNames);
        } catch (final SQLException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, "Failed to retrieve cell sector FLM KPIs"), e);
            }
            throw e;
        }
    }

    /**
     * Retrieve the daily cell sector KPIs for the given execution ID.
     * 
     * @param executionId
     *            The Execution ID for which to retrieve the KPIs
     * @return A {@link Map} of {@link CellKpi} to KPIs
     * @throws SQLException
     *             thrown if an error occurred retrieving the KPIs.
     */
    public Map<CellKpi, Map<String, Object>> retrieveVisibleCellSectorFlmKpis(final String executionId) throws SQLException {
        final List<String> kpiNames = new ArrayList<>(CellSectorFlmVisibleKpis.values().length);
        for (final CellSectorFlmVisibleKpis kpi : CellSectorFlmVisibleKpis.values()) {
            kpiNames.add(kpi.getKpiName());
        }
        try {
            return cellFlmDao.getCellSectorDailyFlmKpis(executionId, kpiNames);
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve cell sector FLM KPIs for execution Id '{}'", executionId, e);
            throw e;
        }
    }
}
