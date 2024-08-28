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

package com.ericsson.oss.services.sonom.flm.optimization;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.messagehandler.PoeExecutionConsumer;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit Tests for {@link ExecutionConsumerController} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionConsumerControllerTest {
    public static final String EXECUTION_ID = "1";
    public static final int CELL_OSS_ID = 1;

    @Mock
    private KafkaConsumerWrapper<PolicyOutputEvent> kafkaConsumerWrapperMock;

    @Mock
    private OptimizationsDao optimizationsDaoMock;

    @Captor
    private ArgumentCaptor<KafkaRecordHandler<ConsumerRecords<String, PolicyOutputEvent>>> kafkaRecordHandlerCaptor;

    private ExecutionConsumerController executionConsumerController;

    private List<OptimizationsDao> optimizationsDaoMocks;

    @Before
    public void setUp() {
        optimizationsDaoMocks = IntStream.range(0, 10)
                .mapToObj(i -> optimizationsDaoMock)
                .collect(Collectors.toList());
    }

    @Test
    public void whenInitialized_thenExecutionConsumerHandlerStarted() {
        executionConsumerController = new ExecutionConsumerController(kafkaConsumerWrapperMock, optimizationsDaoMocks);
        assertThat(executionConsumerController.getExecutionConsumerHandler()).isNotNull();
    }

    @Test
    public void whenPolicyOutputEventReceived_thenOptimizationIsWrittenToDB() throws BadSetupException, InterruptedException, SQLException {
        final ExecutionSummary execution = new ExecutionSummary();
        execution.setId(EXECUTION_ID);
        execution.setState(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE);

        executionConsumerController = new ExecutionConsumerController(kafkaConsumerWrapperMock, optimizationsDaoMocks);

        verify(kafkaConsumerWrapperMock, times(10)).setKafkaRecordHandler(kafkaRecordHandlerCaptor.capture());

        final Set<Long> sectorIds = new HashSet<>();
        sectorIds.add(1L);
        new Thread(() -> executionConsumerController.getExecutionConsumerHandler().consumeMessagesForExecution(EXECUTION_ID, 10,
                Collections.emptySet())).start();
        final PoeExecutionConsumer latestExecutionConsumer = (PoeExecutionConsumer) kafkaRecordHandlerCaptor.getValue();
        final Map<TopicPartition, List<ConsumerRecord<String, PolicyOutputEvent>>> consumerRecords = new HashMap<>();


        consumerRecords.put(new TopicPartition("topic", 1),
                Collections.singletonList(
                        new ConsumerRecord<>("topic", 0, 0, "key",
                                new PolicyOutputEvent("flmpolicyoutputevent", "version", "nameSpace",
                                        "source", "target", 1L, EXECUTION_ID,
                                        new ProposedLoadBalancingQuanta("sourceCell", CELL_OSS_ID, "100",
                                                Arrays.asList(
                                                        new TargetCell("targetCell1", CELL_OSS_ID, "30"),
                                                        new TargetCell("targetCell2", CELL_OSS_ID, "45"),
                                                        new TargetCell("targetCell3", CELL_OSS_ID, "25"))),
                                        Collections.emptyList()))));

        SECONDS.sleep(2);

        final ConsumerRecords<String, PolicyOutputEvent> testData = new ConsumerRecords<>(consumerRecords);

        SECONDS.sleep(2);

        latestExecutionConsumer.handle(testData);
        verify(optimizationsDaoMock).insertOptimization(any());
    }
}