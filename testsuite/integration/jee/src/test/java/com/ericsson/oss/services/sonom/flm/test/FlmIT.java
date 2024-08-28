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

package com.ericsson.oss.services.sonom.flm.test;

import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertTableContent;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertTableExists;
import static com.ericsson.oss.services.sonom.common.test.sql.SqlAssertions.assertViewExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.api.Element;
import com.ericsson.oss.services.sonom.cm.service.api.Model;
import com.ericsson.oss.services.sonom.common.env.DatabaseProperties;
import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.TestExecutionTimeLogger;
import com.ericsson.oss.services.sonom.common.test.util.IntegrationTestUtils;
import com.ericsson.oss.services.sonom.flm.test.util.CmMediationTestConsumer;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;
import com.ericsson.oss.services.sonom.flm.test.util.KpiDatabaseTestUtil;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(OrderedTestRunner.class)
public class FlmIT {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FlmIT.class);

    private static final String CONTENT_TYPE_VALUE = "application/json; charset=UTF-8";
    private static final String HTML_TYPE = "text/html";
    private static final String FLM_BASE_URI = "http://" + ServiceHostnameAndPortProvider.getFlmAlgorithmHostnameAndPort()
            + "/son-om/algorithms/flm/v1/";
    private static final String FLM_DOCS_URI = FLM_BASE_URI + "docs";
    private static final String REQUIRED_ELEMENTS_FILE_PATH = "RequiredElements.json";
    private static final String REQUIRED_KPIS_FILE_PATH = "RequiredKpis.json";

    @Rule
    public TestExecutionTimeLogger testRuleLogTestRunData = new TestExecutionTimeLogger(System.out);

    private RestResponse<String> sendGetRequest(final String uri, final String mediaType) throws IOException {
        final HttpGet httpGetRequest = new HttpGet(uri);
        httpGetRequest.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VALUE);
        httpGetRequest.addHeader(HttpHeaders.ACCEPT, mediaType);
        final RestExecutor restExecutor = new RestExecutor();
        return restExecutor.sendGetRequest(httpGetRequest);
    }

    @Test
    @InSequence(1)
    public void whenFlmDocEndPointHit_thenResponseIsOk_andTheBodyContainsTheCorrectTitle() throws IOException {
        final RestResponse<String> response = sendGetRequest(FLM_DOCS_URI, HTML_TYPE);
        assertEquals("Unexpected statusCode for " + FLM_DOCS_URI, HttpStatus.SC_OK, response.getStatusCode());
        assertThat(response.getEntity()).contains("FLM Activity Execution REST Interface API documentation");
    }

    @Test(timeout = 180000)
    @InSequence(2)
    public void whenFlmStartsUp_thenTheRequiredCmElementsArePostedCorrectlyToCmService() throws Exception {
        try (final CmMediationTestConsumer cmMediationTestConsumer = new CmMediationTestConsumer()) {
            cmMediationTestConsumer.waitUntilMessagesReceived();
            final Model modelFromMediationTopic = cmMediationTestConsumer.getLastMessage();
            assertThat(getFlmCmRequiredElements().getElements())
                    .containsExactlyInAnyOrder(modelFromMediationTopic.getElements().toArray(new Element[0]));
        }
    }

   @Test(timeout = 180000)
   @InSequence(3)
   public void whenFlmStartsItPostsDownRequiredKpisToKpiService_thenKpisShouldBeVisibleWithinTheKpiDefinitionTable()
           throws IOException, ParseException {
       final String kpiDefinitionsTableName = "kpi_definition";

       final List<String> expectedRows = CsvReader.getCsvAsList(Boolean.TRUE, "csv-assertions/kpi_service_user/kpi_definition_table_to_assert.csv");
       final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
       expectedRows.remove(0);

       while (!KpiDatabaseTestUtil.hasKpiTableRequiredAmountOfRows(kpiDefinitionsTableName, determineNumberOfRequiredKPIsBeingSentToKpiService())) {
           IntegrationTestUtils.sleep(10, TimeUnit.SECONDS);
       }

       assertTableContent(kpiDefinitionsTableName, wantedColumns, expectedRows, DatabaseProperties.getKpiServiceJdbcConnection(),
               DatabaseProperties.getKpiServiceJdbcProperties());
   }

    @Test(timeout = 180000)
    @InSequence(4)
    public void whenFlmStarts_thenTableCellConfigurationHistoryIsCreated() throws IOException, ParseException {
        assertTableExists("cell_configuration", DatabaseProperties.getFlmJdbcConnection(),
         DatabaseProperties.getFlmJdbcProperties());
        assertTableExists("cell_configuration_history", DatabaseProperties.getFlmJdbcConnection(),
         DatabaseProperties.getFlmJdbcProperties());
    }

    @Test(timeout = 180000)
    @InSequence(5)
    public void whenFlmStarts_thenViewCellConfigurationViewIsCreated() throws IOException, ParseException {
        assertViewExists("cell_configuration_view", DatabaseProperties.getFlmJdbcConnection(),
         DatabaseProperties.getFlmJdbcProperties());
    }

    @Test(timeout = 180000)
    @InSequence(6)
    public void whenFlmStarts_thenPolicyOutputEventTableIsCreated() throws IOException, ParseException {
        assertTableExists("flm_optimizations", DatabaseProperties.getFlmJdbcConnection(),
                DatabaseProperties.getFlmJdbcProperties());
    }

    private static int determineNumberOfRequiredKPIsBeingSentToKpiService() throws IOException, ParseException {
        final JSONParser jsonParser = new JSONParser();
        try {
            final String flmRequiredKPIsAsString = ResourceLoaderUtils.getClasspathResourceAsString(REQUIRED_KPIS_FILE_PATH);
            final JSONObject requiredKPIs = (JSONObject) jsonParser.parse(flmRequiredKPIsAsString);
            final JSONArray kpi_definitions = (JSONArray) requiredKPIs.get("kpi_definitions");
            final int numberOfRequiredKpis = kpi_definitions.size();

            LOGGER.info("The number of required KPIs found was {}", numberOfRequiredKpis);
            return numberOfRequiredKpis;
        } catch (final IOException | ParseException e) {
            LOGGER.warn("Error retrieving Required KPIs through filepath: {} - {}", REQUIRED_KPIS_FILE_PATH, e);
            throw e;
        }
    }

    private static Model getFlmCmRequiredElements() {
        try {
            final String flmCmRequiredElements = ResourceLoaderUtils.getClasspathResourceAsString(REQUIRED_ELEMENTS_FILE_PATH);
            final ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(flmCmRequiredElements, Model.class);
        } catch (final Exception e) {
            LOGGER.warn("Error loading resource through filepath: '{}', this file is copied into the test directory during mvn test phase.",
                    REQUIRED_ELEMENTS_FILE_PATH, e);
            throw new IllegalStateException("Error reading required CM elements and attributes for validation of the mediation message", e);
        }
    }

}