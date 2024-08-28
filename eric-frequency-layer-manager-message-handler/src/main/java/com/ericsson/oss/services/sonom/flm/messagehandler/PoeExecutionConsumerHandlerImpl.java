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

package com.ericsson.oss.services.sonom.flm.messagehandler;

import java.util.List;

import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class can be used to start multiple KafkaConsumers to receive PolicyOutputEvents. The client should create an instance from this class when
 * the FLM service starts, because having multiple Consumers could involve re-balancing. For each Execution then when PolicyOutputEvents are needed
 * that consumeMessages method can be called.
 */
public class PoeExecutionConsumerHandlerImpl extends GenericExecutionConsumerHandlerImpl<PolicyOutputEvent> {

    /**
     * This constructor can be used to create object that handles multiple KafkaConsumer that consume {@link PolicyOutputEvent}. The consumers will be
     * created on this constructor. The number of consumer will be the same as number of recordHandlers
     *
     * @param kafkaRecordHandlers
     *            for each KafkaConsumer a record handler should be provided that will handle Kafka record.
     * @param kafkaConsumerWrapper
     *            this wrapper can be used for testing ExecutionConsumerHandlerImpl, should be null if real Kafka consumer should be used
     * @throws KafkaConsumerInstantiationException
     *             when failed to create a real KafkaConsumer
     * @throws BadSetupException
     *             when the number of consumers is not between 1 and 10 and number of recordsHandlers is not the same as number of consumers
     */
    public PoeExecutionConsumerHandlerImpl(final List<KafkaRecordHandler<PolicyOutputEvent>> kafkaRecordHandlers,
            final KafkaConsumerWrapper<PolicyOutputEvent> kafkaConsumerWrapper)
            throws KafkaConsumerInstantiationException, BadSetupException {
        super(kafkaRecordHandlers);
        processConsumers(kafkaRecordHandlers, kafkaConsumerWrapper);
    }

    @Override
    protected final GenericExecutionConsumer<PolicyOutputEvent> buildConsumer(final KafkaConsumerWrapper<PolicyOutputEvent> kafkaConsumerWrapper,
            final int consumerId,
            final KafkaRecordHandler<PolicyOutputEvent> recordHandler)
            throws KafkaConsumerInstantiationException, BadSetupException {

        LOGGER.info("Building ExecutionConsumer {}", consumerId);
        final GenericExecutionConsumer<PolicyOutputEvent> consumer = new PoeExecutionConsumer(consumerId, counters, recordHandler);
        KafkaConsumerWrapper<PolicyOutputEvent> wrapper = kafkaConsumerWrapper;
        LOGGER.info("ExecutionConsumer {} created", consumerId);

        if (wrapper == null) {
            wrapper = new PoeManualCommitBatchKafkaConsumerWrapper(consumerId);
        }

        wrapper.setKafkaRecordHandler(consumer);
        consumer.setKafkaConsumerWrapper(wrapper);
        LOGGER.info("ExecutionConsumer {} has been built", consumerId);
        return consumer;
    }

}