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
 *------------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.sonom.flm.database.handlers.KpiHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

import io.vavr.CheckedFunction0;
import io.vavr.Tuple2;

/**
 * Class to implement methods of {@link KpiCombinedViewDao}.
 */
public class KpiCombinedViewDaoImpl implements KpiCombinedViewDao {

    private static final String CELL_GUID_60_VIEW = "cell_guid_60_kpis";
    private static final String CELL_FLM_60_VIEW = "cell_guid_flm_60_kpis";
    private static final String CELL_SECTOR_60_VIEW = "cell_sector_60_kpis";
    private static final String SECTOR_BUSY_HOUR_VIEW = "sector_1440_kpis";
    private static final String COMMA_DELIMITER = ", ";
    private static final String FDN = "fdn";
    private static final String OSS_ID = "oss_id";
    private static final DatabaseAccess KPI_DB_ACCESS = new KpiDatabaseExternalAccess();
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();

    private final DatabaseRetry databaseRetry;

    public KpiCombinedViewDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> getKpis(final List<String> sectorIds,
            final String executionId, final String startDateTime, final String endDateTime,
            final List<String> kpiNames) throws SQLException {

        final String kpisToSelect = StringUtils.join(kpiNames, COMMA_DELIMITER);
        final String dbQuery = String.format(
                "SELECT " +
                    "D.sector_id, sector_busy_hour, A.fdn, A.oss_id, A.local_timestamp, %s" +
                " FROM " +
                    "%s A" + //cell_guid_60_kpis
                    " FULL OUTER JOIN %s B " + //cell_guid_flm_60_kpis
                        " ON " +
                            "A.fdn = B.fdn AND " +
                            "A.oss_id = B.oss_id AND " +
                            "A.local_timestamp = B.local_timestamp " +
                    " FULL OUTER JOIN %s C " + //cell_sector_60_kpis
                        " ON " +
                            "A.fdn = C.cell_fdn AND " +
                            "A.oss_id = C.cell_oss_id AND " +
                            "A.local_timestamp = C.local_timestamp" +
                    " FULL OUTER JOIN %s D " + //sector_1440_kpis
                        " ON " +
                            "C.sector_id = D.sector_id " +
                " WHERE " +
                    "C.local_timestamp = D.sector_busy_hour AND " +
                    "A.local_timestamp >= '%s' AND A.local_timestamp < '%s' AND " +
                    "C.local_timestamp >= '%s' AND C.local_timestamp < '%s' AND " +
                    "D.local_timestamp >= '%s' AND D.local_timestamp < '%s' AND " +
                    "C.sector_id IN (%s) AND " +
                    "D.sector_id IN (%s) AND " +
                    "B.execution_id = '%s'",
                kpisToSelect, CELL_GUID_60_VIEW, CELL_FLM_60_VIEW, CELL_SECTOR_60_VIEW, SECTOR_BUSY_HOUR_VIEW,
                startDateTime, endDateTime, startDateTime, endDateTime, startDateTime, endDateTime,
                StringUtils.join(sectorIds, ","), StringUtils.join(sectorIds, ","), executionId);

        final CheckedFunction0<Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>>>
                kpisForCells = () -> KPI_DB_ACCESS.executeQuery(dbQuery, new KpiHandler(FDN, OSS_ID, kpiNames),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(kpisForCells);
    }
}
