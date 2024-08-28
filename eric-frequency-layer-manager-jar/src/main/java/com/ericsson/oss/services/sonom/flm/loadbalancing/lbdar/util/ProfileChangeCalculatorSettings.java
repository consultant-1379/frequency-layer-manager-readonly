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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.formatMessage;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_THIRD_CELL;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_OVERRIDE_C_CALCULATOR;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This class is used to support the ProfileChange Calculator in handling the custom global settings values.
 */
public class ProfileChangeCalculatorSettings {

    public static final String YES = "yes";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileChangeCalculatorSettings.class);

    private final float targetPushBack;
    private final String overrideCValue;
    private final float minLbdarStepSize;
    private final float existingHighPush;
    private final float leakageThirdCell;
    private final float leakageLbqImpact;
    private final Map<Integer, Float> bandWidthLimits;

    public ProfileChangeCalculatorSettings(final Map<String, String> customizedGlobalSettings) {
        targetPushBack = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_TARGET_PUSH_BACK));
        overrideCValue = customizedGlobalSettings.get(THRESHOLD_OVERRIDE_C_CALCULATOR);
        minLbdarStepSize = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_MINIMUM_LBDAR_STEPSIZE));
        existingHighPush = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_EXISTING_HIGH_PUSH));
        leakageThirdCell = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_LEAKAGE_THIRD_CELL));
        leakageLbqImpact = Float.parseFloat(customizedGlobalSettings.get(THRESHOLD_LEAKAGE_LBQ_IMPACT));
        bandWidthLimits = fillUpBandWidths(JsonParser.parseString(customizedGlobalSettings.get(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE)).getAsJsonArray());
    }

    private Map<Integer, Float> fillUpBandWidths(final JsonArray asJsonArray) {
        final Map<Integer, Float> bandWidths = new HashMap<>();

        for (final JsonElement bandWidthSetting : asJsonArray) {
            final Integer bw = bandWidthSetting.getAsJsonObject().get("BW").getAsInt();
            final Float value = bandWidthSetting.getAsJsonObject().get("value").getAsFloat();
            bandWidths.put(bw, value);
        }

        return bandWidths;
    }

    /**
     * Calculate the MaxLbdarStepSize value based on the bandwidth of the Cell.
     *
     * @param bandwidth
     *            the value of the bandwidth
     * @return returns a the MaxLbdarStepSize value or empty optional if none found for the bandwidth
     */
    public Optional<Float> getMaxLbdarStepSize(final int bandwidth) {
        return Optional.ofNullable(bandWidthLimits.get(bandwidth));
    }

    public float getTargetPushBack() {
        return targetPushBack;
    }

    public float getMinLbdarStepSize() {
        return minLbdarStepSize;
    }

    public float getExistingHighPush() {
        return existingHighPush;
    }

    public float getLeakageThirdCell() {
        return leakageThirdCell;
    }

    public float getLeakageLbqImpact() {
        return leakageLbqImpact;
    }

    public boolean cValueIsOverrode() {
        return YES.equalsIgnoreCase(overrideCValue);
    }

    /**
     * Checks the need of leakage resolution based on the size of leakage and the thresholds.
     *
     * @param targetUsersFromLbq
     *            user increase from source cell to target cell based on lbq.
     * @param targetUsers
     *            user numbers at target cell (original + increase from lbq).
     * @param thirdPartyUsers
     *            user numbers at 3rd cell.
     * @param newTargetUsers
     *            user numbers at target cell after the leakage.
     * @param executionId
     *            execution ID for given enrichedPolicyOutputEvent.
     * @param sectorId
     *            sector ID for given enrichedPolicyOutputEvent.
     * @return true when the leakage to 3rd cell is significant and should be resolved.
     */
    public boolean leakageThresholdsAreBreached(final long targetUsersFromLbq, final long targetUsers,
            final long thirdPartyUsers, final long newTargetUsers, final String executionId, final Long sectorId) {
        boolean leakageThirdCellThreshHoldBreached = false;
        //This checks the increase of 3rd cell
        if ((float) (targetUsers - newTargetUsers) / (float) thirdPartyUsers > getLeakageThirdCell() / 100) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(formatMessage(executionId, sectorId, "Leakage resolution invoked due to impact on leakage Cell"));
            }
            leakageThirdCellThreshHoldBreached = true;
        }
        boolean leakageLbqImpactThresholdBreached = false;
        if ((float) (targetUsers - newTargetUsers) / (float) targetUsersFromLbq > getLeakageLbqImpact() / 100) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(formatMessage(executionId, sectorId, "Leakage resolution invoked due to LBQ impact"));
            }
            leakageLbqImpactThresholdBreached = true;
        }
        return leakageThirdCellThreshHoldBreached || leakageLbqImpactThresholdBreached;
    }

}
