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

import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.FDN;
import static com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants.OSS_ID;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.sonom.flm.database.handlers.CellFlmKpiHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpiHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link KpiCellFlmDao}.
 */
public class KpiCellFlmDaoImpl implements KpiCellFlmDao {

    private static final String KPI_CELL_GUID_60_TABLE = "kpi_cell_guid_60";
    private static final String KPI_CELL_GUID_1440_TABLE = "kpi_cell_guid_1440";
    private static final String KPI_CELL_SECTOR_FLM_1440 = "kpi_cell_sector_flm_1440";
    private static final String CELL_SECTOR_FLM_QUERY = "SELECT cell_fdn as fdn, cell_oss_id as oss_id, local_timestamp, "
            + "%s FROM %s WHERE execution_id = '%s'";
    private static final String CELL_GUID_QUERY = "SELECT fdn, oss_id, local_timestamp, %s FROM %s WHERE local_timestamp = '%s'";
    private static final DatabaseAccess KPI_DB_INTERNAL_ACCESS = new KpiDatabaseAccess();
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();
    private final DatabaseRetry databaseRetry;

    public KpiCellFlmDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public Map<CellKpi, CellKpis> getNotVisibleCellHourlyKpis(final List<String> kpiNames, final String localTimeStamp) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, ", ");
        final String dbQuery = String.format(CELL_GUID_QUERY, kpisToSelect, KPI_CELL_GUID_60_TABLE, localTimeStamp);
        final CheckedFunction0<Map<CellKpi, CellKpis>> kpisForCells = () -> KPI_DB_INTERNAL_ACCESS.executeQuery(dbQuery,
                new CellKpiHandler(),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellKpi, Map<String, Object>> getNotVisibleCellDailyKpis(final String executionDay, final List<String> kpiNames) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, ", ");
        final String dbQuery = String.format(CELL_GUID_QUERY, kpisToSelect, KPI_CELL_GUID_1440_TABLE, executionDay);
        final CheckedFunction0<Map<CellKpi, Map<String, Object>>> kpisForCells = () -> KPI_DB_INTERNAL_ACCESS.executeQuery(dbQuery,
                new CellFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }

    @Override
    public Map<CellKpi, Map<String, Object>> getNotVisibleCellSectorDailyFlmKpis(final String executionId,
            final List<String> kpiNames) throws SQLException {
        final String kpisToSelect = StringUtils.join(kpiNames, ", ");
        final String dbQuery = String.format(CELL_SECTOR_FLM_QUERY, kpisToSelect, KPI_CELL_SECTOR_FLM_1440, executionId);
        final CheckedFunction0<Map<CellKpi, Map<String, Object>>> kpisForCells = () -> KPI_DB_INTERNAL_ACCESS.executeQuery(dbQuery,
                new CellFlmKpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }
}