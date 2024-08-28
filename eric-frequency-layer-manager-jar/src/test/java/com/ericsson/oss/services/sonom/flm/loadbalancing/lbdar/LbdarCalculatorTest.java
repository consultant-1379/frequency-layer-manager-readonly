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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.CELL1;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.F1;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.F2;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.NODE1;
import static com.ericsson.oss.services.sonom.flm.cm.data.stores.TestConstants.P1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarReversionException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.EnrichedProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.ProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit tests for {@link LbdarCalculator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class LbdarCalculatorTest {

    private static final String OUTDOOR = "outdoor";
    private static final Cell S = new Cell(1L, 1, "sourceCellFdn", 1200, "idleModePrioAtReleaseRef1", "cgi1", 10000, OUTDOOR, "undefined");
    private static final Cell T1 = new Cell(2L, 1, "targetCellFdn1", 1300, "idleModePrioAtReleaseRef2", "cgi2", 15000, OUTDOOR, "undefined");
    private static final Cell T2 = new Cell(3L, 1, "targetCellFdn2", 1400, "idleModePrioAtReleaseRef3", "cgi3", 20000, OUTDOOR, "undefined");
    private static final Cell T3 = new Cell(4L, 1, "targetCellFdn3", 1500, "idleModePrioAtReleaseRef4", "cgi4", 10000, OUTDOOR, "undefined");
    private static final String EXECUTION_ID = "FLM_1";
    private static final int CONFIGURATION_ID = 1;
    private static final String C1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1," +
            "MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    private static final TopologyObjectId C1_ID = new TopologyObjectId(C1, 1);
    private static final IdleModePrioAtRelease PROFILE1 = new IdleModePrioAtRelease(1L, P1, 1, "1",
            Arrays.asList(0, 250, 450, 700, 900),
            Arrays.asList(new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_LOAD_THRESHOLD,
                    Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2)),
                    new IdleModePrioAtRelease.DistributionInfo(IdleModePrioAtRelease.ThresholdLevel.HIGH_LOAD_THRESHOLD,
                            Arrays.asList(90f, 10f), Arrays.asList(F1, F2))),
            Collections.emptySet());

    @Mock
    private PolicyOutputEventEnricher mockPolicyOutputEventEnricher;

    @Mock
    private ProfileChangesEnricher mockProfileChangesEnricher;

    @Mock
    private EnrichedPolicyOutputEventValidator mockPolicyOutputEventValidator;

    @Mock
    private ProfileChangesValidator mockProfileChangesValidator;

    @Mock
    private ChangeElementCalculator mockChangeElementCalculator;

    @Mock
    private ReversionElementCalculator mockReverseElementCalculator;

    @Mock
    private ProfileChangesCalculator mockProfileChangeCalculator;

    @Mock
    private CmStore mockCmStore;

    @Mock
    private CellKpiStore mockCellKpiStore;

    private LbdarCalculator objectUnderTest;

    @Test
    public void whenEmptyPolicyOutputEventList_thenEmptyChangeElementListIsReturned() throws FlmAlgorithmException {
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);
        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());
        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);

        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(Collections.emptyList());

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, never()).enrich(any());
        verify(mockProfileChangesEnricher, never()).enrich(any());
        verify(mockChangeElementCalculator, times(0)).calculateChangeElement(any());
    }

    @Test
    public void whenNullProfileChangesReturned_thenEmptyChangeElementListIsReturned() throws FlmAlgorithmException {
        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, never()).enrich(any());
        verify(mockChangeElementCalculator, times(0)).calculateChangeElement(any());
    }

    @Test
    public void whenNullChangeElementReturned_thenEmptyChangeElementListIsReturned()
            throws LbdarException, LbdarUnexpectedException, FlmAlgorithmException, SQLException {
        final EnrichedProfileChanges testEnrichedProfileChanges = new EnrichedProfileChanges(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null);
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(true);
        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockProfileChangesEnricher.enrich(any())).thenReturn(testEnrichedProfileChanges);
        when(mockChangeElementCalculator.calculateChangeElement(any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);

        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockChangeElementCalculator, times(1)).calculateChangeElement(testEnrichedProfileChanges);
    }

    @Test
    public void whenOneChangeElementIsCreated_thenOnePairOfChangeElementIsReturned()
            throws LbdarException, LbdarUnexpectedException, LbdarReversionException, FlmAlgorithmException, SQLException {
        final EnrichedProfileChanges testEnrichedProfileChanges = new EnrichedProfileChanges(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null);
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(true);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockProfileChangesEnricher.enrich(any())).thenReturn(testEnrichedProfileChanges);
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);
        when(mockChangeElementCalculator.calculateChangeElement(any())).thenReturn(new ChangeElement());
        when(mockReverseElementCalculator.calculateReversionElement(any())).thenReturn(new ChangeElement());
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setReversionElementCalculator(mockReverseElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).containsExactly(ImmutablePair.of(new ChangeElement(), new ChangeElement()));
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, times(1))
                .enrich(eq(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null)));
        verify(mockChangeElementCalculator, times(1)).calculateChangeElement(testEnrichedProfileChanges);
        verify(mockReverseElementCalculator, times(1)).calculateReversionElement(new ChangeElement());
    }

    @Test
    public void whenThreeChangeElementIsCreated_thenThreePairOfChangeElementIsReturnedInCorrectOrderWithHighestSourceUsersFirst()
            throws LbdarException, LbdarUnexpectedException, LbdarReversionException, FlmAlgorithmException, SQLException {
        final ProfileChanges profileChangesOne = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .withSectorId(1L)
                .withExecutionId(EXECUTION_ID)
                .withSourceUsersMove(100)
                .build();
        final EnrichedProfileChanges testEnrichedProfileChangesOne = new EnrichedProfileChangesBuilder(
                profileChangesOne).addNode(C1_ID, NODE1, 1).build();

        final ProfileChanges profileChangesTwo = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .withSectorId(2L)
                .withExecutionId(EXECUTION_ID)
                .withSourceUsersMove(110)
                .build();
        final EnrichedProfileChanges testEnrichedProfileChangesTwo = new EnrichedProfileChangesBuilder(
                profileChangesTwo).addNode(C1_ID, NODE1, 2).build();

        final ProfileChanges profileChangesThree = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .withSectorId(3L)
                .withExecutionId(EXECUTION_ID)
                .withSourceUsersMove(120)
                .build();
        final EnrichedProfileChanges testEnrichedProfileChangesThree = new EnrichedProfileChangesBuilder(
                profileChangesThree).addNode(C1_ID, NODE1, 3).build();

        // Profile changes
        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                profileChangesOne);

        // Change elements:
        when(mockProfileChangesEnricher.enrich(any()))
                .thenReturn(testEnrichedProfileChangesOne)
                .thenReturn(testEnrichedProfileChangesTwo)
                .thenReturn(testEnrichedProfileChangesThree);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(true);
        when(mockReverseElementCalculator.calculateReversionElement(any())).thenReturn(new ChangeElement());

        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(new ChangeElementCalculatorImpl(EXECUTION_ID, 1, true));
        objectUnderTest.setReversionElementCalculator(mockReverseElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Arrays.asList(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).getKey().getChangeId()).isEqualTo("3");
        assertThat(result.get(1).getKey().getChangeId()).isEqualTo("2");
        assertThat(result.get(2).getKey().getChangeId()).isEqualTo("1");
    }

    @Test
    public void whenEnrichedPolicyOutputEventNotValid_thenNoChangeElementIsReturned() throws FlmAlgorithmException {
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(false);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(any());
        verify(mockProfileChangesEnricher, times(0)).enrich(any());
        verify(mockChangeElementCalculator, times(0)).calculateChangeElement(any());
    }

    @Test
    public void whenProfileChangesAreNotValid_thenNoChangeElementIsReturned() throws Exception {
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(false);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockProfileChangesEnricher.enrich(any()))
                .thenReturn(new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(any());
        verify(mockProfileChangesEnricher, times(1)).enrich(any());
        verify(mockChangeElementCalculator, times(0)).calculateChangeElement(any());
    }

    private static Execution buildExecution() {
        final Execution execution = new Execution();
        execution.setId(EXECUTION_ID);
        execution.setConfigurationId(CONFIGURATION_ID);
        execution.setOpenLoop(true);
        return execution;
    }

    @Test
    public void whenPolicyOutputEnricherThrowsException_thenNoChangeElementIsReturned() throws FlmAlgorithmException {
        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenAnswer(invocation -> {
                    throw new LbdarException("Exception is thrown");
                });

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockPolicyOutputEventValidator, never()).isValid(any());
    }

    @Test
    public void whenProfileChangeCalculatorThrowsException_thenNoChangeElementIsReturned() throws Exception {
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenThrow(new LbdarException("Exception is thrown"));
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, never()).enrich(any());
    }

    @Test
    public void whenProfileChangeEnricherThrowsException_thenNoChangeElementIsReturned() throws Exception {
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockProfileChangesEnricher.enrich(any()))
                .thenAnswer(invocation -> {
                    throw new LbdarException("Exception is thrown");
                })
                .thenReturn(new EnrichedProfileChanges(
                        new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, times(1))
                .enrich(eq(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null)));
        verify(mockProfileChangesValidator, never()).validate(any());
    }

    @Test
    public void whenChangeElementCalculatorThrowsException_thenNoChangeElementIsReturned() throws Exception {
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));
        when(mockChangeElementCalculator.calculateChangeElement(any())).thenThrow(new RuntimeException("Exception is thrown"));
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(true);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()),
                        null, Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockProfileChangesEnricher.enrich(any()))
                .thenReturn(new EnrichedProfileChanges(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null));
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore, buildCustomizedGlobalSettings(),
                Collections.emptyList());

        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, times(1))
                .enrich(eq(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null)));
        verify(mockProfileChangesValidator, times(1)).validate(any());
    }

    @Test
    public void whenReverseElementCalculatorThrowsException_thenNoChangeElementIsReturned()
            throws LbdarException, LbdarUnexpectedException, LbdarReversionException, FlmAlgorithmException, SQLException {
        final EnrichedProfileChanges testEnrichedProfileChanges = new EnrichedProfileChanges(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null), null, null);
        when(mockPolicyOutputEventValidator.isValid(any(EnrichedPolicyOutputEvent.class))).thenReturn(true);
        when(mockProfileChangesValidator.validate(any(EnrichedProfileChanges.class))).thenReturn(true);

        when(mockPolicyOutputEventEnricher.enrich(any()))
                .thenReturn(new EnrichedPolicyOutputEvent(new PolicyOutputEvent(getTestLoadBalancingQuanta()), null,
                        Collections.emptyMap(), null, null, null, Collections.EMPTY_MAP));
        when(mockProfileChangesEnricher.enrich(any())).thenReturn(testEnrichedProfileChanges);
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(null);
        when(mockCmStore.getCmSectorCellStore()).thenReturn(null);
        when(mockChangeElementCalculator.calculateChangeElement(any())).thenReturn(new ChangeElement());
        when(mockReverseElementCalculator.calculateReversionElement(any())).thenThrow(new LbdarReversionException("Reversion exception thrown"));
        when(mockProfileChangeCalculator.calculateProfileChanges(any())).thenReturn(
                new ProfileChanges(EXECUTION_ID, 1L, 100, null, null));

        objectUnderTest = new LbdarCalculator(buildExecution(), mockCmStore, mockCellKpiStore,
                buildCustomizedGlobalSettings(), Collections.emptyList());
        objectUnderTest.setPolicyOutputEnricher(mockPolicyOutputEventEnricher);
        objectUnderTest.setProfileChangesEnricher(mockProfileChangesEnricher);
        objectUnderTest.setEnrichedPolicyOutputEventValidator(mockPolicyOutputEventValidator);
        objectUnderTest.setProfileChangesValidator(mockProfileChangesValidator);
        objectUnderTest.setChangeElementCalculator(mockChangeElementCalculator);
        objectUnderTest.setReversionElementCalculator(mockReverseElementCalculator);
        objectUnderTest.setProfileChangeCalculator(mockProfileChangeCalculator);

        final List<Pair<ChangeElement, ChangeElement>> result = objectUnderTest.calculateChanges(
                Collections.singletonList(new PolicyOutputEvent(getTestLoadBalancingQuanta())));

        assertThat(result).isEmpty();
        verify(mockPolicyOutputEventEnricher, times(1)).enrich(eq(new PolicyOutputEvent(getTestLoadBalancingQuanta())));
        verify(mockProfileChangesEnricher, times(1))
                .enrich(eq(new ProfileChanges(EXECUTION_ID, 1L, 100, null, null)));
        verify(mockChangeElementCalculator, times(1)).calculateChangeElement(testEnrichedProfileChanges);
        verify(mockReverseElementCalculator, times(1)).calculateReversionElement(new ChangeElement());
    }

    private static Map<String, String> buildCustomizedGlobalSettings() {
        final Map<String, String> customizedGlobalSettings = new HashMap<>();
        customizedGlobalSettings.put("targetPushBack", "2");
        customizedGlobalSettings.put("overrideCCalculator", "No");
        customizedGlobalSettings.put("minLbdarStepsize", "1");
        customizedGlobalSettings.put("maxLbdarStepsize", "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, " +
                "{\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\"," +
                " \"value\":\"30\"}]");
        customizedGlobalSettings.put("leakageThirdCell", "10");
        customizedGlobalSettings.put("leakageLbqImpact", "20");
        customizedGlobalSettings.put("existingHighPush", "30");
        return customizedGlobalSettings;
    }

    private static ProposedLoadBalancingQuanta getTestLoadBalancingQuanta() {
        final List<TargetCell> targetCells = new ArrayList<>();
        targetCells.add(new TargetCell(T1.getFdn(), T1.getOssId(), "200"));
        targetCells.add(new TargetCell(T2.getFdn(), T2.getOssId(), "300"));
        targetCells.add(new TargetCell(T3.getFdn(), T3.getOssId(), "400"));

        return new ProposedLoadBalancingQuanta(S.getFdn(), S.getOssId(), "900", targetCells);
    }
}