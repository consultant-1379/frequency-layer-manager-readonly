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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.messagehandler.utils.TestMessageCounter;
import com.ericsson.oss.services.sonom.flm.messagehandler.utils.TestPolicyOutputEventProducer;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This application can be used to test ExecutionConsumerHandler manually with real Kafka. See the README.md file to see how can you setup
 * a Kafka using the given helm chart and bash script.
 */
public class ExecutionConsumerHandlerKafkaApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionConsumerHandlerKafkaApp.class);

    private ExecutionConsumerHandlerKafkaApp() {
    }

    public static void main(final String[] args) throws KafkaConsumerInstantiationException, InterruptedException, BadSetupException {
        System.setProperty("BOOTSTRAP_SERVER", "eric-data-message-bus-kf-0.eric-data-message-bus-kf.flm-test-kafka-consumers:9092");
        System.setProperty("POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME", "flmPolicyOutputEventTopic");
        final List<KafkaRecordHandler<PolicyOutputEvent>> testHandlers = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            testHandlers.add(new TestMessageCounter(i));
        }
        final ExecutorService service = TestPolicyOutputEventProducer.produceKafkaRecords(1, Arrays.asList("FLM_1", "FLM_2"), 60, 2, 5);

        TimeUnit.MILLISECONDS.sleep(5_000);
        final ExecutionConsumerHandler handler = new PoeExecutionConsumerHandlerImpl(testHandlers, null);
        handler.consumeMessagesForExecution("FLM_1", 120, Collections.emptySet());
        handler.consumeMessagesForExecution("FLM_2", 120, Collections.emptySet());
        assertTrue(handler.waitMessages("FLM_2", 150_000));
        testHandlers.forEach(messageHandler -> LOGGER.info(messageHandler.toString()));
        assertEquals(240, ((TestMessageCounter) testHandlers.get(0)).getMessageCount());
        TimeUnit.MILLISECONDS.sleep(10_000);
        service.shutdown();
        handler.shutdown();
    }
}
