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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateBalancedDistribution;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateCValue;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.leakageSimulation;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.data.Offset;

import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for {@link FormulaSolver} class.
 */
public class FormulaSolverTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void whenCouplingFactorIsCalculated_thenCorrectResultIsGiven() {
        final float sourceUsers = 1000f;
        final float sourceToTarget = 10f;
        final float targetUsers = 500f;
        final float targetToSource = 20f;

        final float targetUsersVersionTwo = 400;

        softly.assertThat(calculateCValue(sourceUsers, sourceToTarget, targetUsers, targetToSource))
                .isCloseTo(1.0f, Offset.offset(0.0f));
        softly.assertThat(calculateCValue(sourceUsers, sourceToTarget, targetUsersVersionTwo, targetToSource))
                .isCloseTo(1.25f, Offset.offset(0.0f));
    }

    @Test
    public void whenBalancedDistributionIsCalculated_thenCorrectResultIsGiven() {
        final long numberOfUsersInTarget = 600;
        final float targetToSource = 20f;
        final float c = 1;
        final long numberOfUsersInSource = 900;

        final float cVersionTwo = 1.25f;
        softly.assertThat(calculateBalancedDistribution(numberOfUsersInTarget, targetToSource, c, numberOfUsersInSource))
                .isEqualTo(13);
        softly.assertThat(calculateBalancedDistribution(numberOfUsersInTarget, targetToSource, cVersionTwo, numberOfUsersInSource))
                .isEqualTo(17);
    }

    @Test
    public void whenLeakageSimulationIsCalculated_thenCorrectResultIsGiven() {
        final long targetUsers = 550;
        final float targetToThirdDist = 20f;
        final long thirdPartyUsers = 1000;
        final float thirdPartyToTarget = 10f;

        final long thirdPartyUsersVersionTwo = 975;

        softly.assertThat(leakageSimulation(targetUsers, targetToThirdDist, thirdPartyUsers, thirdPartyToTarget, 0))
                .isEqualTo(518);
        softly.assertThat(leakageSimulation(targetUsers, targetToThirdDist, thirdPartyUsersVersionTwo, thirdPartyToTarget, 0))
                .isEqualTo(510);
    }

}
