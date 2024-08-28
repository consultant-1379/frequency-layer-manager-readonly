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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChanges;

public class ProfileChangesBuilder {
    private final Map<TopologyObjectId, IdleModePrioAtRelease> profiles = new HashMap<>();
    private final Map<TopologyObjectId, IdleModePrioAtRelease> cellToProfiles = new HashMap<>();
    private final Map<TopologyObjectId, Cell> cells = new HashMap<>();
    private String executionId = "FLM_2_EXECUTION_ID";
    private Long sectorId = 1L;
    private Integer sourceUsersMove = 100;

    public ProfileChangesBuilder reserveCellToProfile(final Cell cell, final IdleModePrioAtRelease profile) {
        reserve(cell, profile);
        return this;
    }

    public ProfileChangesBuilder addProfileChange(final Cell cell, final IdleModePrioAtRelease profile) {
        final IdleModePrioAtRelease newProfile = reserve(cell, profile);

        final TopologyObjectId cellId = new TopologyObjectId(cell.getFdn(), cell.getOssId());
        cells.put(cellId, cell);
        cellToProfiles.put(cellId, newProfile);
        return this;
    }

    public ProfileChangesBuilder withExecutionId(final String executionId) {
        this.executionId = executionId;
        return this;
    }

    public ProfileChangesBuilder withSectorId(final Long sectorId) {
        this.sectorId = sectorId;
        return this;
    }

    public ProfileChangesBuilder withSourceUsersMove(final Integer sourceUsersMove) {
        this.sourceUsersMove = sourceUsersMove;
        return this;
    }

    public ProfileChanges build() {
        return new ProfileChanges(executionId, sectorId, sourceUsersMove, cellToProfiles, cells);
    }

    private IdleModePrioAtRelease reserve(final Cell cell, final IdleModePrioAtRelease profile) {
        final Set<String> reservedBy = new HashSet<>(profile.getReservedBy());
        final TopologyObjectId profileId = new TopologyObjectId(profile.getFdn(), profile.getOssId());
        if (profiles.containsKey(profileId)) {
            reservedBy.addAll(profiles.get(profileId).getReservedBy());
        }
        reservedBy.add(cell.getFdn());

        final IdleModePrioAtRelease newProfile = new IdleModePrioAtRelease(profile.getId(), profile.getFdn(), profile.getOssId(), profile.getName(),
                profile.getThresholds(), profile.getDistributionInfos(), reservedBy);
        profiles.put(profileId, newProfile);
        cellToProfiles.entrySet().stream()
                .filter(entry -> entry.getValue().getFdn().equals(profile.getFdn()) && entry.getValue().getOssId() == profile.getOssId())
                .forEach(entry -> entry.setValue(newProfile));
        return newProfile;
    }
}
