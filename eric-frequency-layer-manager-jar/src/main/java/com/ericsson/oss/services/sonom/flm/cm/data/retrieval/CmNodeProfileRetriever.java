/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020-2021
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObject;
import com.ericsson.oss.presentation.server.sonom.cm.service.rest.api.v1.TopologyObjects;
import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.TopologyType;
import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.NodeWithIdleModePrioAtReleases;
import com.ericsson.oss.services.sonom.flm.cm.util.TopologyObjectsUtils;

/**
 * This class can be used to retrieve Nodes and associated IdleModePrioAtRelease objects from CM Topology Model.
 */
public class CmNodeProfileRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmNodeProfileRetriever.class);
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int SECONDS_TO_ESTABLISH_CONNECTION = 30;
    private static final int CMSERVICE_LIMIT = 2000;
    private static final int[] RECOVERABLE_STATUS_CODES = {HttpStatus.SC_INTERNAL_SERVER_ERROR};
    private final CmRestExecutor cmRestExecutor;

    /**
     * Creates a {@link CmNodeProfileRetriever} with default configurations for the REST client.
     */
    public CmNodeProfileRetriever() {
        cmRestExecutor = new CmRestClientCreator(MAX_RETRY_ATTEMPTS, SECONDS_TO_ESTABLISH_CONNECTION, RECOVERABLE_STATUS_CODES)
                .getClientWithRetry();
    }

    /**
     * Creates a {@link CmNodeProfileRetriever} with provided configurations for the REST client.
     *
     * @param maxRetryAttempts
     *            The number of attempts to connect to make.
     * @param secondsToEstablishConnection
     *            The number of seconds to wait for connection to be established.
     */
    public CmNodeProfileRetriever(final int maxRetryAttempts, final int secondsToEstablishConnection) {
        this(new CmRestClientCreator(maxRetryAttempts, secondsToEstablishConnection, RECOVERABLE_STATUS_CODES).getClientWithRetry());
    }

    /**
     * Creates a {@link CmNodeProfileRetriever} with provided {@link CmRestExecutor}.
     * Should be used in tests only.
     *
     * @param cmRestExecutor
     *            An instance of {@link CmRestExecutor} that is mocked in tests.
     */
    public CmNodeProfileRetriever(final CmRestExecutor cmRestExecutor) {
        this.cmRestExecutor = cmRestExecutor;
    }

    /**
     * Returns an iterator of {@link NodeWithIdleModePrioAtReleases} lists. It uses iterator because not 
     * all the objects should be retrieved at once, but pages of object should be retrieved and filtered by stores.
     *
     * @return a {@link NodeWithIdleModePrioAtReleaseIterator} of list of {@link NodeWithIdleModePrioAtReleases} objects
     */
    public Iterator<Collection<NodeWithIdleModePrioAtReleases>> retrieve() {
        return new NodeWithIdleModePrioAtReleaseIterator();
    }

    /** It retrieves an {@link IdleModePrioAtRelease} object for the given fdn and ossId from CM Service.
     * @param profileFdn the fdn of {@link IdleModePrioAtRelease} object
     * @param profileOssId the ossId for fdn
     * @return {@link IdleModePrioAtRelease} object, or null if not found
     */
    public IdleModePrioAtRelease retrieve(final String profileFdn, final int profileOssId) {
        try {
            final TopologyObject response = cmRestExecutor.getTopologyObjectByOssIdAndFdn(
                    TopologyType.IDLEMODEPRIOATRELEASE.toString(), profileOssId, profileFdn);
            if (response == null) {
                return null;
            }
            return TopologyObjectsUtils.getProfileObjectFromTopologyObject(response);
        } catch (RestExecutionException e) { //NOSONAR Logging is done
            LOGGER.warn("Exception is thrown while getting idleModePrioAtRelease {}", profileFdn);
            return null;
        }
    }

    private class NodeWithIdleModePrioAtReleaseIterator implements Iterator<Collection<NodeWithIdleModePrioAtReleases>> {
        private int pageNumberAlreadyReturnedByNext;
        private int pageNumberRequested;
        private Collection<NodeWithIdleModePrioAtReleases> currentPage;

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         * Reaching the database happens here, this could take some time
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            if (pageNumberAlreadyReturnedByNext == pageNumberRequested) {
                currentPage = requestNewPage();
            }
            return !currentPage.isEmpty();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Collection<NodeWithIdleModePrioAtReleases> next() {
            if (pageNumberAlreadyReturnedByNext == pageNumberRequested) {
                currentPage = requestNewPage();
            }
            pageNumberAlreadyReturnedByNext = pageNumberRequested;
            if (currentPage.isEmpty()) {
                throw new NoSuchElementException("No more NodeWithIdleModePrioAtReleases elements");
            }
            return Collections.unmodifiableCollection(currentPage);
        }

        private Collection<NodeWithIdleModePrioAtReleases> requestNewPage() {
            pageNumberRequested += 1;
            final TopologyObjects response = cmRestExecutor.getTopologyObjectsWithGivenChildTypeWithLimitAndPage(
                TopologyType.NODE.toString(), TopologyType.IDLEMODEPRIOATRELEASE.toString(), CMSERVICE_LIMIT, pageNumberRequested, false);

            return response.getTopologyObjects()
                    .parallelStream()
                    .map(nodeTopologyObject -> new NodeWithIdleModePrioAtReleases(
                            TopologyObjectsUtils.getNodeObjectFromTopologyObject(nodeTopologyObject),
                            TopologyObjectsUtils.getAllProfileObjectsFromTopologyObjects(nodeTopologyObject.getChildren())))
                    .collect(Collectors.toList());
        }

    }

}
