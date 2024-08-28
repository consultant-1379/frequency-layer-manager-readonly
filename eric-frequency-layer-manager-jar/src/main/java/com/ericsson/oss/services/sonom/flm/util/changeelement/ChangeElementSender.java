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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Interface for change element sending.
 */
public interface ChangeElementSender {

    /**
     * Creates batches of the {@link ChangeElement} list based on the BATCH_SIZE variable and sends them sequentially to CM Service change DB. Ensures
     * that each pair of Optimization and Reversion are in the same batch.
     *
     * @param executionId
     *            ID of the {@link Execution}
     * @param changeElements
     *            list of {@link ChangeElement} to be sent
     * @return A map with the number of changes that were successfully sent and that failed to send.
     * @throws FlmAlgorithmException
     *             a wrapper {@link FlmAlgorithmException} is thrown if 0 batch could be sent successfully during execution
     */
    Map<ChangeElementState, Integer> postChangeElements(String executionId, List<Pair<ChangeElement, ChangeElement>> changeElements)
            throws FlmAlgorithmException;

    /**
     * Creates batches of the {@link ChangeElement} list based on the BATCH_SIZE variable and sends them sequentially to CM Service change DB to be
     * updated.
     *
     * @param executionId
     *            ID of the {@link Execution}
     * @param changeElements
     *            list of {@link ChangeElement} to be sent
     * @return A map with the number of changes that were successfully sent and that failed to send.
     * @throws FlmAlgorithmException
     *             a wrapper {@link FlmAlgorithmException} is thrown if 0 batch could be sent successfully during execution
     */
    Map<ChangeElementState, Integer> updateChangeElements(String executionId, List<ChangeElement> changeElements)
            throws FlmAlgorithmException;

}