/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.UTC_TIMESTAMP;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.sonom.flm.database.handlers.CellFlmKpiHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellHourlyFlmKpiHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link KpiCellFlmExternalDao}.
 */
public class KpiCellFlmExternalDaoImpl implements KpiCellFlmExternalDao {

    private static final String CELL_FLM_60_TABLE = "cell_guid_flm_60_kpis";
    private static final String CELL_SECTOR_1440_TABLE = "cell_sector_1440_kpis";
    private static final String CELL_SECTOR_FLM_1440_TABLE = "cell_sector_flm_1440_kpis";
    private static final String CELL_GUID_1440_TABLE = "cell_guid_1440_kpis";
    private static final String CELL_GUID_60_TABLE = "cell_guid_60_kpis";
    private static final String COMMA_DELIMITER = ", ";
    private static final String FDN = "fdn";
    private static final String OSS_ID = "oss_id";
    private static final DatabaseAccess KPI_DB_ACCESS = new KpiDatabaseExternalAccess();
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();

    private final DatabaseRetry databaseRetry;

    public KpiCellFlmExternalDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public Map<CellIdentifier, Map<String, Map<String, Object>>> getCellHourlyFlmKpis(final String executionId,
            final List<String> kpiNames)
            throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT fdn, oss_id, execution_id, local_timestamp, %s FROM %s WHERE execution_id = '%s'",
                kpisToSelect, CELL_FLM_60_TABLE, executionId);
        final CheckedFunction0<Map<CellIdentifier, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellHourlyFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellKpi, Map<String, Object>> getCoverageBalanceKpis(final String executionDay, final List<String> sectorIds,
            final List<String> kpiNames)
            throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT cell_fdn, cell_oss_id, local_timestamp, %s FROM %s WHERE sector_id IN (%s) AND local_timestamp" +
                " =" +
                "'%s' ",
                kpisToSelect, CELL_SECTOR_1440_TABLE, StringUtils.join(sectorIds, COMMA_DELIMITER), executionDay);
        final CheckedFunction0<Map<CellKpi, Map<String, Object>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellFlmKpiHandler("cell_fdn", "cell_oss_id", kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellKpi, Map<String, Object>> getCellDailyKpis(final String executionDay, final List<String> kpiNames) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT fdn, oss_id, local_timestamp, %s FROM %s WHERE local_timestamp" +
                " =" +
                "'%s' ",
                kpisToSelect, CELL_GUID_1440_TABLE, executionDay);
        final CheckedFunction0<Map<CellKpi, Map<String, Object>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellIdentifier, Map<String, Map<String, Object>>> getCellHourlyKpis(final String startDateTime, final String endDateTime,
            final List<String> kpiNames)
            throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT fdn, oss_id, local_timestamp, %s FROM %s WHERE local_timestamp" +
                " >=" +
                "'%s' AND local_timestamp < '%s'",
                kpisToSelect, CELL_GUID_60_TABLE, startDateTime, endDateTime);
        final CheckedFunction0<Map<CellIdentifier, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellHourlyFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellKpi, Map<String, Object>> getCellSectorDailyFlmKpis(final String executionId, final List<String> kpiNames) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format("SELECT cell_fdn as fdn, cell_oss_id as oss_id, local_timestamp, " +
                "%s FROM %s WHERE execution_id = '%s'",
                kpisToSelect, CELL_SECTOR_FLM_1440_TABLE, executionId);
        final CheckedFunction0<Map<CellKpi, Map<String, Object>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellIdentifier, Map<String, Map<String, Object>>> getKpisForGivenCellsPerFlmExecution(final String executionId,
            final String startDateTime, final String endDateTime, final List<String> kpiNames, final List<CellIdentifier> cells)
            throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String cellsToSelect = cells.parallelStream()
                .map(s -> Arrays.asList(s.getFdn(), String.valueOf(s.getOssId())))
                .map(KpiCellFlmExternalDaoImpl::wrapStringInQuotes)
                .collect(Collectors.joining(COMMA_DELIMITER));
        final String dbQuery = String.format("SELECT fdn, oss_id, utc_timestamp, %s FROM %s " +
                "WHERE (fdn,oss_id) IN (%s) " +
                "AND execution_id = '%s' " +
                "AND utc_timestamp >= '%s' " +
                "AND utc_timestamp < '%s'",
                kpisToSelect, CELL_FLM_60_TABLE, cellsToSelect, executionId, startDateTime, endDateTime);
        final CheckedFunction0<Map<CellIdentifier, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellHourlyFlmKpiHandler(FDN, OSS_ID, kpiNames, UTC_TIMESTAMP),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellIdentifier, Map<String, Map<String, Object>>> getHourlyKpisForGivenCells(final String startDateTime, final String endDateTime,
            final List<String> kpiNames, final List<CellIdentifier> cells) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String cellsToSelect = cells.parallelStream()
                .map(s -> Arrays.asList(s.getFdn(), String.valueOf(s.getOssId())))
                .map(KpiCellFlmExternalDaoImpl::wrapStringInQuotes)
                .collect(Collectors.joining(COMMA_DELIMITER));
        final String dbQuery = String.format("SELECT fdn, oss_id, utc_timestamp, %s FROM %s " +
                "WHERE (fdn,oss_id) IN (%s) " +
                "AND utc_timestamp >= '%s' " +
                "AND utc_timestamp < '%s'",
                kpisToSelect, CELL_GUID_60_TABLE, cellsToSelect, startDateTime, endDateTime);
        final CheckedFunction0<Map<CellIdentifier, Map<String, Map<String, Object>>>> kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery,
                new CellHourlyFlmKpiHandler(FDN, OSS_ID, kpiNames, UTC_TIMESTAMP),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    private static String wrapStringInQuotes(final List<String> stringsToWrap) {
        return String.format("('%s', %s)", stringsToWrap.get(0), stringsToWrap.get(1));
    }
}
