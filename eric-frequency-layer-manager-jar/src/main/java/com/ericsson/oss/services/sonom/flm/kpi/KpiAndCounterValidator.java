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
package com.ericsson.oss.services.sonom.flm.kpi;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;
import com.ericsson.oss.services.sonom.kpi.client.KpiServiceRestExecutor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Loads required KPIs and counters and passes them over REST to <code>eric-pm-kpi-calculator</code>.
 */
public class KpiAndCounterValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiAndCounterValidator.class);
    private static final String REQUIRED_KPIS_FILE_PATH = "RequiredKpis.json";
    private static final String REQUIRED_COUNTERS_FILE_PATH = "RequiredCounters.json";
    private final KpiServiceRestExecutor kpiServiceRestExecutor;

    public KpiAndCounterValidator(final Retry retry, final CircuitBreaker circuitBreaker, final RestExecutor restExecutor) {
        kpiServiceRestExecutor = new KpiServiceRestExecutor.Builder()
                .withRestExecutor(restExecutor)
                .withCircuitBreaker(circuitBreaker)
                .withRetry(retry)
                .build();
    }

    /**
     * Loads the required KPIs for the <code>flm-service</code> from the classpath and sends them to <code>eric-pm-kpi-calculator</code>.
     *
     * @return the REST response
     * @throws KpiModelVerificationException
     *             thrown if the REST response status code is not {@link HttpStatus#SC_ACCEPTED}
     */
    public RestResponse<String> sendRequiredKpisForMediation() throws KpiModelVerificationException {
        final String kpiPayload = loadResource(REQUIRED_KPIS_FILE_PATH);
        final RestResponse<String> requestResponse = kpiServiceRestExecutor.putKpis(kpiPayload);
        if (HttpStatus.SC_ACCEPTED != requestResponse.getStatus()) {
            throw new KpiModelVerificationException("Error validating KPIs: " + requestResponse.getEntity());
        }
        return requestResponse;
    }

    /**
     * Loads the required PM counters for the <code>flm-service</code> from the classpath and sends them to <code>eric-pm-kpi-calculator</code>.
     *
     * @return the REST response
     * @throws KpiModelVerificationException
     *             thrown if the REST response status code is not {@link HttpStatus#SC_ACCEPTED}
     */
    public RestResponse<String> sendRequiredCountersForMediation() throws KpiModelVerificationException {
        final String counterPayload = loadResource(REQUIRED_COUNTERS_FILE_PATH);
        final RestResponse<String> requestResponse = kpiServiceRestExecutor.validateCounters(counterPayload);
        if (HttpStatus.SC_ACCEPTED != requestResponse.getStatus()) {
            throw new KpiModelVerificationException("Error validating counters: " + requestResponse.getEntity());
        }
        return requestResponse;
    }

    private static String loadResource(final String filePath) throws KpiModelVerificationException {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(filePath);
        } catch (final Exception e) { //NOSONAR Exception is suitably logged
            LOGGER.warn("Error loading resource through filepath: {} - {}", filePath, e.getClass());
            throw new KpiModelVerificationException("Error loading required KPIs or counters", e);
        }
    }
}
