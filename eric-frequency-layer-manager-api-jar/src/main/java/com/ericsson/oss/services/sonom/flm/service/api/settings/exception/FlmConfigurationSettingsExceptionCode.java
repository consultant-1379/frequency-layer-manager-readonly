/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.api.settings.exception;

/**
 * <code>flm-configuration-settings</code> error codes.
 */
public enum FlmConfigurationSettingsExceptionCode {

    CONFIGURATION_SETTINGS_JSON_PARSING_ERROR(3000, "Parsing error in Json"),
    CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR(3001, "Validation error in Json "),
    CONFIGURATION_SETTINGS_UNKNOWN_GLOBAL_SETTING(3002, "Validation error in Json, Json payload contains an unknown customizedGlobalSetting"),
    CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR(3003, "Validation error in Json, invalid cron expression "),
    CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS(3004, "A configuration with this name already exists"),
    CONFIGURATION_SETTINGS_LIMIT_REACHED(3005, "Only a maximum of 10 configurations can be persisted at once in the flm service"),
    CONFIGURATION_SETTINGS_EMPTY_JSON_ERROR(3006, "Validation error in Json, Json payload is blank or empty"),
    CONFIGURATION_SETTINGS_GROUP_COUNT_ERROR(3007, "Validation error in Json, Json payload contains more than 10,000 groups "),
    CONFIGURATION_SETTINGS_GROUP_NAMES_ERROR(3008, "Validation error in Json, Json payload contains groups with identical names "),
    CONFIGURATION_SETTINGS_ID_INCONSISTENCY(3009, "Configuration Id in Json payload and in path param are different"),
    CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_CUSTOMIZED_DEFAULT_LEVEL_ERROR(
            3010, "Validation error in Json, Json payload contains customizedGlobalSettings at customizedDefaultSettings level"),
    CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_GROUP_LEVEL_ERROR(
            3011, "Validation error in Json, Json payload contains customizedGlobalSettings at customizedGroupSettings level"),
    CONFIGURATION_SETTINGS_JSON_WEEKEND_TOO_MANY_DAYS_VALIDATION_ERROR(
            3012, "Validation error in Json, only up to two days are allowed eg. Saturday,Sunday"),
    CONFIGURATION_SETTINGS_JSON_WEEKEND_REPEATED_DAY_ERROR(
            3013, "Validation error in Json, each day can occur only a single time"),
    CONFIGURATION_SETTINGS_JSON_WEEKEND_INVALID_DAY_VALIDATION_ERROR(
            3014, "Validation error in Json, invalid day used, only Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday supported"),
    CONFIGURATION_SETTINGS_JSON_WEEKEND_NOT_CONSECUTIVE_DAYS_VALIDATION_ERROR(
            3015, "Validation error in Json, only consecutive days are allowed eg Saturday,Sunday and not Monday,Wednesday"),
    CONFIGURATION_SETTINGS_ID_ALREADY_EXISTS(3016, "A configuration with this Id already exists."),
    CONFIGURATION_PA_ONE_ENABLED_KPI_VALIDATION_ERROR(3017, "Validation error in Json, if PA is enabled, at least one PA KPI must be enabled"),
    CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR(
            3018, "Validation error in Json, if PA is enabled, the PA KPI Settings' threshold values should be in valid range of "),
    CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR(
            3019, "Validation error in Json, if PA is enabled, the PA KPI Settings' values minimum step should be "),
    CONFIGURATION_SETTINGS_NAME_VALIDATION_ERROR(
            3020, "Validation error in Json, Invalid name, valid characters in name attribute are a-z, A-Z, 0-9, -, _ only."),
    CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR(
            3021, "Validation error in Json, ulPuschSinr PA KPI relevance threshold value should be an integer in the range of 1 - 25");

    private final int errorCode;
    private final String errorMessage;

    FlmConfigurationSettingsExceptionCode(final int errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
