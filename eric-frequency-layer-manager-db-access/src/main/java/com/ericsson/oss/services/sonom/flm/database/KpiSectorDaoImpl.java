/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.database;

import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.CELL_AVAILABILITY_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.FDN_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.GUID_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.KPI_CELL_GUID_1440_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.KPI_SECTOR_TABLE;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.OSS_ID_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.REFERENCE_CELL_SECTOR_FDN_COLUMN;
import static com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants.SECTOR_ID_COLUMN;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.handlers.KpiSectorIdRefCellMapHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.KpiSectorIdSetHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link KpiSectorDao}.
 */
public class KpiSectorDaoImpl implements KpiSectorDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiSectorDaoImpl.class);
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new KpiDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public KpiSectorDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public Set<Long> getKpiSectorIdsWithRefCell() throws SQLException {
        LOGGER.debug("Retrieving all KPI Sectors");
        final CheckedFunction0<Set<Long>> allSectorIdsFromKpiSectorTable = this::retrieveSectorIdsWithReferenceCellDefined;
        return databaseRetry.executeWithRetryAttempts(allSectorIdsFromKpiSectorTable);
    }

    @Override
    public Set<Long> getKpiSectorIdsWithUnavailableRefCell(final LocalDateTime localDateTime) throws SQLException {
        LOGGER.debug("Retrieving all KPI Sectors whose reference cell is unavailable");
        final CheckedFunction0<Set<Long>> allSectorIdsFromKpiSectorTable = () -> retrieveSectorIdsWithUnavailableRefCell(localDateTime);
        return databaseRetry.executeWithRetryAttempts(allSectorIdsFromKpiSectorTable);
    }

    @Override
    public Map<Long, String> getSectorIdsAndRefCell() throws SQLException {
        LOGGER.debug("Retrieving all KPI Sectors and their corresponding reference cell");
        final CheckedFunction0<Map<Long, String>> allSectorIdsAndRefCellFromKpiSectorTable = this::retrieveSectorIdsAndCorrespondingRefCell;
        return databaseRetry.executeWithRetryAttempts(allSectorIdsAndRefCellFromKpiSectorTable);
    }

    private Set<Long> retrieveSectorIdsWithReferenceCellDefined() throws SQLException {
        final String query = String.format("SELECT %s FROM %s WHERE %s IS NOT NULL", SECTOR_ID_COLUMN, KPI_SECTOR_TABLE,
                REFERENCE_CELL_SECTOR_FDN_COLUMN);
        return retrieveSectorIdsForQuery(query, NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
    }

    private Set<Long> retrieveSectorIdsWithUnavailableRefCell(final LocalDateTime localDateTime) throws SQLException {

        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int CELL_AVAILABILITY_PARAMETER = 1;
            private static final int LOCAL_TIMESTAMP_PARAMETER = 2;
            private static final int HAVING_COUNT_PARAMETER = 3;

            private static final int CELL_UNAVAILABLE_VALUE = 0;
            private static final int DAYS_CELL_OUT_OF_SERVICE_VALUE = 5;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(CELL_AVAILABILITY_PARAMETER, CELL_UNAVAILABLE_VALUE);
                preparedStatement.setTimestamp(LOCAL_TIMESTAMP_PARAMETER, Timestamp.valueOf(localDateTime));
                preparedStatement.setInt(HAVING_COUNT_PARAMETER, DAYS_CELL_OUT_OF_SERVICE_VALUE);
                return preparedStatement;
            }
        };

        final String query = String.format(
                "SELECT %s.%s FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ? AND %s.%s >= ? GROUP BY %s, %s, %s, %s HAVING COUNT(*)=?",
                KPI_SECTOR_TABLE, SECTOR_ID_COLUMN, KPI_CELL_GUID_1440_TABLE,
                KPI_SECTOR_TABLE, KPI_CELL_GUID_1440_TABLE, FDN_COLUMN,
                KPI_SECTOR_TABLE, REFERENCE_CELL_SECTOR_FDN_COLUMN,
                KPI_CELL_GUID_1440_TABLE, CELL_AVAILABILITY_COLUMN,
                KPI_CELL_GUID_1440_TABLE, LOCAL_TIMESTAMP_COLUMN,
                GUID_COLUMN, FDN_COLUMN, OSS_ID_COLUMN, SECTOR_ID_COLUMN);
        return retrieveSectorIdsForQuery(query, preparedStatementHandler);
    }

    private Map<Long, String> retrieveSectorIdsAndCorrespondingRefCell() throws SQLException {
        final String query = String.format(
                "SELECT %s, %s FROM %s WHERE %s IS NOT NULL",
                SECTOR_ID_COLUMN, REFERENCE_CELL_SECTOR_FDN_COLUMN, KPI_SECTOR_TABLE, REFERENCE_CELL_SECTOR_FDN_COLUMN);

        return retrieveSectorIdsAndRefCellForQuery(query, NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
    }

    private Set<Long> retrieveSectorIdsForQuery(final String query, final PreparedStatementHandler preparedStatementHandler)
            throws SQLException {
        try {
            final Set<Long> kpiSectors = databaseAccess.executeQuery(query,
                    new KpiSectorIdSetHandler(), preparedStatementHandler);
            LOGGER.debug("Retrieved Kpi Sector ID's {}", kpiSectors);
            LOGGER.info("Retrieved number of Kpi Sector ID's: {}", kpiSectors.size());
            return kpiSectors;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to execute query {}, got exception {}, retrying", query, e.getClass());
            throw e;
        }
    }

    private Map<Long, String> retrieveSectorIdsAndRefCellForQuery(final String query, final PreparedStatementHandler preparedStatementHandler)
            throws SQLException {
        try {
            final Map<Long, String> kpiSectorsAndRefCell = databaseAccess.executeQuery(query,
                    new KpiSectorIdRefCellMapHandler(), preparedStatementHandler);
            LOGGER.debug("Retrieved KPI sector IDs and reference cells {}", kpiSectorsAndRefCell);
            LOGGER.info("Retrieved number of KPI sector IDs and reference cells: {}", kpiSectorsAndRefCell.size());
            return kpiSectorsAndRefCell;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to execute query {}, got exception {}, retrying", query, e.getMessage());
            throw e;
        }
    }
}
