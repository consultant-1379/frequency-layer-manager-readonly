/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2021
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.AbortedException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

import io.github.resilience4j.core.lang.NonNull;

/**
 * The class is used to count the number of messages received from Kafka. Duplicates are not counted. The message is identified by sectorId
 * For each execution one ExecutionCounter is created;
 * Counters exists for the time period of receiving Kafka messages.
 * The ConsumerHandler waits for the counter to count down to zero or for specific amount of time.
 * ExecutionCounter countDown is called from ExecutionConsumers each different thread
 */
public class ExecutionCounter extends CountDownLatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionCounter.class);
    protected boolean aborted;
    private final String executionId;
    private final Set<Long> sectorIds;

    public ExecutionCounter(final String executionId, final int expectedMessageNum, final Set<Long> sectorIds) {
        super(expectedMessageNum - sectorIds.size());
        this.executionId = executionId;
        this.sectorIds = new HashSet<>();
        this.sectorIds.addAll(sectorIds);
    }

    /**
     * This method is called if a new Kafka message needs to be counted. The method can be called from different threads from ExecutionConsumers
     * @param sectorId the id of a message. We don't need the whole message just an id of it that is the sector.
     * @return false if counter is already zero, true otherwise
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized boolean countDown(final Long sectorId) {
        if (getCount() == 0) {
            return false;
        }
        if (sectorIds.contains(sectorId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(LoggingFormatter.formatMessage(executionId, sectorId, "Duplicate execution"));
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(LoggingFormatter.formatMessage(executionId, sectorId,
                    String.format("Counting down in counter, currentCount %d", getCount())));
            }
            countDown();
            sectorIds.add(sectorId);
        }
        return true;
    }

    @Override
    public boolean await(final long timeout, final @NonNull TimeUnit unit) throws InterruptedException {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final boolean result = super.await(timeout, unit);
        if (aborted) {
            throw new AbortedException();
        }
        return result;
    }

    /**
     * Method to abort execution.
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void abort() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, "Aborting ExecutionCounter"));
        }
        if (getCount() == 0) {
            return;
        }
        this.aborted = true;
        while (getCount() > 0) {
            countDown();
        }
    }
}
