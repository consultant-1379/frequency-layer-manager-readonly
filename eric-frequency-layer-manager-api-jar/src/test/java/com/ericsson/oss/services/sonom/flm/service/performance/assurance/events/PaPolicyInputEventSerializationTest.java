/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.google.gson.Gson;

/**
 * Unit tests for Deserialization of {@link PaPolicyInputEvent} class.
 */
public class PaPolicyInputEventSerializationTest {

    @Test
    public void whenTheOutputEventIsDeserialized_thenItIsAnInstanceOfPaPolicyOutputEvent() throws IOException {
        final Gson gson = new Gson();
        final String output = ResourceLoaderUtils.getClasspathResourceAsString("test_pa_policy_payload.json");
        final PaPolicyInputEvent response = gson.fromJson(output, PaPolicyInputEvent.class);
        assertThat(response).isInstanceOf(PaPolicyInputEvent.class)
                .isEqualTo(PaPolicyPayloadBuilder.buildPaInputEvent());
    }

}
