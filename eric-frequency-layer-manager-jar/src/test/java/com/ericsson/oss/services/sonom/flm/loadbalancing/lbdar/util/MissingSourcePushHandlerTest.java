/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;

public class MissingSourcePushHandlerTest {
    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private MissingSourcePushHandler objectUnderTest;
    private ProfileChangeCalculationHelper helper;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void whenThereIsSourceValue_thenNoPushSetAtSourceCell() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();
        helper = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        objectUnderTest = new MissingSourcePushHandler(helper);
        final boolean result = objectUnderTest.replaceMissingSourcePushWithDefault();

        softly.assertThat(result).isFalse();
    }

    @Test
    public void whenThereIsMissingSourceValue_thenPushSetAtSourceCell() throws LbdarException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsMissingSourceCarrier()
                .build();
        helper = new ProfileChangeCalculationHelper(enrichedPolicyOutputEvent);
        objectUnderTest = new MissingSourcePushHandler(helper);

        final boolean result = objectUnderTest.replaceMissingSourcePushWithDefault();

        softly.assertThat(result).isTrue();
    }
}
