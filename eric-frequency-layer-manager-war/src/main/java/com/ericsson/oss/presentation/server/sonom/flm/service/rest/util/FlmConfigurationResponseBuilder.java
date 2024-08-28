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

package com.ericsson.oss.presentation.server.sonom.flm.service.rest.util;

import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility class to return response entity in json.
 */
public final class FlmConfigurationResponseBuilder {

    public static final String SUCCESS_STATUS = "Success";
    public static final String SUCCESS_MESSAGE = "Proposed configuration is validated and persisted to the database, and scheduled successfully";
    public static final String DELETE_SUCCESS_MESSAGE = "The configuration has been deleted for id: ";
    public static final String FAILED_TO_UPDATE_CONFIGURATION = "Failed to update configuration";
    public static final String CONFIGURATION_FAILED_SCHEMA_VERIFICATION = "The following configuration has failed schema verification";
    public static final String CONFIGURATION_PAYLOAD_NOT_NULL = "The configuration payload must not be null";
    public static final String CONFIGURATION_PAYLOAD_NULL = "The configuration payload is null or empty";
    public static final String ERROR_WHILE_PERSISTING = "Error while persisting the proposed configuration";
    public static final String VALIDATED_BUT_ERROR_PERSISTING = "Configuration settings have successfully been validated " +
            "but their persistence to the database has failed.";

    public static final String ERROR_WHILE_SCHEDULING = "Error while scheduling the proposed configuration";

    public static final String INVALID_ID_FOR_CONFIGURATION = "Invalid id, no configuration found in database for requested id";
    public static final String INVALID_NAME_FOR_CONFIGURATION = "Invalid name, no configuration found in database for requested name";
    public static final String NO_CONFIGURATION_EXIST = "No configuration currently exists in the database for requested id";
    public static final String NO_NAME_CONFIGURATION_EXIST = "No configuration currently exists in the database for requested name";
    public static final String ERROR_RETURNING_CONFIGURATIONS = "Error while returning the configurations";
    public static final String ERROR_DELETING_CONFIGURATIONS = "Failed to delete the configuration";
    public static final String RETRIEVAL_OF_CONFIGURATION_FAILED = "Retrieval of configurations from the database has failed";
    public static final String DELETE_CONFIGURATION_FAILED = "Error while deleting the configuration from the database";
    public static final String CONNECTION_ATTEMPT_FAILED = "The connection attempt to the database has failed";

    public static final String EMPTY_JSON = "{}";

    private FlmConfigurationResponseBuilder() {
    }

    /**
     * Build success json body for response.
     *
     * @return Correct json response
     */
    public static Response buildSuccessResponse() {
        final JsonObject statusJson = new JsonObject();
        statusJson.addProperty("Status", SUCCESS_STATUS);
        statusJson.addProperty("Message", SUCCESS_MESSAGE);
        return Response.ok()
                .entity(statusJson.toString())
                .build();
    }

    /**
     * Build delete success json body for response.
     *
     * @param configurationId
     *            {@link Integer} to be added to the response message
     * @return Correct json response
     */
    public static Response buildDeleteSuccessResponse(final Integer configurationId) {
        final JsonObject statusJson = new JsonObject();
        statusJson.addProperty("Status", SUCCESS_STATUS);
        statusJson.addProperty("Message", DELETE_SUCCESS_MESSAGE + configurationId);
        return Response.ok()
                .entity(statusJson.toString())
                .build();
    }

    /**
     * Build failed json body for response.
     *
     * @param userMessage
     *            {@link String} to be used to show user message
     * @param httpStatusCode
     *            {@link Integer} to be used to show http status code
     * @param developerMessage
     *            {@link String} to be used to show developer message
     * @param links
     *            {@link List} of {@link String} to be used to show any links
     * @param errorCauses
     *            {@link List} of {@link String} to be used to show all causes
     * @param configurationJson
     *            {@link String} to be used to show configuration received from user
     * @return Correct json response
     */
    public static Response buildFailedResponse(final String userMessage, final Response.Status httpStatusCode,
            final String developerMessage, final List<String> links,
            final List<String> errorCauses, final String configurationJson) {
        final JsonObject statusJson = new JsonObject();
        statusJson.addProperty("userMessage", userMessage);
        statusJson.addProperty("httpStatusCode", httpStatusCode.getStatusCode());
        statusJson.addProperty("internalErrorCode", httpStatusCode.getStatusCode());
        statusJson.addProperty("developerMessage", developerMessage);
        statusJson.addProperty("time", new Date().toString());
        if (!links.isEmpty()) {
            final JsonArray jsonArray = new JsonArray();
            links.forEach(jsonArray::add);
            statusJson.add("links", jsonArray);
        }

        final JsonObject errorData = new JsonObject();
        if (!errorCauses.isEmpty()) {
            final JsonArray jsonArray = new JsonArray();
            errorCauses.forEach(s -> jsonArray.add(s.replaceAll("\\[", "").replace("]", "")));
            errorData.add("errorCauses", jsonArray);
        }

        try {
            final JsonObject configuration = new Gson().fromJson(configurationJson, JsonObject.class);
            errorData.add("configuration", configuration);
        } catch (final Exception e) {
            errorData.addProperty("configuration", EMPTY_JSON);
        }
        final JsonArray errorDataArray = new JsonArray();
        errorDataArray.add(errorData);
        statusJson.add("errorData", errorDataArray);
        return Response.status(httpStatusCode)
                .entity(statusJson.toString()).build();
    }

}
