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
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit tests for {@link ChangeElementSender} class.
 */
public class ChangeElementSenderTest {

    private static final int MAX_ATTEMPTS_IN_TESTS = 5;
    private static final int SECONDS_TO_WAIT_IN_TESTS = 1;
    private static final int CHANGE_ELEMENT_LIST_SIZE_IN_TESTS = 1500;

    private static final String CM_SERVICE_HOSTNAME_ENV_PROPERTY = "CM_SERVICE_HOSTNAME";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String CM_SERVICE_PORT_ENV_PROPERTY = "CM_SERVICE_PORT";
    private static final int CM_SERVICE_PORT = 8080;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(CM_SERVICE_PORT);
    private static final String CM_CHANGE_CHANGES_ENDPOINT = "/son-om/cm-change/v1/changes";
    private static final String EXECUTION_ID = "EXECUTION_1";
    private static final List<Pair<ChangeElement, ChangeElement>> TEST_PAIR_LIST = createTestPairList();
    private static final int BATCH_SIZE = ChangeElementSenderImpl.getBatchSize();
    private static final int NUMBER_OF_PARTITIONS_PAIR = (int) Math.ceil(CHANGE_ELEMENT_LIST_SIZE_IN_TESTS / (double) (BATCH_SIZE / 2));
    private static final int NUMBER_OF_LIST_BATCHES = (int) Math.ceil(CHANGE_ELEMENT_LIST_SIZE_IN_TESTS / (double) BATCH_SIZE);
    private static ChangeElementSender objectUnderTest;

    @Before
    public void setUp() {
        System.setProperty(CM_SERVICE_HOSTNAME_ENV_PROPERTY, CM_SERVICE_HOSTNAME);
        System.setProperty(CM_SERVICE_PORT_ENV_PROPERTY, String.valueOf(CM_SERVICE_PORT));

        objectUnderTest = new ChangeElementSenderImpl(MAX_ATTEMPTS_IN_TESTS, SECONDS_TO_WAIT_IN_TESTS);
    }

    private static List<ChangeElement> createTestList() {
        return Stream.generate(ChangeElement::new).limit(CHANGE_ELEMENT_LIST_SIZE_IN_TESTS).collect(Collectors.toList());
    }

    private static List<Pair<ChangeElement, ChangeElement>> createTestPairList() {
        return createTestList().stream().map(pair()).collect(Collectors.toList());
    }

    private static <T> Function<T, Pair<T, T>> pair() {
        return arg -> {
            final ChangeElement reversionElement = new ChangeElement();
            reversionElement.setChangeType(ChangeElement.ChangeType.REVERSION);
            return new ImmutablePair(arg, reversionElement);
        };
    }

    private static void buildCmChangeServicePostWithReturnedStatusCode(final int httpStatus) {
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(CM_CHANGE_CHANGES_ENDPOINT))
                .willReturn(aResponse().withStatus(httpStatus)));
    }

    private static void buildCmChangeServicePostWithChangingResponse(final int initialHttpStatus, final int subsequentHttpStatus) {
        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(CM_CHANGE_CHANGES_ENDPOINT))
                .inScenario("retry scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(initialHttpStatus))
                .willSetStateTo("retry"));

        WIRE_MOCK_RULE.stubFor(post(urlPathEqualTo(CM_CHANGE_CHANGES_ENDPOINT))
                .inScenario("retry scenario")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse().withStatus(subsequentHttpStatus)));
    }

    private static void buildCmChangeServicePutWithReturnedStatusCode(final int httpStatus) {
        WIRE_MOCK_RULE.stubFor(put(urlPathEqualTo(CM_CHANGE_CHANGES_ENDPOINT))
                .willReturn(aResponse().withStatus(httpStatus)));
    }

    @After
    public void afterTest() {
        WIRE_MOCK_RULE.resetAll();
    }

    @Test
    public void whenPostChangeElementPairsCalled_thenCMChangeServiceReturnsCreated() throws FlmAlgorithmException {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_CREATED);
        final Map<ChangeElementState, Integer> testChangeElements = objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST);

        assertThat(testChangeElements.get(ChangeElementState.FAILED)).isZero();
        verify(exactly(NUMBER_OF_PARTITIONS_PAIR), postRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenSomeBatchesFailToSend_thenNumberOfChangeElementsThatFailedAreReturned() throws FlmAlgorithmException {
        buildCmChangeServicePostWithChangingResponse(HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_CREATED);
        final Map<ChangeElementState, Integer> testChangeElements = objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST);
        assertThat(testChangeElements.get(ChangeElementState.FAILED)).isEqualTo(350);
    }

    @Test
    public void whenMultipleBatchesBatchesAreSent_thenNumberOfChangeElementsSentAreReturned() throws FlmAlgorithmException {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_CREATED);
        final Map<ChangeElementState, Integer> testChangeElements = objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST);
        assertThat(testChangeElements.get(ChangeElementState.SENT)).isEqualTo(1500);
    }

    @Test
    public void whenPostChangeElementPairsCalled_thenCMChangeServiceReturnsInternalServerError() {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThatThrownBy(() -> objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST))
                .isInstanceOf(FlmAlgorithmException.class);

        final int numberOfExpectedPostRequest = NUMBER_OF_PARTITIONS_PAIR * MAX_ATTEMPTS_IN_TESTS;
        verify(exactly(numberOfExpectedPostRequest), postRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenPostChangeElementPairsCalled_thenCMChangeServiceReturnsNotFound() {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_NOT_FOUND);
        assertThatThrownBy(() -> objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST))
                .isInstanceOf(FlmAlgorithmException.class);

        final int numberOfExpectedPostRequest = NUMBER_OF_PARTITIONS_PAIR * MAX_ATTEMPTS_IN_TESTS;
        verify(exactly(numberOfExpectedPostRequest), postRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenPostChangeElementPairsCalled_thenCMChangeServiceReturnsWithNonRecoverableStatusCode() {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_BAD_REQUEST);
        assertThatThrownBy(() -> objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST))
                .isInstanceOf(FlmAlgorithmException.class);

        verify(exactly(NUMBER_OF_PARTITIONS_PAIR), postRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenPostChangeElementPairsCalled_thenCMChangeServiceReturnsWithConflict() throws FlmAlgorithmException {
        buildCmChangeServicePostWithReturnedStatusCode(HttpStatus.SC_CONFLICT);
        final Map<ChangeElementState, Integer> testChangeElements = objectUnderTest.postChangeElements(EXECUTION_ID, TEST_PAIR_LIST);

        assertThat(testChangeElements.get(ChangeElementState.FAILED)).isZero();
        assertThat(testChangeElements.get(ChangeElementState.SENT)).isZero();
        assertThat(testChangeElements.get(ChangeElementState.EXISTS)).isEqualTo(5);

        verify(exactly(NUMBER_OF_PARTITIONS_PAIR), postRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenUpdateChangeElementsCalled_thenCMChangeServiceReturnsOk() throws FlmAlgorithmException {
        buildCmChangeServicePutWithReturnedStatusCode(HttpStatus.SC_OK);
        final Map<ChangeElementState, Integer> testChangeElements = objectUnderTest.updateChangeElements(EXECUTION_ID, createTestList());

        assertThat(testChangeElements.get(ChangeElementState.FAILED)).isZero();
        verify(exactly(NUMBER_OF_LIST_BATCHES), putRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenUpdateChangeElementsCalled_thenCMChangeServiceReturnsInternalServerError() {
        buildCmChangeServicePutWithReturnedStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThatThrownBy(() -> objectUnderTest.updateChangeElements(EXECUTION_ID, createTestList()))
                .isInstanceOf(FlmAlgorithmException.class);

        final int numberOfExpectedPostRequest = NUMBER_OF_LIST_BATCHES * MAX_ATTEMPTS_IN_TESTS;
        verify(exactly(numberOfExpectedPostRequest), putRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenUpdateChangeElementsCalled_thenCMChangeServiceReturnsNotFound() {
        buildCmChangeServicePutWithReturnedStatusCode(HttpStatus.SC_NOT_FOUND);
        assertThatThrownBy(() -> objectUnderTest.updateChangeElements(EXECUTION_ID, createTestList()))
                .isInstanceOf(FlmAlgorithmException.class);

        final int numberOfExpectedPostRequest = NUMBER_OF_LIST_BATCHES * MAX_ATTEMPTS_IN_TESTS;
        verify(exactly(numberOfExpectedPostRequest), putRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenUpdateChangeElementsCalled_thenCMChangeServiceReturnsWithNonRecoverableStatusCode() {
        buildCmChangeServicePutWithReturnedStatusCode(HttpStatus.SC_BAD_REQUEST);
        assertThatThrownBy(() -> objectUnderTest.updateChangeElements(EXECUTION_ID, createTestList()))
                .isInstanceOf(FlmAlgorithmException.class);

        verify(exactly(NUMBER_OF_LIST_BATCHES), putRequestedFor(urlEqualTo(CM_CHANGE_CHANGES_ENDPOINT)));
    }

    @Test
    public void whenUpdateChangeElementsCalled_andRestExecutionExceptionOccurs_thenThrowFlmAlgorithmException() {
        final CmRestExecutor cmRestExecutorMock = mock(CmRestExecutor.class);
        objectUnderTest = new ChangeElementSenderImpl(cmRestExecutorMock);

        when(cmRestExecutorMock.updateChangeElementsWithResponse(anyList())).thenThrow(RestExecutionException.class);

        assertThatThrownBy(() -> objectUnderTest.updateChangeElements(EXECUTION_ID, createTestList()))
                .isInstanceOf(FlmAlgorithmException.class);
    }

}