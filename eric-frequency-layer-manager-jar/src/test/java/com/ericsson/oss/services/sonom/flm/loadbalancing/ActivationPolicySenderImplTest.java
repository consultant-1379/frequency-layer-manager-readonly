/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.activation.kafka.ActivationMode;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicy;
import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicyKey;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.kafka.producer.KafkaMessageProducer;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;

/**
 * Unit test for {@link ActivationPolicySenderImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivationPolicySenderImplTest {
    private static final String CM_CHANGE_MEDIATION_KAFKA_TOPIC_NAME = "cmChangeMediation";
    private static final String SOURCE_OF_CHANGE = "FLM_1";
    private static final String EXECUTION_ID = "FLM_1_1212";
    private static final String CRON_EXPRESSION = "";
    private static final int ONCE = 1;
    private final Map<String, Object> filterCriteria = new HashMap<>();

    @Mock
    private KafkaMessageProducer<ActivationPolicyKey, ActivationPolicy> producerMock;
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        System.setProperty("CHANGE_MEDIATION_TOPIC",CM_CHANGE_MEDIATION_KAFKA_TOPIC_NAME);
        filterCriteria.put("executionId", EXECUTION_ID);
        filterCriteria.put("changeType", ChangeElement.ChangeType.OPTIMIZATION.toString());
    }

    @Test
    public void whenSendActivationPolicyToKafkaCalled_thenActivationPolicyWithCorrectFilterCriteriaIsSent() throws FlmAlgorithmException {
        final ActivationPolicySender objectUnderTest = new ActivationPolicySenderImpl(producerMock);

        when(producerMock.sendKafkaMessage(
                eq(CM_CHANGE_MEDIATION_KAFKA_TOPIC_NAME),
                eq(new ActivationPolicyKey(SOURCE_OF_CHANGE)),
                eq(new ActivationPolicy(SOURCE_OF_CHANGE, ActivationMode.IMMEDIATE, CRON_EXPRESSION, filterCriteria)),
                any()))
                .thenReturn(null);

        objectUnderTest.sendActivationPolicyToKafka(EXECUTION_ID, SOURCE_OF_CHANGE);

        verify(producerMock, times(ONCE)).sendKafkaMessage(
                eq(CM_CHANGE_MEDIATION_KAFKA_TOPIC_NAME),
                eq(new ActivationPolicyKey(SOURCE_OF_CHANGE)),
                eq(new ActivationPolicy(SOURCE_OF_CHANGE, ActivationMode.IMMEDIATE, CRON_EXPRESSION, filterCriteria)),
                any());
        verify(producerMock, times(ONCE)).close();
    }

    @Test
    public void whenKafkaProducerThrowsException_thenFLMAlgorithmExceptionIsThrown() throws FlmAlgorithmException {
        final ActivationPolicySender objectUnderTest = new ActivationPolicySenderImpl(producerMock);

        expectedException.expect(FlmAlgorithmException.class);
        when(producerMock.sendKafkaMessage(
                eq(CM_CHANGE_MEDIATION_KAFKA_TOPIC_NAME),
                eq(new ActivationPolicyKey(SOURCE_OF_CHANGE)),
                eq(new ActivationPolicy(SOURCE_OF_CHANGE, ActivationMode.IMMEDIATE, CRON_EXPRESSION, filterCriteria)),
                any()))
                .thenThrow(RuntimeException.class);

        objectUnderTest.sendActivationPolicyToKafka(EXECUTION_ID, SOURCE_OF_CHANGE);

        verify(producerMock, times(ONCE)).close();
    }
}