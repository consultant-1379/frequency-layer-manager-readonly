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

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObjects;
import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.util.TopologyObjectsUtils;

/**
 * Retrieve all Sectors and its associated Cells from CM Data.
 */
public class CmSectorCellRetriever {

    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 30;
    private final CmRestExecutor cmRestExecutor;

    /**
     * Creates a {@link CmSectorCellRetriever} with default configurations for the REST client.
     */
    public CmSectorCellRetriever() {
        this(new CmRestClientCreator(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION).getClientWithRetry());
    }

    /**
     * Creates a {@link CmSectorCellRetriever} with given {@link CmRestExecutor}.
     * Should be used in tests only.
     * @param cmRestExecutor in instance of {@link CmRestExecutor} that is mocked in tests.
     */
    public CmSectorCellRetriever(final CmRestExecutor cmRestExecutor) {
        this.cmRestExecutor = cmRestExecutor;
    }

    /**
     * Creates a {@link CmSectorCellRetriever} with provided configurations for the REST client.
     *
     * @param maxRetryAttempts
     *            The number of attempts to connect to make.
     * @param secondsToEstablishConnection
     *            The number of seconds to wait for connection to be established.
     */
    public CmSectorCellRetriever(final int maxRetryAttempts, final int secondsToEstablishConnection) {
        cmRestExecutor = new CmRestClientCreator(maxRetryAttempts, secondsToEstablishConnection)
                .getClientWithRetry();
    }

    /**
     * Retrieve all Sectors and its associated Cells from CM Data.Default limit is set to 7000 to avoid exhausting Cm-Service heap memory if 10
     * executions in parallel are run.
     * 
     * @return List of {@link TopologySector} with populated Cell associations.
     */
    public List<TopologySector> retrieve() {
        final TopologyObjects response = cmRestExecutor.getTopologyObjectsWithGivenAssociationWithLimitAndPage(TopologyType.SECTOR.toString(),
                TopologyType.CELL.toString(), 7000, null, true);

        return response.getTopologyObjects().parallelStream()
                .map(topologyObject -> new TopologySector(topologyObject.getId(),
                        TopologyObjectsUtils.getAllCellObjectsFromTopologyObjects(topologyObject.getAssociations())))
                .collect(Collectors.toList());
    }
}
