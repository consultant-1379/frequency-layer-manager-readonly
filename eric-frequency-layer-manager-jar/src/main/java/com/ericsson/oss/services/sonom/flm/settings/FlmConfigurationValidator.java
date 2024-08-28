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
package com.ericsson.oss.services.sonom.flm.settings;

import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_PA_ONE_ENABLED_KPI_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_EMPTY_JSON_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_CUSTOMIZED_DEFAULT_LEVEL_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_GROUP_LEVEL_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_GROUP_COUNT_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_GROUP_NAMES_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_ID_INCONSISTENCY;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_PARSING_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_WEEKEND_INVALID_DAY_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_WEEKEND_NOT_CONSECUTIVE_DAYS_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_WEEKEND_REPEATED_DAY_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_JSON_WEEKEND_TOO_MANY_DAYS_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_NAME_VALIDATION_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_UNKNOWN_GLOBAL_SETTING;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.ENABLE_ESS_SETTING_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MINIMUM_SOURCE_RETAINED_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_CONNECTED_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CQI_SAMPLES_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.OPTIMIZATION_SPEED_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PA_KPI_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_THROUGHPUT_R_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_THIRD_CELL;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MAXIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_OVERRIDE_C_CALCULATOR;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME;
import static com.github.fge.jackson.JsonLoader.fromString;

import java.io.IOException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * Class for validating an FLM {@link Configuration} json String before persisting.
 */
public final class FlmConfigurationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmConfigurationValidator.class);
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String ENABLED_ATTRIBUTE_NAME = "enabled";
    private static final String OPEN_LOOP_ATTRIBUTE_NAME = "openLoop";
    private static final String SCHEDULE_ATTRIBUTE_NAME = "schedule";
    private static final String THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW = "BW";
    private static final String THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE = "value";
    private static final String UL_PUSCH_SINR_PA_KPI_SETTINGS_NAME = "UL_PUSCH_SINR";

    private static final List<String> GLOBAL_SETTING_NAMES = Arrays.asList(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME,
            PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, THRESHOLD_TARGET_PUSH_BACK,
            THRESHOLD_OVERRIDE_C_CALCULATOR, THRESHOLD_MINIMUM_LBDAR_STEPSIZE, THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, THRESHOLD_LEAKAGE_THIRD_CELL,
            THRESHOLD_LEAKAGE_LBQ_IMPACT, THRESHOLD_EXISTING_HIGH_PUSH, NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, PA_KPI_SETTINGS);
    private static final List<Double> VALID_VALUES_FOR_QOS_FOR_CAPACITY_ESTIMATION = Arrays.asList(0.0D, 0.1D, 0.2D, 0.3D, 0.4D, 0.5D, 0.6D, 0.7D,
            0.8D, 0.9D, 1D);
    private static final Range<Integer> MIN_NUM_CELL_FOR_CDF_CALCULATION_RANGE = Range.between(0, 1000);
    private static final Range<Integer> MINIMUM_SOURCE_RETAINED_RANGE = Range.between(0, 100);
    private static final Range<Double> THRESHOLD_TARGET_PUSH_BACK_RANGE = Range.between(0.0D, 20.0D);
    private static final List<String> VALID_LOWERCASE_VALUES_FOR_THRESHOLD_OVERRIDE_C_CALCULATOR = Arrays.asList("yes", "no");

    private static final Range<Double> THRESHOLD_MINIMUM_LBDAR_STEPSIZE_RANGE = Range.between(0.0D, 5.0D);
    private static final List<Integer> VALID_VALUES_FOR_MAXIMUM_LBDAR_STEPSIZE_BANDWIDTHS = Arrays.asList(1400, 3000, 5000, 10000, 15000, 20000);
    private static final Range<Integer> MIN_ROPS_FOR_APP_COV_RELIABILITY_RANGE = Range.between(1, 4);
    private static final Range<Integer> MIN_NUM_CQI_SAMPLES_RANGE = Range.between(0, 1_000_000);
    private static final Range<Integer> MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_RANGE = Range.between(0, 30);
    private static final Range<Integer> SIGMA_FOR_TRANSIENT_CALCULATION_RANGE = Range.between(1, 10);
    private static final List<String> VALID_VALUES_FOR_BOOLEAN = Arrays.asList("true", "false");
    private static final Range<Integer> NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_RANGE = Range.between(1, 6);
    private static final Range<Double> UPLINK_PUSCH_SINR_RATIO_THRESHOLD_RANGE = Range.between(0.1D, 2.0D);
    private static final Range<Integer> MIN_TARGET_UPLINK_PUSCH_SINR_RANGE = Range.between(0, 20);
    private static final Range<Double> PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_RANGE = Range.between(0.0D, 10.0D);
    private static final Range<Integer> MIN_CONNECTED_USERS_RANGE = Range.between(0, 200);

    private static final String GROUPS_ARRAY_NAME = "groups";
    private static final String INCLUSION_LIST_ATTRIBUTE_NAME = "inclusionList";
    private static final String EXCLUSION_LIST_ATTRIBUTE_NAME = "exclusionList";
    private static final String WEEKEND_DAYS_ATTRIBUTE_NAME = "weekendDays";
    private static final String WHITESPACE_REGEX = "\\s+";
    private static final int MAXIMUM_NUMBER_OF_GROUPS = 10_000;
    private static final int MAXIMUM_NUMBER_OF_WEEKEND_DAYS = 2;
    private static final String CUSTOMIZED_GLOBAL_SETTINGS_NAME = "customizedGlobalSettings";
    private static final String CUSTOMIZED_DEFAULT_SETTINGS_NAME = "customizedDefaultSettings";
    private static final String CUSTOMIZED_GROUP_SETTINGS_NAME = "customizedGroupSettings";
    private static final String CONFIGURATION_SCHEMA_RESOURCE = "flmConfigurationSchema.json";
    private static final String OPTIMIZATION_SPEED_SLOW = "slow";
    private static final String OPTIMIZATION_SPEED_NORMAL = "normal";
    private static final String OPTIMIZATION_SPEED_FAST = "fast";
    private static final String ENABLED_PA = "enablePA";

    private static final Range<Double> TARGET_THROUGHOUT_R_RANGE = Range.between(0.0D, 2000.0D);
    private static final Range<Double> DELTA_GOAL_FUNCTION_THRESHOLD_RANGE = Range.between(0.0D, 1.0D);
    private static final Range<Double> ZERO_TO_TWO_DOUBLE_RANGE = Range.between(0.0D, 2.0D);
    private static final Range<Double> FLOAT_PERCENTILE_RANGE = Range.between(0.0D, 100.0D);
    private static final Map<String, Range<Double>> CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP = new HashMap<>();
    private static final Map<String, Range<Integer>> CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP = new HashMap<>();

    private static final String EMPTY_JSON = "{}";
    private static final Gson GSON = new Gson();

    static {
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(TARGET_THROUGHPUT_R_ATTR_NAME, TARGET_THROUGHOUT_R_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, DELTA_GOAL_FUNCTION_THRESHOLD_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME, ZERO_TO_TWO_DOUBLE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME, FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME, ZERO_TO_TWO_DOUBLE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME,
                FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME,
                FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME, FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME,
                FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME, FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME, FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME, MIN_ROPS_FOR_APP_COV_RELIABILITY_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MINIMUM_SOURCE_RETAINED_NAME, MINIMUM_SOURCE_RETAINED_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MIN_NUM_CQI_SAMPLES_ATTR_NAME, MIN_NUM_CQI_SAMPLES_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME,
                MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, SIGMA_FOR_TRANSIENT_CALCULATION_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, MIN_TARGET_UPLINK_PUSCH_SINR_RANGE);
        CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.put(MIN_CONNECTED_USERS_ATTR_NAME, MIN_CONNECTED_USERS_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME, FLOAT_PERCENTILE_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, UPLINK_PUSCH_SINR_RATIO_THRESHOLD_RANGE);
        CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.put(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME, PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_RANGE);
    }

    private FlmConfigurationValidator() {
        // Intentionally private.
    }

    /**
     * Given a JSON string of settings, will verify against the FLM settings schema in resources.
     *
     * @param settings
     *            the JSON string of settings to verify
     * @return whether the JSON passed schema verification or not
     * @throws ConfigurationSettingsJsonValidationException
     *             thrown if there is JSON parsing error
     */
    public static boolean validateFlmConfiguration(final String settings) throws ConfigurationSettingsJsonValidationException {
        try {
            final String jsonSchemaString = ResourceLoaderUtils.getClasspathResourceAsString(CONFIGURATION_SCHEMA_RESOURCE);
            return validate(settings, jsonSchemaString);
        } catch (final IOException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to validate JSON against JSON schema", e);
            return false;
        }
    }

    /**
     * Validate JSON data against a JSON schema.
     *
     * @param jsonData
     *            JSON data
     * @param jsonSchema
     *            JSON schema
     * @return boolean validation result
     * @throws ConfigurationSettingsJsonValidationException
     *             thrown if there is JSON parsing error
     */
    private static boolean validate(final String jsonData, final String jsonSchema) throws ConfigurationSettingsJsonValidationException {
        if (jsonData == null || jsonSchema == null) {
            return false;
        }

        if (StringUtils.isBlank(jsonData)) {
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_EMPTY_JSON_ERROR, "json payload is blank");
        }

        if (EMPTY_JSON.equals(jsonData.replace(WHITESPACE_REGEX, ""))) {
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_EMPTY_JSON_ERROR, "json payload is empty");
        }

        validateSchema(jsonData, jsonSchema);
        return true;
    }

    /**
     * Validate configuration Id from Path param vs Json payload.
     *
     * @param id
     *            id of configuration from Json payload
     * @param configurationId
     *            id of configuration from Path param
     * @throws ConfigurationSettingsJsonValidationException
     *             thrown if the id from json configuration is null or does not match with id from path param
     */
    public static void validateConfigurationId(final Integer id, final Integer configurationId)
            throws ConfigurationSettingsJsonValidationException {
        if (id == null) {
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_ID_INCONSISTENCY, "Configuration id can not be null!");
        } else if (!id.equals(configurationId)) {
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_ID_INCONSISTENCY, "Different configuration ids");
        }
    }

    private static void validateSchema(final String jsonData, final String jsonSchema) throws ConfigurationSettingsJsonValidationException {
        //parse Json
        final JsonObject rootJson;
        try {
            rootJson = JsonParser.parseString(jsonData).getAsJsonObject();
        } catch (final JsonSyntaxException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error parsing configuration %n\"{}\"", jsonData, e);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_PARSING_ERROR,
                    String.format("Invalid json - %n%s", e.getMessage()), e);
        }
        final ProcessingReport report = extractReportAndValidate(jsonData, jsonSchema);

        if (report.isSuccess()) {
            LOGGER.debug("Json validated successfully against schema {}", jsonData);
            LOGGER.debug("Validating restrictions not specified by the schema");
            //Schema validation does not cover all eventualities
            validateMandatoryAttributes(rootJson);
            validateOptionalAttributes(rootJson);
        } else {
            final List<String> errorsInJson = new ArrayList<>();
            report.forEach(processingMessage -> errorsInJson
                    .add(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR.getErrorMessage()
                            + processingMessage.getMessage().replaceAll("[]\\[\"]+", "")));

            throw new ConfigurationSettingsJsonValidationException(
                    CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR, errorsInJson,
                    new IllegalArgumentException());
        }
    }

    private static void validateMandatoryAttributes(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        final List<String> mandatoryAttributes = Arrays.asList(NAME_ATTRIBUTE_NAME, ENABLED_ATTRIBUTE_NAME, SCHEDULE_ATTRIBUTE_NAME,
                OPEN_LOOP_ATTRIBUTE_NAME);
        for (final String attribute : mandatoryAttributes) {
            if (!rootJson.has(attribute)) {
                LOGGER.warn("Missing mandatory \"{}\" value from json %n\"{}\"", attribute, rootJson);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR, String.format("Missing mandatory: \"%s\" attribute", attribute));
            }
            if (NAME_ATTRIBUTE_NAME.equals(attribute)) {
                validateName(rootJson.get(NAME_ATTRIBUTE_NAME).getAsString());
            }
            if (SCHEDULE_ATTRIBUTE_NAME.equals(attribute)) {
                validateCronExpression(rootJson.get(SCHEDULE_ATTRIBUTE_NAME).getAsString());
            }
        }
    }

    private static void validateOptionalAttributes(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        validateCustomizedGlobalSettings(rootJson);
        validateCustomizedDefaultSettings(rootJson, CUSTOMIZED_DEFAULT_SETTINGS_NAME);
        validateGroups(rootJson);
        validateCustomizedGlobalSettingOverrides(rootJson);
        validateInclusionList(rootJson);
        validateExclusionList(rootJson);
        validateWeekendDays(rootJson);
    }

    private static void validateCustomizedGlobalSettings(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(CUSTOMIZED_GLOBAL_SETTINGS_NAME)) {
            final JsonObject globalSettings = rootJson.getAsJsonObject(CUSTOMIZED_GLOBAL_SETTINGS_NAME);
            validateOnlyGlobalSettingsArePresent(globalSettings);
            validateIntegerValuesInCustomGlobalSettings(globalSettings);
            validateDoubleValuesInCustomGlobalSettings(globalSettings);
            validateUniqueValuesInCustomGlobalSettings(globalSettings);
            validatePaKpiSettings(globalSettings, rootJson.getAsJsonPrimitive(ENABLED_PA).getAsBoolean());
        }
    }

    private static void validateOnlyGlobalSettingsArePresent(final JsonObject globalSettings) throws ConfigurationSettingsJsonValidationException {
        final Set<String> proposedGlobalSettings = globalSettings.keySet();
        for (final String proposedGlobalSetting : proposedGlobalSettings) {
            if (!GLOBAL_SETTING_NAMES.contains(proposedGlobalSetting)) {
                LOGGER.warn("Unknown customizedGlobalSetting ({}) found in configuration", proposedGlobalSetting);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_UNKNOWN_GLOBAL_SETTING,
                        String.format("Validation error in Json, unknown customizedGlobalSetting (%s) found in configuration",
                                proposedGlobalSetting));
            }
        }
    }

    private static void validateIntegerValuesInCustomGlobalSettings(final JsonObject globalSettings)
            throws ConfigurationSettingsJsonValidationException {
        if (globalSettings.has(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME)) {
            validateIntegerValueInRange(globalSettings, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, MIN_NUM_CELL_FOR_CDF_CALCULATION_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }

        if (globalSettings.has(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME)) {
            validateIntegerValueInRange(globalSettings, NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME,
                    NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
    }

    private static void validateDoubleValuesInCustomGlobalSettings(final JsonObject globalSettings)
            throws ConfigurationSettingsJsonValidationException {
        if (globalSettings.has(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME)) {
            validateFloatOrDoubleValueInRange(globalSettings, PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, FLOAT_PERCENTILE_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
        if (globalSettings.has(THRESHOLD_TARGET_PUSH_BACK)) {
            validateFloatOrDoubleValueInRange(globalSettings, THRESHOLD_TARGET_PUSH_BACK, THRESHOLD_TARGET_PUSH_BACK_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
        if (globalSettings.has(THRESHOLD_LEAKAGE_THIRD_CELL)) {
            validateFloatOrDoubleValueInRange(globalSettings, THRESHOLD_LEAKAGE_THIRD_CELL, FLOAT_PERCENTILE_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
        if (globalSettings.has(THRESHOLD_LEAKAGE_LBQ_IMPACT)) {
            validateFloatOrDoubleValueInRange(globalSettings, THRESHOLD_LEAKAGE_LBQ_IMPACT, FLOAT_PERCENTILE_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
        if (globalSettings.has(THRESHOLD_EXISTING_HIGH_PUSH)) {
            validateFloatOrDoubleValueInRange(globalSettings, THRESHOLD_EXISTING_HIGH_PUSH, FLOAT_PERCENTILE_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
        if (globalSettings.has(THRESHOLD_MINIMUM_LBDAR_STEPSIZE)) {
            validateFloatOrDoubleValueInRange(globalSettings, THRESHOLD_MINIMUM_LBDAR_STEPSIZE, THRESHOLD_MINIMUM_LBDAR_STEPSIZE_RANGE,
                    CUSTOMIZED_GLOBAL_SETTINGS_NAME);
        }
    }

    private static void validateUniqueValuesInCustomGlobalSettings(final JsonObject globalSettings)
            throws ConfigurationSettingsJsonValidationException {
        if (globalSettings.has(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME)) {
            validateQosForCapacityEstimation(globalSettings);
        }
        if (globalSettings.has(THRESHOLD_OVERRIDE_C_CALCULATOR)) {
            validateOverrideCCalculator(globalSettings);
        }
        if (globalSettings.has(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE)) {
            validateMaxLbdarStepsize(globalSettings);
        }
    }

    private static void validateQosForCapacityEstimation(final JsonObject settings) throws ConfigurationSettingsJsonValidationException {
        final JsonElement qosForCapacityEstimationRaw = settings.get(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME);
        try {
            final double qosForCapacityEstimation = qosForCapacityEstimationRaw.getAsDouble();
            if (!VALID_VALUES_FOR_QOS_FOR_CAPACITY_ESTIMATION.contains(qosForCapacityEstimation)) {
                LOGGER.warn("\"{}\" ({}) value should be one of \"{}\"", QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME,
                        qosForCapacityEstimation, VALID_VALUES_FOR_QOS_FOR_CAPACITY_ESTIMATION);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("%s (%s) value should be one of - %s", QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME,
                                qosForCapacityEstimation, VALID_VALUES_FOR_QOS_FOR_CAPACITY_ESTIMATION));
            }
        } catch (final NumberFormatException e) {
            LOGGER.warn("Error parsing \"{}\" double value from customizedGlobalSettings %n\"{}\"",
                    QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, qosForCapacityEstimationRaw);
            throw new ConfigurationSettingsJsonValidationException(
                    CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s should be a number", QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME),
                    e);
        }
    }

    private static void validateMaxLbdarStepsize(final JsonObject settings) throws ConfigurationSettingsJsonValidationException {
        final JsonElement maxLbdarStepsizeRaw = settings.get(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE);
        try {
            final JsonArray maxLbdarStepsize = GSON.fromJson(maxLbdarStepsizeRaw.getAsString(), JsonArray.class);
            validateMaxLbdarStepsizeCategoryKeys(maxLbdarStepsize);
            validateMaxLbdarStepsizeCategoryRanges(maxLbdarStepsize);
            validateMaxLbdarStepsizeBWUniqueness(maxLbdarStepsize);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing \"{}\" Json array from customizedGlobalSettings \"{}\"",
                    THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, maxLbdarStepsizeRaw);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s should be an array, containing objects", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE),
                    e);
        }
    }

    private static void validateMaxLbdarStepsizeCategoryKeys(final JsonArray maxLbdarStepsize)
            throws ConfigurationSettingsJsonValidationException {
        for (int i = 0; i < maxLbdarStepsize.size(); i++) {
            final JsonObject category = maxLbdarStepsize.get(i).getAsJsonObject();
            if (!category.has(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW) || !category.has(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE)) {
                LOGGER.warn("Error parsing one category from \"{}\" \"{}\"", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, category);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("All %s categories should have %s and %s entries", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW, THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE));
            }
        }
    }

    private static void validateMaxLbdarStepsizeCategoryRanges(final JsonArray maxLbdarStepsize)
            throws ConfigurationSettingsJsonValidationException {
        for (int i = 0; i < maxLbdarStepsize.size(); i++) {
            final JsonObject category = maxLbdarStepsize.get(i).getAsJsonObject();
            final JsonElement categoryBwRaw = category.get(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW);
            final JsonElement categoryValueRaw = category.get(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE);
            try {
                final double categoryValue = categoryValueRaw.getAsDouble();
                if (!FLOAT_PERCENTILE_RANGE.contains(categoryValue)) {
                    LOGGER.warn("One \"{}\" value ({}) falls outside of range \"{}\"", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                            categoryValue, FLOAT_PERCENTILE_RANGE);
                    throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                            String.format("One %s value (%s), should be within valid range - %s",
                                    THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, categoryValue, FLOAT_PERCENTILE_RANGE));
                }
                final int categoryBw = categoryBwRaw.getAsInt();
                if (!VALID_VALUES_FOR_MAXIMUM_LBDAR_STEPSIZE_BANDWIDTHS.contains(categoryBw)) {
                    LOGGER.warn("One \"{}\" ({}) value should be one of \"{}\"", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                            categoryBw, VALID_VALUES_FOR_MAXIMUM_LBDAR_STEPSIZE_BANDWIDTHS);
                    throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                            String.format("One %s (%s) value should be one of - %s", THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                                    categoryBw, VALID_VALUES_FOR_MAXIMUM_LBDAR_STEPSIZE_BANDWIDTHS));
                }
            } catch (final NumberFormatException e) {
                LOGGER.warn("Error parsing {} and {} from \"{}\" category \"{}\"",
                        THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW, THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE,
                        THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, category);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("%s category %s and %s entries should be an int and a double, respectively",
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE,
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW,
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_VALUE),
                        e);
            }
        }
    }

    private static void validateMaxLbdarStepsizeBWUniqueness(final JsonArray maxLbdarStepsize)
            throws ConfigurationSettingsJsonValidationException {
        final List<Integer> categoryBws = new ArrayList<>();
        for (int i = 0; i < maxLbdarStepsize.size(); i++) {
            final JsonObject category = maxLbdarStepsize.get(i).getAsJsonObject();
            final JsonElement categoryBwRaw = category.get(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW);
            final int categoryBw = categoryBwRaw.getAsInt();
            if (categoryBws.contains(categoryBw)) {
                LOGGER.warn("One \"{}\" {} value ({}) is not unique",
                        THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW, categoryBw);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("One %s %s value (%s) is not unique",
                                THRESHOLD_MAXIMUM_LBDAR_STEPSIZE, THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_BW, categoryBw));
            } else {
                categoryBws.add(categoryBw);
            }
        }
    }

    private static void validateOverrideCCalculator(final JsonObject settings) throws ConfigurationSettingsJsonValidationException {
        final JsonElement overrideCCalculatorRaw = settings.get(THRESHOLD_OVERRIDE_C_CALCULATOR);
        try {
            String overrideCCalculator = overrideCCalculatorRaw.getAsString();
            overrideCCalculator = overrideCCalculator.toLowerCase(Locale.ENGLISH);
            if (!VALID_LOWERCASE_VALUES_FOR_THRESHOLD_OVERRIDE_C_CALCULATOR.contains(overrideCCalculator)) {
                LOGGER.warn("\"{}\" ({}) value should be one of \"{}\"", THRESHOLD_OVERRIDE_C_CALCULATOR,
                        overrideCCalculator, VALID_LOWERCASE_VALUES_FOR_THRESHOLD_OVERRIDE_C_CALCULATOR);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("%s (%s) value should be one of - %s", THRESHOLD_OVERRIDE_C_CALCULATOR,
                                overrideCCalculator, VALID_LOWERCASE_VALUES_FOR_THRESHOLD_OVERRIDE_C_CALCULATOR));
            }
        } catch (final UnsupportedOperationException e) {
            LOGGER.warn("Error parsing \"{}\" string value from customizedGlobalSettings %n\"{}\"",
                    THRESHOLD_OVERRIDE_C_CALCULATOR, overrideCCalculatorRaw);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s should be string", THRESHOLD_OVERRIDE_C_CALCULATOR), e);
        }
    }

    private static void validateIntegerValueInRange(final JsonObject settings, final String name, final Range<Integer> range, final String fieldName)
            throws ConfigurationSettingsJsonValidationException {
        final JsonElement valueRaw = settings.get(name);
        try {
            final int value = valueRaw.getAsInt();
            if (!range.contains(value)) {
                LOGGER.warn("\"{}\" ({}) falls outside of range \"{}\"", name, value, range);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("%s (%s), should be within valid range - %s", name, value, range));
            }
        } catch (final NumberFormatException e) {
            LOGGER.warn("Error parsing \"{}\" integer value from {} \"{}\"", name, fieldName, valueRaw);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s should be an integer", name), e);
        }
    }

    private static void validateCustomizedDefaultSettings(final JsonObject rootJson, final String settingsName)
            throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(settingsName)) {
            final JsonObject settings = rootJson.getAsJsonObject(settingsName);

            for (final String setting : settings.keySet()) {
                if (CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.containsKey(setting)) {
                    validateFloatOrDoubleValueInRange(settings, setting, CUSTOM_DEFAULT_FLOAT_SETTINGS_NAME_RANGE_MAP.get(setting),
                            settingsName);
                } else if (CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.containsKey(setting)) {
                    validateIntegerValueInRange(settings, setting, CUSTOM_DEFAULT_INTEGER_SETTINGS_NAME_RANGE_MAP.get(setting),
                            settingsName);
                } else if (OPTIMIZATION_SPEED_ATTR_NAME.equals(setting)) {
                    validateValidOptimizationSpeed(settings, setting);
                } else if (ENABLE_ESS_SETTING_ATTR_NAME.equals(setting)) {
                    validateBoolean(settings, setting);
                }
            }
        }
    }

    private static boolean checkAttributeForGlobalSettings(final JsonObject groupSettings) {
        return GLOBAL_SETTING_NAMES.stream().anyMatch(groupSettings::has);
    }

    private static List<String> getGlobalSettingNamesFromCollection(final JsonObject settings) {
        return GLOBAL_SETTING_NAMES.stream().filter(settings::has).collect(Collectors.toList());
    }

    private static void validateFloatOrDoubleValueInRange(final JsonObject settings, final String name, final Range<Double> range,
            final String settingsName)
            throws ConfigurationSettingsJsonValidationException {
        final JsonElement valueRaw = settings.get(name);
        try {
            final double value = valueRaw.getAsDouble();
            if (!range.contains(value)) {
                LOGGER.warn("\"{}\" ({}) falls outside of range \"{}\"", name, value, range);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                        String.format("%s (%s), should be within valid range - %s", name, value, range));
            }
        } catch (final NumberFormatException e) {
            LOGGER.warn("Error parsing \"{}\" float value from {} \"{}\"", name, settingsName, valueRaw);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s should be a float", name), e);
        }
    }

    private static void validateValidOptimizationSpeed(final JsonObject settings, final String name)
            throws ConfigurationSettingsJsonValidationException {
        final JsonElement valueRaw = settings.get(name);
        final String value = valueRaw.getAsString();
        if (!(OPTIMIZATION_SPEED_SLOW.equals(value) || OPTIMIZATION_SPEED_NORMAL.equals(value) || OPTIMIZATION_SPEED_FAST.equals(value))) {
            LOGGER.warn("\"{}\" ({}) value should be one of - [{}, {}, {}]", name, value,
                    OPTIMIZATION_SPEED_SLOW, OPTIMIZATION_SPEED_NORMAL, OPTIMIZATION_SPEED_FAST);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s (%s) value should be one of - [%s, %s, %s]", name, value,
                            OPTIMIZATION_SPEED_SLOW, OPTIMIZATION_SPEED_NORMAL, OPTIMIZATION_SPEED_FAST));
        }
    }

    private static void validateBoolean(final JsonObject settings, final String name)
            throws ConfigurationSettingsJsonValidationException {
        final JsonElement valueRaw = settings.get(name);
        final String value = valueRaw.getAsString();
        if (!VALID_VALUES_FOR_BOOLEAN.contains(value)) {
            LOGGER.warn("\"{}\" ({}) value should be one of \"{}\"", name,
                    value, VALID_VALUES_FOR_BOOLEAN);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                    String.format("%s (%s) value should be one of - %s", name,
                            value, VALID_VALUES_FOR_BOOLEAN));
        }
    }

    private static void validateGroups(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(GROUPS_ARRAY_NAME)) {
            final JsonArray groups = rootJson.getAsJsonArray(GROUPS_ARRAY_NAME);
            final int numberOfGroups = groups.size();
            LOGGER.info("Validating {} groups.", numberOfGroups);
            if (numberOfGroups > MAXIMUM_NUMBER_OF_GROUPS) {
                LOGGER.warn("Number of groups ({}) exceeds the Maximum permitted ({})", numberOfGroups, MAXIMUM_NUMBER_OF_GROUPS);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_GROUP_COUNT_ERROR, String.format("Maximum of %d Groups permitted.", MAXIMUM_NUMBER_OF_GROUPS));
            }
            final Set<String> groupNames = new HashSet<>(numberOfGroups);
            final Set<String> repeatedGroupNames = new HashSet<>();
            for (int i = 0; i < numberOfGroups; i++) {
                final JsonObject thisGroup = groups.get(i).getAsJsonObject();
                final JsonElement groupNameValue = thisGroup.get(NAME_ATTRIBUTE_NAME);
                final String groupName = groupNameValue.getAsString();
                validateName(groupName);
                if (!groupNames.add(groupName)) {
                    repeatedGroupNames.add(groupName);
                }
                try {
                    validateCustomizedDefaultSettings(thisGroup, CUSTOMIZED_GROUP_SETTINGS_NAME);
                } catch (final ConfigurationSettingsJsonValidationException e) {
                    throw new ConfigurationSettingsJsonValidationException(
                            CONFIGURATION_SETTINGS_JSON_VALIDATION_ERROR,
                            String.format("In Group '%s': %s", groupName, e.getErrorMessage()), e);
                }
            }
            if (!repeatedGroupNames.isEmpty()) {
                LOGGER.warn("Group names must be unique, but found {} repeated names %n{}", repeatedGroupNames.size(), repeatedGroupNames);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_GROUP_NAMES_ERROR,
                        String.format("Group names must be unique, but the following name(s) appear more than once:%n%s", repeatedGroupNames));
            }
        }
    }

    private static void validateCustomizedGlobalSettingOverrides(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        validateCustomGlobalOverridesAtCustomDefaultLevel(rootJson);
        validateCustomGlobalOverridesAtGroupLevel(rootJson);
    }

    private static void validateCustomGlobalOverridesAtCustomDefaultLevel(final JsonObject rootJson)
            throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(CUSTOMIZED_DEFAULT_SETTINGS_NAME)) {
            final JsonObject settings = rootJson.getAsJsonObject(CUSTOMIZED_DEFAULT_SETTINGS_NAME);
            if (checkAttributeForGlobalSettings(settings)) {
                final List<String> globalSettingNamesFoundInCustomSettings = getGlobalSettingNamesFromCollection(settings);
                LOGGER.warn("Following customizedGlobalSettings override attempt found in customizedDefaultSettings attribute - {}",
                        globalSettingNamesFoundInCustomSettings);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_CUSTOMIZED_DEFAULT_LEVEL_ERROR,
                        String.format("Attempted overwrite of customizedGlobalSetting(s) - %s in customizedDefaultSettings attribute",
                                globalSettingNamesFoundInCustomSettings.toString()));
            }
        }
    }

    private static void validateCustomGlobalOverridesAtGroupLevel(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(GROUPS_ARRAY_NAME)) {
            final JsonArray groups = rootJson.getAsJsonArray(GROUPS_ARRAY_NAME);
            final int numberOfGroups = groups.size();
            final Set<String> invalidSettingsGroupNames = new HashSet<>();
            for (int i = 0; i < numberOfGroups; i++) {
                final JsonObject thisGroup = groups.get(i).getAsJsonObject();
                final JsonElement groupNameValue = thisGroup.get(NAME_ATTRIBUTE_NAME);
                final String groupName = groupNameValue.getAsString();

                if (thisGroup.has(CUSTOMIZED_GROUP_SETTINGS_NAME)) {
                    final JsonObject groupSettings = thisGroup.getAsJsonObject(CUSTOMIZED_GROUP_SETTINGS_NAME);
                    if (checkAttributeForGlobalSettings(groupSettings)) {
                        invalidSettingsGroupNames.add(groupName);
                    }
                }
            }

            if (!invalidSettingsGroupNames.isEmpty()) {
                LOGGER.warn("Following group names attempt to overwrite customizedGlobalSettings at group level {}", invalidSettingsGroupNames);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_GLOBAL_SETTINGS_AT_GROUP_LEVEL_ERROR,
                        String.format("Following group names attempt to overwrite customizedGlobalSettings at group level - %s",
                                invalidSettingsGroupNames));
            }
        }
    }

    private static void validateInclusionList(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(INCLUSION_LIST_ATTRIBUTE_NAME)) {
            final JsonArray inclusionList = rootJson.getAsJsonArray(INCLUSION_LIST_ATTRIBUTE_NAME);
            final int numberOfGroups = inclusionList.size();
            LOGGER.info("Validating {} groups in {}.", numberOfGroups, INCLUSION_LIST_ATTRIBUTE_NAME);
            final Set<String> groupNames = new HashSet<>(numberOfGroups);
            final Set<String> repeatedGroupNames = new HashSet<>();
            for (int i = 0; i < numberOfGroups; i++) {
                final JsonObject thisGroup = inclusionList.get(i).getAsJsonObject();
                final JsonElement groupNameValue = thisGroup.get(NAME_ATTRIBUTE_NAME);
                final String groupName = groupNameValue.getAsString();
                validateName(groupName);
                if (!groupNames.add(groupName)) {
                    repeatedGroupNames.add(groupName);
                }
            }
            if (!repeatedGroupNames.isEmpty()) {
                LOGGER.warn("Group names in inclusionList must be unique, but found {} repeated names %n{}", repeatedGroupNames.size(),
                        repeatedGroupNames);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_GROUP_NAMES_ERROR,
                        String.format("Group names in inclusionList must be unique, but the following name(s) appear more than once:%n%s",
                                repeatedGroupNames));
            }
        }
    }

    private static void validateExclusionList(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (rootJson.has(EXCLUSION_LIST_ATTRIBUTE_NAME)) {
            final JsonArray exclusionList = rootJson.getAsJsonArray(EXCLUSION_LIST_ATTRIBUTE_NAME);
            final int numberOfGroups = exclusionList.size();
            LOGGER.info("Validating {} groups in {}.", numberOfGroups, EXCLUSION_LIST_ATTRIBUTE_NAME);
            final Set<String> groupNames = new HashSet<>(numberOfGroups);
            final Set<String> repeatedGroupNames = new HashSet<>();
            for (int i = 0; i < numberOfGroups; i++) {
                final JsonObject thisGroup = exclusionList.get(i).getAsJsonObject();
                final JsonElement groupNameValue = thisGroup.get(NAME_ATTRIBUTE_NAME);
                final String groupName = groupNameValue.getAsString();
                validateName(groupName);
                if (!groupNames.add(groupName)) {
                    repeatedGroupNames.add(groupName);
                }
            }
            if (!repeatedGroupNames.isEmpty()) {
                LOGGER.warn("Group names in exclusionList must be unique, but found {} repeated names %n{}", repeatedGroupNames.size(),
                        repeatedGroupNames);
                throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_GROUP_NAMES_ERROR,
                        String.format("Group names in exclusionList must be unique, but the following name(s) appear more than once:%n%s",
                                repeatedGroupNames));
            }
        }
    }

    private static void validateWeekendDays(final JsonObject rootJson) throws ConfigurationSettingsJsonValidationException {
        if (!rootJson.has(WEEKEND_DAYS_ATTRIBUTE_NAME)) {
            return;
        }

        final JsonElement weekendDaysRaw = rootJson.get(WEEKEND_DAYS_ATTRIBUTE_NAME);
        final List<String> weekendDays = Arrays.stream(weekendDaysRaw.getAsString().replaceAll(WHITESPACE_REGEX, "").split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (weekendDays.size() > MAXIMUM_NUMBER_OF_WEEKEND_DAYS) {
            throw new ConfigurationSettingsJsonValidationException(
                    CONFIGURATION_SETTINGS_JSON_WEEKEND_TOO_MANY_DAYS_VALIDATION_ERROR,
                    CONFIGURATION_SETTINGS_JSON_WEEKEND_TOO_MANY_DAYS_VALIDATION_ERROR.getErrorMessage());
        }

        final Set<DayOfWeek> days = new HashSet<>(weekendDays.size());
        for (final String weekendDay : weekendDays) {
            try {
                final DayOfWeek day = DayOfWeek.valueOf(weekendDay.toUpperCase(Locale.ROOT));
                if (!days.add(day)) {
                    throw new ConfigurationSettingsJsonValidationException(
                            CONFIGURATION_SETTINGS_JSON_WEEKEND_REPEATED_DAY_ERROR,
                            CONFIGURATION_SETTINGS_JSON_WEEKEND_REPEATED_DAY_ERROR.getErrorMessage());
                }

                if (days.size() > 1 && !days.contains(day.minus(1))) {
                    throw new ConfigurationSettingsJsonValidationException(
                            CONFIGURATION_SETTINGS_JSON_WEEKEND_NOT_CONSECUTIVE_DAYS_VALIDATION_ERROR,
                            CONFIGURATION_SETTINGS_JSON_WEEKEND_NOT_CONSECUTIVE_DAYS_VALIDATION_ERROR.getErrorMessage());
                }
            } catch (final IllegalArgumentException e) {
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_WEEKEND_INVALID_DAY_VALIDATION_ERROR,
                        CONFIGURATION_SETTINGS_JSON_WEEKEND_INVALID_DAY_VALIDATION_ERROR.getErrorMessage(),
                        e);
            }
        }
    }

    private static ProcessingReport extractReportAndValidate(final String jsonData, final String jsonSchema)
            throws ConfigurationSettingsJsonValidationException {
        try {
            final JsonNode schemaNode = fromString(jsonSchema);
            final JsonNode data = fromString(jsonData);
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            final JsonSchema schema = factory.getJsonSchema(schemaNode);
            return schema.validate(data);
        } catch (final ProcessingException | IOException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error in parsing JSON data '{}'", jsonData, e);
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_JSON_PARSING_ERROR,
                    CONFIGURATION_SETTINGS_JSON_PARSING_ERROR.getErrorMessage(), e);
        }
    }

    static void validateName(final String name) throws ConfigurationSettingsJsonValidationException {
        if (!Pattern.matches("^[a-zA-Z0-9-_]+", name)) {
            throw new ConfigurationSettingsJsonValidationException(CONFIGURATION_SETTINGS_NAME_VALIDATION_ERROR,
                    CONFIGURATION_SETTINGS_NAME_VALIDATION_ERROR.getErrorMessage());
        }
    }

    static void validateCronExpression(final String expression) throws ConfigurationSettingsJsonValidationException {
        try {
            final CronExpression cronExpression = new CronExpression(expression);
            if (cronExpression.getNextValidTimeAfter(Date.from(Instant.now())) == null || !containsValidCharacters(expression)) {
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR,
                        Collections
                                .singletonList(
                                        CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR.getErrorMessage()
                                                + expression),
                        new IllegalArgumentException("Invalid cron expression provided"));
            }

        } catch (final ParseException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error in parsing the cron expression from '{}'", expression, e);
            throw new ConfigurationSettingsJsonValidationException(
                    CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR,
                    CONFIGURATION_SETTINGS_JSON_CRON_VALIDATION_ERROR.getErrorMessage() + expression,
                    e);
        }
    }

    static void validatePaKpiSettings(final JsonObject globalSettings, final boolean enabledPA)
            throws ConfigurationSettingsJsonValidationException {
        if (enabledPA && Objects.nonNull(globalSettings) && Objects.nonNull(globalSettings.get(PA_KPI_SETTINGS))) {
            final String paKpiSettings = GSON.fromJson(globalSettings.get(PA_KPI_SETTINGS), JsonPrimitive.class).getAsString();

            final PaKpiSettings settings = new PaKpiSettings();
            settings.put(paKpiSettings);

            boolean isAtLeastOneEnabled = false;
            for (final PaKpiSettings.PaKpi value : PaKpiSettings.PaKpi.values()) {
                final PaKpiSettings.Data data = settings.get(value);
                LOGGER.info("Validating: '{}'", data);
                if (Objects.nonNull(data)) {
                    if (value.name().equals(UL_PUSCH_SINR_PA_KPI_SETTINGS_NAME) &&
                            !(isRelevanceThresholdInteger(data.relevanceThreshold))) {
                        LOGGER.warn("There is no enabled KPI value, but PA is enabled");
                        throw new ConfigurationSettingsJsonValidationException(
                                CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR,
                                CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR.getErrorMessage());
                    }
                    if (!isAtLeastOneEnabled && Objects.nonNull(data.enableKpi)) {
                        isAtLeastOneEnabled = data.enableKpi;
                    }
                    validateConfidenceInterval(data, value);
                    validateRelevanceThreshold(data, value);
                }
            }
            if (!isAtLeastOneEnabled) {
                LOGGER.warn("There is no enabled KPI value, but PA is enabled");
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_PA_ONE_ENABLED_KPI_VALIDATION_ERROR,
                        CONFIGURATION_PA_ONE_ENABLED_KPI_VALIDATION_ERROR.getErrorMessage());
            }
        }
    }

    private static boolean isRelevanceThresholdInteger(final Double relevanceThreshold) {
        return relevanceThreshold == Math.floor(relevanceThreshold);
    }

    private static void validateConfidenceInterval(final PaKpiSettings.Data data, final PaKpiSettings.PaKpi value)
            throws ConfigurationSettingsJsonValidationException {
        if (Objects.nonNull(data.confidenceInterval)) {
            final String confidence = "" + data.confidenceInterval;
            final int precision = confidence.length() - confidence.indexOf('.') - 1;
            if (precision > 1) {
                LOGGER.warn("confidence: '{}' precision is not in range", data.confidenceInterval);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR,
                        CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR.getErrorMessage()
                                + "0.1 in case of confidence values");
            } else if (!value.isInRange(data.confidenceInterval, value::getConfidenceRange)) {
                LOGGER.warn("'{}' is not in range", data.confidenceInterval);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR,
                        CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR.getErrorMessage()
                                + PaKpiSettings.PaKpi.CELLHOSR.getConfidenceRange()
                                + " in case of confidence values");
            }
        }
    }

    private static void validateRelevanceThreshold(final PaKpiSettings.Data data, final PaKpiSettings.PaKpi value)
            throws ConfigurationSettingsJsonValidationException {
        if (Objects.nonNull(data.relevanceThreshold)) {
            final String relevance = "" + data.relevanceThreshold;
            final int precision = relevance.length() - relevance.indexOf('.') - 1;
            if (precision > 2) {
                LOGGER.warn("relevance: '{}' precision is not in range", data.relevanceThreshold);
                throw new ConfigurationSettingsJsonValidationException(
                        CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR,
                        CONFIGURATION_PA_KPI_PRECISION_VALIDATION_ERROR.getErrorMessage()
                                + "0.01 in case of relevance values");
            } else if (!value.isInRange(data.relevanceThreshold, value::getRelevanceRange)) {
                LOGGER.warn("'{}' is not in range", data.relevanceThreshold);
                if (value.name().equals(UL_PUSCH_SINR_PA_KPI_SETTINGS_NAME)) {
                    throw new ConfigurationSettingsJsonValidationException(
                            CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR,
                            CONFIGURATION_PA_KPI_UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR.getErrorMessage());
                } else {
                    throw new ConfigurationSettingsJsonValidationException(
                            CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR,
                            CONFIGURATION_PA_KPI_THRESHOLD_VALIDATION_ERROR.getErrorMessage()
                                    + PaKpiSettings.PaKpi.CELLHOSR.getRelevanceRange()
                                    + " in case of relevance values");
                }
            }
        }
    }

    private static boolean containsValidCharacters(final String expression) {
        final String[] cronElements = expression.split(" ");
        return cronElements.length <= 7 && ("*".equals(cronElements[6]) || NumberUtils.isCreatable(cronElements[6]));
    }

}
