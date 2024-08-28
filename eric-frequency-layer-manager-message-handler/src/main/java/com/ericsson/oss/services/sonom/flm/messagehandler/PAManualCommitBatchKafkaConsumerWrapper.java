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

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import com.ericsson.oss.services.sonom.common.kafka.deserializer.KafkaJsonDeserializer;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * This class extends the {@link GenericManualCommitBatchKafkaConsumerWrapper} to handle Kafka messages.
 * It does not directly handle the messages but calls the kafkaRecordHandler that is implemented by {@link PAExecutionConsumer}.
 */
public class PAManualCommitBatchKafkaConsumerWrapper extends GenericManualCommitBatchKafkaConsumerWrapper<PaPolicyOutputEvent> {
    private static final String PA_POLICY_OUTPUT_EVENT_KAFKA_GROUP_ID = "PaPolicyOutputEventGroup";
    private static final String CLIENT_ID_CONFIG = "PaPolicyOutputEventMessageConsumer";
    private static final String KAFKA_TOPIC = getEnvironmentValue("PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME");
    private static final KafkaJsonDeserializer<PaPolicyOutputEvent> PA_POLICY_OUTPUT_EVENT_KAFKA_JSON_DESERIALIZER
            = new KafkaJsonDeserializer<>(PaPolicyOutputEvent.class);

    public PAManualCommitBatchKafkaConsumerWrapper(final int consumerId) throws KafkaConsumerInstantiationException {
        super(PA_POLICY_OUTPUT_EVENT_KAFKA_GROUP_ID, KAFKA_TOPIC, CLIENT_ID_CONFIG, consumerId, PA_POLICY_OUTPUT_EVENT_KAFKA_JSON_DESERIALIZER);
    }
}
