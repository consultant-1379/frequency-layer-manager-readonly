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

package com.ericsson.oss.services.sonom.flm.cm.data.retrieval;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObjects;
import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.util.TopologyObjectsUtils;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * Retrieves CellGroup CM Data from the Cm Rest End point.
 */
public class CmCellGroupRetriever {

    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(CmCellGroupRetriever.class);
    private final CmRestExecutor cmEvaluationRestExecutor;

    /**
     * Creates a {@link CmCellGroupRetriever} with default configurations for the REST client.
     */
    public CmCellGroupRetriever() {
        final int[] httpCodes = { HttpStatus.SC_INTERNAL_SERVER_ERROR };

        cmEvaluationRestExecutor = new CmRestClientCreator(MAX_RETRY_ATTEMPTS,
                SECONDS_TO_ESTABLISH_CONNECTION, httpCodes)
                        .getClientWithRetry();
    }

    /**
     * Creates a {@link CmCellGroupRetriever} with provided configurations for the REST client.
     *
     * @param maxRetryAttempts
     *            The number of attempts to connect to make.
     * @param secondsToEstablishConnection
     *            The number of seconds to wait for connection to be established.
     */
    public CmCellGroupRetriever(final int maxRetryAttempts, final int secondsToEstablishConnection) {
        cmEvaluationRestExecutor = new CmRestClientCreator(maxRetryAttempts, secondsToEstablishConnection)
                .getClientWithRetry();
    }

    /**
     * Retrieve CM Data based on group name.
     *
     * @param groupName
     *            The name of the group to retrieve.
     * @param executionId
     *            the id of the execution retrieving the groups
     * @return A future to retrieve the result.
     */
    public Future<List<Cell>> retrieveGroupEvaluation(final String groupName, final String executionId) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        return executorService.submit(() -> {
            try {
                final TopologyObjects response = cmEvaluationRestExecutor.evaluateCollectionByName(groupName);
                logResponse(executionId, response);
                return TopologyObjectsUtils.getAllCellObjectsFromTopologyObjects(response.getTopologyObjects());
            } catch (final RestExecutionException e) {
                if (HttpStatus.SC_NOT_FOUND == e.getStatusCode()) {
                    logConfigurationNotOk(groupName, executionId, "Configuration defined with group name %s, does not exist, " +
                     "execution will continue.");
                    return Collections.emptyList();
                } else if (HttpStatus.SC_NOT_ACCEPTABLE == e.getStatusCode()) {
                    logConfigurationNotOk(groupName, executionId, "Configuration defined with group name %s, does not evaluate " +
                    "to a collection of topology objects, execution will continue");
                    return Collections.emptyList();
                } else {
                    throw e;
                }
            } finally {
                executorService.shutdown();
            }
        });
    }

    private void logConfigurationNotOk(final String groupName, final String executionId, final String message) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                    String.format(message, groupName)));
        }
    }

    private void logResponse(final String executionId, final TopologyObjects response) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                        String.format("Group evaluation response received from CM Topology service containing '%d' objects",
                        response.getTopologyObjects().size())));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                         String.format("Group evaluation topology response object is %s", response)));
        }
    }
}
