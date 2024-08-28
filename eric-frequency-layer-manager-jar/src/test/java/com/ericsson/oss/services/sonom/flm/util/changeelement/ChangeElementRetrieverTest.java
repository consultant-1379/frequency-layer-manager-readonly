/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.util.changeelement;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;

/**
 * Unit tests for {@link ChangeElementRetriever} class.
 */
public class ChangeElementRetrieverTest {
    private static final String CM_SERVICE_HOSTNAME_ENV_PROPERTY = "CM_SERVICE_HOSTNAME";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String CM_SERVICE_PORT_ENV_PROPERTY = "CM_SERVICE_PORT";
    private static final int CM_SERVICE_PORT = 8080;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(CM_SERVICE_PORT);
    private static final String CM_CHANGE_CHANGES_ENDPOINT = "/son-om/cm-change/v1/changes";
    private static final String RESPONSE_BODY = ResourceLoader.loadResource("retrievedChangeElementsResponseBody.json");
    private static final String RESPONSE_BODY_FILTER_ALL = ResourceLoader.loadResource("retrievedChangeElementsResponseBodyFilterAll.json");
    private static final String EMPTY_RESPONSE_BODY = "[]";
    private static final String SOURCE_OF_CHANGE = "alg_FLM_1";
    private static final String EXECUTION_ID = "EXECUTION_1";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String CHANGE_TYPE_OPTIMIZATION = "OPTIMIZATION";
    private static final Long SIX_HOURS = HOURS.toMillis(6);
    private static final Gson GSON = new Gson();
    private static final Long LAST_MODIFIED_TIME_FROM_TEST_DATA = GSON.fromJson(RESPONSE_BODY, ChangeElement[].class)[0].getLastModified();
    private static final Long TEST_PA_WINDOW_END_TIME = LAST_MODIFIED_TIME_FROM_TEST_DATA + SIX_HOURS + HOURS.toMillis(1);//1617984000000L;
    private static final String INDEX_D_OF_D = "Index:%d (of %d)";
    private static ChangeElementRetriever objectUnderTest;
    private static PAExecution paExecution;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private int expectedSize;

    @BeforeClass
    public static void setUp() {
        System.setProperty(CM_SERVICE_HOSTNAME_ENV_PROPERTY, CM_SERVICE_HOSTNAME);
        System.setProperty(CM_SERVICE_PORT_ENV_PROPERTY, String.valueOf(CM_SERVICE_PORT));
        paExecution = new PAExecution(0,
                SCHEDULE,
                new Timestamp(TEST_PA_WINDOW_END_TIME - SIX_HOURS),
                new Timestamp(TEST_PA_WINDOW_END_TIME),
                EXECUTION_ID);
        objectUnderTest = new ChangeElementRetriever(paExecution, SOURCE_OF_CHANGE);
    }

    private static void buildGenericCmServiceGetStubWithRetryBehaviour(final String response) {
        final String path = URI.create(CM_CHANGE_CHANGES_ENDPOINT).getPath();
        final String inScenario = "Retry Scenario";
        final String successScenario = "SUCCESS";

        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(path))
                .inScenario(inScenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR))
                .willSetStateTo(successScenario));

        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(path))
                .inScenario(inScenario)
                .whenScenarioStateIs(successScenario)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(response)));
    }

    private static void buildCmChangeServiceWithReturnedStatusCode(final int httpStatus, final String response) {
        final String decodedUriPath = URI.create(CM_CHANGE_CHANGES_ENDPOINT).getPath();
        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(httpStatus)));
    }

    @After
    public void afterTest() {
        WIRE_MOCK_RULE.resetAll();
    }

    @Test
    public void whenSendingValidRequestToGetChangeElements_thenA200Response() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_OK, RESPONSE_BODY);
        final List<ChangeElement> result = objectUnderTest.retrieveChangeElementList();
        assertThat(result).isNotEmpty();
        expectedSize = 4;
        softly.assertThat(result).hasSize(expectedSize);
        for (int i = 0; i < result.size(); i++) {
            final ChangeElement changeElement = result.get(i);
            assertThat(changeElement.getExecutionId()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(EXECUTION_ID);
            assertThat(changeElement.getStatus()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(STATUS_SUCCEEDED);
            assertThat(changeElement.getChangeType()).as(INDEX_D_OF_D, i, expectedSize).hasToString(CHANGE_TYPE_OPTIMIZATION);
        }
    }

    @Test
    public void whenSendingValidRequestToGetChangeElements_thenA500ResponseIsReturnedAndARetryOccursSuccessfully() {
        buildGenericCmServiceGetStubWithRetryBehaviour(RESPONSE_BODY);
        final List<ChangeElement> result = objectUnderTest.retrieveChangeElementList();
        assertThat(result).isNotEmpty();
        expectedSize = 4;
        softly.assertThat(result).hasSize(expectedSize);
        for (int i = 0; i < result.size(); i++) {
            final ChangeElement changeElement = result.get(i);
            assertThat(changeElement.getExecutionId()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(EXECUTION_ID);
            assertThat(changeElement.getStatus()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(STATUS_SUCCEEDED);
            assertThat(changeElement.getChangeType()).as(INDEX_D_OF_D, i, expectedSize).hasToString(CHANGE_TYPE_OPTIMIZATION);
        }
    }

    @Test
    public void whenSendingValidRequestToGetChangeElements_thenARestExecutionExceptionRaisedIfNotFoundErrorCodeAfterRetries() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_NOT_FOUND, RESPONSE_BODY);
        thrown.expect(RestExecutionException.class);
        objectUnderTest.retrieveChangeElementList();
    }

    @Test
    public void whenRetrieveChangeElementListCalled_andNoReversionChangeElementsExist_thenReturnEmptyList() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_OK, EMPTY_RESPONSE_BODY);
        final List<ChangeElement> result = objectUnderTest.retrieveChangeElementList();
        softly.assertThat(result).isEmpty();
    }

    @Test
    public void whenGetChangeElementsAndNoChangeElementAvailableForOptimization_thenMethodReturnsEmptyList() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_OK, RESPONSE_BODY_FILTER_ALL);
        final List<ChangeElement> result = objectUnderTest.retrieveChangeElementList();
        softly.assertThat(result).isEmpty();
    }

    @Test
    public void whenRetrieveReversionChangeElementListCalled_then200ResponseReturned() {
        final List<ChangeElement> changeElements = Arrays.asList(GSON.fromJson(RESPONSE_BODY, ChangeElement[].class)).stream()
                .filter(changeElement -> changeElement.getChangeType() == ChangeElement.ChangeType.REVERSION)
                .collect(Collectors.toList());

        final String responseBody = GSON.toJson(changeElements);

        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_OK, responseBody);
        final List<ChangeElement> result = objectUnderTest.retrieveReversionChangeElementList();
        expectedSize = 2;
        softly.assertThat(result).hasSize(expectedSize);
        for (int i = 0; i < result.size(); i++) {
            final ChangeElement changeElement = result.get(i);
            assertThat(changeElement.getExecutionId()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(EXECUTION_ID);
            assertThat(changeElement.getStatus()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(STATUS_SUCCEEDED);
            assertThat(changeElement.getChangeType()).hasToString(ChangeElement.ChangeType.REVERSION.toString());
        }
    }

    @Test
    public void whenRetrieveReversionChangeElementListCalled_and500ResponseReturned_thenSuccessfulRetryOccurs() {
        final List<ChangeElement> changeElements = Arrays.asList(GSON.fromJson(RESPONSE_BODY, ChangeElement[].class)).stream()
                .filter(changeElement -> changeElement.getChangeType() == ChangeElement.ChangeType.REVERSION)
                .collect(Collectors.toList());

        final String responseBody = GSON.toJson(changeElements);

        buildGenericCmServiceGetStubWithRetryBehaviour(responseBody);
        final List<ChangeElement> result = objectUnderTest.retrieveReversionChangeElementList();
        expectedSize = 2;
        softly.assertThat(result).hasSize(expectedSize);
        for (int i = 0; i < result.size(); i++) {
            final ChangeElement changeElement = result.get(i);
            assertThat(changeElement.getExecutionId()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(EXECUTION_ID);
            assertThat(changeElement.getStatus()).as(INDEX_D_OF_D, i, expectedSize).isEqualTo(STATUS_SUCCEEDED);
            assertThat(changeElement.getChangeType()).hasToString(ChangeElement.ChangeType.REVERSION.toString());
        }
    }

    @Test
    public void whenRetrieveReversionChangeElementListCalled_andNotFoundErrorOccurs_thenThrowRestExecutionException() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_NOT_FOUND, RESPONSE_BODY);
        thrown.expect(RestExecutionException.class);
        objectUnderTest.retrieveReversionChangeElementList();
    }

    @Test
    public void whenRetrieveReversionChangeElementListCalled_andNoReversionChangeElementsExist_thenReturnEmptyList() {
        buildCmChangeServiceWithReturnedStatusCode(HttpStatus.SC_OK, EMPTY_RESPONSE_BODY);
        final List<ChangeElement> result = objectUnderTest.retrieveReversionChangeElementList();
        softly.assertThat(result).isEmpty();
    }

}
