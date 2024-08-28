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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.OUTDOOR;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.C3;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.CELL_TARGET_TWO;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.F3;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.OSS_ID;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.P3;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCellKpis;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCellKpisSelfRetainTooLow;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCellKpisSelfRetainTooLowAndProfileChangeIsSameAsOriginal;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCmData;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCmDataForDuplicateCellCarrier;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCmDataForSelfRetainTooLow;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildCmDataForSelfRetainTooLowAndProfileChangeCreatedSameAsOriginal;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildDuplicateCarrierCellToFrequency;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildEnrichedProfiles;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildEnrichedProfilesForSelfRetainTooLow;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildEnrichedProfilesMissingCarrier;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildEnrichedProfilesMissingSource;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildFrequencyToCarrier;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildFrequencyToCarrierSelfRetainTooLow;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildFrequencyToCarrierToDuplicateCellCarrier;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildKpiForSourceCellAndTargetTwo;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildPolicyOutputEvent;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.InputDataBuilder.buildPolicyOutputEventWithCustomTotalNumbers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator.EnrichedPolicyOutputEventBuilder.SectorPrototype;

/**
 * Test data builder class, represents one of the pre-defined sectors.
 */
public class EnrichedPolicyOutputEventPrototype {
    private static final FeatureState allTrueFeatureState = new FeatureState(true, true, true, true, "ACTIVATED", true);
    public static final List<Integer> SECTOR_WITH_ONE_TARGET = Collections.singletonList(50);
    public static final List<Integer> SECTOR_WITH_TWO_TARGETS = Arrays.asList(50, 30);
    public static final List<Integer> SECTOR_WITH_TWO_TARGETS_SOURCE_RETAIN_TOO_LOW = Arrays.asList(10, 4);
    protected EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;

    public EnrichedPolicyOutputEventPrototype(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent;
    }

    public EnrichedPolicyOutputEventPrototype(final SectorPrototype model) {
        initInputPackage(model);
    }

    private void initInputPackage(final SectorPrototype model) {
        switch (model) {
            case SIMPLE:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEvent(SECTOR_WITH_ONE_TARGET, true);
                break;
            case COMPLEX:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEvent(SECTOR_WITH_TWO_TARGETS, false);
                break;
            case COMPLEX_INCONSISTENT_USERS_TO_MOVE_NUMBERS:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEventWithInconsistentMoveNumbers(SECTOR_WITH_TWO_TARGETS, false);
                break;
            case INDEPENDENT_TARGETS:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEvent(SECTOR_WITH_TWO_TARGETS, false);

                enrichedPolicyOutputEvent.getProfiles().get(InputDataBuilder.PROFILE_TWO)
                        .setDistributionInfos(InputDataBuilder.buildEnrichedDistributionInfosForTargetOne(true));

                enrichedPolicyOutputEvent.getProfiles().get(InputDataBuilder.PROFILE_THREE)
                        .setDistributionInfos(InputDataBuilder.buildEnrichedDistributionInfosForTargetTwo(true));

                break;
            case INDEPENDENT_THIRD_PARTY:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEvent(SECTOR_WITH_ONE_TARGET, true);

                enrichedPolicyOutputEvent.getProfiles().get(InputDataBuilder.PROFILE_TWO)
                        .setDistributionInfos(InputDataBuilder.buildEnrichedDistributionInfosForTargetOne(false));
                enrichedPolicyOutputEvent.addCell(CELL_TARGET_TWO, new Cell(3L, OSS_ID, C3, 4500, P3, "cgi3", 5000, OUTDOOR, "undefined"));
                enrichedPolicyOutputEvent.getCellKpis().put(CELL_TARGET_TWO, buildKpiForSourceCellAndTargetTwo());
                enrichedPolicyOutputEvent.getFrequencyToCarrier().put(TopologyObjectId.of(F3, OSS_ID), 4500);
                break;
            case CELLS_WITH_SAME_CARRIER:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEventDuplicateCellCarrier(SECTOR_WITH_TWO_TARGETS);
                break;
            case SOURCE_RETAIN_TOO_LOW:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEventSelfRetainTooLow(SECTOR_WITH_TWO_TARGETS_SOURCE_RETAIN_TOO_LOW);
                break;
            case SOURCE_RETAIN_TOO_LOW_PROFILE_CHANGE_SAME_AS_ORIGINAL:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEventSelfRetainTooLowAndProfileChangeCreatedSameAsOriginal(SECTOR_WITH_ONE_TARGET,
                        true);
                break;
            case CELLS_WITH_MISSING_SOURCE:
                enrichedPolicyOutputEvent = getEnrichedPolicyOutputEventMissingSource(SECTOR_WITH_ONE_TARGET);
                break;
        }
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEvent(final List<Integer> usersToMove, final boolean onlyToTargetOne) {
        return new EnrichedPolicyOutputEvent(buildPolicyOutputEvent(usersToMove),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmData(usersToMove.size()),
                buildEnrichedProfiles(onlyToTargetOne),
                buildCellKpis(usersToMove.size()),
                buildFrequencyToCarrier(usersToMove.size()),
                Collections.EMPTY_MAP);
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEventDuplicateCellCarrier(final List<Integer> usersToMove) {
        return new EnrichedPolicyOutputEvent(buildPolicyOutputEvent(usersToMove),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmDataForDuplicateCellCarrier(usersToMove.size()),
                buildEnrichedProfilesMissingSource(),
                buildCellKpis(usersToMove.size()),
                buildFrequencyToCarrierToDuplicateCellCarrier(usersToMove.size()),
                buildDuplicateCarrierCellToFrequency(usersToMove.size()));
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEventMissingSource(final List<Integer> usersToMove) {
        return new EnrichedPolicyOutputEvent(buildPolicyOutputEvent(usersToMove),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmData(usersToMove.size()),
                buildEnrichedProfilesMissingCarrier(),
                buildCellKpis(usersToMove.size()),
                buildFrequencyToCarrier(usersToMove.size()),
                Collections.EMPTY_MAP);
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEventSelfRetainTooLow(final List<Integer> usersToMove) {
        return new EnrichedPolicyOutputEvent(buildPolicyOutputEvent(usersToMove),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmDataForSelfRetainTooLow(),
                buildEnrichedProfilesForSelfRetainTooLow(),
                buildCellKpisSelfRetainTooLow(),
                buildFrequencyToCarrierSelfRetainTooLow(),
                Collections.EMPTY_MAP);
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEventWithInconsistentMoveNumbers(final List<Integer> usersToMove,
            final boolean onlyToTargetOne) {
        return new EnrichedPolicyOutputEvent(buildPolicyOutputEventWithCustomTotalNumbers(usersToMove, 100),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmData(usersToMove.size()),
                buildEnrichedProfiles(onlyToTargetOne),
                buildCellKpis(usersToMove.size()),
                buildFrequencyToCarrier(usersToMove.size()),
                Collections.EMPTY_MAP);
    }

    private EnrichedPolicyOutputEvent getEnrichedPolicyOutputEventSelfRetainTooLowAndProfileChangeCreatedSameAsOriginal(
            final List<Integer> usersToMove, final boolean onlyToTargetOne) {

        return new EnrichedPolicyOutputEvent(buildPolicyOutputEvent(usersToMove),
                new Node(1L, InputDataBuilder.N1, 1, allTrueFeatureState, "RadioNode"),
                buildCmDataForSelfRetainTooLowAndProfileChangeCreatedSameAsOriginal(),
                buildEnrichedProfiles(onlyToTargetOne),
                buildCellKpisSelfRetainTooLowAndProfileChangeIsSameAsOriginal(usersToMove.size()),
                buildFrequencyToCarrier(usersToMove.size()),
                Collections.EMPTY_MAP);
    }

    public EnrichedPolicyOutputEventPrototypeLbq withLBQ() {
        return new EnrichedPolicyOutputEventPrototypeLbq(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEventPrototypeCellLevelInfo withCellLevelInfo() {
        return new EnrichedPolicyOutputEventPrototypeCellLevelInfo(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEventPrototypeProfile withProfile() {
        return new EnrichedPolicyOutputEventPrototypeProfile(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEvent build() {
        return enrichedPolicyOutputEvent;
    }
}