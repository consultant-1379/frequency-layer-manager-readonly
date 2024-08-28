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

package com.ericsson.oss.services.sonom.flm.pa.policy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyInputEvent;

/**
 * Unit tests for {@link FlmPaPolicyInputEventHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlmPaPolicyInputEventHandlerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    public PAExecution mockPAExecution;

    @Mock
    public PAExecutionDao mockPAExecutionDao;

    @Mock
    public KafkaMessageProducer mockKafkaMessageProducer;

    @Mock
    public CountDownLatch mockCountDownLatch;

    public FlmPaPolicyInputEventHandler objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new FlmPaPolicyInputEventHandler(mockPAExecutionDao);
    }

    @Test
    public void whenSendToKafkaTopicIsCalled_thenExecutionSuccessful() throws Exception {
        final FlmPaPolicyInputEventHandler spy = spy(objectUnderTest);
        doReturn(mockKafkaMessageProducer).when(spy).getKafkaMessageProducer(any());
        when(mockPAExecutionDao.update(any())).thenReturn(1);
        doNothing().when(mockKafkaMessageProducer).sendKafkaMessagesBatch(any(), any());
        doReturn(mockCountDownLatch).when(spy).getLatch(anyList());
        when(mockCountDownLatch.await(anyLong(), any())).thenReturn(true);
        when(mockPAExecution.getId()).thenReturn("001_1");

        final PaPolicyInputEvent paPolicyInputEvent = new PaPolicyInputEvent("001", "001_1", 1,
                new Sector("202", new HashMap<>(), new HashMap<>(), new ArrayList<>()));

        spy.sendToKafkaTopic(Arrays.asList(paPolicyInputEvent.toJson()), mockPAExecution);

        verify(spy, times(1)).getKafkaMessageProducer(any());
        verify(mockPAExecutionDao, times(1)).update(any());
        verify(mockKafkaMessageProducer, times(1)).sendKafkaMessagesBatch(any(), any());
        verify(spy, times(1)).getLatch(anyList());
    }

}