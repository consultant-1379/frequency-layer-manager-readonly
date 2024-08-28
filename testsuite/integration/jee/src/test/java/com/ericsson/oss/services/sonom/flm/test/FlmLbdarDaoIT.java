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
package com.ericsson.oss.services.sonom.flm.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LeakageCell;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDao;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;

@RunWith(OrderedTestRunner.class)
public class FlmLbdarDaoIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmLbdarDaoIT.class);

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final ExecutionDao executionDao = new ExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final LbdarDao lbdarDao = new LbdarDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final RetentionDao retentionDao = new RetentionDaoImpl();
    private static final String EXEC_ID = "exec_id_lbdar_1";
    private static final Long SECTOR_ID_1 = 10L;
    private static final Long SECTOR_ID_2 = 11L;
    private static final Long SECTOR_ID_3 = 12L;

    private static final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    private static final LocalDateTime executionDateTime = currentTime.toLocalDateTime().minusDays(15);
    private static final LocalDateTime retentionDateTime = currentTime.toLocalDateTime().minusDays(14);

    private static final LeakageCell LEAKAGE_CELL_1 = new LeakageCell(
            "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1",
            1);
    private static final LeakageCell LEAKAGE_CELL_2 = new LeakageCell(
            "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00002,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-2",
            1);

    @Test
    @InSequence(1)
    public void whenInsertingLeakageCells_thenInsertSucceeds() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID));
        final Set<LeakageCell> leakageCells = new HashSet<>();
        leakageCells.add(LEAKAGE_CELL_1);
        leakageCells.add(LEAKAGE_CELL_2);

        LOGGER.info("Inserting leakage cells");
        final int nrOfRecordsInserted = lbdarDao.insertLeakageCells(EXEC_ID, SECTOR_ID_1, leakageCells);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        LOGGER.info("Read leakage cells");

        final Set<LeakageCell> leakageCellRead = lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_1);
        assertThat(leakageCellRead).hasSize(leakageCells.size());
    }

    @Test
    @InSequence(2)
    public void whenInsertingLeakageCellsThatAlreadyExistsForAnExecutionAndSector_thenNoInsertPerformed() throws SQLException {

        final Set<LeakageCell> leakageCells = new HashSet<>();
        leakageCells.add(LEAKAGE_CELL_1);
        leakageCells.add(LEAKAGE_CELL_2);

        final int nrOfRecordsInserted = lbdarDao.insertLeakageCells(EXEC_ID, SECTOR_ID_1, leakageCells);
        assertThat(nrOfRecordsInserted).isEqualTo(0);
        final Set<LeakageCell> leakageCellRead = lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_1);
        assertThat(leakageCellRead).hasSize(leakageCells.size());
    }

    @Test
    @InSequence(3)
    public void whenInsertingEmptyLeakageCells_thenInsertSucceedsAndNoExceptionThrown() throws SQLException {

        final Set<LeakageCell> leakageCells = new HashSet<>();

        final int nrOfRecordsInserted = lbdarDao.insertLeakageCells(EXEC_ID, SECTOR_ID_2, leakageCells);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        final Set<LeakageCell> leakageCellRead = lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_2);
        assertThat(leakageCellRead).hasSize(leakageCells.size());
    }

    @Test
    @InSequence(4)
    public void whenInsertingNullLeakageCells_thenInsertSucceedsAndNoExceptionThrown() throws SQLException {

        final int nrOfRecordsInserted = lbdarDao.insertLeakageCells(EXEC_ID, SECTOR_ID_3, null);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        final Set<LeakageCell> leakageCellRead = lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_3);
        assertThat(leakageCellRead).isEmpty();
    }

    @Test
    @InSequence(5)
    public void whenRetentionAppliedOnExecutionTable_thenLbdarTableIsAlsoCleanedUp() throws SQLException {
        retentionDao.cleanUpFlmExecutionsTable(retentionDateTime, 140);
        assertThat(lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_1)).isEmpty();
        assertThat(lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_2)).isEmpty();
        assertThat(lbdarDao.getLeakageCells(EXEC_ID, SECTOR_ID_3)).isEmpty();
    }

    private Execution buildDefaultExecutionObject(final String executionId) {

        final Execution execution = new Execution();
        execution.setId(executionId);
        execution.setState(ExecutionState.SETTINGS_PROCESSING);
        execution.setRetryAttempts(0);
        execution.setSchedule("0 0 2 ? * * *");
        execution.setConfigurationId(1);
        execution.setCustomizedGlobalSettings(Collections.emptyMap());
        execution.setCustomizedDefaultSettings(Collections.emptyMap());
        execution.setGroups(Collections.emptyList());
        execution.setOpenLoop(false);
        execution.setInclusionList(Collections.emptyList());
        execution.setExclusionList(Collections.emptyList());
        execution.setWeekendDays("");
        execution.setNumSectorsToEvaluateForOptimization(0);
        execution.setNumOptimizationElementsSent(0);
        execution.setNumOptimizationElementsReceived(0);
        execution.setNumOptimizationLbqs(0);
        execution.setNumChangesWrittenToCmDb(0);
        execution.setNumChangesNotWrittenToCmDb(0);
        execution.setEnablePA(false);
        execution.setFullExecution(true);
        execution.setAdditionalExecutionInformation("");
        execution.setStartTime(Timestamp.valueOf(executionDateTime));
        execution.setStateModifiedTime(Timestamp.valueOf(executionDateTime));
        return execution;
    }
}
