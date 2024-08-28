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

package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.UTC_TIMESTAMP_COLUMN;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.sonom.flm.database.handlers.KpiSectorBusyHourHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.SectorHourlyFlmKpiHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link KpiCellFlmExternalDao}.
 */
public class KpiSectorExternalDaoImpl implements KpiSectorExternalDao {
    private static final String SECTOR_BUSY_HOUR_TABLE = "sector_1440_kpis";
    private static final String SECTOR_60_TABLE = "sector_60_kpis";
    private static final String SECTOR_FLM_60_TABLE = "sector_flm_60_kpis";
    private static final String COMMA_DELIMITER = ", ";
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();
    private static final DatabaseAccess KPI_DB_ACCESS = new KpiDatabaseExternalAccess();
    private final DatabaseRetry databaseRetry;

    public KpiSectorExternalDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public Map<Long, String> getSectorBusyHourForSectorIds(final String queryDate, final List<String> sectorIds) throws SQLException {
        final String dbQuery = String.format("SELECT sector_id, sector_busy_hour FROM %s WHERE local_timestamp = '%s' AND sector_id IN (%s)",
                SECTOR_BUSY_HOUR_TABLE, queryDate, StringUtils.join(sectorIds, COMMA_DELIMITER));
        final CheckedFunction0<Map<Long, String>> allSectorBusyHoursForSectorIds = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new KpiSectorBusyHourHandler(), NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(allSectorBusyHoursForSectorIds);
    }

    @Override
    public Map<Long, Map<String, Map<String, Object>>> getSectorHourlyKpis(final String startDateTime, final String endDateTime,
            final List<String> kpiNames, final Collection<Long> sectorIds) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String sectorIdsToSelect = StringUtils.join(sectorIds, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT sector_id, utc_timestamp, %s FROM %s " +
                "WHERE sector_id IN (%s) " +
                "AND utc_timestamp >= '%s' " +
                "AND utc_timestamp < '%s'",
                kpisToSelect, SECTOR_60_TABLE, sectorIdsToSelect, startDateTime, endDateTime);
        final CheckedFunction0<Map<Long, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new SectorHourlyFlmKpiHandler(kpiNames, UTC_TIMESTAMP_COLUMN), NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<Long, Map<String, Map<String, Object>>> getSectorHourlyKpisForFlmExecution(final String executionId, final String startDateTime,
            final String endDateTime, final List<String> kpiNames, final Collection<Long> sectorIds) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String sectorIdsToSelect = StringUtils.join(sectorIds, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT sector_id, utc_timestamp, %s FROM %s " +
                "WHERE sector_id IN (%s) " +
                "AND execution_id = '%s' " +
                "AND utc_timestamp >= '%s' " +
                "AND utc_timestamp < '%s'",
                kpisToSelect, SECTOR_FLM_60_TABLE, sectorIdsToSelect, executionId, startDateTime, endDateTime);
        final CheckedFunction0<Map<Long, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new SectorHourlyFlmKpiHandler(kpiNames, UTC_TIMESTAMP_COLUMN), NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }
}
