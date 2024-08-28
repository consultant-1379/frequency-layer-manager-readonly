/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper.getTopologyFromTarget;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is used by {@link ProfileChangesCalculatorImpl} to handle the missing source values. If the source cell distribution does not have
 * percentage push value to the target cell then we include a default value of zero.
 */
public class MissingSourcePushHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingSourcePushHandler.class);

    private final ProfileChangeCalculationHelper helper;

    public MissingSourcePushHandler(final ProfileChangeCalculationHelper helper) {
        this.helper = helper;
    }

    /**
     * Replaces the missing source push values with the default value.
     *
     * @return true if source push is replace otherwise false
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public boolean replaceMissingSourcePushWithDefault() throws LbdarException {
        final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent = helper.getEnrichedPolicyOutputEvent();

        final TopologyObjectId sourceCellId = helper.getSourceCellId();

        boolean sourcePushAdded = false;
        for (final TargetCell targetCell : enrichedPolicyOutputEvent.getTargetCells()) {
            final EnrichedIdleModePrioAtRelease sourceProfile = enrichedPolicyOutputEvent.getProfileFromCellId(sourceCellId);
            final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo = helper.getSelectedDistributionInfo(sourceProfile,
                    sourceCellId);
            final TopologyObjectId targetCellId = getTopologyFromTarget(targetCell);
            final Cell targetCellCmData = enrichedPolicyOutputEvent.getCell(getTopologyFromTarget(targetCell));
            final int targetCellCarrier = targetCellCmData.getCarrier();

            if (!hasSourcePushToTarget(distributionInfo, targetCellCarrier)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                            enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                            String.format(Locale.ROOT, "Default source push value of zero added for source cell %s to target cell %s",
                                    sourceCellId.getFdn(), targetCellId.getFdn())));
                }
                final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profiles = enrichedPolicyOutputEvent.getProfiles();
                profiles.put(
                        enrichedPolicyOutputEvent.getProfileFromCellId(helper.getSourceCellId()).getTopologyObjectId(),
                        helper.updateDistributionOfProfile(sourceProfile, sourceCellId, targetCellId, 0F));
                sourcePushAdded = true;
            }
        }
        return sourcePushAdded;
    }

    private boolean hasSourcePushToTarget(final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo,
            final int targetCellCarrier) {
        final int indexOfSource = distributionInfo.getIndexOfCarrier(targetCellCarrier);
        return (indexOfSource != -1);
    }
}
