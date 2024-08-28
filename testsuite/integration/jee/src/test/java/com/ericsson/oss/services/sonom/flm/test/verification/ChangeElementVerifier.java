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
package com.ericsson.oss.services.sonom.flm.test.verification;

import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement.CHANGE_ID_KEY;
import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement.CHANGE_TYPE_KEY;
import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement.EXECUTION_ID_KEY;
import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement.SOURCE_OF_CHANGE_KEY;
import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement.STATUS_KEY;
import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.test.util.CsvReader;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.ericsson.oss.services.sonom.flm.util.metadata.FlmMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ChangeElementVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeElementVerifier.class);
    private static final String CHANGE_ELEMENT_ASSERTION_FILE = "csv-assertions/change_element/change_element_assertions.csv";
    private static final String CM_SERVICE_BASE_URI = "http://" + ServiceHostnameAndPortProvider.getCmServiceHostnameAndPort();
    private static final String CM_CHANGE_ENDPOINT = CM_SERVICE_BASE_URI + "/son-om/cm-change/v1/changes";
    private static final String CSV_DELIMITER = ",";
    private static final Gson GSON = new Gson();
    private static final String CHANGE_ELEMENT_CHANGE_ID_WITH_CELL_KPI_METADATA = "346592836776943766";
    private static final String CHANGE_ELEMENT_CHANGE_ID_WITH_SECTOR_KPI_METADATA = "346592843081761068";
    private static final String NULL_STRING = "null";

    private ChangeElementVerifier() {
    }

    public static void verifyDataFromFilteredResponse(final String flmExecutionId, final Map<String, String> filterForRestGet) {
        final List<String> expectedRows = CsvReader.getCsvAsListOfRowsContainingString(Boolean.TRUE, CHANGE_ELEMENT_ASSERTION_FILE, flmExecutionId);
        final List<String> wantedColumns = CsvReader.getHeader(expectedRows);
        expectedRows.remove(0); // Remove header row

        final List<ChangeElement> actualChangeElements = parseResponse(getFilteredResponse(filterForRestGet));

        assertThat(actualChangeElements).hasSize(expectedRows.size());

        for (final String row : expectedRows) {
            final List<String> expectedValues = Arrays.asList(row.split(CSV_DELIMITER));
            final ChangeElement changeElementToVerify = getChangeElementByChangeId(actualChangeElements, expectedValues.get(0));

            assertThat(changeElementToVerify).isNotNull();
            assertChangeElementMatchesExpectedValues(expectedValues, wantedColumns, changeElementToVerify);
        }
    }

    public static void verifyDataFromFilteredResponseContainsCellMetadataOrSectorMetadata(final Map<String, String> filterForRestGet) {

        final List<ChangeElement> actualChangeElements = parseResponse(getFilteredResponse(filterForRestGet));

        final ChangeElement changeElementWithCellKpiMetadata = getChangeElementByChangeId(actualChangeElements,
                CHANGE_ELEMENT_CHANGE_ID_WITH_CELL_KPI_METADATA);
        final String cellKpiMetadataString = Objects.requireNonNull(changeElementWithCellKpiMetadata).getMetadata().getValue();
        assertThat(cellKpiMetadataString).isNotNull();
        assertThat(cellKpiMetadataString).isNotEqualToIgnoringCase(NULL_STRING);

        final FlmMetadata cellKpiFlmMetadata = GSON.fromJson(cellKpiMetadataString, FlmMetadata.class);
        assertThat(cellKpiFlmMetadata.getPaWindow()).isNotNull();
        assertThat(cellKpiFlmMetadata.getListOfAffectedCellKpisWithTimestamps()).isNotEmpty();
        assertThat(cellKpiFlmMetadata.getListOfAffectedSectorKpisWithTimestamps()).isEmpty();

        final ChangeElement changeElementWithSectorKpiMetadata = getChangeElementByChangeId(actualChangeElements,
                CHANGE_ELEMENT_CHANGE_ID_WITH_SECTOR_KPI_METADATA);
        final String sectorKpiMetadataString = Objects.requireNonNull(changeElementWithSectorKpiMetadata).getMetadata().getValue();
        assertThat(sectorKpiMetadataString).isNotNull();
        assertThat(sectorKpiMetadataString).isNotEqualToIgnoringCase(NULL_STRING);

        final FlmMetadata sectorKpiFlmMetadata = GSON.fromJson(sectorKpiMetadataString, FlmMetadata.class);
        assertThat(sectorKpiFlmMetadata.getPaWindow()).isNotNull();
        assertThat(sectorKpiFlmMetadata.getListOfAffectedCellKpisWithTimestamps()).isEmpty();
        assertThat(sectorKpiFlmMetadata.getListOfAffectedSectorKpisWithTimestamps()).isNotEmpty();

    }

    private static void assertChangeElementMatchesExpectedValues(final List<String> expectedValues, final List<String> wantedColumns,
            final ChangeElement changeElementToVerify) {
        for (int i = 0; i < wantedColumns.size(); i++) {
            assertOnWantedColumn(wantedColumns.get(i), expectedValues.get(i), changeElementToVerify);
        }
    }

    private static void assertOnWantedColumn(final String column, final String expectedValue, final ChangeElement element) {
        LOGGER.info("Asserting Change Element '{}' contains {} '{}'", element.getChangeId(), column, expectedValue);
        switch (column) {
            case EXECUTION_ID_KEY:
                assertThat(element.getExecutionId()).isEqualTo(expectedValue);
                break;
            case SOURCE_OF_CHANGE_KEY:
                assertThat(element.getSourceOfChange()).isEqualTo(expectedValue);
                break;
            case STATUS_KEY:
                assertThat(element.getStatus()).isEqualTo(expectedValue);
                break;
            case CHANGE_ID_KEY:
                assertThat(element.getChangeId()).isEqualTo(expectedValue);
                break;
            case CHANGE_TYPE_KEY:
                assertThat(element.getChangeType().name()).isEqualTo(expectedValue);
                break;
            default:
                break;
        }
    }

    private static List<ChangeElement> parseResponse(final Response response) {
        return new Gson().fromJson(response.readEntity(String.class), new TypeToken<ArrayList<ChangeElement>>() {
        }.getType());
    }

    private static Response getFilteredResponse(final Map<String, String> filters) {
        final StringJoiner filtersJoiner = new StringJoiner("&", "?", "");
        filters.forEach((k, v) -> filtersJoiner.add(k + '=' + v));

        final String restUri = CM_CHANGE_ENDPOINT + filtersJoiner.toString();
        LOGGER.info("Getting change elements from CM REST interface with URI: {}", restUri);

        final Response response = ClientBuilder.newClient().target(restUri).request(MediaType.APPLICATION_JSON).get();
        LOGGER.info("Response {}", response.getStatus());

        return response;
    }

    private static ChangeElement getChangeElementByChangeId(final List<ChangeElement> changeElements, final String changeId) {
        for (final ChangeElement changeElement : changeElements) {
            if (changeId.equals(changeElement.getChangeId())) {
                return changeElement;
            }
        }

        return null;
    }
}
