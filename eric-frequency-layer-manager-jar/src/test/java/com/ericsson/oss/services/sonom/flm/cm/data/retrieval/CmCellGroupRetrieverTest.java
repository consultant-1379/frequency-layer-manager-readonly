/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.retrieval;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit tests for {@link CmCellGroupRetriever} class.
 */
public class CmCellGroupRetrieverTest {

    private static final int CM_SERVICE_PORT = 8080;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().port(CM_SERVICE_PORT));

    private static final String CM_SERVICE_TOPOLOGY_COLLECTIONS_EVALUATE_BASE_URL = "/son-om/cm-topology/v2/collections/evaluate";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String COLLECTION_EVALUATION_BASE_URL = CM_SERVICE_TOPOLOGY_COLLECTIONS_EVALUATE_BASE_URL + "?name=";
    private static final String RESPONSE_BODY = ResourceLoader.loadResource("cellTopologyObjectsResponseBody.json");
    private static final String RELATION_RESPONSE_BODY = ResourceLoader.loadResource("relationTopologyObjectsResponseBody.json");
    private static final String GROUP_NAME = "group";
    private static final String FLM_ = "FLM_";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 1;
    private static CmCellGroupRetriever objectUnderTest;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(4);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        System.setProperty("CM_SERVICE_HOSTNAME", CM_SERVICE_HOSTNAME);
        System.setProperty("CM_SERVICE_PORT", String.valueOf(CM_SERVICE_PORT));
        objectUnderTest = new CmCellGroupRetriever(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION);
    }

    @After
    public void afterTest() {
        WIRE_MOCK_RULE.resetAll();
    }

    @Test
    public void whenSendingAValidRestRequestForCells_butA404ResponseIsReturned_thenThereIsNoRetryAndEmptyListIsReturned() throws Exception {
        buildGenericCmServiceGetStubReturnErrorNoRetry(HttpStatus.SC_NOT_FOUND);

        final Future<List<Cell>> future = objectUnderTest.retrieveGroupEvaluation(GROUP_NAME, FLM_ + System.currentTimeMillis());
        assertThat(future.get()).isEmpty();
    }

    @Test
    public void whenSendingAValidRestRequestForCells_butA406ResponseIsReturned_thenThereIsNoRetryAndEmptyListIsReturned() throws Exception {
        buildGenericCmServiceGetStubReturnErrorNoRetry(HttpStatus.SC_NOT_ACCEPTABLE);

        final Future<List<Cell>> future = objectUnderTest.retrieveGroupEvaluation(GROUP_NAME, FLM_ + System.currentTimeMillis());
        assertThat(future.get()).isEmpty();
    }

    @Test
    public void whenSendingAValidRestRequestForCells_butAnErrorResponseIsReturned_thenThereIsNoRetryAndAnExceptionIsThrown() throws Exception {
        buildGenericCmServiceGetFailureStubWithNoExceptionThrown();

        final Future<List<Cell>> future = objectUnderTest.retrieveGroupEvaluation(GROUP_NAME, FLM_ + System.currentTimeMillis());
        thrown.expect(ExecutionException.class);
        future.get();
    }

    @Test
    public void whenGettingCellObjectsFromTopologyObjects_ensureRelationObjectsAreNotIncluded() throws Exception {
        buildCmServiceCollectionRestSuccessStub(RELATION_RESPONSE_BODY);
        final Future<List<Cell>> future =
                objectUnderTest.retrieveGroupEvaluation(GROUP_NAME, FLM_ + System.currentTimeMillis());
        assertThat(future.get()).isEmpty();
    }

    @Test
    public void whenGettingCellObjectsFromTopologyObjects_ensureCellObjectsAreIncluded() throws Exception {
        buildCmServiceCollectionRestSuccessStub(RESPONSE_BODY);
        final Future<List<Cell>> future =
                objectUnderTest.retrieveGroupEvaluation(GROUP_NAME, FLM_ + System.currentTimeMillis());
        assertThat(future.get()).hasSize(1);
    }

 private static void buildCmServiceCollectionRestSuccessStub(final String response) {
        final String decodedUriPath = URI.create(COLLECTION_EVALUATION_BASE_URL).getPath();
        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withBody(response)
                        .withStatus(HttpStatus.SC_OK)));
    }

    private static void buildGenericCmServiceGetStubReturnErrorNoRetry(final int returnCode) {
        final String path = URI.create(COLLECTION_EVALUATION_BASE_URL).getPath();
        final String inScenario = "No Retry Scenario, No Exception";
        final String failedScenario = "Failed";

        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(path))
                .inScenario(inScenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(returnCode))
                .willSetStateTo(failedScenario));
    }

    private static void buildGenericCmServiceGetFailureStubWithNoExceptionThrown() {
        final String path = URI.create(COLLECTION_EVALUATION_BASE_URL).getPath();
        final String inScenario = "No Retry Scenario, Throw RestExecutionException";
        final String failedScenario = "Failed";

        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(path))
                .inScenario(inScenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(HttpStatus.SC_BAD_REQUEST))
                .willSetStateTo(failedScenario));
    }
}