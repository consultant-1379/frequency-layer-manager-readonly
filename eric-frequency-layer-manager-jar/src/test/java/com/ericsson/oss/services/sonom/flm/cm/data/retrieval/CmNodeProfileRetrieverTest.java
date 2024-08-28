/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithIdleModePrioAtReleases;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit tests for {@link CmNodeProfileRetriever} class.
 */
public class CmNodeProfileRetrieverTest {

    private static final int CM_SERVICE_PORT = 8_080;

    @ClassRule
    public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().port(CM_SERVICE_PORT));

    private static final String CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL = "/son-om/cm-topology/v2/topology-objects/";
    private static final String CM_SERVICE_HOSTNAME = "localhost";
    private static final String BASE_URL = CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL + TopologyType.NODE.toString() + "?child_type="
            + TopologyType.IDLEMODEPRIOATRELEASE.toString();
    private static final String BASE_URL_IDLE = CM_SERVICE_TOPOLOGY_OBJECTS_BASE_URL + TopologyType.IDLEMODEPRIOATRELEASE.toString();
    private static final String RESPONSE_BODY = ResourceLoader.loadResource("nodeWithAssociatedProfilesTopologyObjectsResponseBody.json");
    private static final String RESPONSE_BODY_EMPTY_LIST = ResourceLoader.loadResource(
            "nodeWithAssociatedProfilesTopologyObjectsResponseBodyEmptyList.json");
    private static final String RESPONSE_BODY_IDLEMODEPRIO = ResourceLoader.loadResource(
            "profileTopologyObjectResponseBody.json");

    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 1;

    private static CmNodeProfileRetriever objectUnderTest;

    static {
        System.setProperty("CM_SERVICE_HOSTNAME", CM_SERVICE_HOSTNAME);
        System.setProperty("CM_SERVICE_PORT", String.valueOf(CM_SERVICE_PORT));
    }

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @BeforeClass
    public static void setUp() {
        objectUnderTest = new CmNodeProfileRetriever(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION);
    }

    @After
    public void afterTest() {
        WIRE_MOCK_RULE.resetAll();
    }

    @Test
    public void whenRetrieveCalled_thenIteratorWorks() {
        buildCmServiceRestStub();

        final Iterator result = objectUnderTest.retrieve();
        assertThat(result).hasNext();
        final Collection<NodeWithIdleModePrioAtReleases> nodeWithIdleModePrioAtReleaseList =
                (Collection<NodeWithIdleModePrioAtReleases>) result.next();
        assertThat(nodeWithIdleModePrioAtReleaseList).hasSize(1);
        final NodeWithIdleModePrioAtReleases nodeWithIdleModePrioAtReleases = nodeWithIdleModePrioAtReleaseList.iterator().next();
        final Node node = nodeWithIdleModePrioAtReleases.getNode();
        assertThat(node).isNotNull();
        softly.assertThat(node.getNodeId()).isEqualTo(216_174_981_137_693_279L);
        softly.assertThat(nodeWithIdleModePrioAtReleases.getAssociatedProfiles()).hasSize(4);
        softly.assertThat(result).isExhausted();
        softly.assertThatThrownBy(() -> result.next())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void whenRetrieveCalled_thenIteratorWorksAfterInternalServerError() {
        buildGenericCmServiceGetStubWithInitialFailure();

        final Iterator result = objectUnderTest.retrieve();
        assertThat(result).hasNext();
        final Collection<NodeWithIdleModePrioAtReleases> nodeWithIdleModePrioAtReleaseList =
                (Collection<NodeWithIdleModePrioAtReleases>) result.next();
        assertThat(nodeWithIdleModePrioAtReleaseList).hasSize(1);
        final NodeWithIdleModePrioAtReleases nodeWithIdleModePrioAtReleases = nodeWithIdleModePrioAtReleaseList.iterator().next();
        final Node node = nodeWithIdleModePrioAtReleases.getNode();
        assertThat(node).isNotNull();
        softly.assertThat(node.getNodeId()).isEqualTo(216_174_981_137_693_279L);
        softly.assertThat(nodeWithIdleModePrioAtReleases.getAssociatedProfiles()).hasSize(4);
        softly.assertThat(result).isExhausted();
        softly.assertThatThrownBy(() -> result.next())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void whenRetrieveWithSpecArgsCalled_thenCorrectTopologyObjectReturned() {
        buildCmServiceRest_IdleModePrioResultStub();
        final String fdn = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_053,MeContext=653919_ROSE_BOWL_PERM_DAS13,ManagedElement=1," +
                "ENodeBFunction=1,LoadBalancingFunction=1,IdleModePrioAtRelease=5230";
        final int ossId = 3;
        final IdleModePrioAtRelease idleModePrioAtRelease = objectUnderTest.retrieve(fdn, ossId);
        assertThat(idleModePrioAtRelease).isNotNull();
        assertThat(idleModePrioAtRelease.getId()).isEqualTo(25_590L);
    }

    @Test
    public void whenCMRestExecutorFails_thenHasNextThrowsException() {
        buildCmServiceRestStubNotAnswering(HttpStatus.SC_NOT_FOUND);
        assertThatThrownBy(() -> objectUnderTest.retrieve().next())
                .isInstanceOf(RestExecutionException.class);
    }

    @Test
    public void whenCMRestExecutorFails_thenNextThrowsException() {
        buildCmServiceRestStubNotAnswering(HttpStatus.SC_NOT_FOUND);
        assertThatThrownBy(() -> objectUnderTest.retrieve().next())
                .isInstanceOf(RestExecutionException.class);
    }

    private static void buildCmServiceRest_IdleModePrioResultStub() {
        final String decodedUriPath = URI.create(BASE_URL_IDLE).getPath();

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath + ".*"))
                .willReturn(aResponse().withBody(RESPONSE_BODY_IDLEMODEPRIO).withStatus(HttpStatus.SC_OK)));
    }

    private static void buildCmServiceRestStub() {
        final String decodedUriPath = URI.create(BASE_URL).getPath();
        final String inScenario = "Scenario";

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath))
                .inScenario(inScenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withBody(RESPONSE_BODY).withStatus(HttpStatus.SC_OK))
                .willSetStateTo("SECOND_PAGE"));

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath))
                .inScenario(inScenario)
                .whenScenarioStateIs("SECOND_PAGE")
                .willReturn(aResponse().withBody(RESPONSE_BODY_EMPTY_LIST).withStatus(HttpStatus.SC_OK))
                .willSetStateTo(STARTED));
    }

    private static void buildGenericCmServiceGetStubWithInitialFailure() {
        final String decodedUriPath = URI.create(BASE_URL).getPath();
        final String inScenario = "Retry Scenario";

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath))
                .inScenario(inScenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR))
                .willSetStateTo("FIRST_PAGE"));

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath))
                .inScenario(inScenario)
                .whenScenarioStateIs("FIRST_PAGE")
                .willReturn(aResponse().withBody(RESPONSE_BODY).withStatus(HttpStatus.SC_OK))
                .willSetStateTo("SECOND_PAGE"));

        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath))
                .inScenario(inScenario)
                .whenScenarioStateIs("SECOND_PAGE")
                .willReturn(aResponse().withBody(RESPONSE_BODY_EMPTY_LIST).withStatus(HttpStatus.SC_OK))
                .willSetStateTo(STARTED));
    }

    private static void buildCmServiceRestStubNotAnswering(final int responseCode) {
        final String decodedUriPath = URI.create(BASE_URL_IDLE).getPath();
        WIRE_MOCK_RULE.stubFor(get(urlPathMatching(decodedUriPath + ".*"))
                .willReturn(aResponse()
                        .withStatus(responseCode)));
    }
}
