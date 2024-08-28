/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileValidator.isValid;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * Class for missing data collecting.
 */
public class MissingDataCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingDataCollector.class);

    private final CmNodeObjectsStore profileStore;

    private final ProfileChangeCalculationHelper helper;

    public MissingDataCollector(final CmNodeObjectsStore profileStore,
                                final ProfileChangeCalculationHelper helper) {
        this.profileStore = profileStore;
        this.helper = helper;
    }

    /**
     * Collect the missing data for a 3rd cell for leakage handling.
     * @param changesCalculationInput EnrichedPolicyOutputEvent which have to be completed
     * @param thirdCell third cell for which profile is missing from the EnrichedPolicyOutputEvent.
     * @throws LbdarException when the collected new profile is invalid
     */
    public void updateInputDataWithMissingThirdProfileData(final EnrichedPolicyOutputEvent changesCalculationInput,
                                                           final Cell thirdCell) throws LbdarException {
        final TopologyObjectId profileId = TopologyObjectId.of(thirdCell.getIdleModePrioAtReleaseRef(), thirdCell.getOssId());
        if (!changesCalculationInput.getProfiles().containsKey(profileId)) {
            final IdleModePrioAtRelease profile = profileStore.getIdleModePrioAtRelease(profileId.getFdn(),
                    profileId.getOssId());
            if (profile == null) {
                logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                        String.format("Profile for leakage cell not found %s", thirdCell.getFdn()));
                throw new LbdarException(String.format("Profile for leakage cell not found %s", thirdCell.getFdn()));
            }
            if (!isValid(profile)) {
                logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                        String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                        String.format("LBDAR Distribution info and Load Threshold limits at '%s' profile " +
                                "are not configured correctly.", profile.getFdn()));
                throw new LbdarException(String.format("LBDAR Distribution info and Load Threshold limits at '%s' profile " +
                        "are not configured correctly.", profile.getFdn()));
            }
            final EnrichedIdleModePrioAtRelease enrichedProfile = new EnrichedIdleModePrioAtRelease(profile,
                    changesCalculationInput.getFrequencyFdnToCarrier());
            changesCalculationInput.getProfiles().put(profileId, enrichedProfile);
            helper.setActiveThreshold(changesCalculationInput.getCellKpis().get(thirdCell.getTopologyObjectId()), enrichedProfile,
                    thirdCell.getTopologyObjectId());
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                            changesCalculationInput.getPolicyOutputEvent().getSectorId(),
                            String.format("Collecting missing data for cell %s is done", thirdCell)));
        }
    }

}
