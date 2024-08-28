/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.api.lbq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;

/**
 * Object used to store the proposed load balancing quanta.
 */
public class ProposedLoadBalancingQuanta {

    private final String sourceCellFdn;
    private final int sourceCellOssId;
    private final String sourceUsersMove;
    private List<TargetCell> targetCells;

    public ProposedLoadBalancingQuanta(final String sourceCellFdn,
            final int sourceCellOssId,
            final String sourceUsersMove,
            final List<TargetCell> targetCells) {
        this.sourceCellFdn = sourceCellFdn;
        this.sourceCellOssId = sourceCellOssId;
        this.initializeTargetCells(targetCells);
        this.sourceUsersMove = sourceUsersMove;
    }

    public ProposedLoadBalancingQuanta(final String sourceCellFdn, final int sourceCellOssId,
            final String sourceUsersMove) {
        this(sourceCellFdn, sourceCellOssId, sourceUsersMove, new ArrayList<>());
    }

    public List<TargetCell> getTargetCells() {
        return new ArrayList<>(targetCells);
    }

    public String getSourceCellFdn() {
        return sourceCellFdn;
    }

    public int getSourceCellOssId() {
        return sourceCellOssId;
    }

    public String getSourceUsersMove() {
        return sourceUsersMove;
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {sourceCellFdn: '%s', sourceCellOssId: %d, sourceUsersMove: '%s', targetCells: '%s'}",
                getClass().getSimpleName(), sourceCellFdn, sourceCellOssId, sourceUsersMove,
                targetCells.toString());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProposedLoadBalancingQuanta that = (ProposedLoadBalancingQuanta) o;
        return Objects.equals(sourceCellFdn, that.getSourceCellFdn())
                && Objects.equals(sourceCellOssId, that.getSourceCellOssId())
                && Objects.equals(sourceUsersMove, that.getSourceUsersMove())
                && Objects.equals(targetCells, that.getTargetCells());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCellFdn, sourceCellOssId, sourceUsersMove, targetCells);
    }

    private void initializeTargetCells(final List<TargetCell> targetCells) {
        if (targetCells == null) {
            this.targetCells = new ArrayList<>();
        } else {
            this.targetCells = new ArrayList<>(targetCells);
        }
    }
}
