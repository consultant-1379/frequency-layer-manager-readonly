/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler;

import java.util.Set;

/**
 * The interface is used to receive PolicyOutputEvent with multiple KafkaConsumers.
 * The client should call consumeMessages for a give Execution. The method returns when all the messages have been received or timeout reached.
 * Until these conditions are met the method waits.
 */
public interface ExecutionConsumerHandler {

    /** Should be called when PolicyOutputEvents need to be received for the given Execution. It waits until all the messages have been consumed
     * or timeout reached.
     * @param executionId the unique id of an Execution
     * @param timeOutMillis the timeout valued
     * @return true if all messages have been received before the timeout has been reached, false otherwise
     * @throws InterruptedException when the waiting thread has been interrupted
     */
    boolean waitMessages(String executionId, long timeOutMillis) throws InterruptedException;

    /**
     * This method is used to create counter for the Execution specified with executionId.
     * @param executionId the unique id of an Execution
     * @param messagesToConsume the number of PolicyOutputEvents to receive
     * @param sectorIds a list of sectorIds. It is used as a state for ExecutionCounter to know how many PolicyOutputEvents have been received.
     *                  This restores the state of ExecutionCounter after restart.
     */
    void consumeMessagesForExecution(String executionId, int messagesToConsume, Set<Long> sectorIds);

    /**
     * This method can be used to notify the handler the execution is not needed, so Kafka messages belonging to execution can be dropped.
     * @param executionId the id of the Execution
     */
    void removeExecution(String executionId);

    /**
     It closes the KafkaConsumers and stops all the counters.
     */
    void shutdown();

}
