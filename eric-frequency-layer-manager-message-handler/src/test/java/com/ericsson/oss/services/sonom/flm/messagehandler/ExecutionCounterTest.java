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

package com.ericsson.oss.services.sonom.flm.messagehandler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.errors.InterruptException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link ExecutionCounter} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionCounterTest {

    public static final String EXECUTION_ID = "1";

    @Test
    public void whenExpectedSomeMessageAndGetThem_thenCounterIsZero() {
        final ExecutionCounter counter = new ExecutionCounter("1", 3, Collections.emptySet());
        assertThat(counter.countDown(1L)).isTrue();
        assertThat(counter.countDown(2L)).isTrue();
        assertThat(counter.countDown(3L)).isTrue();
        assertThat(counter.getCount()).isZero();
        assertThat(counter.countDown(4L)).isFalse();
    }

    @Test
    public void whenExpectedSomeMessage_thenDuplicatesAreNotCounted() {
        final ExecutionCounter counter = new ExecutionCounter(EXECUTION_ID, 3, Collections.emptySet());
        counter.countDown(1L);
        counter.countDown(1L);
        counter.countDown(2L);
        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void whenExpectedSomeMessageAndWait_thenWaitReturns() throws InterruptedException {
        final ExecutionCounter counter = new ExecutionCounter(EXECUTION_ID, 3, Collections.emptySet());
        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                SECONDS.sleep(1);
                counter.countDown(1L);
                counter.countDown(2L);
                counter.countDown(3L);
            } catch (InterruptedException e) {
                fail("Test Setup Exception", e);
            }
            counter.countDown(1L);
            counter.countDown(2L);
            counter.countDown(3L);
        });
        assertThat(counter.await(20, SECONDS)).isTrue();
        assertThat(counter.getCount()).isZero();
    }

    @Test
    public void whenExpectedSomeMessageAndWait_thenWaitReturnsAfterTimeout() throws InterruptedException {
        final ExecutionCounter counter = new ExecutionCounter(EXECUTION_ID, 3, Collections.emptySet());
        final ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1_000);
                counter.countDown(1L);
                counter.countDown(2L);
            } catch (final InterruptedException e) {
                throw new InterruptException(e);
            }
            counter.countDown(1L);
            counter.countDown(2L);
        });
        assertThat(counter.await(2_000, TimeUnit.MILLISECONDS)).isFalse();
        assertThat(counter.getCount()).isEqualTo(1);
    }

    @Test
    public void whenExpectedSomeMessageAndStateGiven_thenDuplicatesAreNotCounted() {
        final ExecutionCounter counter = new ExecutionCounter("1", 3, Sets.newSet(1L, 2L));
        counter.countDown(1L);
        counter.countDown(2L);
        assertThat(counter.getCount()).isEqualTo(1L);
    }
}