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

import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.CONFIGURATION_FAILED_SCHEMA_VERIFICATION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.CONFIGURATION_PAYLOAD_NOT_NULL;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.CONFIGURATION_PAYLOAD_NULL;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.CONNECTION_ATTEMPT_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.DELETE_CONFIGURATION_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.EMPTY_JSON;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.ERROR_DELETING_CONFIGURATIONS;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.ERROR_RETURNING_CONFIGURATIONS;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.ERROR_WHILE_PERSISTING;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.ERROR_WHILE_SCHEDULING;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.FAILED_TO_UPDATE_CONFIGURATION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.INVALID_ID_FOR_CONFIGURATION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.INVALID_NAME_FOR_CONFIGURATION;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.NO_CONFIGURATION_EXIST;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.NO_NAME_CONFIGURATION_EXIST;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.RETRIEVAL_OF_CONFIGURATION_FAILED;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.VALIDATED_BUT_ERROR_PERSISTING;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.buildDeleteSuccessResponse;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.buildFailedResponse;
import static com.ericsson.oss.presentation.server.sonom.flm.service.rest.util.FlmConfigurationResponseBuilder.buildSuccessResponse;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.faces.bean.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.jndi.JndiServiceFinder;
import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * REST API for updating flm configuration settings in the database also can retrieve all the configuration settings.
 */
@RequestScoped
@Path("/configurations")
public class ConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationResource.class);
    private static final JndiServiceFinder JNDI_SERVICE_FINDER = new JndiServiceFinder();
    private static final MetricHelper FLM_METRIC_HELPER = ConfigurationResourceUtils.getInstance().getFirstMetricHelper(JNDI_SERVICE_FINDER);
    // build a javax.ws.rs.core.Response depending on custom logic based on
    // exception type and incoming json configuration
    private static final Map<Class, BiFunction<Exception, String, Response>> EXCEPTION_RESPONSE_MAP = new HashMap<>();
    static {
        /*
         * FlmConfigurationSettingsException
         */
        EXCEPTION_RESPONSE_MAP.put(FlmConfigurationSettingsException.class, (exception, configurationJson) -> {
            final String errorMessage = ((FlmConfigurationSettingsException) exception).getErrorMessage();
            return buildFailedResponse(errorMessage, Response.Status.CONFLICT, errorMessage,
                    Collections.emptyList(), Collections.singletonList(exception.getMessage()), configurationJson);
        });

        /*
         * ConfigurationSettingsJsonValidationException
         */
        EXCEPTION_RESPONSE_MAP.put(ConfigurationSettingsJsonValidationException.class, (exception, configurationJson) -> {
            LOGGER.error("Validation exception occurred", exception);
            final ConfigurationSettingsJsonValidationException validationException = (ConfigurationSettingsJsonValidationException) exception;

            return buildFailedResponse(FAILED_TO_UPDATE_CONFIGURATION,
                                       Status.BAD_REQUEST,
                                       CONFIGURATION_FAILED_SCHEMA_VERIFICATION,
                                       Collections.emptyList(),
                                       validationException.getErrorMessage(),
                                       configurationJson);
        });

        /*
         * ActivitySchedulerException
         */
        EXCEPTION_RESPONSE_MAP.put(ActivitySchedulerException.class, (exception, configurationJson) -> {
            LOGGER.error("Activity scheduling exception occurred", exception);
            return buildFailedResponse(ERROR_WHILE_SCHEDULING, Response.Status.INTERNAL_SERVER_ERROR, ERROR_WHILE_SCHEDULING,
                    Collections.emptyList(), Collections.singletonList(exception.getMessage()), configurationJson);
        });
    }

    @Context
    private UriInfo uriInfo;

    private FlmConfigurationService configurationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postConfiguration(final String configuration) {
        final long metricsStartTime = System.nanoTime();
        LOGGER.info("POST request received at '{}'", uriInfo.getAbsolutePath());
        try {
            if (isConfigurationEmpty(configuration)) {
                LOGGER.warn("Json payload is empty");
                return buildFailedResponse(CONFIGURATION_PAYLOAD_NOT_NULL, Response.Status.BAD_REQUEST,
                        CONFIGURATION_PAYLOAD_NULL, Collections.emptyList(), Collections.singletonList(CONFIGURATION_PAYLOAD_NULL), configuration);
            }
            final Configuration newConfiguration = getConfigurationService().postConfiguration(configuration);
            final UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Integer.toString(newConfiguration.getId()));
            return Response.created(builder.build()).entity(newConfiguration).build();
        } catch (final SQLException e) {
            LOGGER.error("Error persisting configuration ", e);
            return buildFailedResponse(ERROR_WHILE_PERSISTING, Response.Status.INTERNAL_SERVER_ERROR,
                    VALIDATED_BUT_ERROR_PERSISTING, Collections.emptyList(), Collections.singletonList(CONNECTION_ATTEMPT_FAILED), configuration);
        } catch (final Exception e) {
            return EXCEPTION_RESPONSE_MAP.get(e.getClass()).apply(e, configuration);
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_CREATE_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_CREATE_REQUESTS);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putConfiguration(@PathParam("id") final Integer configurationId, final String configuration) {
        final long metricsStartTime = System.nanoTime();
        LOGGER.info("PUT request received at '{}'", uriInfo.getAbsolutePath());
        try {
            if (isConfigurationEmpty(configuration)) {
                LOGGER.warn("Json payload is empty");
                return buildFailedResponse(CONFIGURATION_PAYLOAD_NOT_NULL, Response.Status.BAD_REQUEST,
                        CONFIGURATION_PAYLOAD_NULL, Collections.emptyList(), Collections.singletonList(CONFIGURATION_PAYLOAD_NULL), configuration);
            }
            if (null == getConfigurationService().getConfiguration(configurationId)) {
                LOGGER.info("No configuration found in database, creating new configuration from id {}", configurationId);
                final Configuration newConfiguration = getConfigurationService().postConfiguration(configurationId, configuration);
                final UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                builder.path(Integer.toString(newConfiguration.getId()));
                return Response.created(builder.build()).entity(newConfiguration).build();
            } else {
                getConfigurationService().putConfiguration(configurationId, configuration);
                return buildSuccessResponse();
            }
        } catch (final SQLException e) {
            LOGGER.error("Error persisting configuration ", e);
            return buildFailedResponse(ERROR_WHILE_PERSISTING, Response.Status.INTERNAL_SERVER_ERROR,
                    VALIDATED_BUT_ERROR_PERSISTING, Collections.emptyList(), Collections.singletonList(CONNECTION_ATTEMPT_FAILED), configuration);
        } catch (final Exception e) {
            return EXCEPTION_RESPONSE_MAP.get(e.getClass()).apply(e, configuration);
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_UPDATE_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_UPDATE_REQUESTS);
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfiguration(@PathParam("id") final Integer configurationId) {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GET request received at '{}' for id {}", uriInfo.getAbsolutePath(), configurationId);
        }
        final Configuration flmConfiguration;
        try {
            flmConfiguration = getConfigurationService().getConfiguration(configurationId);
            if (flmConfiguration == null) {
                return buildFailedResponse(INVALID_ID_FOR_CONFIGURATION, Response.Status.NOT_FOUND,
                        NO_CONFIGURATION_EXIST, Collections.emptyList(), Collections.singletonList(NO_CONFIGURATION_EXIST), EMPTY_JSON);
            }
            return Response.ok(flmConfiguration).build();
        } catch (final SQLException e) {
            LOGGER.error("Error getting configuration ", e);
            return buildFailedResponse(ERROR_RETURNING_CONFIGURATIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    RETRIEVAL_OF_CONFIGURATION_FAILED, Collections.emptyList(), Collections.singletonList(CONNECTION_ATTEMPT_FAILED), EMPTY_JSON);
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_GET_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_GET_REQUESTS);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigurations(@QueryParam("name") final String name) {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("GET request received at '{}'", uriInfo.getAbsolutePath());
        }
        try {
            if (name == null) {
                return Response.ok(getConfigurationService().getConfigurations()).build();
            }
            final Configuration flmConfiguration = getConfigurationService().getConfiguration(name);
            if (flmConfiguration == null) {
                return buildFailedResponse(INVALID_NAME_FOR_CONFIGURATION, Response.Status.NOT_FOUND,
                        NO_NAME_CONFIGURATION_EXIST, Collections.emptyList(), Collections.singletonList(NO_NAME_CONFIGURATION_EXIST), EMPTY_JSON);
            }
            return Response.ok(flmConfiguration).build();
        } catch (final SQLException e) {
            LOGGER.error("Error getting configuration ", e);
            return buildFailedResponse(ERROR_RETURNING_CONFIGURATIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    RETRIEVAL_OF_CONFIGURATION_FAILED, Collections.emptyList(), Collections.singletonList(CONNECTION_ATTEMPT_FAILED), EMPTY_JSON);
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_GET_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_GET_REQUESTS);
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteConfiguration(@PathParam("id") final Integer configurationId) {
        final long metricsStartTime = System.nanoTime();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DELETE request received at '{}' for id {}", uriInfo.getAbsolutePath(), configurationId);
        }
        try {
            if (getConfigurationService().deleteConfiguration(configurationId)) {
                LOGGER.debug("Successfully deleted Configuration with id {}", configurationId);
                return buildDeleteSuccessResponse(configurationId);
            } else {
                LOGGER.warn("Failed to delete configuration with id {}, does not exist in the database", configurationId);
                return buildFailedResponse(INVALID_ID_FOR_CONFIGURATION, Response.Status.NOT_FOUND,
                        NO_CONFIGURATION_EXIST, Collections.emptyList(), Collections.singletonList(NO_CONFIGURATION_EXIST), EMPTY_JSON);
            }
        } catch (final SQLException e) {
            LOGGER.error("Error deleting configuration ", e);
            return buildFailedResponse(ERROR_DELETING_CONFIGURATIONS, Response.Status.INTERNAL_SERVER_ERROR,
                    DELETE_CONFIGURATION_FAILED, Collections.emptyList(), Collections.singletonList(CONNECTION_ATTEMPT_FAILED), EMPTY_JSON);
        } finally {
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_DELETE_TIME_IN_MILLIS,
                    FLM_METRIC_HELPER.getTimeElapsedInMillis(metricsStartTime));
            FLM_METRIC_HELPER.incrementFlmMetric(FlmMetric.FLM_CONFIGURATION_DELETE_REQUESTS);
        }
    }

    private FlmConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = ConfigurationResourceUtils.getInstance().getFirstConfigurationService(JNDI_SERVICE_FINDER);
        }
        return configurationService;
    }

    private static boolean isConfigurationEmpty(final String configuration) {
        return configuration.replaceAll("\\s+", "").equals(EMPTY_JSON);
    }

}
