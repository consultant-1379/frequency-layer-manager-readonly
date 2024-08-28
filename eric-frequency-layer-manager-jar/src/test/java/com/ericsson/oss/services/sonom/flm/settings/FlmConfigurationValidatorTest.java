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

import static com.ericsson.oss.services.sonom.flm.ResourceLoader.loadResource;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.OPTIMIZATION_SPEED_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_THROUGHPUT_R_ATTR_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.data.MapEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;

/**
 * Unit tests for {@link FlmConfigurationValidator} class.
 */
public class FlmConfigurationValidatorTest {
    private static final String SOME_STRING = "someString";
    private static final String DOUBLE_5_STRING = "\"5.0\"";
    private static final String DOUBLE_2_STRING = "\"2.0\"";
    private static final String TRUE = "true";
    private static final String SETTING_PLACEHOLDER = "\"%s\": \"%s\"";
    private static final String TARGET_THROUGHPUT_R_MBPS = "\"targetThroughputR(Mbps)\": ";
    private static final String VALID_NAME_STRING = "createTestConfig1";
    private static final char COMMA = ',';
    private static final char DOUBLE_QUOTES = '"';
    private static final int MAXIMUM_NUMBER_OF_GROUPS = 10_000;
    private static final String EMPTY_JSON = "{}";
    private static final String TEST_SETUP_FAILURE = "Test Setup Failure, unexpected test resource content";

    private static final String VALID_JSON = loadResource("sampleConfiguration.json");
    private static final String VALID_CRON = "0 0 3 * * ? *";
    private static final String VALIDATION_ERROR_S_INSTEAD_OF_S = "Validation error in Json instance type (%s) does not match any allowed primitive type (allowed: %s)";
    private static final String VALIDATION_ERROR_TOO_MANY_DAYS = "Validation error in Json, only up to two days are allowed eg. Saturday,Sunday";
    private static final String VALIDATION_ERROR_REPEATED_DAY = "Validation error in Json, each day can occur only a single time";
    private static final String VALIDATION_ERROR_UNKNOWN_GLOBAL_SETTING = "Validation error in Json, unknown customizedGlobalSetting (minimumSourceRetained) found in configuration";
    private static final String VALIDATION_ERROR_INVALID_DAY = "Validation error in Json, invalid day used, only Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday supported";
    private static final String VALIDATION_ERROR_NAME_INVALID_ERROR = "Validation error in Json, Invalid name, valid characters in name attribute are a-z, A-Z, 0-9, -, _ only.";
    private static final String VALIDATION_ERROR_NOT_CONSECUTIVE_DAYS = "Validation error in Json, only consecutive days are allowed eg Saturday,Sunday and not Monday,Wednesday";
    private static final String OUT_OF_RANGE_CONFIDENCE_VALUE_ERROR = "Validation error in Json, if PA is enabled, the PA KPI Settings' threshold values should be in valid range of [80.0..99.9] in case of confidence values";
    private static final String OUT_OF_RANGE_RELEVANCE_VALUE_ERROR = "Validation error in Json, if PA is enabled, the PA KPI Settings' threshold values should be in valid range of [90.0..99.99] in case of relevance values";
    private static final String OUT_OF_RANGE_CONFIDENCE_PRECISION_ERROR = "Validation error in Json, if PA is enabled, the PA KPI Settings' values minimum step should be 0.1 in case of confidence values";
    private static final String OUT_OF_RANGE_RELEVANCE_PRECISION_ERROR = "Validation error in Json, if PA is enabled, the PA KPI Settings' values minimum step should be 0.01 in case of relevance values";
    private static final String NOT_BOOLEAN_VALUE_ERROR = "not boolean";
    private static final String NO_PA_IS_ENABLED_ERROR = "Validation error in Json, if PA is enabled, at least one PA KPI must be enabled";
    private static final String UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR = "Validation error in Json, ulPuschSinr PA KPI relevance threshold value should be an integer in the range of 1 - 25";
    private static final String PARSING_ERROR = "Parsing error in Json";
    private static final String INTEGER = "integer";
    private static final String STRING = "string";
    private static final String SCHEDULE = "schedule";
    private static final String BOOLEAN = "boolean";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenJsonIsValid_thenReturnTrue() throws ConfigurationSettingsJsonValidationException {
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(VALID_JSON)).isTrue();
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(buildJsonByReplacingXWithY(TRUE, "false"))).isTrue();
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(
                buildJsonByReplacingFirstXWithY("\"groups\"[^]]*]", "\"groups\": " + buildNLongGroupList(0, true))))
                .as("Configurations with 0 groups should be valid").isTrue();
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(
                buildJsonByReplacingFirstXWithY("\"groups\"[^]]*]", "\"groups\": " + buildNLongGroupList(MAXIMUM_NUMBER_OF_GROUPS, true))))
                .as("Configurations with the maximum number of groups should be valid")
                .isTrue();

        //targetThroughputR can be int or double and "x" is equivalent to x
        final Map<String, String> intDoubleOrQuotedStringToHardCodedValue = Stream
                .of(entry("5.0", DOUBLE_5_STRING), entry("5", DOUBLE_5_STRING), entry("\"5\"", DOUBLE_5_STRING), entry("2.0", DOUBLE_2_STRING),
                        entry("2", DOUBLE_2_STRING))
                .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
        for (final Map.Entry<String, String> valueAndEquivalent : intDoubleOrQuotedStringToHardCodedValue.entrySet()) {
            final String hardCoded = valueAndEquivalent.getValue();
            final String equivalent = valueAndEquivalent.getKey();
            softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(
                    buildJsonByReplacingXWithY(
                            TARGET_THROUGHPUT_R_MBPS.concat(hardCoded),
                            TARGET_THROUGHPUT_R_MBPS.concat(equivalent))))
                    .as("%s%s should be parsed as a valid.", TARGET_THROUGHPUT_R_MBPS, equivalent)
                    .isTrue();
        }
    }

    @Test
    public void whenValidateFlmConfigurationOfValidConfiguration_thenReturnTrue() throws ConfigurationSettingsJsonValidationException {
        assertThat(FlmConfigurationValidator.validateFlmConfiguration(VALID_JSON)).isTrue();
    }

    @Test
    public void whenValidateFlmConfigurationOfNull_thenReturnFalse() throws ConfigurationSettingsJsonValidationException {
        assertThat(FlmConfigurationValidator.validateFlmConfiguration(null)).isFalse();
    }

    @Test
    public void whenValidateFlmConfigurationOfInvalidJson_thenThrowException() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = VALID_JSON.substring(1);
        assertThat(invalidJson).as(TEST_SETUP_FAILURE).isNotEqualTo(VALID_JSON);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValidateFlmConfigurationWithoutSchedule_thenThrowException() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(SCHEDULE, SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Missing mandatory: \"schedule\" attribute"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValidateFlmConfigurationWithInvalidName_thenReturnFalse() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(VALID_NAME_STRING, "TestConfig and 1=1--");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(VALIDATION_ERROR_NAME_INVALID_ERROR);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValidateFlmConfigurationWithYearOutOfRange_thenReturnFalse() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(VALID_CRON, "0 0 0 ? * * 1900");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValidateFlmConfigurationInRelevantStringAtTheEndOfCronExpression_thenReturnFalse()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(VALID_CRON, VALID_CRON + " AND 1=1 --");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValidateFlmConfigurationInValidStringAtTheEndOfCronExpression_thenReturnFalse()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(VALID_CRON, VALID_CRON + "AND");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenJsonIsVoid_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("json payload is blank"));
        FlmConfigurationValidator.validateFlmConfiguration("");
    }

    @Test
    public void whenJsonIsBlank_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final char tab = '\t';
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("json payload is blank");
        FlmConfigurationValidator.validateFlmConfiguration("    " + tab);
    }

    @Test
    public void whenJsonIsEmpty_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("json payload is empty");
        FlmConfigurationValidator.validateFlmConfiguration(EMPTY_JSON);
    }

    @Test
    public void whenJsonIsInvalid_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String replacement = "^";
        final String invalidJson = buildJsonByReplacingXWithY("\"", replacement);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Invalid json"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    //Mandatory Attributes
    //id
    @Test
    public void whenIdIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("8,", DOUBLE_QUOTES + SOME_STRING + DOUBLE_QUOTES + COMMA);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, STRING, INTEGER)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    //name
    @Test
    public void whenNameIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("name", SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Missing mandatory: \"name\" attribute"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenNameIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"createTestConfig1\"", "5");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, INTEGER, STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    //enabled?
    @Test
    public void whenEnabledIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("enabled", SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Missing mandatory: \"enabled\" attribute"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenEnabledValueIsWrongDataTypeDouble_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"enabled\": true", "\"enabled\": 1.2");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, "number", BOOLEAN)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenEnabledValueIsWrongDataTypeStringTrue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"enabled\": true", "\"enabled\": \"true\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, STRING, BOOLEAN)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenEnabledValueIsWrongDataTypeStringFalse_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"enabled\": true", "\"enabled\": \"false\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, STRING, BOOLEAN)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    //schedule
    @Test
    public void whenScheduleIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(SCHEDULE, SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Missing mandatory: \"schedule\" attribute"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenCronExpressionIsNotSafe_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY(VALID_CRON, SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Validation error in Json, invalid cron expression "));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenMandatoryOpenLoopIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("openLoop", SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Missing mandatory: \"openLoop\" attribute"));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenMandatoryOpenLoopIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"openLoop\": true", "\"openLoop\": \"true\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, STRING, BOOLEAN)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenMandatoryOpenLoopIsWrongValue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"openLoop\": true", "\"openLoop\": notABoolean");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(PARSING_ERROR));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    static void expectMessageAboutRange(final ExpectedException thrown, final String rValue, final String customizedSettingName,
            final String customizedSettingRange) {
        thrown.expectMessage(allOf(
                containsString(customizedSettingName + " ("),
                containsString(rValue),
                containsString("), should be within valid range"),
                containsString(customizedSettingRange)));
    }

    @Test
    public void whenOptimizationSpeedIsNotValidOption_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, OPTIMIZATION_SPEED_ATTR_NAME, "normal")),
                String.format(SETTING_PLACEHOLDER, OPTIMIZATION_SPEED_ATTR_NAME, "very fast"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("optimizationSpeed (very fast) value should be one of - [slow, normal, fast]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptimizationSpeedIsWrongDatatype_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, OPTIMIZATION_SPEED_ATTR_NAME, "normal")),
                String.format(SETTING_PLACEHOLDER, OPTIMIZATION_SPEED_ATTR_NAME, "2.0"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("optimizationSpeed (2.0) value should be one of - [slow, normal, fast]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenQosForCapacityEstimationInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.4")),
                String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, SOME_STRING));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("qosForCapacityEstimation should be a number");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenQosForCapacityEstimationInCustomizedGlobalSettingsIsBelowMinimumOrAboveMaximum_thenThrowError() {
        final String failureMessage = "%s with value of %s failed to throw an exception";
        final String exceptionMessage = "%s (%s) value should be one of - [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]";
        final String regex = escapeRegex(String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.4"));

        final String belowMin = "-0.15";
        final String aboveMax = "10.0";

        final String invalidJsonBelowMin = buildJsonByReplacingFirstXWithY(regex,
                String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, belowMin));

        softly.assertThatThrownBy(() -> FlmConfigurationValidator.validateFlmConfiguration(invalidJsonBelowMin))
                .withFailMessage(String.format(failureMessage, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, belowMin))
                .isInstanceOf(ConfigurationSettingsJsonValidationException.class)
                .hasMessageContaining(String.format(exceptionMessage, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, belowMin));

        final String invalidJsonAboveMax = buildJsonByReplacingFirstXWithY(regex,
                String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, aboveMax));

        softly.assertThatThrownBy(() -> FlmConfigurationValidator.validateFlmConfiguration(invalidJsonAboveMax))
                .withFailMessage(String.format(failureMessage, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, aboveMax))
                .isInstanceOf(ConfigurationSettingsJsonValidationException.class)
                .hasMessageContaining(String.format(exceptionMessage, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, aboveMax));
    }

    @Test
    public void whenQosForCapacityEstimationInCustomizedGlobalSettingsIsNotDivisibleByZeroPointOne_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.4")),
                String.format(SETTING_PLACEHOLDER, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.33"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                "qosForCapacityEstimation (0.33) value should be one of - [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOverrideCCalculatorInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = VALID_JSON.replaceFirst("\"overrideCCalculator\": \"No\"",
                "\"overrideCCalculator\": {}");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("overrideCCalculator should be string");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOverrideCCalculatorInCustomizedGlobalSettingsIsWrongValue_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"overrideCCalculator\": \"No\"",
                "\"overrideCCalculator\": 12");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("overrideCCalculator (12) value should be one of - [yes, no]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenMaxLbdarStepsizeInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"maxLbdarStepsize\": \"\\[.*]\"",
                String.format("\"maxLbdarStepsize\": \"%s\"", SOME_STRING));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("maxLbdarStepsize should be an array, containing objects");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenBWInMaxLbdarStepsizeInCustomizedGlobalSettingsIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("BW", SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("All maxLbdarStepsize categories should have BW and value entries");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenMinNumCellForCDFCalculationInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20")),
                String.format(SETTING_PLACEHOLDER, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "30.0"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("minNumCellForCDFCalculation should be an integer");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValueInMaxLbdarStepsizeInCustomizedGlobalSettingsIsMissing_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("value", SOME_STRING);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("All maxLbdarStepsize categories should have BW and value entries");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValueInMaxLbdarStepsizeInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\\\\\"value\\\\\":\\\\\"1\\\\\"",
                String.format("\\\\\"value\\\\\":\\\\\"%s\\\\\"", SOME_STRING));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("maxLbdarStepsize category BW and value entries should be an int and a double, respectively");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenBWInMaxLbdarStepsizeInCustomizedGlobalSettingsIsWrongDatatype_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\\\\\"BW\\\\\":\\\\\"1400\\\\\"",
                String.format("\\\\\"BW\\\\\":\\\\\"%s\\\\\"", SOME_STRING));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("maxLbdarStepsize category BW and value entries should be an int and a double, respectively");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenBWInMaxLbdarStepsizeInCustomizedGlobalSettingsIsIncorrectValue_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\\\\\"BW\\\\\":\\\\\"1400\\\\\"",
                "\\\\\"BW\\\\\":\\\\\"1500\\\\\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("One maxLbdarStepsize (1500) value should be one of - [1400, 3000, 5000, 10000, 15000, 20000]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValueInMaxLbdarStepsizeInCustomizedGlobalSettingsIsBelowMinimum_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\\\\\"value\\\\\":\\\\\"1\\\\\"",
                "\\\\\"value\\\\\":\\\\\"-1\\\\\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                "One maxLbdarStepsize value (-1.0), should be within valid range - [0.0..100.0]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenValueInMaxLbdarStepsizeInCustomizedGlobalSettingsIsAboveMaximum_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\\\\\"value\\\\\":\\\\\"1\\\\\"",
                "\\\\\"value\\\\\":\\\\\"100.1\\\\\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                "One maxLbdarStepsize value (100.1), should be within valid range - [0.0..100.0]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenBWInMaxLbdarStepsizeInCustomizedGlobalSettingsIsNotUnique_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"BW\\\\\":\\\\\"3000\\\\\"", "\\\\\"BW\\\\\":\\\\\"1400\\\\\"");
        assertThat(invalidJson).as(TEST_SETUP_FAILURE).isNotEqualTo(VALID_JSON);
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("One maxLbdarStepsize BW value (1400) is not unique");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    static String escapeRegex(final String json) {
        return json.replaceAll("([.()\\[\\]])", "\\\\$1");
    }

    @Test
    public void whenCustomizedGlobalSettingsIncludedInCustomizedDefaultSettings_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.3")),
                String.format(SETTING_PLACEHOLDER, DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.3") + ",\n        " +
                        String.format(SETTING_PLACEHOLDER, PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "40.0") + ",\n        " +
                        String.format(SETTING_PLACEHOLDER, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                "Attempted overwrite of customizedGlobalSetting(s) - [percentileForMaxConnectedUser, minNumCellForCDFCalculation] in " +
                        "customizedDefaultSettings attribute");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenCustomizedGlobalSettingsIncludedInCustomizedGroupSettingsInGroup_thenThrowError()
            throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY(
                escapeRegex(String.format(SETTING_PLACEHOLDER, TARGET_THROUGHPUT_R_ATTR_NAME, "2.0")),
                String.format(SETTING_PLACEHOLDER, TARGET_THROUGHPUT_R_ATTR_NAME, "2.0") + ",\n        " +
                        String.format(SETTING_PLACEHOLDER, PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "40.0") + ",\n        " +
                        String.format(SETTING_PLACEHOLDER, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                "Following group names attempt to overwrite customizedGlobalSettings at group level - [groupName]");
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenNumberOfGroupsIsMaximum_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"groups\"[^]]*]",
                "\"groups\": " + buildNLongGroupList(MAXIMUM_NUMBER_OF_GROUPS + 1, true));
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Maximum of 10000 Groups permitted."));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenGroupsRepeatAName_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String repeatedGroupName = "builtGroup2";
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"groups\"[^]]*]", "\"groups\": " + buildNLongGroupList(4, true))
                .replace("builtGroup3", repeatedGroupName); //two groups with name "builtGroup2"
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(allOf(
                containsString("Group names must be unique, but the following name(s) appear more than once:"),
                containsString("["),
                containsString("builtGroup2"),
                containsString("]")));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenGroupNameIsWrongDatatypeArray_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"groupName\"", "[{\"key\": \"value\"}]");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, "array", STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenGroupNameIsWrongDatatypeInteger_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"groupName\"", "5");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, INTEGER, STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenGroupNameIsInvalid_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"groupName\"", "\"name and--\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(VALIDATION_ERROR_NAME_INVALID_ERROR);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenInclusionListNameIsInvalid_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"includedGroupName\"", "\"name and--\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(VALIDATION_ERROR_NAME_INVALID_ERROR);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenExclusionListNameIsInvalid_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"excludedGroupName\"", "\"name and--\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(VALIDATION_ERROR_NAME_INVALID_ERROR);
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalInclusionListRepeatAName_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String repeatedGroupName = "builtGroup2";
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"inclusionList\"[^]]*]", "\"inclusionList\": " + buildNLongGroupList(4, false))
                .replace("builtGroup3", repeatedGroupName); //two groups with name "builtGroup2"
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(allOf(
                containsString("Group names in inclusionList must be unique, but the following name(s) appear more than once:"),
                containsString("["),
                containsString("builtGroup2"),
                containsString("]")));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalInclusionListIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"includedGroupName\"", "[{\"key\": \"value\"}]");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(
                containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, "array", STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalExclusionListRepeatAName_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String repeatedGroupName = "builtGroup2";
        final String invalidJson = buildJsonByReplacingFirstXWithY("\"exclusionList\"[^]]*]", "\"exclusionList\": " + buildNLongGroupList(4, false))
                .replace("builtGroup3", repeatedGroupName); //two groups with name "builtGroup2"
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(allOf(
                containsString("Group names in exclusionList must be unique, but the following name(s) appear more than once:"),
                containsString("["),
                containsString("builtGroup2"),
                containsString("]")));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalExclusionListIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"excludedGroupName\"", "[{\"key\": \"value\"}]");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, "array", STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "[\"Saturday\", \"Sunday\"]");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, "array", STRING)));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysIsWrongDataFormat_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"Saturday;Sunday\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_INVALID_DAY));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysHasMoreThanTwoDays_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"Saturday,Sunday,Monday\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_TOO_MANY_DAYS));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysHasNonConsecutiveDays_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"Monday,Wednesday\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_NOT_CONSECUTIVE_DAYS));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysHasDuplicate_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"Sunday,Sunday\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_REPEATED_DAY));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysContainsInvalidDay_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"NotADay\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_INVALID_DAY));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenOptionalWeekendDaysIsEmpty_thenReturnTrue() throws ConfigurationSettingsJsonValidationException {
        final String validJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"\"");

        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(validJson)).isTrue();
    }

    @Test
    public void whenWeekendDaysAreValidDays_thenReturnTrue() throws ConfigurationSettingsJsonValidationException {
        final String validJson = buildJsonByReplacingXWithY("\"Saturday,Sunday\"", "\"Wednesday,Thursday\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(validJson)).isTrue();
    }

    @Test
    public void whenIdInPathParamIsTheSameAsIdFromJsonPayload_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Different configuration ids"));
        FlmConfigurationValidator.validateConfigurationId(4, 5);
    }

    @Test
    public void whenIdInPathParamIsNotTheSameAsIdFromJsonPayload_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Different configuration ids"));
        FlmConfigurationValidator.validateConfigurationId(4, 5);
    }

    @Test
    public void whenIdFromJsonPayloadIsNull_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString("Configuration id can not be null!"));
        FlmConfigurationValidator.validateConfigurationId(null, 5);
    }

    @Test
    public void whenEnablePAIsWrongDataType_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(String.format(VALIDATION_ERROR_S_INSTEAD_OF_S, STRING, BOOLEAN)));
        FlmConfigurationValidator.validateFlmConfiguration(VALID_JSON.replace("\"enablePA\": true", "\"enablePA\": \"true\""));
    }

    @Test
    public void whenEnablePAIsWrongValue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(PARSING_ERROR));
        FlmConfigurationValidator.validateFlmConfiguration(VALID_JSON.replace("\"enablePA\": true", "\"enablePA\": notABoolean"));
    }

    /*
     * Builds n groups with or without customized setting values [{"name":"builtGroup0"},...,{"name":"builtGroup<N-1>"}]
     * [{"name":"builtGroup0","settings":{"targetThroughputR(Mbps)":"1"}},...,{"name":"builtGroup<N-1>","settings":{"targetThroughputR(Mbps)":"1"}}]
     */
    private String buildNLongGroupList(final int n, final boolean withCustomizedSettings) {
        if (n <= 0) {
            return "[]";
        } else {
            final StringBuilder builder = new StringBuilder();
            final String groupPattern = "{\"name\": \"builtGroup%d\"}";

            final String customizedGroupPattern = "{\"name\": \"builtGroup%d\", \"customizedGroupSettings\": {\n" +
                    "        \"targetThroughputR(Mbps)\": \"5.0\",\n" +
                    "        \"deltaGFSOptimizationThreshold\": \"0.3\",\n" +
                    "        \"targetSourceCoverageBalanceRatioThreshold\": \"0.9\",\n" +
                    "        \"sourceTargetSamplesOverlapThreshold\": \"70.0\",\n" +
                    "        \"targetSourceContiguityRatioThreshold\": \"0.9\",\n" +
                    "        \"lBThresholdForInitialErabEstabSuccRate\": \"98.0\",\n" +
                    "        \"lBThresholdForInitialErabEstabSuccRateForQci1\": \"98.5\",\n" +
                    "        \"lBThresholdForErabPercentageLost\": \"2.0\",\n" +
                    "        \"lBThresholdForErabPercentageLostForQci1\": \"1.5\",\n" +
                    "        \"lBThresholdForCellHoSuccRate\": \"70.0\",\n" +
                    "        \"lBThresholdForCellAvailability\": \"70.0\",\n" +
                    "        \"optimizationSpeed\": \"normal\", \n" +
                    "        \"minRopsForAppCovReliability\": \"3\",\n" +
                    "        \"minNumCqiSamples\": \"100\",\n" +
                    "        \"minNumSamplesForTransientCalculation\": \"15\",\n" +
                    "        \"sigmaForTransientCalculation\": \"3\",\n" +
                    "        \"loadBalancingThresholdForEndcUsers\": \"50.0\",\n" +
                    "        \"essEnabled\": true,\n" +
                    "        \"minConnectedUser\": \"10\"\n" +
                    "      }}";

            final String pattern = withCustomizedSettings ? customizedGroupPattern : groupPattern;
            builder.append('[');
            for (int i = 0; i < n; i++) {
                builder.append(String.format(pattern, i));
                builder.append(',');
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
            builder.append(']');
            return builder.toString();
        }
    }

    @Test
    public void whenJsonIsNull_thenReturnFalse() throws ConfigurationSettingsJsonValidationException {
        assertThat(FlmConfigurationValidator.validateFlmConfiguration(null)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasConfidenceThresholdNotInRange_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(OUT_OF_RANGE_CONFIDENCE_VALUE_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"confidenceInterval\\\\\":\\\\\"99\\\\\"", "\\\\\"confidenceInterval\\\\\":\\\\\"19\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasRelevanceThresholdNotInRange_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(OUT_OF_RANGE_RELEVANCE_VALUE_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"99.90\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"19.90\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasConfidencePrecisionNotInRange_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(OUT_OF_RANGE_CONFIDENCE_PRECISION_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"confidenceInterval\\\\\":\\\\\"99\\\\\"", "\\\\\"confidenceInterval\\\\\":\\\\\"89.99\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasRelevancePrecisionNotInRange_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(OUT_OF_RANGE_RELEVANCE_PRECISION_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"99.90\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"95.999\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasNotBooleanEnableKpiValue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(containsString(NOT_BOOLEAN_VALUE_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"enableKPI\\\\\":true", "\\\\\"enableKPI\\\\\":abcd");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasNotNumberConfidencePrecisionValue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(NumberFormatException.class);
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"confidenceInterval\\\\\":\\\\\"99\\\\\"", "\\\\\"confidenceInterval\\\\\":\\\\\"abcd\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasNotNumberRelevancePrecisionValue_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(NumberFormatException.class);
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"99.90\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"abcd\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenPaKpiSettingsHasNoEnabledKpi_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(NO_PA_IS_ENABLED_ERROR));
        final String invalidJson = VALID_JSON
                .replaceAll("\\\\\"enableKPI\\\\\":true", "\\\\\"enableKPI\\\\\":false");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenCustomizedGlobalSettingsContainUnknownSetting_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String invalidJson = buildJsonByReplacingXWithY("\"qosForCapacityEstimation\"", "\"minimumSourceRetained\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(VALIDATION_ERROR_UNKNOWN_GLOBAL_SETTING));
        FlmConfigurationValidator.validateFlmConfiguration(invalidJson);
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsNotInteger_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"16.9876543210\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsNotInteger_thenThrowError_precisionCheck() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR));
        final String invalidJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"15.00000000001\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(invalidJson)).isFalse();
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsIntegerAndInRange_thenConfigurationValidationSucceeds() throws ConfigurationSettingsJsonValidationException {
        final String modifiedJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"1\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(modifiedJson)).isTrue();
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsIntegerAndHigherThanUpperRangeLimit_thenConfigurationValidationFails() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR));
        final String modifiedJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"26\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(modifiedJson)).isFalse();
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsIntegerAndLessThanLowerRangeLimit_thenConfigurationValidationFails() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR));
        final String modifiedJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"0\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(modifiedJson)).isFalse();
    }

    @Test
    public void whenUlPuschSinrPaKpiRelevanceThresholdIsNegativeInteger_thenConfigurationValidationFails() throws ConfigurationSettingsJsonValidationException {
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage(containsString(UL_PUSCH_SINR_THRESHOLD_VALIDATION_ERROR));
        final String modifiedJson = VALID_JSON
                .replaceFirst("\\\\\"relevanceThreshold\\\\\":\\\\\"15\\\\\"", "\\\\\"relevanceThreshold\\\\\":\\\\\"-1\\\\\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(modifiedJson)).isFalse();
    }

    @Test
    public void whenMinConnectedUsersIsNotInteger_thenThrowError() throws ConfigurationSettingsJsonValidationException {
        final String modifiedJson = buildJsonByReplacingXWithY("\"minConnectedUsers\": \"10\"","\"minConnectedUsers\": \"10.3\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("minConnectedUsers should be an integer");
        FlmConfigurationValidator.validateFlmConfiguration(modifiedJson);
    }

    @Test
    public void whenMinConnectedUsersIsIntegerAndInRange_thenConfigurationValidationSucceeds() throws ConfigurationSettingsJsonValidationException {
        final String modifiedJson = buildJsonByReplacingXWithY("\"minConnectedUsers\": \"10\"","\"minConnectedUsers\": \"15\"");
        softly.assertThat(FlmConfigurationValidator.validateFlmConfiguration(modifiedJson)).isTrue();
    }

    @Test
    public void whenMinConnectedUsersIsIntegerAndAboveRangeLimit_thenConfigurationValidationFails() throws ConfigurationSettingsJsonValidationException {
        final String modifiedJson = buildJsonByReplacingXWithY("\"minConnectedUsers\": \"10\"","\"minConnectedUsers\": \"201\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("minConnectedUsers (201), should be within valid range - [0..200]");
        FlmConfigurationValidator.validateFlmConfiguration(modifiedJson);
    }

    @Test
    public void whenMinConnectedUsersIsIntegerAndBelowRangeLimit_thenConfigurationValidationFails() throws ConfigurationSettingsJsonValidationException {
        final String modifiedJson = buildJsonByReplacingXWithY("\"minConnectedUsers\": \"10\"","\"minConnectedUsers\": \"-1\"");
        thrown.expect(ConfigurationSettingsJsonValidationException.class);
        thrown.expectMessage("minConnectedUsers (-1), should be within valid range - [0..200]");
        FlmConfigurationValidator.validateFlmConfiguration(modifiedJson);
    }

    private String buildJsonByReplacingXWithY(final String x, final String y) {
        final String validJsonWithReplacement = VALID_JSON.replace(x, y);
        assertThat(validJsonWithReplacement).as(TEST_SETUP_FAILURE).isNotEqualTo(VALID_JSON);
        return validJsonWithReplacement;
    }

    private String buildJsonByReplacingFirstXWithY(final String x, final String y) {
        final String validJsonWithReplacement = VALID_JSON.replaceFirst(x, y);
        assertThat(validJsonWithReplacement).as(TEST_SETUP_FAILURE).isNotEqualTo(VALID_JSON);
        return validJsonWithReplacement;
    }
}
