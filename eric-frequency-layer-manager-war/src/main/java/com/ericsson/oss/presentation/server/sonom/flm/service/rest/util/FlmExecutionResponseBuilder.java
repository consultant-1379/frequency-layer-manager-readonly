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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility class to return response entity in json.
 */
public final class FlmExecutionResponseBuilder {
    public static final String INVALID_ID_FOR_EXECUTION = "Invalid id, no execution found in database for requested id";
    public static final String INVALID_ID_FOR_PAEXECUTION = "Invalid id, no PA execution found in database for " +
            "requested id";
    public static final String NO_EXECUTION_EXIST = "No execution exists in the database for requested id";
    public static final String NO_PAEXECUTION_EXIST = "No PA execution exists in the database for requested id";
    public static final String CONNECTION_ATTEMPT_FAILED = "The connection attempt to the database has failed";
    public static final String ERROR_RETURNING_EXECUTIONS = "Error while returning the executions";
    public static final String ERROR_RETURNING_PAEXECUTIONS = "Error while returning the PA executions";
    public static final String RETRIEVAL_OF_EXECUTION_FAILED = "Retrieval of execution from the database has failed";
    public static final String RETRIEVAL_OF_PAEXECUTION_FAILED = "Retrieval of PA execution from the database has " +
            "failed";

    private FlmExecutionResponseBuilder() {
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
     * @return Correct json response
     */
    public static Response buildFailedResponse(final String userMessage, final Response.Status httpStatusCode,
            final String developerMessage, final List<String> links,
            final List<String> errorCauses) {
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

        final JsonArray errorDataArray = new JsonArray();
        errorDataArray.add(errorData);
        statusJson.add("errorData", errorDataArray);
        return Response.status(httpStatusCode)
                .entity(statusJson.toString()).build();
    }
}
