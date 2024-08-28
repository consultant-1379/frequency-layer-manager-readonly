/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.presentation.server.sonom.flm.service.rest;

import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.common.test.rest.ResponseSoftAssertions;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * Unit tests for {@link ConfigurationResource} class.
 */
@RunWith(PowerMockRunner.class)
public class ConfigurationResourceTest {
    //TODO: assert on metrics
    private static final URI DUMMY_URI = URI.create("www.ericsson.com/test");
    private static final String EMPTY_JSON = "{}";
    private static final String WHITESPACE = "\\s+";
    private static final String VALID_CONFIG = getValidConfig();
    private static final String INVALID_CONFIG = getInvalidConfig();
    private static final String VOID = "";
    private static final String CONFIGURATION_NAME = "custom";
    private static final String MOCKED = "mocked";
    private static final String A_CONFIGURATION_WITH_THIS_NAME_ALREADY_EXISTS = "A configuration with this name already exists";
    private static final int CONFIGURATION_ID = 999_999_999;

    @Rule
    public final ResponseSoftAssertions softly = new ResponseSoftAssertions();

    //code relies on absolute classes so can't use Mockito here
    private final ConfigurationSettingsJsonValidationException mockCsjvException = createMockCsjvException();
    private final FlmConfigurationSettingsException mockFlmConfigurationSettingsException = createMockFcsException();

    @Mock
    private MetricHelper mockMetricHelper;

    @Mock
    private FlmConfigurationService mockConfigurationService;

    @Mock
    private Configuration mockConfiguration;

    private ConfigurationResource objectUnderTest;

    private Response response;
    private String actualEntity;

    @Before
    public void doSetUp() throws ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException,
            FlmConfigurationSettingsException {
        final ConfigurationResourceUtils configurationResourceUtilsMock = mock(ConfigurationResourceUtils.class);
        when(configurationResourceUtilsMock.getFirstMetricHelper(any())).thenReturn(mockMetricHelper);
        when(configurationResourceUtilsMock.getFirstConfigurationService(any())).thenReturn(mockConfigurationService);
        Whitebox.setInternalState(ConfigurationResourceUtils.class, "instance", configurationResourceUtilsMock);

        when(mockConfiguration.getId()).thenReturn(CONFIGURATION_ID);
        when(mockConfigurationService.postConfiguration(VALID_CONFIG)).thenReturn(mockConfiguration);
        when(mockConfigurationService.postConfiguration(CONFIGURATION_ID, VALID_CONFIG)).thenReturn(mockConfiguration);
        when(mockConfigurationService.postConfiguration(INVALID_CONFIG)).thenThrow(mockCsjvException);
        when(mockConfigurationService.postConfiguration(CONFIGURATION_ID, INVALID_CONFIG)).thenThrow(mockCsjvException);
        objectUnderTest = new ConfigurationResource();
        setUpUriInfo();
    }

    @Test
    public void whenPostConfiguration_withEmptyConfig_ThenReturn400AndErrorDescription() {
        response = objectUnderTest.postConfiguration(EMPTY_JSON);
        assertResponseCodeAndBodyContents(HttpStatus.SC_BAD_REQUEST,
                "The configuration payload must not be null",
                "The configuration payload is null or empty");
    }

    @Test
    public void whenPostConfiguration_withValidConfig_ThenReturn201AndConfigWithID() {
        response = objectUnderTest.postConfiguration(VALID_CONFIG);
        assertThat(response).hasStatusCode(HttpStatus.SC_CREATED)
                .containsEntity(mockConfiguration);
    }

    @Test
    public void whenPostConfiguration_withValidButConflictingConfig_ThenReturn409AndErrorDescription() throws ConfigurationSettingsJsonValidationException, SQLException,
            ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(any())).thenThrow(mockFlmConfigurationSettingsException);

        response = objectUnderTest.postConfiguration(VALID_CONFIG);
        assertResponseCodeAndBodyContents(HttpStatus.SC_CONFLICT,
                A_CONFIGURATION_WITH_THIS_NAME_ALREADY_EXISTS
        );

        assertEqualsIgnoringWhitespace(actualEntity);
    }

    @Test
    public void whenPostConfiguration_withInvalidConfig_ThenReturn400AndErrorDescription() {
        response = objectUnderTest.postConfiguration(INVALID_CONFIG);
        assertResponseCodeAndBodyContents(HttpStatus.SC_BAD_REQUEST,
                "Failed to update configuration",
                "The following configuration has failed schema verification",
                INVALID_CONFIG.replaceAll(WHITESPACE, VOID).trim()
        );
    }

    @Test
    public void whenPostConfiguration_withNonJson_ThenThrowNullPointerException() {
        assertThatThrownBy(() -> objectUnderTest.postConfiguration("NON_JSON"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void whenPostConfiguration_andActivitySchedulerExceptionThrown_ThenReturn500AndErrorDescription()
            throws ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(any())).thenThrow(new ActivitySchedulerException(MOCKED));

        response = objectUnderTest.postConfiguration(VALID_CONFIG);
        softly.assertThat(response).hasStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        final Object entity = response.getEntity();
        assertThat(entity).isInstanceOf(String.class);
        final String actual = String.valueOf(entity);
        softly.assertThat(actual).contains("Error while scheduling the proposed configuration");
        assertEqualsIgnoringWhitespace(actual);
    }

    @Test
    public void whenPostConfiguration_andSqlExceptionThrown_ThenReturn500AndErrorDescription()
            throws ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(any())).thenThrow(new SQLException(MOCKED));

        response = objectUnderTest.postConfiguration(VALID_CONFIG);

        assertResponseCodeAndBodyContents(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Error while persisting the proposed configuration",
                "Configuration settings have successfully been validated but their persistence to the database has failed."
        );
        assertEqualsIgnoringWhitespace(actualEntity);
    }

    @Test
    public void whenPutConfiguration_withEmptyConfig_ThenReturn400AndErrorDescription() {
        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, EMPTY_JSON);
        assertResponseCodeAndBodyContents(HttpStatus.SC_BAD_REQUEST,
                "The configuration payload must not be null",
                "The configuration payload is null or empty"
        );
    }

    @Test
    public void whenPutConfiguration_withValidConfig_ThenReturn201AndConfigWithID() {
        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, VALID_CONFIG);
        assertThat(response).hasStatusCode(HttpStatus.SC_CREATED)
                .containsEntity(mockConfiguration);
    }

    @Test
    public void whenPutConfiguration_withValidExistingConfigId_ThenReturn200AndConfigWithID() throws SQLException {
        when(mockConfigurationService.getConfiguration(CONFIGURATION_ID)).thenReturn(mockConfiguration);
        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, VALID_CONFIG);
        assertThat(response).hasStatusCode(HttpStatus.SC_OK)
                .containsEntity("{\"Status\":\"Success\",\"Message\":\"" +
                        "Proposed configuration is validated and persisted to the database, and scheduled successfully\"}");
    }

    @Test
    public void whenPutConfiguration_withValidButConflictingConfig_ThenReturn409AndErrorDescription() throws ConfigurationSettingsJsonValidationException, SQLException,
            ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(any(Integer.class), any())).thenThrow(mockFlmConfigurationSettingsException);

        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, VALID_CONFIG);

        assertResponseCodeAndBodyContents(
                HttpStatus.SC_CONFLICT,
                A_CONFIGURATION_WITH_THIS_NAME_ALREADY_EXISTS,
                A_CONFIGURATION_WITH_THIS_NAME_ALREADY_EXISTS
        );

        assertEqualsIgnoringWhitespace(actualEntity);
    }

    @Test
    public void whenPutConfiguration_withInvalidConfig_ThenReturn400AndErrorDescription() {
        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, INVALID_CONFIG);

        assertResponseCodeAndBodyContents(HttpStatus.SC_BAD_REQUEST,
                "Failed to update configuration",
                "The following configuration has failed schema verification",
                INVALID_CONFIG.replaceAll(WHITESPACE, VOID).trim()
        );
    }

    @Test
    public void whenPutConfiguration_withNonJson_ThenThrowNullPointerException() {
        assertThatThrownBy(() -> objectUnderTest.putConfiguration(CONFIGURATION_ID, "NON_JSON"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void whenPutConfiguration_andActivitySchedulerExceptionThrown_ThenReturn500AndErrorDescription()
            throws ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(eq(CONFIGURATION_ID), any())).thenThrow(new ActivitySchedulerException(MOCKED));

        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, VALID_CONFIG);

        assertResponseCodeAndBodyContents(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error while scheduling the proposed configuration");
        assertEqualsIgnoringWhitespace(actualEntity);
    }

    @Test
    public void whenPutConfiguration_andSqlExceptionThrown_ThenReturn500AndErrorDescription()
            throws ConfigurationSettingsJsonValidationException, SQLException, ActivitySchedulerException, FlmConfigurationSettingsException {
        when(mockConfigurationService.postConfiguration(eq(CONFIGURATION_ID), anyString())).thenThrow(new SQLException(MOCKED));
        response = objectUnderTest.putConfiguration(CONFIGURATION_ID, VALID_CONFIG);

        assertResponseCodeAndBodyContents(
                HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Error while persisting the proposed configuration",
                "Configuration settings have successfully been validated but their persistence to the database has failed."
        );

        assertEqualsIgnoringWhitespace(actualEntity);
    }

    @Test
    public void whenGetConfiguration_withValidId_ThenReturn200AndConfiguration() throws SQLException {
        when(mockConfigurationService.getConfiguration(CONFIGURATION_ID)).thenReturn(mockConfiguration);
        response = objectUnderTest.getConfiguration(CONFIGURATION_ID);

        softly.assertThat(response).hasStatusCode(HttpStatus.SC_OK);
        softly.assertThat(response.getEntity()).isEqualTo(mockConfiguration);
    }

    @Test
    public void whenGetConfiguration_withInvalidId_ThenReturn404AndErrorDescription() {
        response = objectUnderTest.getConfiguration(-1);
        assertResponseCodeAndBodyContents(HttpStatus.SC_NOT_FOUND,
                "Invalid id, no configuration found in database for requested id",
                "No configuration currently exists in the database for requested id"
        );
    }

    @Test
    public void whenGetConfiguration_andSqlExceptionThrown_ThenReturn500AndErrorDescription()
            throws SQLException {
        when(mockConfigurationService.getConfiguration(CONFIGURATION_ID)).thenThrow(new SQLException(MOCKED));

        response = objectUnderTest.getConfiguration(CONFIGURATION_ID);

        assertResponseCodeAndBodyContents(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Error while returning the configurations",
                "Retrieval of configurations from the database has failed",
                "The connection attempt to the database has failed"
        );
    }

    @Test
    public void whenGetConfigurations_withValidName_ThenReturn200AndConfigurations() throws SQLException {
        when(mockConfigurationService.getConfiguration(CONFIGURATION_NAME)).thenReturn(mockConfiguration);
        response = objectUnderTest.getConfigurations(CONFIGURATION_NAME);

        softly.assertThat(response).hasStatusCode(HttpStatus.SC_OK);
        softly.assertThat(response.getEntity()).isEqualTo(mockConfiguration);
    }

    @Test
    public void whenGetConfigurations_withNullName_ThenReturn200AndConfigurations() throws SQLException {
        when(mockConfigurationService.getConfigurations()).thenReturn(Collections.singletonList(mockConfiguration));

        response = objectUnderTest.getConfigurations(null);

        softly.assertThat(response).hasStatusCode(HttpStatus.SC_OK);
        final Object entity = response.getEntity();
        assertThat(entity).isInstanceOf(List.class);
        softly.assertThat(entity).isEqualTo(Collections.singletonList(mockConfiguration));
    }

    @Test
    public void whenGetConfigurations_withNonexistentName_ThenReturn404AndErrorDescription() {
        response = objectUnderTest.getConfigurations("nonexistent config");

        assertResponseCodeAndBodyContents(HttpStatus.SC_NOT_FOUND,
                "Invalid name, no configuration found in database for requested name",
                "No configuration currently exists in the database for requested name"
        );
    }

    @Test
    public void whenGetConfigurations_andSqlExceptionThrown_ThenReturn500AndErrorDescription() throws SQLException {
        when(mockConfigurationService.getConfiguration(CONFIGURATION_NAME)).thenThrow(new SQLException(MOCKED));

        response = objectUnderTest.getConfigurations(CONFIGURATION_NAME);

        assertResponseCodeAndBodyContents(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Error while returning the configurations",
                "Retrieval of configurations from the database has failed",
                "The connection attempt to the database has failed"
        );
    }

    @Test
    public void whenDeleteConfiguration_withValidId_ThenReturn200AndConfiguration() throws SQLException {
        when(mockConfigurationService.deleteConfiguration(CONFIGURATION_ID)).thenReturn(true);

        response = objectUnderTest.deleteConfiguration(CONFIGURATION_ID);

        assertResponseCodeAndBodyContents(HttpStatus.SC_OK,
                String.format("The configuration has been deleted for id: %d", CONFIGURATION_ID)
        );
    }

    @Test
    public void whenDeleteConfiguration_withInvalidId_ThenReturn404AndErrorDescription() {
        response = objectUnderTest.deleteConfiguration(-1);
        assertResponseCodeAndBodyContents(HttpStatus.SC_NOT_FOUND,
                "Invalid id, no configuration found in database for requested id",
                "No configuration currently exists in the database for requested id"
        );
    }

    @Test
    public void whenDeleteConfiguration_andSqlExceptionThrown_ThenReturn500AndErrorDescription() throws SQLException {
        when(mockConfigurationService.deleteConfiguration(CONFIGURATION_ID)).thenThrow(new SQLException(MOCKED));

        response = objectUnderTest.deleteConfiguration(CONFIGURATION_ID);

        assertResponseCodeAndBodyContents(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Failed to delete the configuration",
                "Error while deleting the configuration from the database",
                "The connection attempt to the database has failed"
        );
    }

    private void assertEqualsIgnoringWhitespace(final String entity) {
        final String noWhitespace = entity.replaceAll(WHITESPACE, VOID).trim();
        final String expected = VALID_CONFIG.replaceAll(WHITESPACE, VOID).trim();
        softly.assertThat(noWhitespace).contains(expected);
    }

    private void assertResponseCodeAndBodyContents(final int httpStatusCode, final String... bodyContents) {
        softly.assertThat(response).hasStatusCode(httpStatusCode);
        final Object entity = response.getEntity();
        assertThat(entity).isInstanceOf(String.class);
        actualEntity = String.valueOf(entity);
        softly.assertThat(actualEntity).contains(bodyContents);
    }

    private void setUpUriInfo() {
        final UriInfo uriInfoMock = mock(UriInfo.class);
        Whitebox.setInternalState(objectUnderTest, "uriInfo", uriInfoMock);
        when(uriInfoMock.getAbsolutePath()).thenReturn(DUMMY_URI);
        final UriBuilder mockUriBuilder = mock(UriBuilder.class);
        when(uriInfoMock.getAbsolutePathBuilder()).thenReturn(mockUriBuilder);
        when(mockUriBuilder.build()).thenReturn(DUMMY_URI);
    }

    private static String getInvalidConfig() {
        return "{\n" +
                "  \"id\": 2,\n" +
                "  \"fakeSetting\": \"fakeValue\",\n" +
                "  \"weekendDays\": 3,\n" +
                "  \"enablePA\": true\n" +
                "}";
    }

    private static String getValidConfig() {
        final String path = "Configuration.json";
        String config = null;
        try {
            config = ResourceLoaderUtils.getClasspathResourceAsString(path);
        } catch (final IOException e) {
            fail("Test Setup Failure, could not load" + path);
        }

        return config;
    }

    private static ConfigurationSettingsJsonValidationException createMockCsjvException() {
        return new ConfigurationSettingsJsonValidationException(
                FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_PARSING_ERROR, MOCKED);
    }

    private static FlmConfigurationSettingsException createMockFcsException() {
        return new FlmConfigurationSettingsException(FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS);
    }

}