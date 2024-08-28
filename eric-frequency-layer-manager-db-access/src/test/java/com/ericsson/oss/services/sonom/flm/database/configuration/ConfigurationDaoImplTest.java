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
package com.ericsson.oss.services.sonom.flm.database.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.ConfigurationInsertHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ModifySequenceValueHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.SequenceCurrentValueHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;

/**
 * Unit tests for {@link ConfigurationDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public final class ConfigurationDaoImplTest {
    private static final String TARGET_THROUGHPUT_R_ATTR_NAME = "targetThroughputR(Mbps)";
    private static final String DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME = "deltaGFSOptimizationThreshold";
    private static final String QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME = "qosForCapacityEstimation";
    private static final String PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME = "percentileForMaxConnectedUser";
    private static final String MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME = "minNumCellForCDFCalculation";
    private static final String THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME = "targetPushBack";
    private static final String THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME = "overrideCCalculator";
    private static final String THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME = "minimumSourceRetained";
    private static final String THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME = "minLbdarStepsize";
    private static final String THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME = "maxLbdarStepsize";
    private static final String THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME = "leakageThirdCell";
    private static final String THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME = "leakageLbqImpact";
    private static final String THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME = "existingHighPush";
    private static final String MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME = "minNumSamplesForTransientCalculation";
    private static final String SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME = "sigmaForTransientCalculation";
    private static final String UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME = "ulPuschSinrRatioThreshold";
    private static final String MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME = "minTargetUlPuschSinr";
    private static final String PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD = "percentageBadRsrpRatioThreshold";
    private static final String MIN_CONNECTED_USERS_ATTR_NAME = "minConnectedUsers";
    private static final String NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME = "numberOfKpiDegradedHoursThreshold";
    private static final String PA_KPI_SETTINGS = "paKpiSettings";
    private static final String DEFAULT_PA_KPI_VALUES = "{\"cellHandoverSuccessRate\":{\"enableKPI\":true,\"confidenceInterval\":\"99\",\"relevanceThreshold\":\"99.90\"},\"initialAndAddedERabEstabSrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"99.90\"},\"initialAndAddedERabEstabSrQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"99.90\"},\"eRabRetainabilityPercentageLostHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"0.01\"},\"eRabRetainabilityPercentageLostQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"0.01\"},\"avgDlPdcpThroughputSector\":{\"enableKPI\":true,\"confidenceInterval\":\"99\"},\"avgUlPdcpThroughputSector\":{\"enableKPI\":true,\"confidenceInterval\":\"99\"},\"ulPuschSinrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"15\"}}";
    private static final String ONE_DISABLED_PA_KPI_VALUES = "{\"avgUlPdcpThroughputSector\":{\"enableKPI\":false,\"confidenceInterval\":\"99\"},\"cellHandoverSuccessRate\":{\"enableKPI\":true,\"confidenceInterval\":\"99\",\"relevanceThreshold\":\"99.90\"},\"initialAndAddedERabEstabSrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"99.90\"},\"initialAndAddedERabEstabSrQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"99.90\"},\"eRabRetainabilityPercentageLostHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"0.01\"},\"eRabRetainabilityPercentageLostQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"0.01\"},\"avgDlPdcpThroughputSector\":{\"enableKPI\":true,\"confidenceInterval\":\"99\"},\"ulPuschSinrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"15\"}}";

    private static final String MAX_LBDAR_STEPSIZE = "[{\"BW\":\"1400\",\"value\":\"1\"}," +
            "{\"BW\":\"3000\",\"value\":\"2\"},{\"BW\":\"5000\",\"value\":\"3\"},{\"BW\":\"10000\",\"value\":\"10\"}," +
            "{\"BW\":\"15000\",\"value\":\"10\"},{\"BW\":\"20000\",\"value\":\"10\"}]";
    private static final String DEFAULT = "default";
    private static final String FIRST_GROUP = "firstGroup";
    private static final String YES = "Yes";
    private static final String SATURDAY = "Saturday";

    @InjectMocks
    private final ConfigurationDao objectUnderTest = new ConfigurationDaoImpl();

    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenCreate_thenExecutionReturnedValidConfiguration() throws SQLException {
        final String secondGroup = "secondGroup";
        final Map<String, String> settings = new HashMap<>();
        settings.put(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "15");
        settings.put(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "3");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.6");
        globalSettings.put(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "50.0");
        globalSettings.put(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20");
        globalSettings.put(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1");
        globalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, YES);
        globalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2");
        globalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE);
        globalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "20");
        globalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30");
        globalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "40");
        globalSettings.put(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4");
        globalSettings.put(PA_KPI_SETTINGS,
                "{\"cellHandoverSuccessRate\": { \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"eRabRetainabilityPercentageLostHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"eRabRetainabilityPercentageLostQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"avgDlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, \"avgUlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }}");
        final Configuration configuration = new ConfigurationBuilder().withCustomizedDefaultSettings(settings)
                .withCustomizedGlobalSettings(globalSettings).disabled()
                .withGroups(Collections.singletonList(new CustomizedGroup(FIRST_GROUP, null)))
                .closedLoop().withInclusionList(Arrays.asList(new Group(FIRST_GROUP), new Group(secondGroup)))
                .withExclusionList(Collections.singletonList(new Group(FIRST_GROUP)))
                .withSaturdayWeekend()
                .withPANotEnabled()
                .build();

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(1);

        final Configuration configurationResult = objectUnderTest.create(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        softly.assertThat(configurationResult.isEnabled()).isFalse();
        softly.assertThat(configurationResult.getCustomizedDefaultSettings()).contains(
                entry(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0"),
                entry(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8"),
                entry(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "15"),
                entry(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "3"),
                entry(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10"));
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.6"),
                entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "50.0"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"),
                entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, YES),
                entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2"),
                entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE),
                entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "20"),
                entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30"),
                entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "40"),
                entry(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4"));
        softly.assertThat(configurationResult.getGroups()).hasSize(1);
        softly.assertThat(configurationResult.isOpenLoop()).isFalse();
        softly.assertThat(configurationResult.getInclusionList()).hasSize(2);
        softly.assertThat(configurationResult.getExclusionList()).hasSize(1);
        softly.assertThat(configurationResult.getWeekendDays()).isEqualTo(SATURDAY);
        softly.assertThat(configurationResult.isEnablePA()).isFalse();
    }

    @Test
    public void whenCreateDefaults_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().build();
        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class), any(Object[].class))).thenReturn(1);

        final Configuration configurationResult = objectUnderTest.create(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        softly.assertThat(configurationResult.isEnabled()).isTrue();
        softly.assertThat(configurationResult.getCustomizedDefaultSettings())
                .containsOnly(
                        entry(TARGET_THROUGHPUT_R_ATTR_NAME, "5.0"), entry(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.2"),
                        entry("targetSourceCoverageBalanceRatioThreshold", "0.9"), entry("sourceTargetSamplesOverlapThreshold", "90.0"),
                        entry("targetSourceContiguityRatioThreshold", "0.7"),
                        entry("loadBalancingThresholdForInitialAndAddedErabEstabSuccRate", "98.0"),
                        entry("loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1", "98.5"),
                        entry("loadBalancingThresholdForErabPercentageLost", "2.0"),
                        entry("loadBalancingThresholdForErabPercentageLostForQci1", "1.5"), entry("loadBalancingThresholdForCellHoSuccRate", "70.0"),
                        entry("loadBalancingThresholdForCellAvailability", "70.0"), entry("optimizationSpeed", "normal"),
                        entry("loadBalancingThresholdForEndcUsers", "50.0"), entry("essEnabled", "true"),
                        entry(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10"),
                        entry("minRopsForAppCovReliability", "3"), entry("minNumCqiSamples", "100"),
                        entry(MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "15"), entry(SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME, "3"),
                        entry(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, "0.8"), entry(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, "5"),
                        entry(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD, "1.2"), entry(MIN_CONNECTED_USERS_ATTR_NAME, "10"));
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.5"), entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "40.0"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"), entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "2"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, "No"),
                entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "1"), entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE),
                entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "10"), entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "20"),
                entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "30"), entry(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4"));
        softly.assertThat(configurationResult.getGroups()).isEmpty();
        softly.assertThat(configurationResult.isOpenLoop()).isTrue();
        softly.assertThat(configurationResult.getInclusionList()).isEmpty();
        softly.assertThat(configurationResult.getExclusionList()).isEmpty();
        softly.assertThat(configurationResult.getWeekendDays()).isEqualTo("Saturday,Sunday");
        softly.assertThat(configurationResult.isEnablePA()).isFalse();
    }

    @Test
    public void whenCreate_thenExecutionReturnedSqlException() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().build();
        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenThrow(SQLException.class);

        try {
            objectUnderTest.create(configuration);
            shouldHaveThrown(SQLException.class);
        } catch (final SQLException e) {
            verify(databaseAccessMock, times(1))
                    .executeInsert(anyString(), any(ConfigurationInsertHandler.class), any(Object[].class));
        }
    }

    @Test
    public void whenCreateWithId_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(3).disabled().closedLoop().build();
        final Map<String, String> settings = new HashMap<>();
        settings.put(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        configuration.setCustomizedDefaultSettings(settings);
        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7");
        globalSettings.put(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "70.0");
        globalSettings.put(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "30");
        globalSettings.put(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1");
        globalSettings.put(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, YES);
        globalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2");
        globalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE);
        globalSettings.put(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "20");
        globalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30");
        globalSettings.put(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "40");
        globalSettings.put(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4");
        globalSettings.put(PA_KPI_SETTINGS,
                "{\"cellHandoverSuccessRate\": { \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"eRabRetainabilityPercentageLostHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"eRabRetainabilityPercentageLostQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"avgDlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, \"avgUlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }}");
        configuration.setCustomizedGlobalSettings(globalSettings);
        configuration.setGroups(Collections.singletonList(new CustomizedGroup("", null)));
        configuration.setInclusionList(Collections.singletonList(new Group("")));
        configuration.setExclusionList(Collections.singletonList(new Group("")));
        configuration.setWeekendDays(SATURDAY);
        configuration.setEnablePA(true);

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class), any(Object[].class))).thenReturn(3);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(10);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);
        assertThat(configurationResult).isNotNull();

        softly.assertThat(configurationResult.getId()).isEqualTo(3);
        softly.assertThat(configurationResult.isEnabled()).isFalse();

        softly.assertThat(configurationResult.getCustomizedDefaultSettings()).contains(
                entry(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0"), entry(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8"),
                entry(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10"));
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7"), entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "70.0"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "30"), entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, YES), entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2"),
                entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE), entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "20"),
                entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30"), entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "40"),
                entry(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4"));

        softly.assertThat(configurationResult.getGroups()).hasSize(1);
        softly.assertThat(configurationResult.isOpenLoop()).isFalse();
        softly.assertThat(configurationResult.getInclusionList()).hasSize(1);
        softly.assertThat(configurationResult.getExclusionList()).hasSize(1);
        softly.assertThat(configurationResult.getWeekendDays()).isEqualTo(SATURDAY);
        softly.assertThat(configurationResult.isEnablePA()).isTrue();

        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        verify(databaseAccessMock, times(1)).executeQuery(anyString(), any(SequenceCurrentValueHandler.class));
    }

    @Test
    public void whenCreatedWithSameIdAsSeqId_thenVerifySetIndexOfSequence() throws SQLException {
        final int configurationId = 11;
        final Configuration configuration = new ConfigurationBuilder().withId(configurationId).build();

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(11);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(11);

        objectUnderTest.createWithId(configuration);

        verify(databaseAccessMock, times(1)).executeQuery(eq("SELECT last_value FROM configuration_settings_id_seq;"),
                any(SequenceCurrentValueHandler.class));
        verify(databaseAccessMock, never()).executeQuery(eq("SELECT setval ('configuration_settings_id_seq',11);"),
                any(ModifySequenceValueHandler.class));
    }

    @Test
    public void whenCreatedWithIdBiggerThanSeqId_thenVerifySetIndexOfSequence() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(10).build();

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class), any(Object[].class))).thenReturn(10);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(7);

        objectUnderTest.createWithId(configuration);

        verify(databaseAccessMock, times(1)).executeQuery(eq("SELECT last_value FROM configuration_settings_id_seq;"),
                any(SequenceCurrentValueHandler.class));
        verify(databaseAccessMock, times(1)).executeQuery(eq("SELECT setval ('configuration_settings_id_seq',10);"),
                any(ModifySequenceValueHandler.class));
    }

    @Test
    public void whenGetById_thenReturnRightConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(1).build();
        when(databaseAccessMock.executeQuery(anyString(), any(ConfigurationHandler.class),
                any(PreparedStatementHandler.class))).thenReturn(Collections.singletonList(configuration));

        final Configuration result = objectUnderTest.get(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    public void whenGetAll_thenReturnRightConfigurationList() throws SQLException {
        final Configuration configuration1 = new ConfigurationBuilder().withId(1).build();
        final Configuration configuration2 = new ConfigurationBuilder().withId(2).build();
        when(databaseAccessMock.executeQuery(anyString(), any(ConfigurationHandler.class)))
                .thenReturn(Arrays.asList(configuration1, configuration2));
        final List<Configuration> configurations = objectUnderTest.getAll();

        softly.assertThat(configurations).hasSize(2);
        final Configuration configuration0Result = configurations.get(0);
        softly.assertThat(configuration0Result).isNotNull();
        softly.assertThat(configuration0Result.getId()).isEqualTo(1);
        final Configuration configuration1Result = configurations.get(1);
        softly.assertThat(configuration1Result).isNotNull();
        softly.assertThat(configuration1Result.getId()).isEqualTo(2);
    }

    @Test
    public void whenGetByName_thenReturnRightConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(1).build();
        when(databaseAccessMock.executeQuery(anyString(), any(ConfigurationHandler.class),
                any(PreparedStatementHandler.class))).thenReturn(Collections.singletonList(configuration));

        final Configuration result = objectUnderTest.get(DEFAULT);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(DEFAULT);
    }

    @Test
    public void whenGetByNameNoMatch_thenReturnNull() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(ConfigurationHandler.class),
                any(PreparedStatementHandler.class))).thenReturn(Collections.emptyList());

        final Configuration result = objectUnderTest.get(DEFAULT);
        assertThat(result).isNull();
    }

    @Test
    public void whenCreateWithIdDefaults_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(10).build();

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(10);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(11);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(10);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.5"), entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "40.0"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"), entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "2"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, "No"), entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "1"),
                entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE), entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "10"),
                entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "20"), entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "30"));

    }

    @Test
    public void whenCreateWithMissingGlobalSettings_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Map<String, String> settings = new HashMap<>();
        settings.put(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7");
        globalSettings.put(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "50.0");
        globalSettings.put(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1");
        globalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2");
        globalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30");
        final Configuration configuration = new ConfigurationBuilder().withCustomizedDefaultSettings(settings)
                .withCustomizedGlobalSettings(globalSettings).disabled()
                .withGroups(Collections.singletonList(new CustomizedGroup(FIRST_GROUP, null)))
                .closedLoop().withInclusionList(Arrays.asList(new Group(FIRST_GROUP), new Group("secondGroup")))
                .withExclusionList(Collections.singletonList(new Group(FIRST_GROUP)))
                .withSaturdayWeekend()
                .build();
        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(1);

        final Configuration configurationResult = objectUnderTest.create(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        softly.assertThat(configurationResult.isEnabled()).isFalse();
        softly.assertThat(configurationResult.getCustomizedDefaultSettings()).contains(
                entry(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0"),
                entry(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8"));
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7"), entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "50.0"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"), entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, "No"), entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2"),
                entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE), entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "10"),
                entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30"), entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "30"));

        softly.assertThat(configurationResult.getGroups()).hasSize(1);
    }

    @Test
    public void whenCreateWithIdWithMissingGlobalSettings_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(3).disabled().build();
        final Map<String, String> settings = new HashMap<>();
        settings.put(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        configuration.setCustomizedDefaultSettings(settings);
        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7");
        globalSettings.put(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "70");
        globalSettings.put(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1");
        globalSettings.put(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2");
        globalSettings.put(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30");
        configuration.setCustomizedGlobalSettings(globalSettings);
        configuration.setGroups(Collections.singletonList(new CustomizedGroup("", null)));
        configuration.setInclusionList(Collections.singletonList(new Group("")));
        configuration.setExclusionList(Collections.singletonList(new Group("")));
        configuration.setWeekendDays(SATURDAY);

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(3);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(10);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(3);
        softly.assertThat(configurationResult.isEnabled()).isFalse();
        softly.assertThat(configurationResult.getCustomizedDefaultSettings()).contains(
                entry(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0"),
                entry(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8"));
        softly.assertThat(configurationResult.getGroups()).hasSize(1);
        final Map<String, String> customizedGlobalSettings = configurationResult.getCustomizedGlobalSettings();
        softly.assertThat(customizedGlobalSettings).contains(
                entry(QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME, "0.7"), entry(PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME, "70"),
                entry(MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME, "20"), entry(THRESHOLD_TARGET_PUSH_BACK_ATTR_NAME, "1"),
                entry(THRESHOLD_OVERRIDE_C_CALCULATOR_ATTR_NAME, "No"), entry(THRESHOLD_MINIMUM_LBDAR_STEPSIZE_ATTR_NAME, "2"),
                entry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE), entry(THRESHOLD_LEAKAGE_THIRD_CELL_ATTR_NAME, "10"),
                entry(THRESHOLD_LEAKAGE_LBQ_IMPACT_ATTR_NAME, "30"), entry(THRESHOLD_EXISTING_HIGH_PUSH_ATTR_NAME, "30"),
                entry(NUMBER_OF_KPI_DEGRADED_HOURS_THRESHOLD_ATTR_NAME, "4"));
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        verify(databaseAccessMock, times(1)).executeQuery(anyString(), any(SequenceCurrentValueHandler.class));
    }

    @Test
    public void whenCreateWithMissingMaxLbdarStepsizeBandwithsInGlobalSettings_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Map<String, String> globalSettings = new HashMap<>();
        final String maxLbdarStepsizeWithMissingBW = "[{\"BW\":\"1400\", \"value\":\"1\"}, " +
                "{\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"3\"}, {\"BW\":\"10000\", \"value\":\"10\"}, " +
                "{\"BW\":\"15000\", \"value\":\"10\"}]";
        globalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, maxLbdarStepsizeWithMissingBW);
        final Configuration configuration = new ConfigurationBuilder()
                .withCustomizedGlobalSettings(globalSettings)
                .build();
        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(1);

        final Configuration configurationResult = objectUnderTest.create(configuration);

        softly.assertThat(configurationResult.getId()).isEqualTo(1);
        softly.assertThat(configurationResult.getCustomizedGlobalSettings()).containsEntry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME,
                MAX_LBDAR_STEPSIZE);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
    }

    @Test
    public void whenCreateWithIdWithMissingMaxLbdarStepsizeBandwithsInGlobalSettings_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(3).disabled().build();
        final Map<String, String> globalSettings = new HashMap<>();
        final String maxLbdarStepsizeWithMissingBW = "[{\"BW\":\"1400\", \"value\":\"1\"}, " +
                "{\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"3\"}, {\"BW\":\"10000\", \"value\":\"10\"}, " +
                "{\"BW\":\"15000\", \"value\":\"10\"}]";
        globalSettings.put(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, maxLbdarStepsizeWithMissingBW);
        configuration.setCustomizedGlobalSettings(globalSettings);

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(3);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(10);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);

        assertThat(configurationResult.getId()).isEqualTo(3);
        assertThat(configurationResult.getCustomizedGlobalSettings())
                .containsEntry(THRESHOLD_MAXIMUM_LBDAR_STEPSIZE_ATTR_NAME, MAX_LBDAR_STEPSIZE);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        verify(databaseAccessMock, times(1)).executeQuery(anyString(), any(SequenceCurrentValueHandler.class));
    }

    @Test
    public void whenCreateWithIdWithEmptyPaKpiSettingsInGlobalSettings_thenExecutionReturnedValidConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(3).disabled().build();
        final Map<String, String> globalSettings = new HashMap<>();
        configuration.setCustomizedGlobalSettings(globalSettings);

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(3);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(10);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);

        assertThat(configurationResult.getId()).isEqualTo(3);
        assertThat(configurationResult.getCustomizedGlobalSettings())
                .containsEntry(PA_KPI_SETTINGS, DEFAULT_PA_KPI_VALUES);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        verify(databaseAccessMock, times(1)).executeQuery(anyString(), any(SequenceCurrentValueHandler.class));
    }

    @Test
    public void whenCreateWithIdWithOneDisabledKpiSettingsInGlobalSettings_thenExecutionReturnedFilledConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(3).disabled().build();
        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(PA_KPI_SETTINGS, "{\"avgUlPdcpThroughputSector\":{\"enableKPI\":false,\"confidenceInterval\":\"99\"}}");
        configuration.setCustomizedGlobalSettings(globalSettings);

        when(databaseAccessMock.executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class))).thenReturn(3);
        when(databaseAccessMock.executeQuery(anyString(), any(SequenceCurrentValueHandler.class))).thenReturn(10);

        final Configuration configurationResult = objectUnderTest.createWithId(configuration);

        assertThat(configurationResult.getId()).isEqualTo(3);
        assertThat(configurationResult.getCustomizedGlobalSettings())
                .containsEntry(PA_KPI_SETTINGS, ONE_DISABLED_PA_KPI_VALUES);
        verify(databaseAccessMock, times(1)).executeInsert(anyString(), any(ConfigurationInsertHandler.class),
                any(Object[].class));
        verify(databaseAccessMock, times(1)).executeQuery(anyString(), any(SequenceCurrentValueHandler.class));
    }

    @Test
    public void whenRecordDeleted_thenReturnTrue() throws SQLException {
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class))).thenReturn(1);
        final boolean result = objectUnderTest.delete(10);
        assertThat(result).isTrue();
    }

    @Test
    public void whenNoRecordDeleted_thenReturnFalse() throws SQLException {
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class))).thenReturn(0);
        final boolean result = objectUnderTest.delete(10);
        assertThat(result).isFalse();
    }

    @Test
    public void whenUpdate_thenCreatedRecordNumReturned() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(10).build();
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class))).thenReturn(1);
        assertThat(objectUnderTest.update(configuration)).isEqualTo(1);
    }

    @Test
    public void whenUpdateSettingsOnUpgrade_thenNewlyDefinedSettingsWithDefaultValuesAddedToConfiguration() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(10).build();

        final Map<String, String> settings = new HashMap<>();
        settings.put(TARGET_THROUGHPUT_R_ATTR_NAME, "6.0");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        configuration.setCustomizedDefaultSettings(settings);

        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(PA_KPI_SETTINGS,
                "{\"cellHandoverSuccessRate\": { \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"eRabRetainabilityPercentageLostHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"eRabRetainabilityPercentageLostQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"avgDlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, \"avgUlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }}");
        configuration.setCustomizedGlobalSettings(globalSettings);

        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class))).thenReturn(1);
        objectUnderTest.updateSettingsOnUpgrade(configuration);

        assertThat(configuration.getCustomizedGlobalSettings().get(PA_KPI_SETTINGS))
                .isEqualTo(DEFAULT_PA_KPI_VALUES);
        assertThat(configuration.getCustomizedDefaultSettings()).contains(
                entry(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, "0.8"),
                entry(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, "5"),
                entry(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD, "1.2"));
    }

    @Test
    public void whenUpdateSettingsOnUpgrade_thenOnlyMissingSettingsAreUpdated() throws SQLException {
        final Configuration configuration = new ConfigurationBuilder().withId(10).build();

        final Map<String, String> settings = new HashMap<>();
        settings.put(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, "0.9");
        settings.put(DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME, "0.8");
        settings.put(THRESHOLD_MINIMUM_SOURCE_RETAINED_ATTR_NAME, "10");
        configuration.setCustomizedDefaultSettings(settings);

        final Map<String, String> globalSettings = new HashMap<>();
        globalSettings.put(PA_KPI_SETTINGS,
                "{\"cellHandoverSuccessRate\": { \"enableKPI\": true, \"confidenceInterval\": \"99\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"initialAndAddedERabEstabSrQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"99.90\" }, \"eRabRetainabilityPercentageLostHourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"eRabRetainabilityPercentageLostQci1Hourly\": { \"enableKPI\": true, \"confidenceInterval\": \"97.50\", \"relevanceThreshold\": \"0.01\" }, \"avgDlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }, \"avgUlPdcpThroughputSector\": { \"enableKPI\": true, \"confidenceInterval\": \"99\" }}");
        configuration.setCustomizedGlobalSettings(globalSettings);

        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class))).thenReturn(1);
        objectUnderTest.updateSettingsOnUpgrade(configuration);

        assertThat(configuration.getCustomizedDefaultSettings()).contains(
                entry(UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME, "0.9"),
                entry(MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME, "5"),
                entry(PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD, "1.2"));
    }

    private static class ConfigurationBuilder {
        final private Configuration configuration = new Configuration();

        ConfigurationBuilder() {
            configuration.setName(DEFAULT);
            configuration.setSchedule("0 0 2 ? * * *");
            configuration.setEnabled(true);
            configuration.setOpenLoop(true);
        }

        ConfigurationBuilder withId(final int id) {
            configuration.setId(id);
            return this;
        }

        ConfigurationBuilder disabled() {
            configuration.setEnabled(false);
            return this;
        }

        ConfigurationBuilder withGroups(final List<CustomizedGroup> groups) {
            configuration.setGroups(groups);
            return this;
        }

        ConfigurationBuilder withCustomizedDefaultSettings(final Map<String, String> customizedDefaultSettings) {
            configuration.setCustomizedDefaultSettings(customizedDefaultSettings);
            return this;
        }

        ConfigurationBuilder withCustomizedGlobalSettings(final Map<String, String> customizedGlobalSettings) {
            configuration.setCustomizedGlobalSettings(customizedGlobalSettings);
            return this;
        }

        ConfigurationBuilder closedLoop() {
            configuration.setOpenLoop(false);
            return this;
        }

        ConfigurationBuilder withInclusionList(final List<Group> inclusionList) {
            configuration.setInclusionList(inclusionList);
            return this;
        }

        ConfigurationBuilder withExclusionList(final List<Group> exclusionList) {
            configuration.setExclusionList(exclusionList);
            return this;
        }

        ConfigurationBuilder withSaturdayWeekend() {
            configuration.setWeekendDays(SATURDAY);
            return this;
        }

        ConfigurationBuilder withPANotEnabled() {
            configuration.setEnablePA(false);
            return this;
        }

        public Configuration build() {
            return configuration;
        }
    }
}
