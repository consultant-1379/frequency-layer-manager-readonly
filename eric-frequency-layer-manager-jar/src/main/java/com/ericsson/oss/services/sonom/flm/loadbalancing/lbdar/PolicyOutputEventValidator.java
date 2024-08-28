/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
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

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class validates the given PolicyOutputEvent.
 */
public final class PolicyOutputEventValidator {

    private static final String EXCLUSION_REASON_MESSAGE_FOR_SECTOR = "No optimization is possible for sector because";

    /**
     * This method determines if the data in the {@link PolicyOutputEvent} is valid.
     * 
     * @param policyOutputEvent
     *            an object of {@link PolicyOutputEvent}
     * @throws LbdarUnexpectedException
     *             thrown if the {@link PolicyOutputEvent} is invalid
     */
    public void isValid(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        loadBalancingQuantaIsValid(policyOutputEvent);
        sourceCellIsValid(policyOutputEvent);
        targetCellsArePresent(policyOutputEvent);
        targetCellsAreValid(policyOutputEvent);
        usersToMoveNumbersAreInNumberFormat(policyOutputEvent);
        usersToMoveNumbersAreConsistent(policyOutputEvent);
    }

    private void usersToMoveNumbersAreInNumberFormat(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        if (!NumberUtils.isCreatable(policyOutputEvent.getLoadBalancingQuanta().getSourceUsersMove()) ||
                policyOutputEvent.getLoadBalancingQuanta().getTargetCells().stream()
                        .anyMatch(c -> !NumberUtils.isCreatable(c.getTargetUsersMove()))) {
            throw new LbdarUnexpectedException(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " users to move numbers has non-numeric content.");
        }
    }

    private void loadBalancingQuantaIsValid(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        if (policyOutputEvent.getLoadBalancingQuanta() == null) {
            throw new LbdarUnexpectedException(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " LoadBalancingQuanta is missing");
        }
    }

    private void sourceCellIsValid(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        final String sourceCellFdn = policyOutputEvent.getLoadBalancingQuanta().getSourceCellFdn();
        final int sourceCellOssId = policyOutputEvent.getLoadBalancingQuanta().getSourceCellOssId();
        if (sourceCellFdn.isEmpty() || sourceCellOssId == -1) {
            throw new LbdarUnexpectedException(
                    String.format(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " source cell is missing or incomplete sourceCellFdn=%s, sourceCellOssId=%d",
                            policyOutputEvent.getLoadBalancingQuanta().getSourceCellFdn(),
                            policyOutputEvent.getLoadBalancingQuanta().getSourceCellOssId()));
        }
    }

    private void targetCellsArePresent(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        if (policyOutputEvent.getLoadBalancingQuanta().getTargetCells() == null ||
                policyOutputEvent.getLoadBalancingQuanta().getTargetCells().isEmpty()) {
            throw new LbdarUnexpectedException(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " target cells are missing");
        }
    }

    private void targetCellsAreValid(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        if (!areTargetCellsValid(policyOutputEvent.getLoadBalancingQuanta().getTargetCells())) {
            throw new LbdarUnexpectedException(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " target cells are invalid");
        }
    }

    private void usersToMoveNumbersAreConsistent(final PolicyOutputEvent policyOutputEvent) throws LbdarUnexpectedException {
        if (!areUsersToMoveNumbersConsistent(policyOutputEvent.getLoadBalancingQuanta())) {
            throw new LbdarUnexpectedException(EXCLUSION_REASON_MESSAGE_FOR_SECTOR + " users-to-move numbers are inconsistent in LBQ");
        }
    }

    private boolean areTargetCellsValid(final List<TargetCell> targetCells) {
        return targetCells.stream()
                .noneMatch(t -> t.getTargetCellFdn().isEmpty() ||
                        t.getTargetCellOssId() == -1 ||
                        t.getTargetUsersMove().isEmpty());
    }

    private boolean areUsersToMoveNumbersConsistent(final ProposedLoadBalancingQuanta loadBalancingQuanta) {
        final int totalUsersToMove = Integer.parseInt(loadBalancingQuanta.getSourceUsersMove());
        return totalUsersToMove > 0 && loadBalancingQuanta.getTargetCells().stream()
                .map(TargetCell::getTargetUsersMove)
                .mapToInt(Integer::parseInt)
                .sum() == totalUsersToMove;
    }

}
