/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit tests for {@link PolicyOutputEventValidator} class.
 */
public class PolicyOutputEventValidatorTest {

    private static final String FDN = "fdn";
    private static final int OSS_ID = 1;
    private static final String EXCLUSION_REASON_MESSAGE_FOR_SECTOR = "No optimization is possible for sector because";

    private final PolicyOutputEventValidator objectUnderTest = new PolicyOutputEventValidator();

    private EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenEnrichedPolicyOutputEventContainsValidData_thenValidationWillPass() throws LbdarUnexpectedException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .build();

        objectUnderTest.isValid(enrichedPolicyOutputEvent.getPolicyOutputEvent());
    }

    @Test
    public void whenUsersToMoveNumbersInLbqAreInconsistent_thenValidationThrowsException() throws LbdarUnexpectedException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnectionButInconsistentUsersToMoveNumbers()
                .build();

        thrown.expect(LbdarUnexpectedException.class);
        thrown
                .expectMessage(StringContains.containsString(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " users-to-move numbers are inconsistent in LBQ"));

        objectUnderTest.isValid(enrichedPolicyOutputEvent.getPolicyOutputEvent());
    }

    @Test
    public void whenLoadBalancingQuantaContainsZeroUsersToMove_thenValidationThrowsException() throws LbdarUnexpectedException {
        enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCells()
                .withLBQ()
                .usersToMoveAtTargetOne(0)
                .build();

        thrown.expect(LbdarUnexpectedException.class);
        thrown
                .expectMessage(StringContains.containsString(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " users-to-move numbers are inconsistent in LBQ"));

        objectUnderTest.isValid(enrichedPolicyOutputEvent.getPolicyOutputEvent());
    }

    @Test
    public void whenAtSourceCellUsersToMoveNumbersHasNonNumericContent_thenValidationThrowsException() throws LbdarUnexpectedException {
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta(FDN, OSS_ID, "NaN");
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(StringContains.containsString(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " target cells are missing"));

        objectUnderTest.isValid(policyOutputEvent);
    }

    @Test
    public void whenAtOneOfTheTargetCellUsersToMoveNumbersHasNonNumericContent_thenValidationThrowsException() throws LbdarUnexpectedException {
        final List<TargetCell> targetCells = new ArrayList<>();
        targetCells.add(new TargetCell(FDN, OSS_ID, "50"));
        targetCells.add(new TargetCell(FDN, OSS_ID, "NaN"));
        targetCells.add(new TargetCell(FDN, OSS_ID, "50"));
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta("fdn", OSS_ID, "100", targetCells);
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(
                StringContains.containsString(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " users to move numbers has non-numeric content."));

        objectUnderTest.isValid(policyOutputEvent);
    }

    @Test
    public void whenLbqIsMissing_thenValidationThrowsException() throws LbdarUnexpectedException {
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(null);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(
                StringContains.containsString(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " LoadBalancingQuanta is missing"));

        objectUnderTest.isValid(policyOutputEvent);
    }

    @Test
    public void whenSourceCellMissing_thenValidationThrowsException() throws LbdarUnexpectedException {
        final List<TargetCell> targetCells = new ArrayList<>();
        targetCells.add(new TargetCell(FDN, OSS_ID, "50"));
        targetCells.add(new TargetCell(FDN, OSS_ID, "NaN"));
        targetCells.add(new TargetCell(FDN, OSS_ID, "50"));
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta("", OSS_ID, "100", targetCells);
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(
                StringContains.containsString(
                        EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " source cell is missing or incomplete sourceCellFdn=, sourceCellOssId=1"));

        objectUnderTest.isValid(policyOutputEvent);
    }

    @Test
    public void whenTargetCellsMissing_thenValidationThrowsException() throws LbdarUnexpectedException {
        final ProposedLoadBalancingQuanta lbq = new ProposedLoadBalancingQuanta("fdn", OSS_ID, "100", null);
        final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(
                StringContains.containsString(
                        EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " target cells are missing"));

        objectUnderTest.isValid(policyOutputEvent);
    }
}
