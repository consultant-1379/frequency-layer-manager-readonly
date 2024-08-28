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

import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus.PENDING_APPROVAL;
import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus.PROPOSED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus;
import com.ericsson.oss.services.sonom.cm.service.change.api.ProposedChange;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.SourceOfChangeCalculator;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class implement a {@link ChangeElementCalculator} that calculates a ChangeElement.
 */
public class ChangeElementCalculatorImpl implements ChangeElementCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeElementCalculatorImpl.class);
    private final String executionId;
    private final Map<TopologyObjectId, Integer> profilesUsedByNodesForExecution = new HashMap<>();
    private final String sourceOfChange;
    private final ChangeElementStatus changeElementStatus;

    public ChangeElementCalculatorImpl(final String executionId, final int configurationId, final boolean isOpenLoop) {
        this.executionId = executionId;
        changeElementStatus = isOpenLoop ? PENDING_APPROVAL : PROPOSED;
        sourceOfChange = SourceOfChangeCalculator.calculateSourceOfChange(configurationId);
    }

    /**
     * It calculates and returns a ChangeElement for the given profileChanges.
     * @param enrichedProfileChanges in instance of {@link EnrichedProfileChanges}
     * @return returns a {@link ChangeElement} or null if was not able to create the ChangeElement
     */
    @Override
    public ChangeElement calculateChangeElement(final EnrichedProfileChanges enrichedProfileChanges) {
        final long sectorId = enrichedProfileChanges.getProfileChanges().getSectorId();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId, "Calculating ChangeElement for EnrichedProfileChanges"));
        }
        final ProfileChanges profileChanges = enrichedProfileChanges.getProfileChanges();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId, String.format("EnrichedProfileChanges are validated for %d cells",
                profileChanges.getCellToIdleModePrioAtReleases().size())));
        }
        if (!profilesAreAvailable(sectorId, enrichedProfileChanges)) {
            return null;
        }
        final List<ProposedChange> proposedChanges = new ArrayList<>();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId, "Enough profiles found"));
        }
        for (final Map.Entry<TopologyObjectId, IdleModePrioAtRelease> entry: profileChanges.getCellToIdleModePrioAtReleases().entrySet()) {
            proposedChanges.addAll(buildProposedChanges(profileChanges.getCell(entry.getKey()), enrichedProfileChanges.getNodeForCell(entry.getKey()),
                    entry.getValue()));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId,
                String.format("ChangeElement created with %d profiles and %d ProposedChanges",
                profileChanges.getCellToIdleModePrioAtReleases().size(), proposedChanges.size())));
        }
        return ChangeElement.create(executionId,
                sourceOfChange,
                changeElementStatus.toString(),
                sourceOfChange,
                System.currentTimeMillis(),
                proposedChanges,
                String.valueOf(sectorId));
    }

    private boolean profilesAreAvailable(final long sectorId, final EnrichedProfileChanges profileChanges) {
        final SectorUsedProfilesAccumulator accumulator = new SectorUsedProfilesAccumulator(profileChanges.getProfilesUsedByNodes(),
                profilesUsedByNodesForExecution);

        for (final Map.Entry<TopologyObjectId, IdleModePrioAtRelease> entry:
                profileChanges.getProfileChanges().getCellToIdleModePrioAtReleases().entrySet()) {
            final TopologyObjectId cellId = entry.getKey();
            final Node node = profileChanges.getNodeForCell(cellId);
            final TopologyObjectId nodeId = node.getTopologyObjectId();

            final int newProfilesNeededForThisChange = profilesNeededForProfileChange(entry.getValue());
            if (accumulator.isAvailable(node, newProfilesNeededForThisChange)) {
                accumulator.increase(nodeId, newProfilesNeededForThisChange);
            } else {
                LoggingFormatter.logFilteredSector(executionId, String.valueOf(sectorId),
                        String.format("Not enough profiles for cell %s on node '%s'", cellId, nodeId));
                return false;
            }
        }
        return true;
    }

    private int profilesNeededForProfileChange(final IdleModePrioAtRelease profile) {
        return (profile.getReservedBy().size() == 1 ? 0 : 1);
    }

    private List<ProposedChange> buildProposedChanges(final Cell cell, final Node node, final IdleModePrioAtRelease idleModePrioAtRelease) {
        final String profileName = ProfileNameGenerator.generateProfileName(cell.getCgi(), idleModePrioAtRelease.getName());
        final int profileOssId = node.getOssId();
        final String profileFdn = node.getFdn() + "," + profileName;

        final List<ProposedChange> proposedChanges = new ArrayList<>();
        proposedChanges.add(new ProposedChange(profileOssId,
                node.getNodeType(),
                ModelConstants.IDLE_MODE_PRIO_AT_RELEASE,
                -1L,
                profileFdn,
                "CREATE",
                ParameterChangesBuilder.buildProfileParameterChanges(idleModePrioAtRelease)));
        proposedChanges.add(new ProposedChange(cell.getOssId(),
                node.getNodeType(),
                ModelConstants.CELL,
                cell.getCellId(),
                cell.getFdn(),
                "MODIFY",
                ParameterChangesBuilder.buildCellParameterChanges(profileFdn)));
        if (idleModePrioAtRelease.getReservedBy().size() == 1) {
            proposedChanges.add(new ProposedChange(profileOssId,
                    node.getNodeType(),
                    ModelConstants.IDLE_MODE_PRIO_AT_RELEASE,
                    idleModePrioAtRelease.getId(),
                    idleModePrioAtRelease.getFdn(),
                    "DELETE",
                    Collections.emptyList()));
        } else {
            profilesUsedByNodesForExecution.compute(node.getTopologyObjectId(), (oldNodeId, usedProfiles) ->
                    usedProfiles == null ? 1 : usedProfiles + 1);

        }
        return proposedChanges;
    }
}
