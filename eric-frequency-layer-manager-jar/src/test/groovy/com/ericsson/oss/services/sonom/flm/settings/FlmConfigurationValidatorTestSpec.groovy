/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.settings

import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException
import org.assertj.core.api.Assertions
import org.assertj.core.api.JUnitSoftAssertions
import org.junit.Rule
import org.junit.rules.ExpectedException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.ericsson.oss.services.sonom.flm.ResourceLoader.loadResource
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CQI_SAMPLES_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_THROUGHPUT_R_ATTR_NAME
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_EXISTING_HIGH_PUSH
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_LEAKAGE_LBQ_IMPACT
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_MINIMUM_LBDAR_STEPSIZE
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.THRESHOLD_TARGET_PUSH_BACK
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME
import static org.assertj.core.api.Assertions.assertThat

/**
 * Unit tests for validation of Ranged Settings in {@link FlmConfigurationValidator} class.
 */
class FlmConfigurationValidatorTestSpec extends Specification {//TODO: MIN_CONNECTED_USERS covered?

    @Shared
    def SOME_STRING = "someString"
    @Shared
    def SETTING_PLACEHOLDER = "\"%s\": \"%s\""
    @Shared
    def VALID_JSON = loadResource("sampleConfiguration.json")
    @Shared
    def VALID_GROUP_JSON = loadResource("sampleConfigurationWithGroup.json")

    @Shared
    def THRESHOLD_MINIMUM_SOURCE_RETAINED = "minimumSourceRetained"
    @Shared
    def THRESHOLD_LEAKAGE_THIRD_CELL = "leakageThirdCell"
    @Shared
    def NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME = "numberOfKpiDegradedHoursThreshold"
    @Shared
    def FLOAT_TYPE = "float"
    @Shared
    def INTEGER_TYPE = "integer"
    @Shared
    def LESS_THAN_0 = "-0.1"
    @Shared
    def ZERO_TO_100_FLOAT = "[0.0..100.0]"
    @Shared
    def TEST_SETUP_FAILURE = "Test Setup Failure. Json is not valid"
    @Shared
    def OPEN_BRACKET = " ("
    @Shared
    def SHOULD_BE_WITHIN_RANGE = "), should be within valid range"

    @Rule
    public final ExpectedException thrown = ExpectedException.none()
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions()

    def setupSpec() {
        expect:
            Assertions.assertThat(FlmConfigurationValidator.validateFlmConfiguration(VALID_JSON)).as(TEST_SETUP_FAILURE).isTrue()
            assertThat(FlmConfigurationValidator.validateFlmConfiguration(VALID_GROUP_JSON)).as(TEST_SETUP_FAILURE).isTrue()
    }

    @Unroll
    def "when Customized #settingName Setting (with group:#withGroup) is Above Maximum then Throw Error"() throws ConfigurationSettingsJsonValidationException {
        given:
            def validJson = withGroup ? VALID_GROUP_JSON : VALID_JSON
            def invalidJson = validJson.replaceFirst(buildRegex(settingName, originalValue), buildReplacement(settingName, newValueAboveRange))

        when:
            FlmConfigurationValidator.validateFlmConfiguration(invalidJson)

        then:
            def thrown = thrown(ConfigurationSettingsJsonValidationException.class)
            expectMessageAboutRange(thrown, newValueAboveRange, settingName, expectedRange)

        where:
            settingName                                                                            | withGroup | newValueAboveRange | originalValue || expectedRange
            PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME                                            | false     | "100.1"            | "40.0"        || ZERO_TO_100_FLOAT
            MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME                                             | false     | "2001"             | "20"          || "[0..1000]"
            THRESHOLD_TARGET_PUSH_BACK                                                             | false     | "20.0001"          | "2"           || "[0.0..20.0]"
            THRESHOLD_MINIMUM_SOURCE_RETAINED                                                      | false     | "101"              | "20"          || "[0..100]"
            THRESHOLD_MINIMUM_LBDAR_STEPSIZE                                                       | false     | "6.0"              | "1"           || "[0.0..5.0]"
            THRESHOLD_LEAKAGE_THIRD_CELL                                                           | false     | "100.1"            | "10"          || ZERO_TO_100_FLOAT
            THRESHOLD_LEAKAGE_LBQ_IMPACT                                                           | false     | "100.1"            | "20"          || ZERO_TO_100_FLOAT
            THRESHOLD_EXISTING_HIGH_PUSH                                                           | false     | "100.1"            | "30"          || ZERO_TO_100_FLOAT
            NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME                                       | false     | "7"                | "4"           || "[1..6]"
            // Default Settings
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | false     | "2000.1"           | "5.0"         || "[0.0..2000.0]"
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | true      | "2000.1"           | "5.0"         || "[0.0..2000.0]"
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | false     | "1.5"              | "0.3"         || "[0.0..1.0]"
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | true      | "1.5"              | "0.3"         || "[0.0..1.0]"
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | false     | "2.5"              | "0.9"         || "[0.0..2.0]"
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | true      | "2.5"              | "0.9"         || "[0.0..2.0]"
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | false     | "100.1"            | "70.0"        || ZERO_TO_100_FLOAT
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | true      | "100.5"            | "70.0"        || ZERO_TO_100_FLOAT
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | false     | "2.5"              | "0.9"         || "[0.0..2.0]"
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | true      | "2.5"              | "0.9"         || "[0.0..2.0]"
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | false     | "100.5"            | "98.0"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | true      | "100.5"            | "98.0"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | false     | "100.5"            | "98.5"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | true      | "100.5"            | "98.5"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | false     | "100.5"            | "2.0"         || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | true      | "100.5"            | "2.0"         || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | false     | "100.5"            | "1.5"         || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | true      | "100.5"            | "1.5"         || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | false     | "100.5"            | "70.0"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | true      | "100.5"            | "70.0"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | false     | "100.5"            | "70.0"        || ZERO_TO_100_FLOAT
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | true      | "100.5"            | "70.0"        || ZERO_TO_100_FLOAT
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | false     | "5"                | "3"           || "[1..4]"
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | true      | "5"                | "3"           || "[1..4]"
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | false     | "1000001"          | "100"         || "[0..1000000]"
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | true      | "1000001"          | "100"         || "[0..1000000]"
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | false     | "31"               | "15"          || "[0..30]"
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | true      | "31"               | "15"          || "[0..30]"
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | false     | "11"               | "3"           || "[1..10]"
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | true      | "11"               | "3"           || "[1..10]"
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | false     | "2.1"              | "0.8"         || "[0.1..2.0]"
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | true      | "2.1"              | "0.8"         || "[0.1..2.0]"
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | false     | "21"               | "5"           || "[0..20]"
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | true      | "21"               | "5"           || "[0..20]"
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | false     | "10.01"            | "1.2"         || "[0.0..10.0]"
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | true      | "10.01"            | "1.2"         || "[0.0..10.0]"
//            MIN_CONNECTED_USERS_ATTR_NAME | false | "21" | "10" || "[0..20]"
//            MIN_CONNECTED_USERS_ATTR_NAME | true | "21" | "10" || "[0..20]"
    }

    @Unroll
    def "when Customized #settingName Setting (with group:#withGroup) is Below Minimum then Throw Error"() throws ConfigurationSettingsJsonValidationException {
        given:
            def validJson = withGroup ? VALID_GROUP_JSON : VALID_JSON
            def invalidJson = validJson.replaceFirst(buildRegex(settingName, originalValue), buildReplacement(settingName, newValueBelowRange))

        when:
            FlmConfigurationValidator.validateFlmConfiguration(invalidJson)

        then:
            def thrown = thrown(ConfigurationSettingsJsonValidationException.class)
            expectMessageAboutRange(thrown, newValueBelowRange, settingName, expectedRange)

        where:
            settingName                                                                            | withGroup | expectedRange     | newValueBelowRange | originalValue || expectedType
            PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME                                            | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "40.0"         | FLOAT_TYPE
            MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME                                             | false     | "[0..1000]"       | "-1"               | "20"           | INTEGER_TYPE
            THRESHOLD_TARGET_PUSH_BACK                                                             | false     | "[0.0..20.0]"     | LESS_THAN_0        | "2"            | FLOAT_TYPE
            THRESHOLD_MINIMUM_SOURCE_RETAINED                                                      | false     | "[0..100]"        | "-1"               | "20"           | INTEGER_TYPE
            THRESHOLD_MINIMUM_LBDAR_STEPSIZE                                                       | false     | "[0.0..5.0]"      | LESS_THAN_0        | "1"            | FLOAT_TYPE
            THRESHOLD_LEAKAGE_THIRD_CELL                                                           | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "10"           | FLOAT_TYPE
            THRESHOLD_LEAKAGE_LBQ_IMPACT                                                           | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "20"           | FLOAT_TYPE
            THRESHOLD_EXISTING_HIGH_PUSH                                                           | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "30"           | FLOAT_TYPE
            NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME                                       | false     | "[1..6]"          | "0"                | "4"            | INTEGER_TYPE
            // Default Settings
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | false     | "[0.0..2000.0]"   | LESS_THAN_0        | "5.0"          | FLOAT_TYPE
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | true      | "[0.0..2000.0]"   | LESS_THAN_0        | "5.0"          | FLOAT_TYPE
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | false     | "[0.0..1.0]"      | LESS_THAN_0        | "0.3"          | FLOAT_TYPE
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | true      | "[0.0..1.0]"      | LESS_THAN_0        | "0.3"          | FLOAT_TYPE
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | false     | "[0.0..2.0]"      | LESS_THAN_0        | "0.9"          | FLOAT_TYPE
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | true      | "[0.0..2.0]"      | LESS_THAN_0        | "0.9"          | FLOAT_TYPE
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | false     | "[0.0..2.0]"      | LESS_THAN_0        | "0.9"          | FLOAT_TYPE
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | true      | "[0.0..2.0]"      | LESS_THAN_0        | "0.9"          | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "98.0"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "98.0"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "98.5"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "98.5"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "2.0"          | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "2.0"          | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "1.5"          | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "1.5"          | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | false     | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | true      | ZERO_TO_100_FLOAT | LESS_THAN_0        | "70.0"         | FLOAT_TYPE
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | false     | "[1..4]"          | "0"                | "3"            | INTEGER_TYPE
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | true      | "[1..4]"          | "0"                | "3"            | INTEGER_TYPE
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | false     | "[0..1000000]"    | "-1"               | "100"          | INTEGER_TYPE
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | true      | "[0..1000000]"    | "-1"               | "100"          | INTEGER_TYPE
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | false     | "[0..30]"         | "-1"               | "15"           | INTEGER_TYPE
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | true      | "[0..30]"         | "-1"               | "15"           | INTEGER_TYPE
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | false     | "[1..10]"         | "0"                | "3"            | INTEGER_TYPE
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | true      | "[1..10]"         | "0"                | "3"            | INTEGER_TYPE
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | false     | "[0.1..2.0]"      | "0.01"             | "0.8"          | FLOAT_TYPE
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | true      | "[0.1..2.0]"      | "0.01"             | "0.8"          | FLOAT_TYPE
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | false     | "[0..20]"         | "-1"               | "5"            | INTEGER_TYPE
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | true      | "[0..20]"         | "-1"               | "5"            | INTEGER_TYPE
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | false     | "[0.0..10.0]"     | LESS_THAN_0        | "1.2"          | FLOAT_TYPE
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | true      | "[0.0..10.0]"     | LESS_THAN_0        | "1.2"          | FLOAT_TYPE
//            MIN_CONNECTED_USERS_ATTR_NAME | false | "[0..20]" | "-1" | "10" | INTEGER_TYPE
//            MIN_CONNECTED_USERS_ATTR_NAME | true | "[0..20]" | "-1" |"10" | INTEGER_TYPE
    }

    @Unroll
    def "when Customized #settingName Setting (with group:#withGroup) is Wrong Data Type then Throw Error"() throws ConfigurationSettingsJsonValidationException {
        given:
            def validJson = withGroup ? VALID_GROUP_JSON : VALID_JSON
            def invalidJson = validJson.replaceFirst(buildRegex(settingName, originalValue), buildReplacement(settingName, SOME_STRING))

        when:
            FlmConfigurationValidator.validateFlmConfiguration(invalidJson)

        then:
            def thrown = thrown(ConfigurationSettingsJsonValidationException.class)
            assertThat(thrown).hasMessageContaining(
                    String.format("%s should be a%s %s", settingName, INTEGER_TYPE == expectedType ? "n" : "", expectedType))

        where:
            settingName                                                                            | withGroup | originalValue || expectedType
            PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME                                            | false     | "40.0"        || FLOAT_TYPE
            MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME                                             | false     | "20"          || INTEGER_TYPE
            THRESHOLD_TARGET_PUSH_BACK                                                             | false     | "2"           || FLOAT_TYPE
            THRESHOLD_MINIMUM_SOURCE_RETAINED                                                      | false     | "20"          || INTEGER_TYPE
            THRESHOLD_MINIMUM_LBDAR_STEPSIZE                                                       | false     | "1"           || FLOAT_TYPE
            THRESHOLD_LEAKAGE_THIRD_CELL                                                           | false     | "10"          || FLOAT_TYPE
            THRESHOLD_LEAKAGE_LBQ_IMPACT                                                           | false     | "20"          || FLOAT_TYPE
            THRESHOLD_EXISTING_HIGH_PUSH                                                           | false     | "30"          || FLOAT_TYPE
            NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME                                       | false     | "4"           || INTEGER_TYPE
            // Default Settings|
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | false     | "5.0"         || FLOAT_TYPE
            TARGET_THROUGHPUT_R_ATTR_NAME                                                          | true      | "5.0"         || FLOAT_TYPE
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | false     | "0.3"         || FLOAT_TYPE
            DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME                                             | true      | "0.3"         || FLOAT_TYPE
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | false     | "0.9"         || FLOAT_TYPE
            TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME                               | true      | "0.9"         || FLOAT_TYPE
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | false     | "70.0"        || FLOAT_TYPE
            SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME                                      | true      | "70.0"        || FLOAT_TYPE
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | false     | "0.9"         || FLOAT_TYPE
            TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME                                     | true      | "0.9"         || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | false     | "98.0"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME          | true      | "98.0"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | false     | "98.5"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME | true      | "98.5"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | false     | "2.0"         || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME                            | true      | "2.0"         || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | false     | "1.5"         || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME                   | true      | "1.5"         || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | false     | "70.0"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME                               | true      | "70.0"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | false     | "70.0"        || FLOAT_TYPE
            LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME                               | true      | "70.0"        || FLOAT_TYPE
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | false     | "3"           || INTEGER_TYPE
            MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME                                             | true      | "3"           || INTEGER_TYPE
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | false     | "100"         || INTEGER_TYPE
            MIN_NUM_CQI_SAMPLES_ATTR_NAME                                                          | true      | "100"         || INTEGER_TYPE
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | false     | "15"          || INTEGER_TYPE
            MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                    | true      | "15"          || INTEGER_TYPE
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | false     | "3"           || INTEGER_TYPE
            SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME                                              | true      | "3"           || INTEGER_TYPE
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | false     | "0.8"         || FLOAT_TYPE
            UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME                                            | true      | "0.8"         || FLOAT_TYPE
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | false     | "5"           || INTEGER_TYPE
            MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME                                                 | true      | "5"           || INTEGER_TYPE
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | false     | "1.2"         || FLOAT_TYPE
            PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME                                          | true      | "1.2"         || FLOAT_TYPE
//            MIN_CONNECTED_USERS_ATTR_NAME | false | "-1" || INTEGER_TYPE
//            MIN_CONNECTED_USERS_ATTR_NAME | true | "-1" || INTEGER_TYPE
    }

    private void expectMessageAboutRange(final Exception thrown, final String rValue, final String customizedSettingName,
                                         final String customizedSettingRange) {
        assertThat(thrown).hasMessageContainingAll(
                customizedSettingName + OPEN_BRACKET,
                rValue,
                SHOULD_BE_WITHIN_RANGE,
                customizedSettingRange)
    }

    private String buildReplacement(final String settingName, final String newValue) {
        return String.format(SETTING_PLACEHOLDER, settingName, newValue)
    }

    private String buildRegex(final String settingName, final String originalValue) {
        return FlmConfigurationValidatorTest.escapeRegex(String.format(SETTING_PLACEHOLDER, settingName, originalValue))
    }

}
