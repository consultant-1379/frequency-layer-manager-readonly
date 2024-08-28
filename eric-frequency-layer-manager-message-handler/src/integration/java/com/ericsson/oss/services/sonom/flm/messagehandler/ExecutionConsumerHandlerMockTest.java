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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.InterruptException;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.messagehandler.utils.TestKafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.messagehandler.utils.TestMessageCounter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This test is using the TestKafkaConsumerWrapper to mock the ManualCommitBatchKafkaConsumerWrapper
 * that is used in production and Kafka test.
 */
public class ExecutionConsumerHandlerMockTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private ExecutionConsumerHandler objectUnderTest;

    @Test
    public void testWithMock() throws KafkaConsumerInstantiationException, InterruptedException, BadSetupException {
        final TestMessageCounter messageCounter = new TestMessageCounter(0);
        final TestKafkaConsumerWrapper testConsumerWrapper = new TestKafkaConsumerWrapper(4_000L);

        objectUnderTest = new PoeExecutionConsumerHandlerImpl(Collections.singletonList(messageCounter), testConsumerWrapper);
        TimeUnit.MILLISECONDS.sleep(2_000);
        objectUnderTest.consumeMessagesForExecution("2", 6, Collections.emptySet());
        TimeUnit.MILLISECONDS.sleep(4_000);
        assertThat(objectUnderTest.waitMessages("2", 15_000)).isTrue();

        TimeUnit.MILLISECONDS.sleep(5_000);
        final List<Map<Integer, ConsumerRecord<String, PolicyOutputEvent>>> commits = testConsumerWrapper.getCommits();
        softly.assertThat(commits).hasSize(3);
        softly.assertThat(commits.get(0).values()).hasSize(2);
        softly.assertThat(commits.get(2).values()).hasSize(2);
        softly.assertThat(messageCounter.getMessageCount()).isEqualTo(6);
    }

    @Test
    public void testShutDown() throws KafkaConsumerInstantiationException, InterruptedException, BadSetupException {
        final TestMessageCounter messageCounter = new TestMessageCounter(0);
        final TestKafkaConsumerWrapper testConsumerWrapper = new TestKafkaConsumerWrapper(4_000L);
        final ExecutionConsumerHandler handler = new PoeExecutionConsumerHandlerImpl(Collections.singletonList(messageCounter), testConsumerWrapper);

        assertThat(handler.waitMessages("2", 7_000)).isFalse();
        objectUnderTest = new PoeExecutionConsumerHandlerImpl(Collections.singletonList(messageCounter), testConsumerWrapper);

        issueShutdown(handler);
        handler.consumeMessagesForExecution("2", 6, Collections.emptySet());
        assertThat(handler.waitMessages("2", 7_000)).isFalse();
    }

    private void issueShutdown(final ExecutionConsumerHandler handler) {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(5_000);
            } catch (final InterruptedException e) {
                throw new InterruptException(e);
            }
            handler.shutdown();
        });
    }
}
