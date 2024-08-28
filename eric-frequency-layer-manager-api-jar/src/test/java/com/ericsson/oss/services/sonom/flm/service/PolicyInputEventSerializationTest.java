/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;
import com.google.gson.Gson;

/**
 * Unit tests for Deserialization of {@link PolicyInputEvent}.
 */
public class PolicyInputEventSerializationTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private final Gson gson = new Gson();
    private PolicyInputEvent policyInputEvent;
    private PolicyInputEvent response;

    private Object deserializedObj1;
    private Object deserializedObj2;
    private String serializedObj1;
    private String serializedObj2;

    @Before
    public void setUp() throws IOException {
        final String input = ResourceLoaderUtils.getClasspathResourceAsString("test_payload.json");

        final HashMap<String, String> cell1Kpis = new HashMap<>(3);
        cell1Kpis.put("goal_function_resource_efficiency", "2");
        cell1Kpis.put("unhappy_users", "2");

        final HashMap<String, String> cell2Kpis = new HashMap<>(3);
        cell2Kpis.put("goal_function_resource_efficiency", "3");
        cell2Kpis.put("unhappy_users", "1");

        final HashMap<String, String> cellSettings = new HashMap<>(2);
        cellSettings.put("target_throughput_r", "3");
        cellSettings.put("delta_gfs_optimization_threshold", "3");

        final OptimizationCell<String> c1 = new OptimizationCell<>("001", 11, cell1Kpis, Collections.emptyMap(), cellSettings);
        final OptimizationCell<String> c2 = new OptimizationCell<>("002", 22, cell2Kpis, Collections.emptyMap(), cellSettings);

        final List<OptimizationCell> optimizationCellList = Arrays.asList(c1, c2);
        policyInputEvent = new PolicyInputEvent(optimizationCellList, "101", "202");
        serializedObj1 = policyInputEvent.toJson();
        serializedObj2 = policyInputEvent.toJson();
        deserializedObj1 = gson.fromJson(serializedObj1, PolicyInputEvent.class);
        deserializedObj2 = gson.fromJson(serializedObj2, PolicyInputEvent.class);
        //The following is done to convert the string into the object of type PolicyInputEvent
        //so that the toJson() can be used and it is read as json(in ordered) rather than a multiline string
        response = gson.fromJson(input, PolicyInputEvent.class);
    }

    @Test
    public void objectSerializedToJsonConsistencyCheck() {
        softly.assertThat(serializedObj2).isEqualTo(serializedObj1);
        softly.assertThat(deserializedObj2).isEqualTo(deserializedObj1);
        softly.assertThat(deserializedObj1).isEqualTo(policyInputEvent);
        softly.assertThat(deserializedObj2).isEqualTo(policyInputEvent);
    }

    @Test
    public void objectEqualityWithTestPayload() {
        assertThat(response).isEqualTo(policyInputEvent);
    }

    @Test
    public void jsonDeserializedObjectInstanceCheck() {
        assertThat(deserializedObj1).isInstanceOf(PolicyInputEvent.class).isEqualTo(policyInputEvent);
    }
}
