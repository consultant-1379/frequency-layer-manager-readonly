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

package com.ericsson.oss.services.sonom.flm.service.api.targetcell;

import java.util.Objects;

/**
 * Object used to store details about target cells that are related to source cells.
 */
public class TargetCell {
    private final String targetCellFdn;
    private final int targetCellOssId;
    private final String targetUsersMove;

    public TargetCell(final String targetCellFdn, final int targetCellOssId,
            final String targetUsersMove) {

        this.targetCellFdn = targetCellFdn;
        this.targetCellOssId = targetCellOssId;
        this.targetUsersMove = targetUsersMove;
    }

    public String getTargetUsersMove() {
        return targetUsersMove;
    }

    public String getTargetCellFdn() {
        return targetCellFdn;
    }

    public int getTargetCellOssId() {
        return targetCellOssId;
    }

    @Override
    public String toString() {
        return String.format("%s:: {targetCellFdn: '%s', targetCellOssId: %d, targetUsersMove: '%s' }", getClass().getSimpleName(),
                targetCellFdn, targetCellOssId, targetUsersMove);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TargetCell that = (TargetCell) o;
        return Objects.equals(targetCellFdn, that.getTargetCellFdn())
                && Objects.equals(targetCellOssId, that.getTargetCellOssId())
                && Objects.equals(targetUsersMove, that.getTargetUsersMove());
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetCellFdn, targetCellOssId, targetUsersMove);

    }

}
