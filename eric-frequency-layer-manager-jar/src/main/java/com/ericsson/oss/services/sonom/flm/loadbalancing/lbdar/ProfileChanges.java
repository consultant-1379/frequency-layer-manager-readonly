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

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;

/**
 * This class is a result of ProfileChangesCalculator. It contains a list of {@link Cell}s and the new IdleModePrioAtReleases that needs to be
 * activated at ENM. The sourceUsedMove data is needed for sorting ProfileChanges, while cellCMData is used at ChangeElementCalculator
 */
public class ProfileChanges {
    private final String executionId;
    private final Long sectorId;
    private final Integer sourceUsersMove;
    private final Map<TopologyObjectId, IdleModePrioAtRelease> cellToProfiles;
    private final Map<TopologyObjectId, Cell> cellCmData;

    public ProfileChanges(final String executionId, final Long sectorId, final Integer sourceUsersMove,
                          final Map<TopologyObjectId, IdleModePrioAtRelease> cellToProfiles,
                          final Map<TopologyObjectId, Cell> cellCmData) {
        this.executionId = executionId;
        this.sectorId = sectorId;
        this.sourceUsersMove = sourceUsersMove;
        this.cellToProfiles = cellToProfiles;
        this.cellCmData = cellCmData;
    }

    public Integer getSourceUsersMove() {
        return sourceUsersMove;
    }

    public Set<TopologyObjectId> getCells() {
        return cellCmData.keySet();
    }

    public Cell getCell(final TopologyObjectId cellId) {
        return cellCmData.getOrDefault(cellId, null);
    }

    public Map<TopologyObjectId, IdleModePrioAtRelease> getCellToIdleModePrioAtReleases() {
        return cellToProfiles;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProfileChanges that = (ProfileChanges) o;
        return Objects.equals(executionId, that.executionId) &&
               Objects.equals(sectorId, that.sectorId) &&
               Objects.equals(sourceUsersMove, that.sourceUsersMove) &&
               Objects.equals(cellToProfiles, that.cellToProfiles) &&
               Objects.equals(cellCmData, that.cellCmData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, sectorId, cellToProfiles, cellCmData, sourceUsersMove);
    }

    public Long getSectorId() {
        return sectorId;
    }

    public String getExecutionId() {
        return executionId;
    }
}
