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

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.C1;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.C2;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.F2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

@RunWith(MockitoJUnitRunner.class)

public class EnrichedPolicyOutputEventTest {

    private static final int OSS_ID = 1;
    private static final String SOURCE_FDN = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    private static final String TARGET_FDN = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00003-1";
    private static final TopologyObjectId SOURCE_CELL_TOPOLOGY_OBJECT_ID = TopologyObjectId.of(SOURCE_FDN, OSS_ID);
    private static final TopologyObjectId TARGET_CELL_TOPOLOGY_OBJECT_ID = TopologyObjectId.of(TARGET_FDN, OSS_ID);
    private static final TargetCell TARGET_CELL = new TargetCell(TARGET_FDN, OSS_ID, "0");
    private static final List<TargetCell> targetList = Collections.singletonList(TARGET_CELL);
    private static final String EXCEPTION_MESSAGE = "Value of usersToMove was not found or was zero/negative at cell";

    private EnrichedPolicyOutputEvent objectUnderTest;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PolicyOutputEvent mockPolicyOutputEvent;

    @Mock
    private ProposedLoadBalancingQuanta mockProposedLoadBalancingQuanta;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        objectUnderTest = new EnrichedPolicyOutputEvent(mockPolicyOutputEvent, null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP);
    }

    @Test
    public void whenGetUsersToMoveFromCellIdCalledAndTheTargetWasNotFound_thenExceptionThrown() throws LbdarUnexpectedException {
        when(mockPolicyOutputEvent.getLoadBalancingQuanta()).thenReturn(mockProposedLoadBalancingQuanta);
        when(mockProposedLoadBalancingQuanta.getSourceCellFdn()).thenReturn(SOURCE_FDN);
        when(mockProposedLoadBalancingQuanta.getSourceCellOssId()).thenReturn(OSS_ID);
        when(mockProposedLoadBalancingQuanta.getTargetCells()).thenReturn(targetList);

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(StringContains.containsString(EXCEPTION_MESSAGE));

        objectUnderTest.getUsersToMoveFromCellId(TARGET_CELL_TOPOLOGY_OBJECT_ID);
    }

    @Test
    public void whenGetUsersToMoveFromCellIdCalledAndUsersIsZero_thenExceptionThrown() throws LbdarUnexpectedException {
        when(mockPolicyOutputEvent.getLoadBalancingQuanta()).thenReturn(mockProposedLoadBalancingQuanta);
        when(mockProposedLoadBalancingQuanta.getSourceCellFdn()).thenReturn(SOURCE_FDN);
        when(mockProposedLoadBalancingQuanta.getSourceCellOssId()).thenReturn(OSS_ID);
        when(mockProposedLoadBalancingQuanta.getSourceUsersMove()).thenReturn("0");

        thrown.expect(LbdarUnexpectedException.class);
        thrown.expectMessage(StringContains.containsString(EXCEPTION_MESSAGE));

        objectUnderTest.getUsersToMoveFromCellId(SOURCE_CELL_TOPOLOGY_OBJECT_ID);
    }

    @Test
    public void whenSectorWithDuplicateCellCarriersAndCellFoundInDuplicateCarrierCellToFrequency_thenFrequencyForTheCellIsFound()
            throws LbdarException {
        final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsWithSameCarrier()
                .build();

        assertThat(enrichedPolicyOutputEvent.getFrequencyForCarrier(2300, new TopologyObjectId(C2, OSS_ID))).isEqualTo(F2);
    }

    @Test
    public void whenSectorWithDuplicateCellCarriersAndCellNotFoundInDuplicateCarrierCellToFrequency_thenLbdarExceptionThrown() throws LbdarException {
        final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsWithSameCarrier()
                .build();

        exceptionRule.expect(LbdarException.class);
        exceptionRule.expectMessage("was found 2 times.");
        enrichedPolicyOutputEvent.getFrequencyForCarrier(2300, new TopologyObjectId(C1, OSS_ID));
    }

    @Test
    public void whenSectorWithMissingCellCarriers_thenLbdarExceptionThrown() throws LbdarException {
        final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithThreeCellsFullConnection()
                .build();
        final int invalidTargetCarrier = 9999;

        exceptionRule.expect(LbdarException.class);
        exceptionRule.expectMessage("was found 0 times.");
        enrichedPolicyOutputEvent.getFrequencyForCarrier(invalidTargetCarrier, new TopologyObjectId(C1, OSS_ID));
    }

    @Test
    public void whenCopyMake_thenShallowCopyIsMade() {
        final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent = EnrichedPolicyOutputEventBuilder
                .sectorWithTwoCellsWithSameCarrier()
                .build();

        final EnrichedPolicyOutputEvent copy = enrichedPolicyOutputEvent.copyMe();

        softly.assertThat(enrichedPolicyOutputEvent.getPolicyOutputEvent()).isEqualTo(copy.getPolicyOutputEvent());
        softly.assertThat(enrichedPolicyOutputEvent.getSourceCellNode()).isEqualTo(copy.getSourceCellNode());
        softly.assertThat(enrichedPolicyOutputEvent.getCellCmData()).isEqualTo(copy.getCellCmData());
        softly.assertThat(enrichedPolicyOutputEvent.getProfiles()).isEqualTo(copy.getProfiles());
        softly.assertThat(enrichedPolicyOutputEvent.getCellKpis()).isEqualTo(copy.getCellKpis());
        softly.assertThat(enrichedPolicyOutputEvent.getDuplicateCarrierCellToFrequency()).isEqualTo(copy.getDuplicateCarrierCellToFrequency());
    }
}