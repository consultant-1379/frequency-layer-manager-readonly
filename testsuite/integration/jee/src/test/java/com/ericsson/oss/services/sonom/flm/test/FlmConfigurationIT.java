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

package com.ericsson.oss.services.sonom.flm.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.TestExecutionTimeLogger;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.test.util.ConfigurationBuilder;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RunWith(OrderedTestRunner.class)
public class FlmConfigurationIT {

    private static final Gson GSON = new Gson();
    private static final String SCHEME_AND_AUTHORITY = "http://" + ServiceHostnameAndPortProvider.getFlmAlgorithmHostnameAndPort();
    private static final String CONTEXT_ROOT = "/son-om/algorithms/flm/v1";
    private static final String BASE_URI = SCHEME_AND_AUTHORITY + CONTEXT_ROOT;
    private static final String CONFIGURATIONS = "/configurations";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String ENABLED_ATTRIBUTE_NAME = "enabled";
    private static final String OPEN_LOOP_ATTRIBUTE_NAME = "openLoop";
    private static final String SCHEDULE_ATTRIBUTE_NAME = "schedule";
    private static final String CONFIGURATION_SETTINGS_ID_ALREADY_EXISTS = "A configuration with this Id already exists.";
    private static final String CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS = "A configuration with this name already exists";
    private static final String CONFIGURATION_SETTINGS_LIMIT_REACHED =
            "Only a maximum of 10 configurations can be persisted at once in the flm service";
    private static final String CONFIGURATION_UPDATE_FAILED = "Failed to update configuration";
    private static final String SCHEMA_VERIFICATION_FAILED = "The following configuration has failed schema verification";
    private static final String CONFIGURATION_ID_INVALID = "Invalid id, no configuration found in database for requested id";
    private static final String CONFIGURATION_NAME_INVALID = "Invalid name, no configuration found in database for requested name";
    private static final String NO_NAME_CONFIGURATION_EXIST = "No configuration currently exists in the database for requested name";
    private static final String USER_MESSAGE = "userMessage";
    private static final String DEVELOPER_MESSAGE = "developerMessage";
    private static final String ERROR_DATA = "errorData";
    private static final String ERROR_CAUSES = "errorCauses";
    private static final String STATUS = "Status";
    private static final String MESSAGE = "Message";
    private static final String ID_DOES_NOT_EXIST = "No configuration currently exists in the database for requested id";
    private static final String NO_CONFIGURATION_EXIST = "No configuration currently exists in the database for requested id";
    private static final String DELETE_SUCCESS_MESSAGE = "The configuration has been deleted for id: ";
    private static final String SUCCESS = "Success";
    private static final int MAXIMUM_GROUPS_PER_CONFIGURATION = 10_000;
    private static final String CREATE_CONFIGURATION_SETTINGS_JSON = "createConfigurationSettings.json";
    private static final String CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON = "createConfigurationSettingsWithId.json";
    private static final String TEST_CONFIG = "testconfig";
    private static final String AT_2_AM = "0 0 2 ? * * *";
    private static final String FORWARD_SLASH_10 = "/10";

    @Rule
    public TestExecutionTimeLogger testRuleLogTestRunData = new TestExecutionTimeLogger(System.out);

    /*
     * DB, at start, after install/downgrade/upgrade has 1 record that contains the default configuration, see the INSERT INTO
     * flm.configuration_settings & SELECT setval('flm.configuration_settings_id_seq'... statement in 'init-eric-son-flm-data.sql' sequence = 1
     */

    @Test
    @InSequence(1)
    public void whenPutWithIdCreatesAGap_thenNextInsertHonorsGap() {
        final String configurationJsonFileWithId = "reinstantiateConfigurationSettingsWithId.json";
        final String configurationJsonFileWithoutId = "reinstantiateConfigurationSettingsWithoutId.json";

        // insert with ID 3
        final int idThatCreatesGap = 3;
        httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/" + idThatCreatesGap),
                configurationJsonFileWithId);

        // insert without ID
        httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configurationJsonFileWithoutId);

        // check that the last inserted items honors the gap (last inserted ID+1 is generated)
        final Response resultResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "/" +
                (idThatCreatesGap + 1)));
        assertThat(resultResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonObject resultConfiguration = deserializeFromResponse(resultResponse, JsonObject.class);
        assertThat(resultConfiguration.get("id").getAsInt(), is(idThatCreatesGap + 1));

        // cleanup IDs 3,4
        httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/" + idThatCreatesGap));
        httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/" + (idThatCreatesGap + 1)));
    }

    @Test
    @InSequence(2)
    public void whenDeleteConfigurationCalledWithNonExistingConfigID_ThenReturn404NotFound() {
        final Response getResponse = httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/379"));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_NOT_FOUND));
        final JsonObject response = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(CONFIGURATION_ID_INVALID, is(response.get(USER_MESSAGE).getAsString()));
        assertThat(NO_CONFIGURATION_EXIST, is(response.get(DEVELOPER_MESSAGE).getAsString()));
    }

    @Test
    @InSequence(3)
    public void whenDeleteConfigurationCalledWithExistingConfigID_ThenReturn200_AndCorrectMessageReturned() {
        final Response postResponse = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), "createConfigurationToDelete.json");
        final Configuration actualConfiguration = deserializeFromResponse(postResponse, Configuration.class);
        final Response deleteResponse = httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/" + actualConfiguration.getId()));

        assertThat(deleteResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonObject response = deserializeFromResponse(deleteResponse, JsonObject.class);
        assertThat(SUCCESS, is(response.get(STATUS).getAsString()));
        assertThat(DELETE_SUCCESS_MESSAGE + actualConfiguration.getId(), is(response.get(MESSAGE).getAsString()));

        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + actualConfiguration.getId()));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_NOT_FOUND));
    }

    @Test
    @InSequence(4)
    public void whenConfigurationIsPassedAsJson_thenCorrespondingColumnsInDatabaseUpdated() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(readJson("updateConfigurationSettings.json"), Configuration.class))
                .withNumberOfGroups(0).build();
        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/1"), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_OK));
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS));
        assertThat(response.getStatus(), is(HttpStatus.SC_OK));
        final JsonArray configurationArray = deserializeFromResponse(getResponse, JsonArray.class);
        final Configuration actualConfiguration = GSON.fromJson(configurationArray.get(0).toString(), Configuration.class);
        final Configuration expectedConfiguration = GSON.fromJson(readJson("updateConfigurationSettings.json"), Configuration.class);
        expectedConfiguration.setGroups(configuration.getGroups());

        assertThat(expectedConfiguration, is(actualConfiguration));
    }

    @Test
    @InSequence(5)
    public void whenGetAllConfigurations_thenReturns200Ok() {
        final Response response = httpGetRequest(getFlmServiceUri(CONFIGURATIONS));
        assertThat(response.getStatus(), is(HttpStatus.SC_OK));
        final JsonArray configurationArray = deserializeFromResponse(response, JsonArray.class);
        assertThat(configurationArray.size(), is(1));
        final Configuration actualConfiguration = GSON.fromJson(configurationArray.get(0).toString(), Configuration.class);
        assertThat(actualConfiguration.getId(), is(1));
        assertThat(actualConfiguration.getName(), is(TEST_CONFIG));
    }

    @Test
    @InSequence(6)
    public void whenPutConfigurationWithInvalidCronExpression_thenReturns400BadRequest() {
        final Response putResponse = buildHttpClient(getFlmServiceUri(CONFIGURATIONS) + "/1")
                .put(Entity.json(getConfigurationWithInvalidCronExpression()));
        assertThat(putResponse.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        final JsonObject response = deserializeFromResponse(putResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(7)
    public void whenGetConfigurationById_thenSingleConfigurationReturned() {
        final int configurationId = 1;
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "/" + configurationId));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonObject configuration = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(configuration.get("id").getAsInt(), is(configurationId));
    }

    @Test
    @InSequence(8)
    public void whenGetConfigurationByName_thenSingleConfigurationReturned() {
        final String configurationName = TEST_CONFIG;
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "?name=" + configurationName));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonObject configuration = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(configuration.get("name").getAsString(), is(configurationName));
    }

    @Test
    @InSequence(9)
    public void whenIdDoesNotExist_thenConfigurationByIdReturnsNotFound() {
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "/678"));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_NOT_FOUND));
        final JsonObject response = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_ID_INVALID));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), is(ID_DOES_NOT_EXIST));
    }

    @Test
    @InSequence(10)
    public void whenNameDoesNotExist_thenConfigurationByNameReturnsNotFound() {
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "?name=nameNotInExistence"));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_NOT_FOUND));
        final JsonObject response = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_NAME_INVALID));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), is(NO_NAME_CONFIGURATION_EXIST));
    }

    @Test
    @InSequence(11)
    public void whenCreateRequestIsSentWithValidJSONAndNoId_thenConfigurationIsCreatedAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class);
        expectedConfiguration.setGroups(configuration.getGroups());

        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(12)
    public void whenCreateRequestIsSentWithValidJSONAndId_ConfigurationIsCreatedWithIdAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON), Configuration.class))
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON), Configuration.class);
        expectedConfiguration.setGroups(configuration.getGroups());

        assertThat(actualConfiguration.getId(), is(expectedConfiguration.getId()));
        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(13)
    public void whenCreateRequestIsSentWithValidJSONButAConfigurationWithThisNameAlreadyExists_thenConflictOccurs() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withNumberOfGroups(0).withName(TEST_CONFIG).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CONFLICT));
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS));
    }

    @Test
    @InSequence(14)
    public void whenCreateConfigurationWithInvalidCronExpression_thenReturns400BadRequest() {
        final Response putResponse = buildHttpClient(getFlmServiceUri(CONFIGURATIONS))
                .post(Entity.json(getConfigurationWithInvalidCronExpression()));
        assertThat(HttpStatus.SC_BAD_REQUEST, is(putResponse.getStatus()));
        final JsonObject response = deserializeFromResponse(putResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(15)
    public void whenCreateConfigurationWithInvalidEnabledField_thenReturns400BadRequest() throws IOException {
        final Configuration configuration = GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class);
        final String configurationJson = GSON.toJson(configuration);
        final Response postResponse = buildHttpClient(getFlmServiceUri(CONFIGURATIONS))
                .post(Entity.json(configurationJson.replaceFirst("false,", "\"invalidEnabledValue\",")));
        assertThat(HttpStatus.SC_BAD_REQUEST, is(postResponse.getStatus()));
        final JsonObject response = deserializeFromResponse(postResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(16)
    public void whenCreateConfigurationExceedsTheMaxOf10_000Groups_thenTheConfigurationIsRejected() throws IOException {
        final String configName = "createTestConfig3";
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName(configName)
                .withNumberOfGroups(MAXIMUM_GROUPS_PER_CONFIGURATION + 1).build();
        final Response multiGroupResponse = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(multiGroupResponse.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    @InSequence(17)
    public void whenCreateConfigurationExceedsTheMaxOfTenConfigurations_thenConflictOccurs() throws IOException {
        final int maxConfigurations = 10;
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonArray configurationArray = deserializeFromResponse(getResponse, JsonArray.class);
        final String configName = "createFillerConfig";
        final List<Configuration> fillerConfigurations = new ArrayList<>(10);
        Configuration configuration = GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class);
        for (int i = 0; i < maxConfigurations - configurationArray.size(); i++) {
            configuration = new ConfigurationBuilder(configuration)
                    .withName(configName + i)
                    .withNumberOfGroups(0)
                    .build();
            final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
            assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
            fillerConfigurations.add(deserializeFromResponse(response, Configuration.class));
        }
        configuration.setName(configName + maxConfigurations);
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CONFLICT));
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_LIMIT_REACHED));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_LIMIT_REACHED));
        fillerConfigurations.forEach(FlmConfigurationIT::deleteConfiguration);
    }

    @Test
    @InSequence(18)
    public void whenCreateConfigurationWithInvalidJson_thenReturn400BadRequest() {
        final String invalidConfigurationJson = "{\"id\": {())";
        final Response postResponse = buildHttpClient(getFlmServiceUri(CONFIGURATIONS))
                .post(Entity.json(invalidConfigurationJson));
        assertThat(HttpStatus.SC_BAD_REQUEST, is(postResponse.getStatus()));
        final JsonObject response = deserializeFromResponse(postResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString(), not(isEmptyString()));
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString(), not(isEmptyString()));
    }

    @Test
    @InSequence(19)
    public void whenUpdateRequestIsSentWithValidJSON_thenConfigurationIsUpdatedAndReturned() throws IOException {
        // crate a configuration which is different from createConfigurationSettingsWithId.json in every aspect except Id
        final int id = 66;
        final String locationUri = getFlmServiceUri(CONFIGURATIONS + "/" + id);
        final Configuration initialConfiguration = new ConfigurationBuilder().withId(id).withName("updateConfigurationTest")
                .withEnabled(true).withSchedule("0 0 8 * * ? 2099").withOpenLoop(false).withWeekendDays("").withEnablePA(false).build();
        final Response createResponse = httpPutRequest(locationUri, initialConfiguration);
        assertThat(createResponse.getStatus(), is(HttpStatus.SC_CREATED));
        // update every parameter of the newly created configuration
        final Configuration configuration = new ConfigurationBuilder(
                GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON), Configuration.class)).build();
        final Response updateResponse = httpPutRequest(locationUri, configuration);
        assertThat(updateResponse.getStatus(), is(HttpStatus.SC_OK));
        // get the updated configuration
        final Response getResponse = httpGetRequest(locationUri);
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_OK));
        final Configuration actualConfiguration = deserializeFromResponse(getResponse, Configuration.class);

        final Configuration expectedConfiguration = GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON), Configuration.class);
        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(20)
    public void whenPutConfigurationWithDifferentConfigurationIds_thenReturns400BadRequest() {
        // create a new update config with the index 2, and try to update CONFIGURATIONS + "/1" with that
        final Response putResponse = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/1"),
                "updateConfigurationEndpointMismatch.json");
        assertThat(putResponse.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        final JsonObject response = deserializeFromResponse(putResponse, JsonObject.class);
        assertThat(CONFIGURATION_UPDATE_FAILED, is(response.get(USER_MESSAGE).getAsString()));
        assertThat(SCHEMA_VERIFICATION_FAILED, is(response.get(DEVELOPER_MESSAGE).getAsString()));
    }

    @Test
    @InSequence(21)
    public void whenPutConfigurationWithInvalidQosForCapacityEstimation_thenReturns400BadRequest() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                replaceGlobalSetting("qosForCapacityEstimation", "0.55"))
                .withNumberOfGroups(0).build();
        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(22)
    public void whenPutConfigurationWithValidQosForCapacityEstimation_thenConfigurationIsCreatedAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestConfigQosValid")
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestConfigQosValid").build();
        expectedConfiguration.setGroups(configuration.getGroups());

        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(23)
    public void whenPutConfigurationWithInvalidOptimizationSpeed_thenReturns400BadRequest() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                replaceGlobalSetting("optimizationSpeed", "very fast"))
                .withNumberOfGroups(0).build();
        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(24)
    public void whenPutConfigurationWithValidOptimizationSpeed_thenConfigurationIsCreatedAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidOptimizationSpeed")
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidOptimizationSpeed").build();
        expectedConfiguration.setGroups(configuration.getGroups());

        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(23)
    public void whenPutConfigurationWithInvalidOverrideCCalculator_thenReturns400BadRequest() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                replaceGlobalSetting("overrideCCalculator", "someString"))
                .withNumberOfGroups(0).build();
        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(24)
    public void whenPutConfigurationWithValidOverrideCCalculator_thenConfigurationIsCreatedAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidOverrideCCalculator")
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidOverrideCCalculator").build();
        expectedConfiguration.setGroups(configuration.getGroups());

        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(23)
    public void whenPutConfigurationWithInvalidMaxLbdarStepsize_thenReturns400BadRequest() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                replaceGlobalSetting("maxLbdarStepsize", "someString"))
                .withNumberOfGroups(0).build();
        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
    }

    @Test
    @InSequence(24)
    public void whenPutConfigurationWithValidMaxLbdarStepsize_thenConfigurationIsCreatedAndReturned() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidMaxLbdarStepsize")
                .withNumberOfGroups(0).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final String locationUri = response.getLocation().toString();
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

        final Configuration expectedConfiguration = new ConfigurationBuilder(GSON.fromJson(
                readJson(CREATE_CONFIGURATION_SETTINGS_JSON), Configuration.class))
                .withName("createTestValidMaxLbdarStepsize").build();
        expectedConfiguration.setGroups(configuration.getGroups());

        compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(25)
    public void whenCreateConfigurationWithMandatoryAttributeMissing_thenReturns400BadRequest() {
        final Map<String, Configuration> attributeMap = new HashMap<>(4);
        attributeMap.put(NAME_ATTRIBUTE_NAME, new ConfigurationBuilder().withEnabled(false).withSchedule(AT_2_AM).withOpenLoop(true).build());
        attributeMap.put(ENABLED_ATTRIBUTE_NAME,
                new ConfigurationBuilder().withName("shouldNotExist").withSchedule(AT_2_AM).withOpenLoop(true).build());
        attributeMap.put(SCHEDULE_ATTRIBUTE_NAME,
                new ConfigurationBuilder().withName("shouldNotExist").withEnabled(false).withOpenLoop(true).build());
        attributeMap.put(OPEN_LOOP_ATTRIBUTE_NAME,
                new ConfigurationBuilder().withName("shouldNotExist").withEnabled(false).withSchedule(AT_2_AM).build());
        attributeMap.forEach((attribute, configuration) -> {
            final Response postResponse = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
            assertThat(HttpStatus.SC_BAD_REQUEST, is(postResponse.getStatus()));
            final List<String> errorCauses = getErrorCauses(deserializeFromResponse(postResponse, JsonObject.class));
            assertThat(errorCauses.size(), greaterThan(0));
            assertThat(errorCauses.get(0), is(String.format("Missing mandatory: \"%s\" attribute", attribute)));
        });
    }

    @Test
    @InSequence(26)
    public void whenCreateConfigurationWithOnlyMandatoryAttributes_thenConfigurationIsCreatedAndReturned() {
        final Configuration configuration = new ConfigurationBuilder().withName("mandatory").withEnabled(false).withSchedule(AT_2_AM)
                .withOpenLoop(true).build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
        final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);
        deleteConfiguration(actualConfiguration);
    }

    @Test
    @InSequence(27)
    public void whenCreateRequestIsSentWithValidJSONAndIdThatIsInUse_thenConflictOccurs() throws IOException {
        final Configuration configuration = new ConfigurationBuilder(
                GSON.fromJson(readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON), Configuration.class))
                .withId(1)
                .withNumberOfGroups(0)
                .build();
        final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CONFLICT));
        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_ID_ALREADY_EXISTS));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_ID_ALREADY_EXISTS));
    }

    @Test
    @InSequence(28)
    public void whenPutRequestIsSentAndConfigurationNameIsInUse_thenConflictOccurs() throws IOException {
        final String configurationJson = readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON);
        final Configuration configuration = new ConfigurationBuilder(GSON.fromJson(configurationJson, Configuration.class)).withName(TEST_CONFIG)
                .withNumberOfGroups(0)
                .build();

        final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/66"), configuration);
        assertThat(response.getStatus(), is(HttpStatus.SC_CONFLICT));

        final JsonObject object = deserializeFromResponse(response, JsonObject.class);
        assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS));
        assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS));
    }

    @Test
    @InSequence(29)
    public void whenPutRequestIsSentAndConfigurationNameRemainsTheSame_thenShouldUpdate() throws IOException {
        final String configurationJson = readJson(CREATE_CONFIGURATION_SETTINGS_WITH_ID_JSON);
        final int id = 100;
        final Configuration configurationCreate = new ConfigurationBuilder(GSON.fromJson(configurationJson, Configuration.class)).withId(id)
                .withEnabled(false)
                .withSchedule(AT_2_AM)
                .withNumberOfGroups(0)
                .build();

        final Response responseCreate = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configurationCreate);
        assertThat(responseCreate.getStatus(), is(HttpStatus.SC_CREATED));

        final String updateSchedule = "0 0 5 ? * * *";
        final Configuration configurationUpdate = new ConfigurationBuilder(GSON.fromJson(configurationJson, Configuration.class)).withId(id)
                .withEnabled(true)
                .withSchedule(updateSchedule)
                .withNumberOfGroups(0)
                .build();

        final Response responseUpdate = httpPutRequest(getFlmServiceUri(String.format("%s/%d", CONFIGURATIONS, id)), configurationUpdate);
        assertThat(responseUpdate.getStatus(), is(HttpStatus.SC_OK));

        final Response responseGet = httpGetRequest(getFlmServiceUri(String.format("%s/%d", CONFIGURATIONS, id)));
        assertThat(responseGet.getStatus(), is(HttpStatus.SC_OK));

        final Configuration actualConfiguration = deserializeFromResponse(responseGet, Configuration.class);

        final Configuration expectedConfiguration = GSON.fromJson(configurationJson, Configuration.class);
        expectedConfiguration.setId(id);
        expectedConfiguration.setEnabled(true);
        expectedConfiguration.setSchedule(updateSchedule);
        expectedConfiguration.setGroups(configurationUpdate.getGroups());

        assertThat(actualConfiguration.getId(), is(expectedConfiguration.getId()));
        compareConfigurations(actualConfiguration, expectedConfiguration, responseCreate.getLocation().toString());
        deleteConfiguration(actualConfiguration);
    }

    private static void compareConfigurations(final Configuration actualConfiguration, final Configuration expectedConfiguration,
            final String locationUri) {
        if (!expectedConfiguration.equals(actualConfiguration)) {
            assertThat(actualConfiguration.getName(), is(expectedConfiguration.getName()));
            assertThat(actualConfiguration.isEnabled(), is(expectedConfiguration.isEnabled()));
            assertThat(actualConfiguration.getSchedule(), is(expectedConfiguration.getSchedule()));
            assertThat(actualConfiguration.isOpenLoop(), is(expectedConfiguration.isOpenLoop()));
            // only check optional attributes if they are present in the expected response, therefore we care about them
            if (expectedConfiguration.getCustomizedDefaultSettings() != null) {
                assertThat(actualConfiguration.getCustomizedDefaultSettings(), is(expectedConfiguration.getCustomizedDefaultSettings()));
            }
            if (expectedConfiguration.getGroups() != null) {
                assertThat(actualConfiguration.getGroups(), is(expectedConfiguration.getGroups()));
            }
            if (expectedConfiguration.getInclusionList() != null) {
                assertThat(actualConfiguration.getInclusionList(), is(expectedConfiguration.getInclusionList()));
            }
            if (expectedConfiguration.getExclusionList() != null) {
                assertThat(actualConfiguration.getExclusionList(), is(expectedConfiguration.getExclusionList()));
            }
            if (expectedConfiguration.getWeekendDays() != null) {
                assertThat(actualConfiguration.getWeekendDays(), is(expectedConfiguration.getWeekendDays()));
            }
            if (expectedConfiguration.isEnablePA() != null) {
                assertThat(actualConfiguration.isEnablePA(), is(expectedConfiguration.isEnablePA()));
            }
        }

        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS));
        assertThat(getResponse.getStatus(), is(HttpStatus.SC_OK));
        final JsonArray configurationArray = deserializeFromResponse(getResponse, JsonArray.class);
        assertThat("More than one configuration now should exist", configurationArray.size() > 1);

        //verify location header points at the new resource
        final Response getNewConfiguration = httpGetRequest(locationUri);
        assertThat(getNewConfiguration.getStatus(), is(HttpStatus.SC_OK));
        final JsonObject jsonObject = deserializeFromResponse(getNewConfiguration, JsonObject.class);

        assertThat(actualConfiguration.getId(), is(jsonObject.get("id").getAsInt()));
        assertThat(actualConfiguration.getName(), is(jsonObject.get("name").getAsString()));
        assertThat(actualConfiguration.getSchedule(), is(jsonObject.get("schedule").getAsString()));
    }

    private static String getFlmServiceUri(final String resource) {
        return BASE_URI + resource;
    }

    private static String readJson(final String fileName) throws IOException {
        InputStream jsonObjectStream = null;
        try {
            jsonObjectStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            final List<String> linesAsList = IOUtils.readLines(jsonObjectStream, "UTF-8");
            return String.join("", linesAsList);
        } finally {
            if (jsonObjectStream != null) {
                jsonObjectStream.close();
            }
        }
    }

    private static Configuration replaceGlobalSetting(final String settingsName, final String newValue) throws IOException {
        return replaceGlobalSetting(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), settingsName, newValue);
    }

    private static Configuration replaceGlobalSetting(final String validJson, final String settingsName, final String newValue) {
        final Configuration configuration = GSON.fromJson(validJson, Configuration.class);
        configuration.getCustomizedGlobalSettings().put(settingsName, newValue);
        return configuration;
    }

    private static Configuration replaceDefaultSetting(final String settingsName, final String newValue) throws IOException {
        return replaceDefaultSetting(readJson(CREATE_CONFIGURATION_SETTINGS_JSON), settingsName, newValue);
    }

    private static Configuration replaceDefaultSetting(final String validJson, final String settingsName, final String newValue) {
        final Configuration configuration = GSON.fromJson(validJson, Configuration.class);
        configuration.getCustomizedDefaultSettings().put(settingsName, newValue);
        return configuration;
    }

    private static Response httpPutRequest(final String uri, final Configuration configuration) {
        return buildHttpClient(uri).put(Entity.json(GSON.toJson(configuration)));
    }

    private static Response httpPutRequest(final String uri, final String jsonFileName) {
        return buildHttpClient(uri).put(Entity.json(ResourceLoaderUtils.getClasspathResourceAsStream(jsonFileName)));
    }

    private static Response httpPostRequest(final String uri, final String jsonFileName) {
        return buildHttpClient(uri).post(Entity.json(ResourceLoaderUtils.getClasspathResourceAsStream(jsonFileName)));
    }

    private static Response httpPostRequest(final String uri, final Configuration configuration) {
        return buildHttpClient(uri).post(Entity.json(GSON.toJson(configuration)));
    }

    private static Response httpGetRequest(final String uri) {
        return buildHttpClient(uri).get();
    }

    private static Response httpDeleteRequest(final String uri) {
        return buildHttpClient(uri).delete();
    }

    private static <E> E deserializeFromResponse(final Response response, final Type type) {
        return GSON.fromJson(response.readEntity(String.class), type);
    }

    private static String getConfigurationWithInvalidCronExpression() {
        final String cronExpression = CronMaker.now(20);
        final Configuration configuration = new Configuration();
        configuration.setId(1);
        configuration.setName("customConfigName");
        configuration.setSchedule(cronExpression + "/366/");
        configuration.setEnabled(true);
        configuration.setOpenLoop(true);
        return GSON.toJson(configuration);
    }

    private static Invocation.Builder buildHttpClient(final String uri) {
        return ClientBuilder.newClient()
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON);
    }

    private static void deleteConfiguration(final Configuration configuration) {
        final Response deleteResponse = httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/" + configuration.getId()));
        assertThat(deleteResponse.getStatus(), is(HttpStatus.SC_OK));
    }

    private static List<String> getErrorCauses(final JsonObject rootJson) {
        final List<String> errorCauses = new ArrayList<>();
        if (rootJson.has(ERROR_DATA)) {
            final JsonArray errorData = rootJson.getAsJsonArray(ERROR_DATA);
            for (int i = 0; i < errorData.size(); i++) {
                final JsonObject error = errorData.get(i).getAsJsonObject();
                if (error.has(ERROR_CAUSES)) {
                    errorCauses.addAll(Stream.of(error.getAsJsonArray(ERROR_CAUSES)).map(JsonArray::getAsString).collect(Collectors.toList()));
                }
            }
        }
        return errorCauses;
    }

    @FunctionalInterface
    private interface ReplaceSettingMethod {
        Configuration call(String settingName, String newValue) throws IOException;
    }

    @RunWith(Parameterized.class)
    public static class FlmConfigurationParameterizedSettingsIT {
        private static final ReplaceSettingMethod REPLACE_GLOBAL_SETTING_METHOD = FlmConfigurationIT::replaceGlobalSetting;
        private static final ReplaceSettingMethod REPLACE_DEFAULT_SETTING_METHOD = FlmConfigurationIT::replaceDefaultSetting;

        private final String settingName;
        private final String minValue;
        private final String maxValue;
        private final String belowMinValue;
        private final String aboveMaxValue;
        private final ReplaceSettingMethod replaceSettingMethod;

        public FlmConfigurationParameterizedSettingsIT(final String settingName, final String minValue, final String maxValue,
                final String belowMinValue, final String aboveMaxValue, final ReplaceSettingMethod replaceSettingMethod) {
            this.settingName = settingName;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.belowMinValue = belowMinValue;
            this.aboveMaxValue = aboveMaxValue;
            this.replaceSettingMethod = replaceSettingMethod;
        }

        @Parameterized.Parameters
        public static Object[][] input() {
            final String floatZero = "0.0";
            final String lessThan0 = "-0.00001";
            final String float100 = "100.0";
            final String moreThan100 = "100.00001";
            final String int100 = "100";
            return new Object[][]{
                    //      settingName, min, max, belowMin, aboveMax, replaceSettingMethod
                    // GLOBAL SETTINGS
                    {"minNumCellForCDFCalculation", "0", "1000", "-1", "1001", REPLACE_GLOBAL_SETTING_METHOD},
                    {"percentileForMaxConnectedUser", floatZero, float100, lessThan0, moreThan100, REPLACE_GLOBAL_SETTING_METHOD},
                    {"targetPushBack", "0", "20", lessThan0, "20.00001", REPLACE_GLOBAL_SETTING_METHOD},
                    {"minLbdarStepsize", "0", "5", lessThan0, "5.00001", REPLACE_GLOBAL_SETTING_METHOD},
                    {"leakageThirdCell", "0", int100, lessThan0, moreThan100, REPLACE_GLOBAL_SETTING_METHOD},
                    {"leakageLbqImpact", "0", int100, lessThan0, moreThan100, REPLACE_GLOBAL_SETTING_METHOD},
                    {"existingHighPush", "0", int100, lessThan0, moreThan100, REPLACE_GLOBAL_SETTING_METHOD},
                    {"numberOfKpiDegradedHoursThreshold", "1", "6", "0", "7", REPLACE_GLOBAL_SETTING_METHOD},
                    // DEFAULT SETTINGS
                    {"targetThroughputR(Mbps)", floatZero, "2000.0", lessThan0, "2000.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"deltaGFSOptimizationThreshold", floatZero, "1.0", lessThan0, "1.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"targetSourceCoverageBalanceRatioThreshold", floatZero, "2.0", lessThan0, "2.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"sourceTargetSamplesOverlapThreshold", floatZero, float100, lessThan0, moreThan100, REPLACE_DEFAULT_SETTING_METHOD},
                    {"targetSourceContiguityRatioThreshold", floatZero, "2.0", lessThan0, "2.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForInitialAndAddedErabEstabSuccRate", floatZero, float100, lessThan0, moreThan100,
                            REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1", floatZero, float100, lessThan0, moreThan100,
                            REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForErabPercentageLost", floatZero, float100, lessThan0, moreThan100, REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForErabPercentageLostForQci1", floatZero, float100, lessThan0, moreThan100,
                            REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForCellHoSuccRate", floatZero, float100, lessThan0, moreThan100, REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForCellAvailability", floatZero, float100, lessThan0, moreThan100, REPLACE_DEFAULT_SETTING_METHOD},
                    {"minimumSourceRetained", "0", int100, "-1", "101", REPLACE_DEFAULT_SETTING_METHOD},
                    {"minRopsForAppCovReliability", "1", "4", "0", "5", REPLACE_DEFAULT_SETTING_METHOD},
                    {"minNumCqiSamples", "0", "1000000", "-1", "1000001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"minNumSamplesForTransientCalculation", "0", "30", "-1", "31", REPLACE_DEFAULT_SETTING_METHOD},
                    {"sigmaForTransientCalculation", "1", "10", "0", "11", REPLACE_DEFAULT_SETTING_METHOD},
                    {"loadBalancingThresholdForEndcUsers", "0", int100, lessThan0, moreThan100, REPLACE_DEFAULT_SETTING_METHOD},
                    {"ulPuschSinrRatioThreshold", "0.1", "2.0", "0", "2.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"minTargetUlPuschSinr", "0", "20", lessThan0, "20.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"percentageBadRsrpRatioThreshold", floatZero, "10.0", lessThan0, "10.00001", REPLACE_DEFAULT_SETTING_METHOD},
                    {"minConnectedUsers", "0", "200", "-1", "201", REPLACE_DEFAULT_SETTING_METHOD}
            };
        }

        @Test
        public void whenPutDefaultConfigurationSettingAboveMaxRange_thenReturns400BadRequest() throws IOException {
            final Configuration configuration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, aboveMaxValue))
                    .withNumberOfGroups(0).build();
            final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
            final JsonObject object = deserializeFromResponse(response, JsonObject.class);
            assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
            assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
            assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
        }

        @Test
        public void whenPutDefaultConfigurationSettingBelowMinRange_thenReturns400BadRequest() throws IOException {
            final Configuration configuration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, belowMinValue))
                    .withNumberOfGroups(0).build();
            final Response response = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + FORWARD_SLASH_10), configuration);
            final JsonObject object = deserializeFromResponse(response, JsonObject.class);
            assertThat(response.getStatus(), is(HttpStatus.SC_BAD_REQUEST));
            assertThat(object.get(USER_MESSAGE).getAsString(), is(CONFIGURATION_UPDATE_FAILED));
            assertThat(object.get(DEVELOPER_MESSAGE).getAsString(), is(SCHEMA_VERIFICATION_FAILED));
        }

        @Test
        public void whenPostDefaultConfigurationSettingEqualToMinRange_thenConfigurationIsCreatedAndReturned() throws IOException {
            final Configuration configuration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, minValue))
                    .withNumberOfGroups(0).build();
            final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
            assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
            final String locationUri = response.getLocation().toString();
            final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

            final Configuration expectedConfiguration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, minValue)).build();
            expectedConfiguration.setGroups(configuration.getGroups());

            compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
            deleteConfiguration(actualConfiguration);
        }

        @Test
        public void whenPostDefaultConfigurationSettingEqualToMaxRange_thenConfigurationIsCreatedAndReturned() throws IOException {
            final Configuration configuration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, maxValue))
                    .withNumberOfGroups(0).build();
            final Response response = httpPostRequest(getFlmServiceUri(CONFIGURATIONS), configuration);
            assertThat(response.getStatus(), is(HttpStatus.SC_CREATED));
            final String locationUri = response.getLocation().toString();
            final Configuration actualConfiguration = deserializeFromResponse(response, Configuration.class);

            final Configuration expectedConfiguration = new ConfigurationBuilder(
                    replaceSettingMethod.call(settingName, maxValue)).build();
            expectedConfiguration.setGroups(configuration.getGroups());

            compareConfigurations(actualConfiguration, expectedConfiguration, locationUri);
            deleteConfiguration(actualConfiguration);
        }
    }

}
