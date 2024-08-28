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
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * Unit Tests for {@link PAExecutionConsumerHandlerImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAExecutionConsumerHandlerImplTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    KafkaConsumerWrapper<PaPolicyOutputEvent> wrapperMock;

    private PAExecutionConsumerHandlerImpl objectUnderTest;

    @Test
    public void whenConstructorCalledWithProperArguments_thenNoExceptionIsThrown() throws Exception {
        new PAExecutionConsumerHandlerImpl(getRecordHandlers(2), wrapperMock);
        verify(wrapperMock, times(3)).setKafkaRecordHandler(any());
    }

    @Test
    public void whenConstructorCalledWithTooFewRecordHandlers_thenExceptionIsThrown() throws Exception {
        thrown.expect(BadSetupException.class);
        new PAExecutionConsumerHandlerImpl(Collections.emptyList(), wrapperMock);
    }

    @Test
    public void whenConstructorCalledWithTooManyRecordHandlers_thenExceptionIsThrown() throws Exception {
        thrown.expect(BadSetupException.class);
        new PAExecutionConsumerHandlerImpl(getRecordHandlers(12), wrapperMock);
    }

    @Test
    public void whenConsumerMessagesCalled_theCountersAreCreated() throws KafkaConsumerInstantiationException, BadSetupException {
        objectUnderTest = new PAExecutionConsumerHandlerImpl(getRecordHandlers(3), wrapperMock);
        objectUnderTest.consumeMessagesForExecution("1", 10, Sets.newHashSet(Arrays.asList(1L, 2L, 3L, 4L)));
        objectUnderTest.consumeMessagesForExecution("2", 20, Sets.newHashSet(Arrays.asList(1L, 2L, 3L)));
        objectUnderTest.removeExecution("3");
        assertThat(objectUnderTest.getCounters()).containsOnlyKeys("1", "2");
        assertThat(objectUnderTest.getCounters().get("1").getCount()).isEqualTo(6);
        assertThat(objectUnderTest.getCounters().get("2").getCount()).isEqualTo(17);
    }

    @Test
    public void whenShutdownCalled_thenNoExceptionIsThrown() throws KafkaConsumerInstantiationException, BadSetupException {
        objectUnderTest = new PAExecutionConsumerHandlerImpl(getRecordHandlers(1), wrapperMock);
        objectUnderTest.shutdown();
        verify(wrapperMock, times(2)).shutdown();
    }

    private List<KafkaRecordHandler<PaPolicyOutputEvent>> getRecordHandlers(final int numberOfRecordHandlers) {
        final List<KafkaRecordHandler<PaPolicyOutputEvent>> recordHandlers = new ArrayList<>();
        for (int i = 0; i < numberOfRecordHandlers; i++) {
            recordHandlers.add(paPolicyOutputEvent -> {
            });
        }
        recordHandlers.add(paPolicyOutputEvent -> {
        });
        return recordHandlers;
    }
}
