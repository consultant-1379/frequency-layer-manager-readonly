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
package com.ericsson.oss.services.sonom.flm.cm.data.retrieval;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import java.net.URI;
import java.util.List;

import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit tests for {@link CmSectorCellRetriever} class.
 */
public class CmSectorCellRetrieverTest {

    private static final String CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL = "/son-om/cm-topology/v2/topology-objects/";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String BASE_URL = CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL + TopologyType.SECTOR.toString() + "?association_type="
            + TopologyType.CELL.toString() + "&limit=7000";
    private static final String RESPONSE_BODY = ResourceLoader.loadResource("sectorWithAssociatedCellTopologyObjectsResponseBody.json");
    private static final int CM_SERVICE_PORT = 8_080;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 1;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().port(CM_SERVICE_PORT));

    private static CmSectorCellRetriever objectUnderTest;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    static {
        System.setProperty("CM_SERVICE_HOSTNAME", CM_SERVICE_HOSTNAME);
        System.setProperty("CM_SERVICE_PORT", String.valueOf(CM_SERVICE_PORT));
    }

    @BeforeClass
    public static void setUp() {
        objectUnderTest = new CmSectorCellRetriever(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION);
    }

    @After
    public void afterTest() {
        WIRE_MOCK_RULE.resetAll();
    }

    @Test
    public void whenSendingAValidRestRequestForSectorsAndCells_thenA200ResponseWithListOfSectorsAndCells() {
        buildCmServiceRestSuccessStub(HttpStatus.SC_OK);
        final List<TopologySector> result = objectUnderTest.retrieve();
        softly.assertThat(result).hasSize(1);
        final TopologySector topologySector = result.get(0);
        softly.assertThat(topologySector.getSectorId()).isEqualTo(1L);
        softly.assertThat(topologySector.getAssociatedCells()).hasSize(3);
    }

    @Test
    public void whenSendingAValidRestRequestForSectorsAndCells_butAnInternalServerErrorOccurs_thenA500ResponseIsReturnedAndARetryOccursSuccessfully() {
        buildGenericCmServiceGetStubWithRetryBehaviour();

        final List<TopologySector> result = objectUnderTest.retrieve();
        softly.assertThat(result).hasSize(1);
        final TopologySector topologySector = result.get(0);
        softly.assertThat(topologySector.getSectorId()).isEqualTo(1L);
        softly.assertThat(topologySector.getAssociatedCells()).hasSize(3);

        verify(exactly(2), getRequestedFor(urlEqualTo(BASE_URL))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8")));
    }

    @Test
    public void whenSendingAValidRestRequestForSectorsAndCells_thenARestExecutionExceptionRaisedIfNotFoundErrorCodeAfterRetries() {
        buildCmServiceRestSuccessStub(HttpStatus.SC_NOT_FOUND);
        thrown.expect(RestExecutionException.class);
        objectUnderTest.retrieve();
    }

    private static void buildGenericCmServiceGetStubWithRetryBehaviour() {
        final String path = URI.create(BASE_URL).getPath();
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
                        .withBody(RESPONSE_BODY)));
    }

    private static void buildCmServiceRestSuccessStub(final int responseCode) {
        final String decodedUriPath = URI.create(BASE_URL).getPath();

        WIRE_MOCK_RULE.stubFor(get(urlPathEqualTo(decodedUriPath))
                .willReturn(aResponse()
                        .withBody(RESPONSE_BODY)
                        .withStatus(responseCode)));
    }

}
