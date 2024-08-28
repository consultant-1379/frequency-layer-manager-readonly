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
package com.ericsson.oss.services.sonom.flm.test;

import static com.ericsson.oss.services.sonom.flm.test.util.TestDataBuilders.buildEmptyLoadBalancingQuanta;
import static com.ericsson.oss.services.sonom.flm.test.util.TestDataBuilders.buildSampleLoadBalancingQuanta;
import static com.ericsson.oss.services.sonom.flm.test.util.TestDataBuilders.buildSampleTargetCells;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDao;
import com.ericsson.oss.services.sonom.flm.database.retention.RetentionDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.test.util.TestDataBuilders.PolicyOutputEventBuilder;

@RunWith(OrderedTestRunner.class)
public class FlmOptimizationsDaoIT {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final ExecutionDao executionDao = new ExecutionDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final OptimizationsDao optimizationsDao = new OptimizationsDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);
    private static final RetentionDao retentionDao = new RetentionDaoImpl();
    private static final String EXEC_ID = "exec_id_1";
    private static final String EXEC_ID_TWO = "exec_id_2";
    private static final String EXEC_ID_THREE = "exec_id_3";

    private static final String EXEC_ID_FOUR = "exec_id_4";
    private static final String EXEC_ID_FIVE = "exec_id_5";
    private static final String EXEC_ID_SIX = "exec_id_6";
    private static final String EXEC_ID_SEVEN = "exec_id_7";
    private static final String EXEC_ID_EIGHT = "exec_id_8";
    private static final Long SECTOR_ID = 10L;
    private static final Long SECTOR_ID_TWO = 20L;

    private static final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    private static final LocalDateTime executionDateTime = currentTime.toLocalDateTime().minusDays(15);
    private static final LocalDateTime retentionDateTime = currentTime.toLocalDateTime().minusDays(14);

    @Test
    @InSequence(1)
    public void whenInsertingTwoOptimizations_thenWeGetBackTwoOptimizations() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        final PolicyOutputEvent modifiedPolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID_TWO)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        int nrOfRecordsInserted = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(modifiedPolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        final List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizations(EXEC_ID);
        assertThat(optimizations).hasSize(2);
    }

    @Test
    @InSequence(2)
    public void whenInsertingTwoOptimizations_thenWeGetBackTheCorrectNumberOfSectors() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_TWO));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_TWO, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        final PolicyOutputEvent modifiedPolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_TWO, SECTOR_ID_TWO)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        int nrOfRecordsInserted = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(modifiedPolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        final Integer sectors = optimizationsDao.getNumberOfPolicyOutputEvents(EXEC_ID_TWO);
        assertThat(sectors).isEqualTo(2);
    }

    @Test
    @InSequence(3)
    public void whenInsertingSameOptimizationTwice_thenWeGetBackOneOptimization() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_THREE));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_THREE, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        int nrOfRecordsInserted = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isZero();
        final List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizations(EXEC_ID_THREE);
        assertThat(optimizations).hasSize(1);
    }

    @Test
    @InSequence(4)
    public void whenLBQ_IsNull_thenNoExceptionIsThrown() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_FOUR));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_FOUR, SECTOR_ID)
                .build();
        final int nrOfRecordsUpdated = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsUpdated).isEqualTo(1);
        final List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizations(EXEC_ID_FOUR);
        assertThat(optimizations).hasSize(1);
    }

    @Test
    @InSequence(5)
    public void whenTargetCellsIsNull_thenNoExceptionIsThrown() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_FIVE));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_FIVE, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(null))
                .build();
        final int nrOfRecordsUpdated = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsUpdated).isEqualTo(1);
        final List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizations(EXEC_ID_FIVE);
        assertThat(optimizations).hasSize(1);
    }

    @Test
    @InSequence(6)
    public void whenInsertingOptimizationsForDifferentExecutions_thenWeGetBackTheCorrectNumberOfSectorsAndTheCorrectOptimizations()
            throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_SIX));
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_SEVEN));

        final PolicyOutputEvent policyOutputEventOne = new PolicyOutputEventBuilder(EXEC_ID_SIX, SECTOR_ID)
                .build();
        final PolicyOutputEvent policyOutputEventTwo = new PolicyOutputEventBuilder(EXEC_ID_SIX, SECTOR_ID_TWO)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        final PolicyOutputEvent policyOutputEventThree = new PolicyOutputEventBuilder(EXEC_ID_SEVEN, SECTOR_ID)
                .build();
        final PolicyOutputEvent policyOutputEventFour = new PolicyOutputEventBuilder(EXEC_ID_SEVEN, SECTOR_ID_TWO)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();

        // Check insert optimizations
        int nrOfRecordsInserted = optimizationsDao.insertOptimization(policyOutputEventOne);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(policyOutputEventTwo);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(policyOutputEventThree);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(policyOutputEventFour);
        assertThat(nrOfRecordsInserted).isEqualTo(1);

        // Check sectors
        Integer sectors = optimizationsDao.getNumberOfPolicyOutputEvents(EXEC_ID_SIX);
        assertThat(sectors).isEqualTo(2);

        sectors = optimizationsDao.getNumberOfPolicyOutputEvents(EXEC_ID_SEVEN);
        assertThat(sectors).isEqualTo(2);

        // Check optimizations
        List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizations(EXEC_ID_SIX);

        assertThat(optimizations).contains(policyOutputEventOne);
        assertThat(optimizations).contains(policyOutputEventTwo);
        optimizations = optimizationsDao.getOptimizations(EXEC_ID_SEVEN);
        assertThat(optimizations).contains(policyOutputEventThree);
        assertThat(optimizations).contains(policyOutputEventFour);

    }

    @Test
    @InSequence(7)
    public void whenInsertingOptimizationsWithEmptyLBQ_thenNoEmptyOptimizationsAreReceived() throws SQLException {
        executionDao.insert(buildDefaultExecutionObject(EXEC_ID_EIGHT));

        final PolicyOutputEvent samplePolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_EIGHT, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        final PolicyOutputEvent modifiedPolicyOutputEvent = new PolicyOutputEventBuilder(EXEC_ID_EIGHT, SECTOR_ID_TWO)
                .withLbq(buildEmptyLoadBalancingQuanta()).build();
        int nrOfRecordsInserted = optimizationsDao.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        nrOfRecordsInserted = optimizationsDao.insertOptimization(modifiedPolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        final List<PolicyOutputEvent> optimizations = optimizationsDao.getOptimizationsFiltered(EXEC_ID_EIGHT);
        assertThat(optimizations).hasSize(1);
    }

    @Test
    @InSequence(8)
    public void whenRetentionAppliedOnExecutionTable_thenOptimizationTableIsAlsoCleanedUp() throws SQLException {
        retentionDao.cleanUpFlmExecutionsTable(retentionDateTime, 140);

        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_TWO)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_THREE)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_FOUR)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_FIVE)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_SIX)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_SEVEN)).isEmpty();
        assertThat(optimizationsDao.getOptimizationsFiltered(EXEC_ID_EIGHT)).isEmpty();

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
