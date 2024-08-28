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

package com.ericsson.oss.services.sonom.flm.service.metrics.api;

import javax.ejb.Remote;

/**
 * Interface defining the metrics operations for <code>son-frequency-layer-manager</code>.
 */
@Remote
public interface MetricHelper {

    /**
     * Increments a metric counter with the value specified.
     *
     * @param metric
     *            The name of the counter to increment
     * @param value
     *            The value to increment the counter by
     */
    void incrementFlmMetric(FlmMetric metric, long value);

    /**
     * Increments a metric counter by 1.
     *
     * @param metric
     *            The name of the counter to increment
     */
    void incrementFlmMetric(FlmMetric metric);

    /**
     * Returns the time elapsed since the given start time.
     *
     * @param metricsStartTime
     *            the start time in milliseconds
     * @return the elapsed time in milliseconds
     */
    long getTimeElapsedInMillis(long metricsStartTime);
}
