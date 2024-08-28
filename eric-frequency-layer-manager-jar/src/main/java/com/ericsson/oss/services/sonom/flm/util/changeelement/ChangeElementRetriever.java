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

package com.ericsson.oss.services.sonom.flm.util.changeelement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmRestClientCreator;
import com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * ChangeElementRetriever is used to retrieve List of {@link ChangeElement} object based on the parameters provided.
 */
public class ChangeElementRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeElementRetriever.class);

    private final PAExecution paExecution;
    private final CmRestExecutor cmRestExecutor;
    private final String sourceOfChange;

    /**
     * Constructor for ChangeElementRetriever.
     * 
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param cmRestExecutor
     *            {@link CmRestExecutor} instance is used to retrieve {@link List} of {@link ChangeElement}
     * @param sourceOfChange
     *            This parameter is used to filter parameter for REST call
     */
    public ChangeElementRetriever(final PAExecution paExecution, final CmRestExecutor cmRestExecutor,
            final String sourceOfChange) {
        this.paExecution = paExecution;
        this.cmRestExecutor = cmRestExecutor;
        this.sourceOfChange = sourceOfChange;
    }

    /**
     * Constructor for ChangeElementRetriever.
     * 
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param sourceOfChange
     *            This parameter is used to filter parameter for REST call
     */
    ChangeElementRetriever(final PAExecution paExecution, final String sourceOfChange) {
        this(paExecution, new CmRestClientCreator(PAConstants.MAX_RETRY_ATTEMPTS,
                PAConstants.SECONDS_TO_WAIT).getClientWithRetry(), sourceOfChange);
    }

    /**
     * Retrieves list of {@link ChangeElement}.
     * 
     * @return returns a list of {@link ChangeElement} or an empty list
     */
    public List<ChangeElement> retrieveChangeElementList() {
        LOGGER.info("Retrieving change elements for Execution ID {}, sourceOfChange {} and status {}",
                paExecution.getFlmExecutionId(),
                sourceOfChange,
                ChangeElementStatus.SUCCEEDED.getValue());
        List<ChangeElement> retrievedChangeElements = cmRestExecutor.getChangeElements(sourceOfChange, paExecution.getFlmExecutionId(),
                ChangeElementStatus.SUCCEEDED.getValue(), null, null);
        if (retrievedChangeElements.isEmpty()) {
            LOGGER.warn("Retrieved list of change elements is empty");
            return retrievedChangeElements;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "List of change elements retrieved for Execution ID {}, sourceOfChange {} and status {}: {}",
                    paExecution.getFlmExecutionId(),
                    sourceOfChange, ChangeElementStatus.SUCCEEDED.getValue(),
                    retrievedChangeElements);
        }
        LOGGER.info("{} Change elements retrieved for Execution ID {}, sourceOfChange {} and status {}",
                retrievedChangeElements.size(),
                paExecution.getFlmExecutionId(),
                sourceOfChange, ChangeElementStatus.SUCCEEDED.getValue());
        retrievedChangeElements = processResult(retrievedChangeElements);
        return retrievedChangeElements;
    }

    /**
     * Retrieves a list of {@link ChangeElement} that are of changeType REVERSION.
     *
     * @return returns a list of reversion {@link ChangeElement} or an empty list
     */
    public List<ChangeElement> retrieveReversionChangeElementList() {
        LOGGER.info("Retrieving reversion change elements for Execution ID {}, sourceOfChange {}",
                paExecution.getFlmExecutionId(), sourceOfChange);

        final List<ChangeElement> retrievedChangeElements = cmRestExecutor.getChangeElements(sourceOfChange, paExecution.getFlmExecutionId(), null,
                null,
                ChangeElement.ChangeType.REVERSION.toString());

        if (retrievedChangeElements.isEmpty()) {
            LOGGER.warn("Retrieved list of reversion change elements is empty");
            return retrievedChangeElements;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "List of reversion change elements retrieved for Execution ID {}, sourceOfChange {}: {}",
                    paExecution.getFlmExecutionId(), sourceOfChange, retrievedChangeElements);
        }

        LOGGER.info("{} Reversion change elements retrieved for Execution ID {}, sourceOfChange {}",
                retrievedChangeElements.size(), paExecution.getFlmExecutionId(), sourceOfChange);

        return retrievedChangeElements;
    }

    private List<ChangeElement> processResult(final List<ChangeElement> retrievedChangeElements) {
        final List<ChangeElement> changeElements = filterChangeType(retrievedChangeElements);
        if (changeElements.isEmpty()) {
            LOGGER.warn("List of change elements is empty after filtering for ChangeType= OPTIMIZATION");
            return changeElements;
        }
        final List<ChangeElement> finalListOfChangeElements = filterCorrespondingReversionElements(changeElements, retrievedChangeElements);
        if (finalListOfChangeElements.isEmpty()) {
            LOGGER.warn("List of change elements is empty after filtering out elements whose" +
                    " corresponding Reversion was SUCCESSFUL ");
        }
        LOGGER.info("{} Change elements left after filtering", finalListOfChangeElements.size());
        return finalListOfChangeElements;
    }

    private List<ChangeElement> filterChangeType(final List<ChangeElement> changeElements) {
        LOGGER.info("Filtering change elements based on change type (= OPTIMIZATION)");
        return changeElements.stream()
                .filter(e -> e.getChangeType() == ChangeElement.ChangeType.OPTIMIZATION)
                .collect(Collectors.toList());
    }

    private List<ChangeElement> filterCorrespondingReversionElements(final List<ChangeElement> changeElements,
            final List<ChangeElement> retrievedChangeElements) {
        LOGGER.info("Filtering change elements based on reversion status");
        final List<ChangeElement> filteredChangeElement = new ArrayList<>();
        final Set<String> reversionChangeElements = retrievedChangeElements.stream()
                .filter(e -> e.getChangeType() == ChangeElement.ChangeType.REVERSION)
                .filter(e -> e.getStatus().equals(ChangeElementStatus.SUCCEEDED.getValue()))
                .map(ChangeElement::getChangeId).collect(Collectors.toSet());
        for (final ChangeElement element : changeElements) {
            if (!reversionChangeElements.contains(element.getChangeId())) {
                filteredChangeElement.add(element);
            }
        }
        return filteredChangeElement;
    }

}