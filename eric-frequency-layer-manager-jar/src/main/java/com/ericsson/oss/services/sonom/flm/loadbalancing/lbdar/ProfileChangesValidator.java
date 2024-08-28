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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is used to validate {@link EnrichedProfileChanges} if it contains all the data needed to calculate ChangeElements.
 */
public class ProfileChangesValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileChangesValidator.class);

    /**
     * This method validates an {@link EnrichedProfileChanges} object.
     * @param enrichedProfileChanges an {@link EnrichedProfileChanges} object
     * @return true if all the data is available for ChangeElementCalculation, false otherwise
     */
    public boolean validate(final EnrichedProfileChanges enrichedProfileChanges) {
        LOGGER.info("Starting to validate Profile Changes");
        if (enrichedProfileChanges == null) {
            LOGGER.warn("ProfileChanges are not validated because it is missing");
            return false;
        }
        if (!validateProfileChanges(enrichedProfileChanges)) {
            return false;
        }
        if (!validateCellToNodes(enrichedProfileChanges)) {
            return false;
        }
        return validateNodeFeature(enrichedProfileChanges);
    }

    private boolean validateProfileChanges(final EnrichedProfileChanges enrichedProfileChanges) {
        if (enrichedProfileChanges.getProfileChanges() == null) {
            LOGGER.warn("ProfileChanges are not validated because it is missing");
            return false;
        }
        final String executionId = enrichedProfileChanges.getProfileChanges().getExecutionId();
        final long sectorId = enrichedProfileChanges.getProfileChanges().getSectorId();
        if (enrichedProfileChanges.getProfileChanges().getCellToIdleModePrioAtReleases() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, sectorId,
                 "ProfileChanges are not validated because the profiles map is null"));
            }
            return false;
        }
        if (enrichedProfileChanges.getProfileChanges().getCellToIdleModePrioAtReleases().isEmpty()) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, sectorId,
                 "ProfileChanges are not validated because the profile map is empty"));
            }
            return false;
        }
        if (enrichedProfileChanges.getProfileChanges().getSourceUsersMove() <= 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, sectorId,
                    String.format("ProfileChanges are not validated because the source users move is <= 0, sourceUsersMove=%d",
                        enrichedProfileChanges.getProfileChanges().getSourceUsersMove())));
            }
            return false;
        }
        return true;
    }

    private boolean validateCellToNodes(final EnrichedProfileChanges enrichedProfileChanges) {
        return enrichedProfileChanges.getProfileChanges().getCellToIdleModePrioAtReleases().keySet().stream()
                .allMatch(cellId -> {
                    if (enrichedProfileChanges.getNodeForCell(cellId) == null) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn(LoggingFormatter.formatMessage(enrichedProfileChanges.getProfileChanges().getExecutionId(),
                                enrichedProfileChanges.getProfileChanges().getSectorId(),
                                String.format("ProfileChanges are not validated because the node was not found for cell %s", cellId)));
                        }
                        return false;
                    }
                    return true;
                });
    }

    private boolean validateNodeFeature(final EnrichedProfileChanges enrichedProfileChanges) {
        return enrichedProfileChanges.getProfileChanges().getCellToIdleModePrioAtReleases().keySet().stream()
                .allMatch(cellId -> {
                    if (!enrichedProfileChanges.getNodeForCell(cellId).getFeatureState().isLoadBasedDistributionAtReleaseActivated()) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn(LoggingFormatter.formatMessage(enrichedProfileChanges.getProfileChanges().getExecutionId(),
                                enrichedProfileChanges.getProfileChanges().getSectorId(),
                                String.format("ProfileChanges are not validated because the LBDAR is not active on node %s",
                                enrichedProfileChanges.getNodeForCell(cellId).getFdn())));
                        }
                        return false;
                    }
                    return true;
                });
    }
}
