/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.messagehandler.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The class is used to build test data
 */
public class TestDataBuilder {

    private TestDataBuilder() {
    }

    public static ConsumerRecords<String, PolicyOutputEvent> buildConsumerRecords(final String executionId,
            final long sectorId,
            final int partition,
            final long offset) {
        final Map<TopicPartition, List<ConsumerRecord<String, PolicyOutputEvent>>> consumerRecords = new HashMap<>();
        consumerRecords.put(new TopicPartition("topic", partition),
                Collections.singletonList(buildConsumerRecord(executionId, sectorId, partition, offset)));
        return new ConsumerRecords<>(consumerRecords);
    }

    public static ConsumerRecord<String, PolicyOutputEvent> buildConsumerRecord(final String executionId,
            final Long sectorId,
            final int partition,
            final long offset) {
        return new ConsumerRecord<>("topic", partition, offset, "key",
                buildPolicyOutputEvent(executionId, sectorId,
                        buildLoadBalancingQuanta()));
    }

    public static PolicyOutputEvent buildPolicyOutputEvent(final String executionId,
            final Long sectorId,
            final ProposedLoadBalancingQuanta loadBalancingQuanta) {
        return new PolicyOutputEvent("flmpolicyoutputevent", "version", "nameSpace", "source", "target",
                sectorId, executionId, loadBalancingQuanta, Collections.emptyList());
    }

    public static ProposedLoadBalancingQuanta buildLoadBalancingQuanta() {
        return new ProposedLoadBalancingQuanta(
                "sourceCell",
                1,
                "100",
                Arrays.asList(
                        new TargetCell("" +
                                "targetCell1", 1, "30"),
                        new TargetCell("" +
                                "targetCell2", 1, "45"),
                        new TargetCell("" +
                                "targetCell3", 1, "25")));
    }
}
