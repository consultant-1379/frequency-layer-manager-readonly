/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.google.gson.Gson;

/**
 * Unit tests for Deserialization of {@link PolicyOutputEvent}.
 */
public class PolicyOutputEventDeserializationTest {

    @Test
    public void jsonDeserializedObjectInstanceCheck() throws IOException {
        final Gson gson = new Gson();
        final String output = ResourceLoaderUtils.getClasspathResourceAsString("test_output_payload.json");
        final PolicyOutputEvent response = gson.fromJson(output, PolicyOutputEvent.class);
        assertThat(response).isInstanceOf(PolicyOutputEvent.class);
    }

}
