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
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * This class can be used to start multiple KafkaConsumers to receive PaPolicyOutputEvents. The client should create an instance from this class when
 * the FLM service starts, because having multiple Consumers could involve re-balancing. For each Execution then when PolicyOutputEvents are needed
 * that consumeMessages method can be called.
 */
public class PAExecutionConsumerHandlerImpl extends GenericExecutionConsumerHandlerImpl<PaPolicyOutputEvent> {
    /**
     * This constructor can be used to create object that handles multiple KafkaConsumer that consume {@link PaPolicyOutputEvent}. The consumers will
     * be created on this constructor. The number of consumer will be the same as number of recordHandlers
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
    public PAExecutionConsumerHandlerImpl(final List<KafkaRecordHandler<PaPolicyOutputEvent>> kafkaRecordHandlers,
            final KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaConsumerWrapper)
            throws KafkaConsumerInstantiationException, BadSetupException {
        super(kafkaRecordHandlers);
        processConsumers(kafkaRecordHandlers, kafkaConsumerWrapper);
    }

    @Override
    protected final GenericExecutionConsumer<PaPolicyOutputEvent> buildConsumer(final KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaConsumerWrapper,
            final int consumerId,
            final KafkaRecordHandler<PaPolicyOutputEvent> recordHandler)
            throws KafkaConsumerInstantiationException, BadSetupException {
        LOGGER.info("Building ExecutionConsumer {}", consumerId);
        final GenericExecutionConsumer<PaPolicyOutputEvent> consumer = new PAExecutionConsumer(consumerId, counters, recordHandler);
        KafkaConsumerWrapper<PaPolicyOutputEvent> wrapper = kafkaConsumerWrapper;
        LOGGER.info("ExecutionConsumer {} created", consumerId);

        if (wrapper == null) {
            wrapper = new PAManualCommitBatchKafkaConsumerWrapper(consumerId);
        }

        wrapper.setKafkaRecordHandler(consumer);
        consumer.setKafkaConsumerWrapper(wrapper);
        LOGGER.info("ExecutionConsumer {} has been built", consumerId);
        return consumer;
    }
}
