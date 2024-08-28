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

package com.ericsson.oss.presentation.server.sonom.flm.service.rest;

import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.CONNECTION_ATTEMPT_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.ERROR_RETURNING_EXECUTIONS;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.ERROR_RETURNING_PAEXECUTIONS;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.INVALID_ID_FOR_EXECUTION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.INVALID_ID_FOR_PAEXECUTION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.NO_EXECUTION_EXIST;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.NO_PAEXECUTION_EXIST;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.RETRIEVAL_OF_EXECUTION_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.RETRIEVAL_OF_PAEXECUTION_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmExecutionResponseBuilder.buildFailedResponse;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.jndi.JndiServiceFinder;
import com.ericsson.oss.services.sonom.flm.service.api.FlmExecutionService;
import com.ericsson.oss.services.sonom.flm.service.api.PAExecutionService;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.executions.VisibleExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * REST API to see all executions that have ran in EC SON.
 */
@RequestScoped
@Path("/executions")
public class ExecutionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResource.class);
    private static final JndiServiceFinder JNDI_SERVICE_FINDER = new JndiServiceFinder();
    private static final MetricHelper FLM_METRIC_HELPER = JNDI_SERVICE_FINDER.findFirst(MetricHelper.class);

    private FlmExecutionService executionService;
    private PAExecutionService paExecutionService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllExecution() {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GET request received at '{}'", uriInfo.getAbsolutePath());
        }
        try {
            final List<ExecutionSummary> executionSummaries =
                    getExecutionService().getExecutionSummaries().stream().sorted(Comparator.comparing(ExecutionSummary::getStartTime)
                    .reversed()).map(ExecutionSummary::new).collect(Collectors.toList());
            return Response.ok(executionSummaries).build();
        } catch (final SQLException e) {
            LOGGER.error("Error getting execution ", e);
            return buildFailedResponse(ERROR_RETURNING_EXECUTIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    RETRIEVAL_OF_EXECUTION_FAILED, Arrays.asList(), Arrays.asList(CONNECTION_ATTEMPT_FAILED));
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST);
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExecution(@PathParam("id") final String executionId) {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GET request received at '{}'", uriInfo.getAbsolutePath());
        }
        try {
            final Execution execution = getExecutionService().getExecution(executionId);
            if (execution == null) {
                return buildFailedResponse(INVALID_ID_FOR_EXECUTION, Response.Status.NOT_FOUND,
                        NO_EXECUTION_EXIST, Arrays.asList(), Arrays.asList(NO_EXECUTION_EXIST));
            }
            return Response.ok(new VisibleExecution(execution)).build();
        } catch (final SQLException e) {
            LOGGER.error("Error getting execution ", e);
            return buildFailedResponse(ERROR_RETURNING_EXECUTIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    RETRIEVAL_OF_EXECUTION_FAILED, Arrays.asList(), Arrays.asList(CONNECTION_ATTEMPT_FAILED));
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST);
        }
    }

    @GET
    @Path("/{id}/pa")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPAExecutions(@PathParam("id") final String flmExecutionId) {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GET request received at '{}'", uriInfo.getAbsolutePath());
        }
        try {
            final List<PAExecution> paExecutions = getPAExecutionService().getPAExecutions(flmExecutionId);
            if (paExecutions == null || paExecutions.isEmpty()) {
                return buildFailedResponse(INVALID_ID_FOR_PAEXECUTION, Response.Status.NOT_FOUND,
                        NO_PAEXECUTION_EXIST, Arrays.asList(), Arrays.asList(NO_PAEXECUTION_EXIST));
            }
            return Response.ok(paExecutions).build();
        } catch (final SQLException e) {
            LOGGER.error("Error getting PA execution ", e);
            return buildFailedResponse(ERROR_RETURNING_PAEXECUTIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    RETRIEVAL_OF_PAEXECUTION_FAILED, Arrays.asList(), Arrays.asList(CONNECTION_ATTEMPT_FAILED));
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_EXECUTION_GET_REQUEST);
        }
    }

    private FlmExecutionService getExecutionService() {
        if (executionService == null) {
            executionService = JNDI_SERVICE_FINDER.findFirst(FlmExecutionService.class);
        }
        return executionService;
    }

    private PAExecutionService getPAExecutionService() {
        if (paExecutionService == null) {
            paExecutionService = JNDI_SERVICE_FINDER.findFirst(PAExecutionService.class);
        }
        return paExecutionService;
    }
}
