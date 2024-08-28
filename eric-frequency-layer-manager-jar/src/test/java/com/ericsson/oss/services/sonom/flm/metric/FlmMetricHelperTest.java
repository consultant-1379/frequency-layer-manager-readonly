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

package com.ericsson.oss.services.sonom.flm.metric;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.service.metrics.MetricReporter;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Unit tests for {@link FlmMetricHelper} class.
 */
public class FlmMetricHelperTest {
    private static final MetricHelper OBJECT_UNDER_TEST = new FlmMetricHelper();

    @BeforeClass
    public static void beforeClass() {
        MetricReporter.getInstance().initializeFlmMetrics();
    }

    @Test
    public void whenMetricsAreInitialized_thenVerifyMetricCanBeIncremented() {
        final MetricReporter metricReporter = MetricReporter.getInstance();
        final long countBeforeIncrement = metricReporter.getMetricRegistry().counter(FlmMetric.FLM_ALG_EXECUTION.name())
                .getCount();
        OBJECT_UNDER_TEST.incrementFlmMetric(FlmMetric.FLM_ALG_EXECUTION);
        final long countAfterIncrement = metricReporter.getMetricRegistry().counter(FlmMetric.FLM_ALG_EXECUTION.name())
                .getCount();
        assertThat(countAfterIncrement).isEqualTo(countBeforeIncrement + 1);
    }

    @Test
    public void whenGetTimeElapsedInMillisIsCalledWithTimeInThePast_thenItReturnsAPositiveValue() {
        final long oneDayAgo = System.nanoTime() - TimeUnit.DAYS.toNanos(1);
        final Long elapsedInMillis = OBJECT_UNDER_TEST.getTimeElapsedInMillis(oneDayAgo);
        assertThat(elapsedInMillis).isGreaterThan(0);
    }

    @Test
    public void whenGetTimeElapsedInMillisIsCalledWithFutureValue_thenItReturnTimeShouldBeLessThanZero() {
        final long now = System.nanoTime();
        final long tomorrow = now + TimeUnit.DAYS.toNanos(1);
        final Long elapsedInMillis = OBJECT_UNDER_TEST.getTimeElapsedInMillis(tomorrow);
        assertThat(elapsedInMillis).isLessThan(0);
    }

    @Test
    public void whenGetTimeElapsedInMillisIsCalledWithCurrentTime_thenItsReturnShouldBeLessThanThatTime() {
        final long now = System.nanoTime();
        final long elapsedInMillis = OBJECT_UNDER_TEST.getTimeElapsedInMillis(now);
        assertThat(elapsedInMillis).isGreaterThanOrEqualTo(0);
    }
}
