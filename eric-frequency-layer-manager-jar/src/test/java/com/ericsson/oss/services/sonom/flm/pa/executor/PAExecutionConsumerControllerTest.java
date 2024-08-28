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

package com.ericsson.oss.services.sonom.flm.pa.executor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.messagehandler.PAExecutionConsumer;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.util.PaPolicyOutputEventBuilder;

/**
 * Unit Tests for {@link PAExecutionConsumerController} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAExecutionConsumerControllerTest {

    public static final String EXECUTION_ID = "1";
    private static final String FLM_PA_EXECUTION_ID = "FLM_PA_execution_one";

    @Mock
    private KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaConsumerWrapperMock;

    @Mock
    private PAOutputEventDao paOutputEventDaoMock;

    @Captor
    private ArgumentCaptor<KafkaRecordHandler<ConsumerRecords<String, PaPolicyOutputEvent>>> kafkaRecordHandlerCaptor;

    private PAExecutionConsumerController executionConsumerController;

    private List<PAOutputEventDao> paOutputEventDaoMocks;

    @Before
    public void setUp() {
        paOutputEventDaoMocks = IntStream.range(0, 10)
                .mapToObj(i -> paOutputEventDaoMock)
                .collect(Collectors.toList());
    }

    @Test
    public void whenInitialized_thenPAExecutionConsumerHandlerStarted() {
        executionConsumerController = new PAExecutionConsumerController(kafkaConsumerWrapperMock, paOutputEventDaoMocks);
        assertThat(executionConsumerController.getPAExecutionConsumerHandler()).isNotNull();
    }

    @Test
    public void whenPAPolicyOutputEventReceived_thenOutputIsWrittenToDB() throws BadSetupException, InterruptedException, SQLException {

        executionConsumerController = new PAExecutionConsumerController(kafkaConsumerWrapperMock, paOutputEventDaoMocks);

        verify(kafkaConsumerWrapperMock, times(10)).setKafkaRecordHandler(kafkaRecordHandlerCaptor.capture());
        new Thread(() -> executionConsumerController.getPAExecutionConsumerHandler().consumeMessagesForExecution(FLM_PA_EXECUTION_ID, 10,
                Collections.emptySet())).start();
        final PAExecutionConsumer latestExecutionConsumer = (PAExecutionConsumer) kafkaRecordHandlerCaptor.getValue();

        final Map<TopicPartition, List<ConsumerRecord<String, PaPolicyOutputEvent>>> consumerRecords = new HashMap<>();

        final PaPolicyOutputEvent paPolicyOutputEvent = PaPolicyOutputEventBuilder.buildPaPolicyOutputEvent();
        consumerRecords.put(new TopicPartition("topic", 1),
                Collections.singletonList(
                        new ConsumerRecord<>("topic", 0, 0, "key",
                                paPolicyOutputEvent)));

        SECONDS.sleep(2);

        final ConsumerRecords<String, PaPolicyOutputEvent> testData = new ConsumerRecords<>(consumerRecords);

        SECONDS.sleep(2);

        latestExecutionConsumer.handle(testData);

        verify(paOutputEventDaoMock).insertPaPolicyOutputEvent(any());
    }
}
