/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.ericsson.oss.services.sonom.flm.service.metrics.MetricReporter;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Class used to increment FLM metrics counter.
 */
public class FlmMetricHelper implements MetricHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmMetricHelper.class);
    private static final MetricReporter REPORTER = MetricReporter.getInstance();

    @Override
    public void incrementFlmMetric(final FlmMetric metric, final long value) {
        final Counter counter = REPORTER.getMetricRegistry().counter(metric.name());
        LOGGER.debug("Increment counter {} with value {}...", metric, value);
        counter.inc(value);
    }

    @Override
    public void incrementFlmMetric(final FlmMetric metric) {
        incrementFlmMetric(metric, 1);
    }

    @Override
    public long getTimeElapsedInMillis(final long metricsStartTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - metricsStartTime);
    }

}
