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

package com.ericsson.oss.services.sonom.flm.service.startup;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.cm.service.api.exception.CmModelValidationException;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit test for {@link FlmRequirementsValidator} class and its use of {@link CmKpiRestResilienceCreator}.
 */
@RunWith(StrictStubs.class)
@SuppressWarnings("PMD.MoreThanOneLogger")
public class FlmRequirementsValidatorTest {

    private static final int CM_SERVICE_PORT = 8080;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().port(CM_SERVICE_PORT));
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String KPI_SERVICE_HOSTNAME = "localhost";
    private static final int KPI_SERVICE_PORT = 8080;
    private static final String CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL = "/son-om/cm-topology/v2/model";
    private static final String KPI_SERVICE_BASE_URL = "/son-om/kpi/v1/kpis/definitions";
    private static final String MODEL_COUNTERS_BASE_URL = "/son-om/kpi/v1/model/counters";
    private static final String FOR_SUCCESS = "FOR SUCCESS";
    private static final String MODEL_VALIDATION_RETURNED_STATUS_CODE__ = "Model validation returned status code {}";
    private static final String REQUIRED_KPIS_HAVE_BEEN_ACCEPTED_STATUS_CODE__ = "Required KPIs have been accepted. Status code: {}";

    static {
        System.setProperty("CM_SERVICE_HOSTNAME", CM_SERVICE_HOSTNAME);
        System.setProperty("CM_SERVICE_PORT", String.valueOf(CM_SERVICE_PORT));
        System.setProperty("KPI_SERVICE_HOSTNAME", KPI_SERVICE_HOSTNAME);
        System.setProperty("KPI_SERVICE_PORT", String.valueOf(KPI_SERVICE_PORT));
    }

    private static FlmRequirementsValidator objectUnderTest;

    @Rule
    public Timeout globalTimeout = new Timeout(3, TimeUnit.MINUTES);

    @Mock
    @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
    private Logger objUnderTestLogger;

    @Mock
    @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
    private Logger resilienceLogger;

    @BeforeClass
    public static void setUp() {
        objectUnderTest = new FlmRequirementsValidator();
    }

    @Before
    public void before() {
        reset(objUnderTestLogger);
        reset(resilienceLogger);
        when(resilienceLogger.isDebugEnabled()).thenReturn(true);
        Whitebox.setInternalState(FlmRequirementsValidator.class, "LOGGER", objUnderTestLogger);
        Whitebox.setInternalState(CmKpiRestResilienceCreator.class, "LOGGER", resilienceLogger);
    }

    @Test
    public void whenValidationReturns202_validateCm_thenResponseIsReturned_andThereIsNoRetry() {
        final int scAccepted = HttpStatus.SC_ACCEPTED;
        buildCmServiceRestStub(scAccepted, CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL);
        objectUnderTest.validateCm();
        verify(resilienceLogger, times(1)).isDebugEnabled();
        verify(resilienceLogger, times(1)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scAccepted);
        verify(objUnderTestLogger).info(eq("Required CM model has been accepted: {}"), any(RestResponse.class));
        final String decodedUriPath = URI.create(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL).getPath();
        WIRE_MOCK_RULE.verify(postRequestedFor(urlPathEqualTo(decodedUriPath)));
    }

    @Test
    public void whenValidationReturns202_validateKpi_thenResponseIsReturned_andThereIsNoRetry() {
        final int scAccepted = HttpStatus.SC_ACCEPTED;
        buildKpiServiceRestStub(scAccepted);
        buildKpiCountersRestStub(scAccepted);
        objectUnderTest.validateKpis();
        verify(resilienceLogger, times(2)).isDebugEnabled();
        verify(resilienceLogger, times(2)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scAccepted);
        verify(objUnderTestLogger).info(REQUIRED_KPIS_HAVE_BEEN_ACCEPTED_STATUS_CODE__, scAccepted);
        verify(objUnderTestLogger).info(eq("Required counters have been accepted: {}"), any(RestResponse.class));

        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)));
        final String decodedCountersUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedCountersUriPath)));
    }

    @Test
    public void whenValidationReturns404_validateCm_thenResponseIsReturned_andThereIsRetry() {
        buildScenarioBasedRetryStub(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL);
        final int scNotFound = HttpStatus.SC_NOT_FOUND;
        objectUnderTest.validateCm();
        verify(resilienceLogger, times(2)).isDebugEnabled();
        verify(resilienceLogger).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scNotFound);
        verify(resilienceLogger).warn("Failed to validate model (status code: {}), retrying", scNotFound);
        verify(resilienceLogger).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, HttpStatus.SC_ACCEPTED);
        verify(objUnderTestLogger).info(eq("Required CM model has been accepted: {}"), any(RestResponse.class));
        final String decodedUriPath = URI.create(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL).getPath();
        WIRE_MOCK_RULE.verify(postRequestedFor(urlPathEqualTo(decodedUriPath)));
    }

    @Test
    public void whenValidationReturns404_validateKpi_thenRetryUntil202() {
        final int scNotFound = HttpStatus.SC_NOT_FOUND;
        buildScenarioBasedKpiServiceRetryStubWithInitialFailureCode(scNotFound);
        buildCmServiceRestStub(HttpStatus.SC_ACCEPTED, MODEL_COUNTERS_BASE_URL);
        objectUnderTest.validateKpis();
        verify(resilienceLogger, times(3)).isDebugEnabled();
        verify(resilienceLogger, times(1)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scNotFound);
        verify(resilienceLogger).warn("Failed to validate model (status code: {}), retrying", scNotFound);
        verify(resilienceLogger, times(2)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, HttpStatus.SC_ACCEPTED);
        verify(objUnderTestLogger).info(REQUIRED_KPIS_HAVE_BEEN_ACCEPTED_STATUS_CODE__, HttpStatus.SC_ACCEPTED);
        verify(objUnderTestLogger).info(eq("Required counters have been accepted: {}"), any(RestResponse.class));
        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        final String decodedCountersUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)));
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedCountersUriPath)));
    }

    @Test
    public void whenValidationReturns500_validateKpi_thenRetryUntil202() {
        final int scInternalError = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        buildScenarioBasedKpiServiceRetryStubWithInitialFailureCode(scInternalError);
        buildCmServiceRestStub(HttpStatus.SC_ACCEPTED, MODEL_COUNTERS_BASE_URL);
        objectUnderTest.validateKpis();
        verify(resilienceLogger, times(3)).isDebugEnabled();
        verify(resilienceLogger, times(1)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scInternalError);
        verify(resilienceLogger).warn("Failed to validate model (status code: {}), retrying", scInternalError);
        verify(resilienceLogger, times(2)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, HttpStatus.SC_ACCEPTED);
        verify(objUnderTestLogger).info(REQUIRED_KPIS_HAVE_BEEN_ACCEPTED_STATUS_CODE__, HttpStatus.SC_ACCEPTED);
        verify(objUnderTestLogger).info(eq("Required counters have been accepted: {}"), any(RestResponse.class));

        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        final String decodedCountersUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)));
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedCountersUriPath)));
    }

    @Test
    public void whenValidationReturnsOtherCode_validateKpi_thenKpiModelVerificationExceptionIsThrown_andThereIsNoRetry() {
        final int someErrorCode = HttpStatus.SC_METHOD_FAILURE;
        buildScenarioBasedKpiServiceRetryStubWithInitialFailureCode(someErrorCode);
        buildScenarioBasedRetryStub(MODEL_COUNTERS_BASE_URL);

        WIRE_MOCK_RULE.verify(0, putRequestedFor(urlEqualTo(KPI_SERVICE_BASE_URL)));
        objectUnderTest.validateKpis();
        WIRE_MOCK_RULE.verify(1, putRequestedFor(urlEqualTo(KPI_SERVICE_BASE_URL)));

        verify(resilienceLogger, times(1)).isDebugEnabled();
        verify(resilienceLogger).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, someErrorCode);
        verify(objUnderTestLogger).error(eq("Error validating KPIs and counters"), any(KpiModelVerificationException.class));

        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        final String decodedCountersUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)));
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedCountersUriPath)));
    }

    @Test
    public void whenValidationReturns400_validateCm_thenCmModelValidationExceptionIsThrown_andThereIsNoRetry() {
        final int scBadRequest = HttpStatus.SC_BAD_REQUEST;
        buildCmServiceRestStub(scBadRequest, CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL);
        WIRE_MOCK_RULE.verify(0, postRequestedFor(urlEqualTo(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL)));
        objectUnderTest.validateCm();
        WIRE_MOCK_RULE.verify(1, postRequestedFor(urlEqualTo(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL)));
        verify(resilienceLogger, times(1)).isDebugEnabled();
        verify(resilienceLogger).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scBadRequest);
        verify(objUnderTestLogger).error(eq("Error validating the CM model"), any(CmModelValidationException.class));

        final String decodedUriPath = URI.create(CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL).getPath();
        WIRE_MOCK_RULE.verify(postRequestedFor(urlPathEqualTo(decodedUriPath)));
    }

    @Test
    public void whenValidationReturns400_validateKpi_thenKpiModelVerificationExceptionIsThrown_andThereIsNoRetry() {
        final int scBadRequest = HttpStatus.SC_BAD_REQUEST;
        buildKpiServiceRestStub(scBadRequest);
        buildKpiCountersRestStub(scBadRequest);
        objectUnderTest.validateKpis();

        verify(resilienceLogger, times(1)).isDebugEnabled();
        verify(resilienceLogger, times(1)).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scBadRequest);
        verify(objUnderTestLogger).error(eq("Error validating KPIs and counters"), any(KpiModelVerificationException.class));

        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        final String decodedCountersUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)));
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedCountersUriPath)));
    }

    @Test
    public void whenValidationReturns400_validateCm_logsCmModeValidationException() {
        final int scBadRequest = HttpStatus.SC_BAD_REQUEST;
        buildCmServiceRestStub(scBadRequest, CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL);
        objectUnderTest.validateCm();
        verify(resilienceLogger, times(1)).isDebugEnabled();
        verify(resilienceLogger).debug(MODEL_VALIDATION_RETURNED_STATUS_CODE__, scBadRequest);
        verify(objUnderTestLogger).error(eq("Error validating the CM model"), any(CmModelValidationException.class));
    }

    @After
    public void after() {
        //ignore any random exception if it does not affect anything above
        verify(resilienceLogger, atLeast(0)).warn(eq("Failed to validate model (an exception occurred), retrying"), any(Throwable.class));
        //no other logs expected
        verifyNoMoreInteractions(resilienceLogger);
        verifyNoMoreInteractions(objUnderTestLogger);
        WIRE_MOCK_RULE.resetAll();
    }

    private static void buildKpiServiceRestStub(final int responseCode) {
        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withStatus(responseCode)));
    }

    private static void buildCmServiceRestStub(final int responseCode, final String serviceUrl) {
        final String decodedUriPath = URI.create(serviceUrl).getPath();
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withStatus(responseCode)));
    }

    private static void buildKpiCountersRestStub(final int responseCode) {
        final String decodedUriPath = URI.create(MODEL_COUNTERS_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withStatus(responseCode)));
    }

    private static void buildScenarioBasedKpiServiceRetryStubWithInitialFailureCode(final int failureCode) {
        final String decodedUriPath = URI.create(KPI_SERVICE_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath))
                .inScenario("KPI_STATE")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(failureCode)
                )
                .willSetStateTo(FOR_SUCCESS));

        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(decodedUriPath)) //post?
                .inScenario("KPI_STATE")
                .whenScenarioStateIs(FOR_SUCCESS)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_ACCEPTED)));
    }

    private static void buildScenarioBasedRetryStub(final String baseUrl) {
        final String decodedUriPath = URI.create(baseUrl).getPath();
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedUriPath))
                .inScenario("CM_STATE")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_NOT_FOUND))
                .willSetStateTo(FOR_SUCCESS));
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(decodedUriPath))
                .inScenario("CM_STATE")
                .whenScenarioStateIs(FOR_SUCCESS)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_ACCEPTED)));
    }

}
