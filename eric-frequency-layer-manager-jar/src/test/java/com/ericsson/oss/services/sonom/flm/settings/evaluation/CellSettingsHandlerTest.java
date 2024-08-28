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
package com.ericsson.oss.services.sonom.flm.settings.evaluation;

import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.ENABLE_ESS_SETTING_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_CONNECTED_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MINIMUM_SOURCE_RETAINED_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CQI_SAMPLES_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.OPTIMIZATION_SPEED_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_THROUGHPUT_R_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.core.data.Offset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDao;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;

/**
 * Unit tests for {@link CellSettingsHandler} class. Note:{@link CellSettingsHandler} does not validate values, so some values in test are out of
 * range
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsHandlerTest {

    private static final double DEFAULT_QOS_FOR_CAPACITY_ESTIMATION = 0.5;
    private static final double DEFAULT_PERCENTILE_FOR_MAX_CONNECTED_USER = 40.0;
    private static final int DEFAULT_MIN_NUM_CELL_FOR_CDF_CALCULATION = 20;
    private static final String DEFAULT_CUSTOM_GLOBAL_SETTINGS = "{\"qosForCapacityEstimation\":\"" + DEFAULT_QOS_FOR_CAPACITY_ESTIMATION + "\", " +
            "\"percentileForMaxConnectedUser\":\"" + DEFAULT_PERCENTILE_FOR_MAX_CONNECTED_USER + "\", " +
            "\"minNumCellForCDFCalculation\":\"" + DEFAULT_MIN_NUM_CELL_FOR_CDF_CALCULATION + "\"}";
    private static final String OPTIMIZATION_SPEED_SLOW = "slow";
    private static final String OPTIMIZATION_SPEED_NORMAL = "normal";
    private static final String OPTIMIZATION_SPEED_FAST = "fast";
    private static final double DEFAULT_TARGET_THROUGHPUT_R = 5.0D;
    private static final double DEFAULT_DELTA_GFS_OPTIMIZATION_THRESHOLD = 0.2D;
    private static final double DEFAULT_TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD = 0.9D;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE = 98.0;
    private static final int ONE_HUNDRED_PERCENT = 100;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST = ONE_HUNDRED_PERCENT
            - DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1 = 98.5;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1 = ONE_HUNDRED_PERCENT
            - DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1;
    private static final Double DEFAULT_SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD = 90.0D;
    private static final Double DEFAULT_TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD = 0.7D;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE = 70.D;
    private static final Double DEFAULT_LB_THRESHOLD_FOR_CELL_AVAILABILITY = 70.D;
    private static final Integer DEFAULT_MINIMUM_SOURCE_RETAINED = 10;
    private static final Integer DEFAULT_MIN_ROPS_FOR_APP_COV_RELIABILITY = 3;
    private static final Integer DEFAULT_MIN_NUM_CQI_SAMPLES = 100;
    private static final Integer DEFAULT_MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION = 15;
    private static final Integer DEFAULT_SIGMA_FOR_TRANSIENT_CALCULATION = 3;
    private static final Double DEFAULT_UPLINK_PUSCH_SINR_RATIO_THRESHOLD = 0.8D;
    private static final Integer DEFAULT_MIN_TARGET_UPLINK_PUSCH_SINR = 5;
    private static final Double DEFAULT_PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD = 1.2D;
    private static final Integer DEFAULT_MIN_CONNECTED_USERS = 10;
    private static final Double DEFAULT_THRESHOLD_FOR_ENDC = 50.D;
    private static final Boolean DEFAULT_THRESHOLD_FOR_ESS = true;
    private static final Integer DEFAULT_NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS = 20;
    private static final Integer DEFAULT_SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS = 72;

    private static final String DEFAULT_CUSTOM_DEFAULT_SETTINGS = "{\"targetThroughputR(Mbps)\":\"" + DEFAULT_TARGET_THROUGHPUT_R + "\", " +
            "\"deltaGFSOptimizationThreshold\":\"" + DEFAULT_DELTA_GFS_OPTIMIZATION_THRESHOLD + "\", " +
            "\"targetSourceCoverageBalanceRatioThreshold\":\"" + DEFAULT_TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD + "\", " +
            "\"sourceTargetSamplesOverlapThreshold\":\"" + DEFAULT_SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD + "\", " +
            "\"targetSourceContiguityRatioThreshold\":\"" + DEFAULT_TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD + "\", " +
            "\"loadBalancingThresholdForInitialAndAddedErabEstabSuccRate\":\"" + DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE
            + "\", " +
            "\"loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1\":\""
            + DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1 + "\", " +
            "\"loadBalancingThresholdForErabPercentageLost\":\"" + DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST + "\", " +
            "\"loadBalancingThresholdForErabPercentageLostForQci1\":\"" + DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1 + "\", " +
            "\"loadBalancingThresholdForCellHoSuccRate\":\"" + DEFAULT_LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE + "\", " +
            "\"loadBalancingThresholdForCellAvailability\":\"" + DEFAULT_LB_THRESHOLD_FOR_CELL_AVAILABILITY + "\", " +
            "\"optimizationSpeed\":\"" + OPTIMIZATION_SPEED_NORMAL + "\", " +
            "\"loadBalancingThresholdForEndcUsers\":\"" + DEFAULT_THRESHOLD_FOR_ENDC + "\", " +
            "\"essEnabled\":\"" + DEFAULT_THRESHOLD_FOR_ESS + "\", " +
            "\"minRopsForAppCovReliability\":\"" + DEFAULT_MIN_ROPS_FOR_APP_COV_RELIABILITY + "\", " +
            "\"minNumCqiSamples\":\"" + DEFAULT_MIN_NUM_CQI_SAMPLES + "\", " +
            "\"minimumSourceRetained\":\"" + DEFAULT_MINIMUM_SOURCE_RETAINED + "\", " +
            "\"minNumSamplesForTransientCalculation\":\"" + DEFAULT_MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION + "\", " +
            "\"sigmaForTransientCalculation\":\"" + DEFAULT_SIGMA_FOR_TRANSIENT_CALCULATION + "\", " +
            "\"ulPuschSinrRatioThreshold\":\"" + DEFAULT_UPLINK_PUSCH_SINR_RATIO_THRESHOLD + "\", " +
            "\"minTargetUlPuschSinr\":\"" + DEFAULT_MIN_TARGET_UPLINK_PUSCH_SINR + "\", " +
            "\"percentageBadRsrpRatioThreshold\":\"" + DEFAULT_PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD + "\", " +
            "\"minConnectedUsers\":\"" + DEFAULT_MIN_CONNECTED_USERS + "\"}";

    private static final Offset<Double> DELTA = offset(0.000001D);
    private static final String GROUP_NAME_ONE = "test_group1";
    private static final String GROUP_NAME_TWO = "test_group2";
    private static final String COMMA = ",";
    private static final String FDN = "fdn";

    @Mock
    private CmStore mockCmStore;

    @Mock
    public Execution mockExecution;

    @Mock
    public CellSettingsDao mockCellSettingsDao;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private CmSectorCellStore mockCmSectorCellStore;

    @Mock
    private ConfigurationDao mockConfigurationDao;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private CmCellGroupRetriever mockCmCellRetriever;

    @Mock
    private Future<List<Cell>> mockRetrievedGroup;
    @Mock
    private Future<List<Cell>> mockRetrievedGroup2;

    private CellSettingsHandler objectUnderTest;

    @Test
    public void whenGetSettingValue_WithCustomSettings_thenCustomSettingIsUsed() {
        final Map<String, String> customSettings = new HashMap<>(15);
        final double initValue = 9999.4D;
        double expectedDouble = initValue;
        final int customIntValue = 2;
        customSettings.put(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(TARGET_THROUGHPUT_R_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME, String.valueOf(expectedDouble));
        final String optimizationSpeed = OPTIMIZATION_SPEED_FAST;
        customSettings.put(OPTIMIZATION_SPEED_ATTR_NAME, optimizationSpeed);
        final Boolean essEnabled = DEFAULT_THRESHOLD_FOR_ESS;
        customSettings.put(ENABLE_ESS_SETTING_ATTR_NAME, essEnabled.toString());
        customSettings.put(MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME, String.valueOf(customIntValue));
        customSettings.put(MIN_NUM_CQI_SAMPLES_ATTR_NAME, String.valueOf(customIntValue));
        customSettings.put(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME, String.valueOf(customIntValue));
        customSettings.put(MINIMUM_SOURCE_RETAINED_NAME, String.valueOf(customIntValue));
        customSettings.put(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, String.valueOf(customIntValue));
        customSettings.put(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, String.valueOf(customIntValue));
        customSettings.put(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(expectedDouble++));
        customSettings.put(MIN_CONNECTED_USERS_ATTR_NAME, String.valueOf(customIntValue));

        expectedDouble = initValue;
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_THROUGHPUT_R_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getRoundedCustomizedDefaultSettingValue(customSettings, DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME))
                .isCloseTo(expectedDouble++, DELTA);
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        OPTIMIZATION_SPEED_ATTR_NAME))
                .isEqualTo(optimizationSpeed);
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_NUM_CQI_SAMPLES_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        ENABLE_ESS_SETTING_ATTR_NAME))
                .isEqualTo(essEnabled.toString());
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MINIMUM_SOURCE_RETAINED_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(expectedDouble++));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_CONNECTED_USERS_ATTR_NAME))
                .isEqualTo(String.valueOf(customIntValue));
    }

    @Test
    public void whenGetSettingValue_WithEmptyCustomSettings_thenDefaultSettingIsUsed() {
        final Map<String, String> customSettings = Collections.emptyMap();
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_QOS_FOR_CAPACITY_ESTIMATION));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_PERCENTILE_FOR_MAX_CONNECTED_USER));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_GLOBAL_SETTINGS,
                        MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_NUM_CELL_FOR_CDF_CALCULATION));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_THROUGHPUT_R_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_TARGET_THROUGHPUT_R));
        softly.assertThat(CellSettingsHandler.getRoundedCustomizedDefaultSettingValue(customSettings,
                        DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME))
                .isCloseTo(DEFAULT_DELTA_GFS_OPTIMIZATION_THRESHOLD, DELTA);
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_LB_THRESHOLD_FOR_CELL_AVAILABILITY));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        OPTIMIZATION_SPEED_ATTR_NAME))
                .isEqualTo(OPTIMIZATION_SPEED_NORMAL);
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_ROPS_FOR_APP_COV_RELIABILITY));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_NUM_CQI_SAMPLES_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_NUM_CQI_SAMPLES));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MINIMUM_SOURCE_RETAINED_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MINIMUM_SOURCE_RETAINED));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_SIGMA_FOR_TRANSIENT_CALCULATION));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_UPLINK_PUSCH_SINR_RATIO_THRESHOLD));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_TARGET_UPLINK_PUSCH_SINR));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_THRESHOLD_FOR_ENDC));
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        ENABLE_ESS_SETTING_ATTR_NAME))
                .isEqualTo(DEFAULT_THRESHOLD_FOR_ESS.toString());
        softly.assertThat(CellSettingsHandler.getCustomizedSettingValue(customSettings, DEFAULT_CUSTOM_DEFAULT_SETTINGS,
                        MIN_CONNECTED_USERS_ATTR_NAME))
                .isEqualTo(String.valueOf(DEFAULT_MIN_CONNECTED_USERS));
    }

    @Test
    public void whenGetSettingValueFromGroup_WithCustomSetting_thenCustomSettingIsUsed() {
        final Map<String, String> groupCustomSettings = new HashMap<>(15);
        final double initValue = 9999.6D;
        double inputDouble = initValue;
        final int customIntValue = 2;
        groupCustomSettings.put(TARGET_THROUGHPUT_R_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME,
                String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME, String.valueOf(inputDouble));
        final String optimizationSpeed = OPTIMIZATION_SPEED_SLOW;
        groupCustomSettings.put(OPTIMIZATION_SPEED_ATTR_NAME, optimizationSpeed);
        groupCustomSettings.put(MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(MIN_NUM_CQI_SAMPLES_ATTR_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(MINIMUM_SOURCE_RETAINED_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, String.valueOf(customIntValue));
        groupCustomSettings.put(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(inputDouble++));
        groupCustomSettings.put(MIN_CONNECTED_USERS_ATTR_NAME, String.valueOf(customIntValue));
        final Boolean essEnabled = DEFAULT_THRESHOLD_FOR_ESS;
        groupCustomSettings.put(ENABLE_ESS_SETTING_ATTR_NAME, essEnabled.toString());

        double expectedDouble = initValue;
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings, TARGET_THROUGHPUT_R_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings, DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME))
                .contains(expectedDouble);
        softly.assertThat(CellSettingsHandler.getSettingStringValueFromGroup(groupCustomSettings,
                        OPTIMIZATION_SPEED_ATTR_NAME))
                .contains(optimizationSpeed);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MIN_NUM_CQI_SAMPLES_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MINIMUM_SOURCE_RETAINED_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingDoubleValueFromGroup(groupCustomSettings,
                        PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME))
                .contains(expectedDouble++);
        softly.assertThat(CellSettingsHandler.getSettingIntegerValueFromGroup(groupCustomSettings,
                        MIN_CONNECTED_USERS_ATTR_NAME))
                .contains(customIntValue);
        softly.assertThat(CellSettingsHandler.getSettingStringValueFromGroup(groupCustomSettings,
                        ENABLE_ESS_SETTING_ATTR_NAME))
                .contains(essEnabled.toString());
    }

    @Test
    public void whenGetSettingValueFromGroup_WithEmptySetting_thenNothingReturned() {
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(TARGET_THROUGHPUT_R_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(TARGET_THROUGHPUT_R_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME))
                .isEmpty();
        softly.assertThat(
                        getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME))
                .isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME))
                .isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(OPTIMIZATION_SPEED_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(MIN_NUM_CQI_SAMPLES_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(MINIMUM_SOURCE_RETAINED_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalIntegerValueFromEmptyGroup(MIN_CONNECTED_USERS_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME)).isEmpty();
        softly.assertThat(getSettingOptionalDoubleValueFromEmptyGroup(ENABLE_ESS_SETTING_ATTR_NAME)).isEmpty();
    }

    private Optional<Double> getSettingOptionalDoubleValueFromEmptyGroup(final String attributeName) {
        return CellSettingsHandler.getSettingDoubleValueFromGroup(Collections.emptyMap(), attributeName);
    }

    private Optional<Integer> getSettingOptionalIntegerValueFromEmptyGroup(final String attributeName) {
        return CellSettingsHandler.getSettingIntegerValueFromGroup(Collections.emptyMap(), attributeName);
    }

    @Test
    public void whenEvaluateCellSettings_withoutCustomizedSettings_thenPersistRetrievedCellsWithDefaultSettingsApplied() throws Exception {
        when(mockCmStore.getCmSectorCellStore()).thenReturn(mockCmSectorCellStore);
        final long cellId = 123L;
        final int ossId = 111;
        final Cell cellInGroup = new Cell(cellId, ossId, FDN, null, null, null);
        final Long sectorId = 222L;
        final TopologySector sector = new TopologySector(sectorId, Collections.singletonList(cellInGroup));
        when(mockCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(Collections.singletonList(sector));
        when(mockConfigurationDao.get(anyInt())).thenReturn(mockConfiguration);
        when(mockConfiguration.getCustomizedGlobalSettings()).thenReturn(Collections.emptyMap());
        when(mockConfiguration.getCustomizedDefaultSettings()).thenReturn(Collections.emptyMap());
        final String executionId = "execution1";
        when(mockExecution.getId()).thenReturn(executionId);
        final int configId = 333;
        when(mockExecution.getConfigurationId()).thenReturn(configId);
        when(mockConfiguration.getGroups()).thenReturn(Collections.singletonList(new CustomizedGroup("group1", Collections.emptyMap())));

        when(mockCmCellRetriever.retrieveGroupEvaluation(anyString(), anyString())).thenReturn(mockRetrievedGroup);
        when(mockRetrievedGroup.get()).thenReturn(Collections.singletonList(cellInGroup));

        objectUnderTest = new CellSettingsHandler(mockCmStore, mockExecution, mockCmCellRetriever, mockConfigurationDao);

        objectUnderTest.evaluateCellSettings(mockCellSettingsDao, executionId);

        verify(mockCmCellRetriever, times(1)).retrieveGroupEvaluation(anyString(), anyString());
        final CellSettings settingsForCell123 = createCellSettingsWithDefaultValues();
        settingsForCell123.setId(cellId);
        settingsForCell123.setOssId(ossId);
        settingsForCell123.setFdn(FDN);
        settingsForCell123.setExecutionId(executionId);
        settingsForCell123.setConfigurationId(configId);
        settingsForCell123.setSectorId(sectorId);

        verify(mockCellSettingsDao).insertCellSettings(Collections.singletonList(settingsForCell123));
    }

    @Test
    public void whenEvaluateCellSettings_withDifferentGroupAndDefaultSettings_thenPersistRetrievedCellsWithGroupOrDefaultsSettingsApplied()
            throws Exception {
        when(mockCmStore.getCmSectorCellStore()).thenReturn(mockCmSectorCellStore);
        final long cellInGroup1Id = 111L;
        final long cellInGroup2Id = 222L;
        final int ossId = 14;
        final Cell cellInGroup1 = new Cell(cellInGroup1Id, ossId, FDN + cellInGroup1Id, null, null, null);
        final Cell cellInGroup2 = new Cell(cellInGroup2Id, ossId, FDN + cellInGroup2Id, null, null, null);
        final Long sectorId = 123L;
        final TopologySector sector = new TopologySector(sectorId, Arrays.asList(cellInGroup1, cellInGroup2));
        when(mockCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(Collections.singletonList(sector));
        when(mockConfigurationDao.get(anyInt())).thenReturn(mockConfiguration);
        when(mockConfiguration.getCustomizedGlobalSettings()).thenReturn(Collections.emptyMap());
        final Map<String, String> customizedDefaults = new HashMap<>();
        final double customDefaultTargetThroughputR = 6666.0D;
        customizedDefaults.put(TARGET_THROUGHPUT_R_ATTR_NAME, String.valueOf(customDefaultTargetThroughputR));
        final double customDefaultTargetSourceContRatio = 5555.0D;
        customizedDefaults.put(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME, String.valueOf(customDefaultTargetSourceContRatio));
        final double customDefaultDeltaGfs = 3333.0D;
        customizedDefaults.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, String.valueOf(customDefaultDeltaGfs));
        when(mockConfiguration.getCustomizedDefaultSettings()).thenReturn(customizedDefaults);
        final String executionId = "execution1";
        when(mockExecution.getId()).thenReturn(executionId);
        final int configId = 333;
        when(mockExecution.getConfigurationId()).thenReturn(configId);
        final double group1TargetThroughputR = 7777.0D;
        final double group2TargetSourceContRatio = 8888.0D;
        when(mockConfiguration.getGroups()).thenReturn(Arrays.asList(
                new CustomizedGroup("group1", Collections.singletonMap(TARGET_THROUGHPUT_R_ATTR_NAME, String.valueOf(group1TargetThroughputR))),
                new CustomizedGroup("group2", Collections.singletonMap(TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME,
                        String.valueOf(group2TargetSourceContRatio)))));
        when(mockCmCellRetriever.retrieveGroupEvaluation("group1", executionId)).thenReturn(mockRetrievedGroup);
        when(mockCmCellRetriever.retrieveGroupEvaluation("group2", executionId)).thenReturn(mockRetrievedGroup2);
        when(mockRetrievedGroup.get()).thenReturn(Collections.singletonList(cellInGroup1));
        when(mockRetrievedGroup2.get()).thenReturn(Collections.singletonList(cellInGroup2));
        objectUnderTest = new CellSettingsHandler(mockCmStore, mockExecution, mockCmCellRetriever, mockConfigurationDao);

        objectUnderTest.evaluateCellSettings(mockCellSettingsDao, executionId);

        verify(mockCmCellRetriever, times(1)).retrieveGroupEvaluation("group1", executionId);
        verify(mockCmCellRetriever, times(1)).retrieveGroupEvaluation("group2", executionId);
        final CellSettings settingsForCell111 = createCellSettingsWithDefaultValues();
        settingsForCell111.setTargetThroughputR(group1TargetThroughputR);
        settingsForCell111.setTargetSourceContiguityRatioThreshold(customDefaultTargetSourceContRatio);
        settingsForCell111.setDeltaGfsOptimizationThreshold(customDefaultDeltaGfs);
        settingsForCell111.setId(cellInGroup1Id);
        settingsForCell111.setOssId(ossId);
        settingsForCell111.setFdn(FDN + cellInGroup1Id);
        settingsForCell111.setExecutionId(executionId);
        settingsForCell111.setConfigurationId(configId);
        settingsForCell111.setSectorId(sectorId);

        final CellSettings settingsForCell222 = createCellSettingsWithDefaultValues();
        settingsForCell222.setTargetThroughputR(customDefaultTargetThroughputR);
        settingsForCell222.setTargetSourceContiguityRatioThreshold(group2TargetSourceContRatio);
        settingsForCell222.setDeltaGfsOptimizationThreshold(customDefaultDeltaGfs);
        settingsForCell222.setId(cellInGroup2Id);
        settingsForCell222.setOssId(ossId);
        settingsForCell222.setFdn(FDN + cellInGroup2Id);
        settingsForCell222.setExecutionId(executionId);
        settingsForCell222.setConfigurationId(configId);
        settingsForCell222.setSectorId(sectorId);

        final ArgumentCaptor<List<CellSettings>> allCellSettingsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockCellSettingsDao).insertCellSettings(allCellSettingsCaptor.capture());

        final List<CellSettings> allCellSettings = allCellSettingsCaptor.getValue();
        assertThat(allCellSettings).hasSize(2)
                .containsExactlyInAnyOrder(settingsForCell111, settingsForCell222);
    }

    private CellSettings createCellSettingsWithDefaultValues() {
        final CellSettings settings = new CellSettings();
        settings.setQosForCapacityEstimation(DEFAULT_QOS_FOR_CAPACITY_ESTIMATION);
        settings.setPercentileForMaxConnectedUser(DEFAULT_PERCENTILE_FOR_MAX_CONNECTED_USER);
        settings.setMinNumCellForCdfCalculation(DEFAULT_MIN_NUM_CELL_FOR_CDF_CALCULATION);
        settings.setDeltaGfsOptimizationThreshold(DEFAULT_DELTA_GFS_OPTIMIZATION_THRESHOLD);
        settings.setTargetThroughputR(DEFAULT_TARGET_THROUGHPUT_R);
        settings.setDeltaGfsOptimizationThreshold(DEFAULT_DELTA_GFS_OPTIMIZATION_THRESHOLD);
        settings.setTargetSourceCoverageBalanceRatioThreshold(DEFAULT_TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD);
        settings.setSourceTargetSamplesOverlapThreshold(DEFAULT_SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD);
        settings.setTargetSourceContiguityRatioThreshold(DEFAULT_TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD);
        settings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE);
        settings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
                DEFAULT_LB_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1);
        settings.setLoadBalancingThresholdForErabPercentageLost(DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST);
        settings.setLoadBalancingThresholdForErabPercentageLostForQci1(DEFAULT_LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1);
        settings.setLoadBalancingThresholdForCellHoSuccRate(DEFAULT_LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE);
        settings.setLoadBalancingThresholdForCellAvailability(DEFAULT_LB_THRESHOLD_FOR_CELL_AVAILABILITY);
        settings.setOptimizationSpeed(OPTIMIZATION_SPEED_NORMAL);
        settings.setMinimumSourceRetained(DEFAULT_MINIMUM_SOURCE_RETAINED);
        settings.setMinRopsForAppCovReliability(DEFAULT_MIN_ROPS_FOR_APP_COV_RELIABILITY);
        settings.setMinNumCqiSamples(DEFAULT_MIN_NUM_CQI_SAMPLES);
        settings.setMinNumSamplesForTransientCalculation(DEFAULT_MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION);
        settings.setSigmaForTransientCalculation(DEFAULT_SIGMA_FOR_TRANSIENT_CALCULATION);
        settings.setUlPuschSinrRatioThreshold(DEFAULT_UPLINK_PUSCH_SINR_RATIO_THRESHOLD);
        settings.setMinTargetUlPuschSinr(DEFAULT_MIN_TARGET_UPLINK_PUSCH_SINR);
        settings.setPercentageBadRsrpRatioThreshold(DEFAULT_PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD);
        settings.setMinConnectedUsers(DEFAULT_MIN_CONNECTED_USERS);
        settings.setloadBalancingThresholdForEndcUsers(DEFAULT_THRESHOLD_FOR_ENDC);
        settings.setEssEnabled(DEFAULT_THRESHOLD_FOR_ESS);
        settings.setNumCallsCellHourlyReliabilityThresholdInHours(DEFAULT_NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS);
        settings.setSyntheticCountersCellReliabilityThresholdInRops(DEFAULT_SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS);
        return settings;
    }

    @Test
    public void whenRoundNumberToPrecisionOne_thenReturnCeilingOfNumber() {
        final int intValue = 5;
        softly.assertThat(CellSettingsHandler.roundNumberToPrecisionOne(intValue)).isCloseTo(intValue, DELTA);
        final double doubleOfPrecisionOne = 3.2D;
        final String description = "%f -> %f";
        softly.assertThat(CellSettingsHandler.roundNumberToPrecisionOne(doubleOfPrecisionOne))
                .as(description, doubleOfPrecisionOne, doubleOfPrecisionOne)
                .isCloseTo(doubleOfPrecisionOne, DELTA);
        final double doubleCloseToFloor = 3.134D; //close to 3.1
        softly.assertThat(CellSettingsHandler.roundNumberToPrecisionOne(doubleCloseToFloor))
                .as(description, doubleCloseToFloor, doubleOfPrecisionOne)
                .isCloseTo(doubleOfPrecisionOne, DELTA);
        final double doubleCloseToCeiling = 3.182D; //close to 3.2
        softly.assertThat(CellSettingsHandler.roundNumberToPrecisionOne(doubleCloseToCeiling))
                .as(description, doubleCloseToCeiling, doubleOfPrecisionOne)
                .isCloseTo(doubleOfPrecisionOne, DELTA);
    }

    @Test
    public void whenBuildExclusionList_withNullListInSettings_thenReturnGroupName() {
        final CellSettings cellSettings = new CellSettings();
        softly.assertThat(cellSettings.getExclusionList()).isNull();

        softly.assertThat(CellSettingsHandler.buildExclusionListWithGroupAppended(cellSettings, GROUP_NAME_ONE))
                .isEqualTo(GROUP_NAME_ONE);
    }

    @Test
    public void whenBuildExclusionListWithGroupAppended_withNonNullListInSettings_thenReturnListWithGroupAppended() {
        final CellSettings cellSettings = new CellSettings();
        cellSettings.setExclusionList(GROUP_NAME_ONE);
        softly.assertThat(CellSettingsHandler.buildExclusionListWithGroupAppended(cellSettings, GROUP_NAME_TWO))
                .isEqualTo(GROUP_NAME_ONE + COMMA + GROUP_NAME_TWO);
    }

    @Test
    public void testEvaluateExclusionListGroups_withEmptyGroup_thenSettingsUnchanged() throws ExecutionException, InterruptedException {
        when(mockCmCellRetriever.retrieveGroupEvaluation(anyString(), anyString())).thenReturn(mockRetrievedGroup);
        when(mockRetrievedGroup.get()).thenReturn(Collections.emptyList());
        objectUnderTest = new CellSettingsHandler(mockCmStore, mockExecution, mockCmCellRetriever, mockConfigurationDao);
        final Map<String, CellSettings> allCellSettings = mock(Map.class);

        objectUnderTest.evaluateExclusionListGroups(Collections.singletonList(new Group(GROUP_NAME_ONE)), "exec1", allCellSettings);
        verifyZeroInteractions(allCellSettings);
    }

    @Test
    public void testEvaluateExclusionListGroups_withNoSettingsForCellsInGroup_thenSettingsUnchanged() throws ExecutionException,
            InterruptedException {
        when(mockCmCellRetriever.retrieveGroupEvaluation(anyString(), anyString())).thenReturn(mockRetrievedGroup);
        when(mockRetrievedGroup.get()).thenReturn(Collections.singletonList(new Cell(1L, 1, FDN, null, null, null)));
        objectUnderTest = new CellSettingsHandler(mockCmStore, mockExecution, mockCmCellRetriever, mockConfigurationDao);
        final CellSettings settings1 = new CellSettings();
        final Map<String, CellSettings> allCellSettings = Collections.singletonMap("someOtherCell", settings1);
        final String before = allCellSettings.toString();

        objectUnderTest.evaluateExclusionListGroups(Collections.singletonList(new Group(GROUP_NAME_ONE)), "exec1", allCellSettings);
        assertThat(allCellSettings.toString()).isEqualTo(before);
    }

    @Test
    public void whenEvaluateExclusionListGroups_thenExlusionListIsEvaluatedForEachCell() throws ExecutionException, InterruptedException {
        when(mockCmCellRetriever.retrieveGroupEvaluation(anyString(), anyString())).thenReturn(mockRetrievedGroup);
        final int ossId = 1;
        when(mockRetrievedGroup.get()).thenReturn(Collections.singletonList(new Cell(1L, ossId, FDN, null, null, null)));
        objectUnderTest = new CellSettingsHandler(mockCmStore, mockExecution, mockCmCellRetriever, mockConfigurationDao);
        final CellSettings settings1 = new CellSettings();
        final String globalCellId = ossId + "-" + FDN;
        final Map<String, CellSettings> allCellSettings = Collections.singletonMap(globalCellId, settings1);
        softly.assertThat(allCellSettings.get(globalCellId).getExclusionList()).isNull();

        objectUnderTest.evaluateExclusionListGroups(Collections.singletonList(new Group(GROUP_NAME_ONE)), "exec1", allCellSettings);
        softly.assertThat(allCellSettings.get(globalCellId).getExclusionList())
                .isEqualTo(CellSettingsHandler.buildExclusionListWithGroupAppended(new CellSettings(), GROUP_NAME_ONE));
    }
}