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
package com.ericsson.oss.services.sonom.flm.database.optimization;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.OverlapInfoHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

import io.vavr.CheckedFunction0;

/**
 * Implementation of {@link OptimizationsWithOverlapInfoDao}.
 */
public class OptimizationsWithOverlapInfoDaoImpl implements OptimizationsWithOverlapInfoDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizationsWithOverlapInfoDaoImpl.class);
    private static final String FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {} - {}";
    private static final int DROP_MINUTES = 15;
    private static final int LOG_HOURS = 23;
    private static final String EMPTY_LBQ = "{\"sourceCellFdn\":\"\",\"sourceCellOssId\":-1,\"sourceUsersMove\":\"\"," +
            "\"targetCells\":[{\"targetCellFdn\":\"\",\"targetCellOssId\":-1,\"targetUsersMove\":\"\"}]}";
    private final DatabaseRetry databaseRetry;
    private final DatabaseAccess databaseAccess = new FlmDatabaseAccess();

    public OptimizationsWithOverlapInfoDaoImpl(final int maxRetryAttempts, final int retryWaitDurationInSec) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDurationInSec);
    }

    @Override
    public List<Pair<PolicyOutputEvent, OverlapInfo>> getOptimizationsWithOverlapInfo(final String executionId) throws SQLException {
        LOGGER.debug("Retrieving optimizations with overlap information for execution {}", executionId);
        final CheckedFunction0<List<Pair<PolicyOutputEvent, OverlapInfo>>> optimizationsWithOverlap = () -> retrieveOptimizationsWithOverlapInfo(
                executionId);
        return databaseRetry.executeWithRetryAttempts(optimizationsWithOverlap);
    }

    private List<Pair<PolicyOutputEvent, OverlapInfo>> retrieveOptimizationsWithOverlapInfo(final String executionId) throws SQLException {
        final String query = String.format("select" +
                "    opt.sector_id, " +
                "    opt.execution_id, " +
                "    opt.lbq, " +
                "    opt.created, " +
                "    fin.overlapped_executions, " +
                "    case when (fin.time_diff is null) then 'not_overlapping' " +
                "         when (fin.time_diff <= %d) then 'overlap_drop_needed' " +
                "         else 'overlap_log_needed' " +
                "    end as overlap " +
                "from flm_optimizations as opt " +
                "left join ( " +
                "    select " +
                "        overlapping.sector_id, " +
                "        overlapping.execution_id, " +
                "        time_diff, " +
                "        overlapping.overlapped_executions " +
                "    from ( " +
                "        select " +
                "            optim.execution_id, " +
                "            optim.sector_id, " +
                "            string_agg(other_execs.execution_id, ',') overlapped_executions, " +
                "            min(abs(extract (EPOCH from (this_exec.start_time - other_execs.start_time)))) as time_diff " +
                "        from flm_optimizations optim " +
                "        inner join flm_executions this_exec on optim.execution_id = this_exec.id " +
                "        left join ( " +
                "            select exec_sector.*, flm_executions.start_time " +
                "            from ( " +
                "                select distinct cell_confs.execution_id, cell_confs.sector_id " +
                "                from ( " +
                "                    select execution_id, sector_id " +
                "                    from cell_configuration where exclusion_list is NULL" +
                "                    union " +
                "                    select execution_id, sector_id " +
                "                    from cell_configuration_history where exclusion_list is NULL) cell_confs) exec_sector " +
                "            inner join flm_executions on exec_sector.execution_id = flm_executions.id) other_execs  " +
                "            on optim.execution_id <> other_execs.execution_id " +
                "                and optim.sector_id = other_execs.sector_id " +
                "                and optim.execution_id = '%s' " +
                "    where this_exec.id = '%s' " +
                "       and (other_execs.execution_id is null or abs(extract (EPOCH from (this_exec.start_time - other_execs.start_time))) <= %d ) " +
                "    group by  " +
                "       optim.execution_id, " +
                "       optim.sector_id, " +
                "       optim.lbq::text ) overlapping) fin " +
                "    on fin.sector_id=opt.sector_id " +
                "    where opt.execution_id = '%s' " +
                "       and opt.lbq::text != '%s' ;", DROP_MINUTES * 60, executionId, executionId, LOG_HOURS * 60 * 60, executionId, EMPTY_LBQ);
        try {
            return databaseAccess.executeQuery(query, new OverlapInfoHandler());
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.error(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }
}
