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

package com.ericsson.oss.services.sonom.flm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;

/**
 * Unit tests for {@link FlmPolicyInputEventHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmPolicyInputEventHandlerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    public Execution mockExecution;
    @Mock
    public ExecutionDao mockExecutionDao;
    @Mock
    public KafkaMessageProducer mockKafkaMessageProducer;
    @Mock
    public CountDownLatch mockCountDownLatch;

    private FlmPolicyInputEventHandler spy;

    @Before
    public void setUp() {
        final FlmPolicyInputEventHandler objectUnderTest = new FlmPolicyInputEventHandler(mockExecutionDao);
        spy = spy(objectUnderTest);
    }

    @Test
    public void whenSendToKafkaTopicIsCalled_thenExecutionSuccessFull() throws SQLException, FlmAlgorithmException, InterruptedException {
        doReturn(mockKafkaMessageProducer).when(spy).getKafkaMessageProducer(any());
        when(mockExecutionDao.update(any())).thenReturn(1);
        doNothing().when(mockKafkaMessageProducer).sendKafkaMessagesBatch(any(), any());
        doReturn(mockCountDownLatch).when(spy).getLatch(anyList());
        when(mockCountDownLatch.await(anyLong(), any())).thenReturn(true);

        final List<OptimizationCell> optimizationCellList = Collections.singletonList(getOptimizationCell());
        final PolicyInputEvent policyInputEvent = new PolicyInputEvent(optimizationCellList, "101", "202");

        spy.sendToKafkaTopic(Collections.singletonList(policyInputEvent.toJson()), mockExecution);

        verify(spy, times(1)).getKafkaMessageProducer(any());
        verify(mockExecutionDao, times(1)).update(any());
        verify(mockKafkaMessageProducer, times(1)).sendKafkaMessagesBatch(any(), any());
        verify(spy, times(1)).getLatch(anyList());
    }

    @Test
    public void whenUpdateNumberOfOptimizationElementsSentIsCalledAndZeroUpdatedRecord_thenExceptionIsThrown() throws FlmAlgorithmException,
            SQLException {
        when(mockExecutionDao.update(any())).thenReturn(0);

        thrown.expect(FlmAlgorithmException.class);
        thrown.expectMessage("Error persisting number of optimization elements sent");
        spy.updateNumberOfOptimizationElementsSent(mockExecution);
    }

    private OptimizationCell getOptimizationCell() {
        final String cellFdn = "SubNetwork=ONRM_ROOT_MO,SubNetwork=Athlone,MeContext=Ath_Node1," +
                "ManagedElement=Ath_Node1,ENodeBFunction=1,EUtranCellFDD=cell-001";
        final int ossId = 1;

        return new OptimizationCell(cellFdn, ossId, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
    }

}
