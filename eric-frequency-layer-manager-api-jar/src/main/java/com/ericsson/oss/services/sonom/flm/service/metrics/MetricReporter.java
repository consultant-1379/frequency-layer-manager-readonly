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
package com.ericsson.oss.services.sonom.flm.service.metrics;

import java.util.Arrays;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;

/**
 * Class used to report metrics to a JMX Reporter.
 */
public final class MetricReporter {

    private static final MetricRegistry REGISTRY = new MetricRegistry();
    private static final MetricReporter INSTANCE = new MetricReporter();

    private MetricReporter() {
        JmxReporter.forRegistry(REGISTRY).inDomain("com.ericsson.oss.services.sonom.flm.metrics").build().start();
    }

    /**
     * Creates or retrieves a @{@link MetricReporter} instance.
     * 
     * @return a @{@link MetricReporter} instance
     */
    public static MetricReporter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the metrics to be collected for <code>son-frequency-layer-manager</code>.
     */
    public void initializeFlmMetrics() {
        Arrays.stream(FlmMetric.values())
                .forEach(metric -> INSTANCE.getMetricRegistry().counter(metric.name()));
    }

    /**
     * Retrieves the {@link MetricRegistry} instance.
     * 
     * @return the {@link MetricRegistry} instance
     */
    public MetricRegistry getMetricRegistry() {
        return REGISTRY;
    }
}
