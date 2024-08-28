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

package com.ericsson.oss.services.sonom.flm.messagehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit tests for {@link PoeExecutionConsumerHandlerImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PoeExecutionConsumerHandlerImplTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    KafkaConsumerWrapper<PolicyOutputEvent> wrapperMock;

    private PoeExecutionConsumerHandlerImpl objectUnderTest;

    @Test
    public void whenConstructorCalledWithProperArguments_thenNoExceptionIsThrown() throws Exception {
        new PoeExecutionConsumerHandlerImpl(getRecordHandlers(1), wrapperMock);
        verify(wrapperMock, times(2)).setKafkaRecordHandler(any());
    }

    @Test
    public void whenConstructorCalledWithTooFewRecordHandlers_thenExceptionIsThrown() throws Exception {
        thrown.expect(BadSetupException.class);
        new PoeExecutionConsumerHandlerImpl(Collections.emptyList(), wrapperMock);
    }

    @Test
    public void whenConstructorCalledWithTooMuchRecordHandlers_thenExceptionIsThrown() throws Exception {
        thrown.expect(BadSetupException.class);
        new PoeExecutionConsumerHandlerImpl(getRecordHandlers(11), wrapperMock);
    }

    @Test
    public void whenConsumeMessagesCalled_thenCountersAreCreated() throws KafkaConsumerInstantiationException, BadSetupException {
        objectUnderTest = new PoeExecutionConsumerHandlerImpl(getRecordHandlers(3), wrapperMock);
        objectUnderTest.consumeMessagesForExecution("1", 10, Sets.newHashSet(Arrays.asList(1L, 2L, 3L)));
        objectUnderTest.consumeMessagesForExecution("2", 20, Sets.newHashSet(Arrays.asList(1L, 2L)));
        objectUnderTest.removeExecution("3");
        assertThat(objectUnderTest.getCounters()).containsOnlyKeys("1", "2");
        assertThat(objectUnderTest.getCounters().get("1").getCount()).isEqualTo(7);
        assertThat(objectUnderTest.getCounters().get("2").getCount()).isEqualTo(18);
    }

    @Test
    public void whenShutdownCalled_thenNoExceptionIsThrown() throws KafkaConsumerInstantiationException, BadSetupException {
        objectUnderTest = new PoeExecutionConsumerHandlerImpl(getRecordHandlers(1), wrapperMock);
        objectUnderTest.shutdown();
        verify(wrapperMock, times(2)).shutdown();
    }


    private List<KafkaRecordHandler<PolicyOutputEvent>> getRecordHandlers(final int numberOfRecordHandlers) {
        final List<KafkaRecordHandler<PolicyOutputEvent>> recordHandlers = new ArrayList<>();
        for (int i = 0; i < numberOfRecordHandlers; i++) {
            recordHandlers.add(policyOutputEvent -> {
            });
        }
        recordHandlers.add(policyOutputEvent -> {
        });
        return recordHandlers;
    }
}