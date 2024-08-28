/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.messagehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.kafka.consumer.CommitType;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.messagehandler.utils.PATestDataBuilder;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * Unit tests for {@link PAExecutionConsumer} class.
 */

@RunWith(MockitoJUnitRunner.class)
public class PAExecutionConsumerTest {

    private static final String ONE = "1";
    private static final int ONCE = 1;

    @Mock
    private ExecutionCounter counterMock;

    @Mock
    private KafkaConsumerWrapper consumerWrapperMock;

    @Mock
    private KafkaRecordHandler<PaPolicyOutputEvent> kafkaRecordHandlerMock;

    private ConsumerRecords<String, PaPolicyOutputEvent> testData;
    private PAExecutionConsumer objectUnderTest;

    @Before
    public void setUp() throws BadSetupException {
        objectUnderTest = new PAExecutionConsumer(1, buildCounters(), kafkaRecordHandlerMock);
        objectUnderTest.setKafkaConsumerWrapper(consumerWrapperMock);
    }

    @Test
    public void whenConsumeRecordsCalled_thenConsumeRecordOnWrapperCalled() throws BadSetupException {
        doNothing().when(consumerWrapperMock).consumeRecords();
        objectUnderTest.consumeRecords();
        verify(consumerWrapperMock, times(ONCE)).consumeRecords();
    }

    @Test
    public void whenHandleCalledAndCounterExists_thenRecordsHandledAndCommitted() {
        testData = PATestDataBuilder.buildConsumerRecords(ONE, ONE, 0, 0);
        when(counterMock.getCount()).thenReturn(1L);

        objectUnderTest.handle(testData);

        verify(counterMock, times(ONCE)).getCount();
        verify(counterMock, times(ONCE)).countDown(1L);
        verify(kafkaRecordHandlerMock, times(ONCE)).handle(any(PaPolicyOutputEvent.class));
        final ArgumentCaptor<Map<Integer, ConsumerRecord<String, PaPolicyOutputEvent>>> varArgs = ArgumentCaptor.forClass(Map.class);
        verify(consumerWrapperMock, times(ONCE)).commitRecords(eq(CommitType.SYNCHRONOUS), varArgs.capture());
        assertThat(varArgs.getValue()).hasSize(1);
        assertCommitsEqual(testData.records("topic").iterator().next(), varArgs.getValue());
    }

    @Test
    public void whenHandleCalledAndNoCounter_thenRecordIsNotHandled() {
        testData = PATestDataBuilder.buildConsumerRecords("2", ONE, 0, 0);

        objectUnderTest.handle(testData);

        verify(kafkaRecordHandlerMock, never()).handle(any(PaPolicyOutputEvent.class));
        final ArgumentCaptor<Map<Integer, ConsumerRecord<String, PaPolicyOutputEvent>>> varArgs = ArgumentCaptor.forClass(Map.class);
        verify(consumerWrapperMock, times(ONCE)).commitRecords(eq(CommitType.SYNCHRONOUS), varArgs.capture());
        assertThat(varArgs.getValue()).hasSize(1);
    }

    @Test
    public void whenHandleCalledWithNeededExecutionCounterZero_thenRecordIsNotHandled() {
        testData = PATestDataBuilder.buildConsumerRecords(ONE, ONE, 0, 0);
        when(counterMock.getCount()).thenReturn(0L);

        objectUnderTest.handle(testData);

        verify(counterMock, times(ONCE)).getCount();
        verify(kafkaRecordHandlerMock, never()).handle(any(PaPolicyOutputEvent.class));
        final ArgumentCaptor<Map<Integer, ConsumerRecord<String, PolicyOutputEvent>>> varArgs = ArgumentCaptor.forClass(Map.class);
        verify(consumerWrapperMock, times(ONCE)).commitRecords(eq(CommitType.SYNCHRONOUS), varArgs.capture());
        assertThat(varArgs.getValue()).hasSize(1);
    }

    @Test
    public void whenShutdownCalled_thenShutdownOnWrapperCalled() {
        doNothing().when(consumerWrapperMock).shutdown();
        objectUnderTest.shutDown();
        verify(consumerWrapperMock, times(ONCE)).shutdown();
    }

    @Test
    public void whenValidatingEvent_andExecutionIdIsNull_andSectorIdIsSet_thenEventFailsValidation() {
        assertThat(objectUnderTest.isValidEvent(PATestDataBuilder.buildConsumerRecord(null, ONE, 0, 0)))
                .isFalse();
    }

    @Test
    public void whenValidatingEvent_andExecutionIdIsSet_andSectorIdIsNull_thenEventFailsValidation() {
        assertThat(objectUnderTest.isValidEvent(PATestDataBuilder.buildConsumerRecord(ONE, null, 0, 0)))
                .isFalse();
    }

    @Test
    public void whenValidatingEvent_andExecutionIdIsSet_andSectorIdIsSet_thenEventSucceedsValidation() {
        assertThat(objectUnderTest.isValidEvent(PATestDataBuilder.buildConsumerRecord(ONE, ONE, 0, 0)))
                .isTrue();
    }

    private ConcurrentMap<String, ExecutionCounter> buildCounters() {
        final ConcurrentMap<String, ExecutionCounter> result = new ConcurrentHashMap<>();
        result.put(ONE, counterMock);
        return result;
    }

    private void assertCommitsEqual(final ConsumerRecord<String, PaPolicyOutputEvent> expectedConsumerRecord,
            final Map<Integer, ConsumerRecord<String, PaPolicyOutputEvent>> actualCommit) {
        assertThat(actualCommit).containsOnly(entry(0, expectedConsumerRecord));
    }
}
