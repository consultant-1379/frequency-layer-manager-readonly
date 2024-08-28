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

package com.ericsson.oss.services.sonom.flm.kpi.util;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.CUSTOMIZED_GLOBAL_SETTINGS_PARAMETERS_MISSING_ERROR;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.kpi.KpiValidation;
import com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;

/**
 * Utility class for constructing the additional parameters map. The key and value pairs of the map can later be used as the parameter list of an
 * on-demand KPI Calculation request.
 */
public final class AdditionalParametersUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalParametersUtils.class);
    private static final String TRANSIENT_NUM_DAYS = getEnvironmentValue("TRANSIENT_NUM_DAYS", "11");

    private AdditionalParametersUtils() {
    }

    /**
     * Method to get additional parameters for reference cell.
     * 
     * @param sectorsForRecalculation
     *            {@link Set} list of sector ids needing recalculation.
     * @return {@link Map} containing additional parameter for reference cell.
     */
    public static Map<String, String> getAdditionalParametersForReferenceCell(final Set<Long> sectorsForRecalculation) {
        final Map<String, String> additionalParameters = new HashMap<>(1);

        if (sectorsForRecalculation.isEmpty()) {
            LOGGER.info("No calculation of reference cell required.");
        } else {
            LOGGER.info("Sending calculation request for reference cells.");
            final String sectorsForRefCellCalculation = StringUtils.join(sectorsForRecalculation, ",");
            additionalParameters.put(AdditionalKpiParameters.SECTORS_WITHOUT_REF_CELL.getKey(),
                    "kpi_db://kpi_cell_sector_1440.sector_id in (" + sectorsForRefCellCalculation + ')');
        }
        return additionalParameters;
    }

    /**
     * Method to get additional parameters for signal range.
     * 
     * @param sectorsForRecalculation
     *            {@link Set} list of sector ids needing recalculation.
     * @return {@link Map} containing additional parameter for signal range.
     */
    public static Map<String, String> getAdditionalParametersForSignalRange(final Set<Long> sectorsForRecalculation) {
        final Map<String, String> additionalParameters = new HashMap<>(1);
        final String sectorsForRefCellCalculation = StringUtils.join(sectorsForRecalculation, ",");

        additionalParameters.put(AdditionalKpiParameters.SECTORS_FOR_SIGNAL_RANGE_RECALCULATION.getKey(),
                sectorsForRecalculation.isEmpty()
                        ? StringUtils.EMPTY
                        : ("OR kpi_cell_sector_1440.sector_id in (" + sectorsForRefCellCalculation + ')'));
        return additionalParameters;
    }

    /**
     * Method to get additional parameters for global settings.
     * 
     * @param customizedGlobalSettings
     *            {@link Map} containing the global settings.
     * @return {@link Map} containing additional parameter for global settings.
     * @throws FlmAlgorithmException
     *             thrown if global settings are empty.
     */
    public static Map<String, String> getAdditionalParametersForGlobalSettings(final Map<String, String> customizedGlobalSettings)
            throws FlmAlgorithmException {
        final Map<String, String> globalSettingsKpiParameters = KpiValidation.createGlobalSettingsParameters(customizedGlobalSettings);

        if (globalSettingsKpiParameters.isEmpty()) {
            LOGGER.error("Error creating Customized Global Settings Parameters - KPI calculation will not be successful");
            throw new FlmAlgorithmException(CUSTOMIZED_GLOBAL_SETTINGS_PARAMETERS_MISSING_ERROR);
        }

        return globalSettingsKpiParameters;
    }

    /**
     * Method to get additional parameters for transient.
     * 
     * @return {@link Map} containing additional parameter for transient
     */
    public static Map<String, String> getAdditionalParametersForTransient() {
        final Map<String, String> additionalParameters = new HashMap<>();
        additionalParameters.put(AdditionalKpiParameters.TRANSIENT_NUM_DAYS.getKey(), TRANSIENT_NUM_DAYS);
        return additionalParameters;
    }
}
