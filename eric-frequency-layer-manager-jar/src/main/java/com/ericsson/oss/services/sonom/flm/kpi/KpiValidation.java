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

package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.KPI_CALCULATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.KPI_CALCULATION_LOST;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationState;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class to handle basic KPI data validation operations.
 */
public class KpiValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiValidation.class);

    private static final String JSON_STRING_DELIMITER = "{";

    /**
     * Validate KPI State is failure state (I.e. LOST or FAILED).
     *
     * @param kpiCalculationState
     *            The current algorithm state.
     * @param calculationId
     *            The current calculation ID for logging purposes.
     * @throws FlmAlgorithmException
     *             If state is in failure state.
     */
    public void validateKpiState(final KpiCalculationState kpiCalculationState, final String calculationId) throws FlmAlgorithmException {
        final String exceptionMessage = String.format("KPI calculation failed: %s for calculation ID: %s", kpiCalculationState,
                calculationId);
        if (kpiCalculationState == KpiCalculationState.FAILED) {
            throw new FlmAlgorithmException(KPI_CALCULATION_ERROR,
                    exceptionMessage);
        } else if (kpiCalculationState == KpiCalculationState.LOST) {
            throw new FlmAlgorithmException(KPI_CALCULATION_LOST,
                    exceptionMessage);
        }
    }

    /**
     * Validates we have a calculation ID.
     *
     * @param executionId
     *            The execution ID for logging purposes.
     * @param calculationId
     *            The calculation ID to verify if it is null or not.
     * @return true if null, false otherwise.
     */
    public boolean inValidKpiCalculationId(final String executionId, final String calculationId) {
        if (calculationId == null) {
            LOGGER.info("No calculation ID was found for execution {}. The status for reference cell will not be polled", executionId);
            return true;
        }
        return false;
    }

    /**
     * Convert a {@link Map} a set of KPI definition parameters in the format param.name - value.
     *
     * @param globalSettings
     *            The settings to create KPI definition parameters from.
     * @return {@link Map} of KPI definition parameters.
     */
    public static Map<String, String> createGlobalSettingsParameters(final Map<String, String> globalSettings) {
        final Map<String, String> globalSettingsParameters = new HashMap<>();

        if (!Objects.isNull(globalSettings)) {
            for (final Map.Entry<String, String> globalSetting : globalSettings.entrySet()) {
                if (globalSetting.getValue().startsWith(JSON_STRING_DELIMITER)) {
                    handleComplexSetting(globalSettingsParameters, globalSetting.getValue());
                } else {
                    globalSettingsParameters.put(createKpiParameter(globalSetting.getKey()), globalSetting.getValue());
                }
            }
        }

        return globalSettingsParameters;
    }

    private static String createKpiParameter(final String setting) {
        return "param." + camelToSnake(setting);
    }

    private static String camelToSnake(final String str) {
        final String acronymRegex = "([A-Z]+)([A-Z])";
        final String replacement = "$1_$2";
        final String acronymReplaced = str.replaceAll(acronymRegex, replacement);

        final String regex = "([a-z0-9])([A-Z]+)";
        return acronymReplaced.replaceAll(regex, replacement).toLowerCase(Locale.ENGLISH);
    }

    private static void handleComplexSetting(final Map<String, String> globalSettingsParameters, final String complexSettings) {
        final JsonObject jsonObject = JsonParser.parseString(complexSettings).getAsJsonObject();

        for (final String mainSettingName : jsonObject.keySet()) {
            final JsonElement subSettings = jsonObject.get(mainSettingName);
            for (final String subSettingName : subSettings.getAsJsonObject().keySet()) {
                final String subSettingValue = (subSettings.getAsJsonObject().get(subSettingName).toString());
                final String kpiSettingName = String.format("%s_%s", mainSettingName, subSettingName);
                globalSettingsParameters.put(createKpiParameter(kpiSettingName), subSettingValue.replaceAll("\"", ""));
            }
        }
    }
}
