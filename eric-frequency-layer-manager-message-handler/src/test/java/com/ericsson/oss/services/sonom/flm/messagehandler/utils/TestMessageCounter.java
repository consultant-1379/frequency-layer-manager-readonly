/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class is used to count the unique {@link PolicyOutputEvent}s identified by executionId and sectorId in tests
 */
public class TestMessageCounter implements KafkaRecordHandler<PolicyOutputEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestMessageCounter.class);
    private final Map<String, Integer> counter = new HashMap<>();
    private final int instance;
    public TestMessageCounter(final int instance) {
        this.instance = instance;
    }
    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void handle(final PolicyOutputEvent policyOutputEvent) {
        try {
            LOGGER.info("Message handled executionId {} sectorId {} in instance {}", policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                    instance);
            counter.compute(policyOutputEvent.getExecutionId() + policyOutputEvent.getSectorId(), (key, value) -> value == null?0:value+1);
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }
    }


    @Override
    public String toString() {
        return "Unique messages received: " + getMessageCount() + " in instance" + instance;
    }

    public int getMessageCount() {
        return counter.size();
    }
}
