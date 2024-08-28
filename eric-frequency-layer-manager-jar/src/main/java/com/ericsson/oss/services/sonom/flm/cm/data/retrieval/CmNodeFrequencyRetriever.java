/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.retrieval;

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObjects;
import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithEutranFrequencies;
import com.ericsson.oss.services.sonom.flm.cm.util.TopologyObjectsUtils;

/**
 * A class that is used to retrieve nodes with frequencies from CM Topology Model.
 */
public class CmNodeFrequencyRetriever {
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 30;
    private static final int RESPONSE_LIMIT = 7000;
    private final CmRestExecutor cmRestExecutor;

    /**
     * Creates a {@link CmNodeFrequencyRetriever} with default configurations for the REST client.
     */
    public CmNodeFrequencyRetriever() {
        this(new CmRestClientCreator(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION).getClientWithRetry());
    }

    /**
     * Creates a {@link CmNodeFrequencyRetriever} with provided configurations for the REST client.
     *
     * @param maxRetryAttempts
     *            The number of attempts to connect to make.
     * @param secondsToEstablishConnection
     *            The number of seconds to wait for connection to be established.
     */
    public CmNodeFrequencyRetriever(final int maxRetryAttempts, final int secondsToEstablishConnection) {
        this(new CmRestClientCreator(maxRetryAttempts, secondsToEstablishConnection).getClientWithRetry());
    }

    /**
     * Create a {@link CmNodeFrequencyRetriever} with the provided {@link CmRestExecutor} for testing.
     * @param cmRestExecutor an instance {@link CmRestExecutor}
     */
    public CmNodeFrequencyRetriever(final CmRestExecutor cmRestExecutor) {
        this.cmRestExecutor = cmRestExecutor;
    }

    /**
     * Retrieve all Nodes and its child Frequencies from CM Data. Default limit is set to RESPONSE_LIMIT to avoid exhausting Cm-Service heap memory
     * if 10 executions in parallel are run.
     *
     * @return List of {@link NodeWithEutranFrequencies} with populated EUtranFrequencies associations.
     */
    public List<NodeWithEutranFrequencies> retrieve() {
        final TopologyObjects response = cmRestExecutor.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(TopologyType.NODE.toString(),
                TopologyType.EUTRANFREQUENCY.toString(), RESPONSE_LIMIT, null, true);

        return response.getTopologyObjects().parallelStream()
                .map(topologyObject -> new NodeWithEutranFrequencies(TopologyObjectsUtils.getNodeObjectFromTopologyObject(topologyObject),
                        TopologyObjectsUtils.getAllEUtranFrequencyObjectsFromTopologyObjects(topologyObject.getChildren())))
                .collect(Collectors.toList());
    }
}
