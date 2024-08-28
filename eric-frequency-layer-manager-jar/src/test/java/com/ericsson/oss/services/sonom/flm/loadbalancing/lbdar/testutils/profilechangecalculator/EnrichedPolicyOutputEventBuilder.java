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

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;

/**
 * Test data builder class. Builds predefined {@link EnrichedPolicyOutputEvent}) objects with different sector-scenarios.
 */
public class EnrichedPolicyOutputEventBuilder {

    private EnrichedPolicyOutputEventBuilder() {
    }

    public enum SectorPrototype {
        SIMPLE,
        COMPLEX,
        INDEPENDENT_TARGETS,
        INDEPENDENT_THIRD_PARTY,
        COMPLEX_INCONSISTENT_USERS_TO_MOVE_NUMBERS,
        CELLS_WITH_SAME_CARRIER,
        SOURCE_RETAIN_TOO_LOW,
        SOURCE_RETAIN_TOO_LOW_PROFILE_CHANGE_SAME_AS_ORIGINAL,
        CELLS_WITH_MISSING_SOURCE
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has only two cells.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithTwoCells() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.SIMPLE);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells. All the cells have connection with each
     * other.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithThreeCellsFullConnection() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.COMPLEX);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells. The target cells have no connection with
     * each other.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithThreeCellsAndTargetCellsHaveNoConnection() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.INDEPENDENT_TARGETS);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells. The target cells are connected but one of
     * the target cell has no connection with source cell.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithThreeCellsOneHasNoConnectionWithSource() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.INDEPENDENT_THIRD_PARTY);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells. All the cells have connection with each
     * other but the users to move in LBQ are inconsistent.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithThreeCellsFullConnectionButInconsistentUsersToMoveNumbers() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.COMPLEX_INCONSISTENT_USERS_TO_MOVE_NUMBERS);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells and two target cells have the same carrier.
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithTwoCellsWithSameCarrier() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.CELLS_WITH_SAME_CARRIER);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has only two cells. Source cell needs a push.
     *
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithTwoCellsMissingSourceCarrier() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.CELLS_WITH_MISSING_SOURCE);
    }

    /**
     * Build a prototype of {@link EnrichedPolicyOutputEventPrototype}) where the sector has three cells and the calculation result in negative self
     * retain
     * 
     * @return an {@link EnrichedPolicyOutputEventPrototype}) with the selected sector-type.
     */
    public static EnrichedPolicyOutputEventPrototype sectorWithTwoCellsForSelfRetainTooLow() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.SOURCE_RETAIN_TOO_LOW);
    }

    public static EnrichedPolicyOutputEventPrototype sectorWithTwoCellsSelfRetainTooLowAndProfileChangeCreatedIsSameAsOriginal() {
        return new EnrichedPolicyOutputEventPrototype(SectorPrototype.SOURCE_RETAIN_TOO_LOW_PROFILE_CHANGE_SAME_AS_ORIGINAL);
    }
}
