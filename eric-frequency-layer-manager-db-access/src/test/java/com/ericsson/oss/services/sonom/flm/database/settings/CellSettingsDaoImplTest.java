/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.database.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.SettingsHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;

/**
 * Unit tests for {@link CellSettingsDaoImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CellSettingsDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 2;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DatabaseAccess databaseAccessMock;

    @Mock
    private CellSettings cellSettingsMock;

    @InjectMocks
    private final CellSettingsDaoImpl objectUnderTest = new CellSettingsDaoImpl(MAX_RETRY_ATTEMPTS, 0);

    @Test
    public void whenInsertingCellSettings_thenAttributesExtractedInTheRightOrder() throws SQLException {
        final InOrder inOrder = inOrder(cellSettingsMock);
        final List<CellSettings> allCellSettings = Collections.singletonList(cellSettingsMock);

        when(cellSettingsMock.getId()).thenReturn(1L);
        when(cellSettingsMock.getOssId()).thenReturn(2);
        when(cellSettingsMock.getFdn()).thenReturn("fdn");
        when(cellSettingsMock.getExecutionId()).thenReturn("executionId");
        when(cellSettingsMock.getConfigurationId()).thenReturn(3);
        when(cellSettingsMock.getQosForCapacityEstimation()).thenReturn(4.0D);
        when(cellSettingsMock.getPercentileForMaxConnectedUser()).thenReturn(5.0D);
        when(cellSettingsMock.getMinNumCellForCdfCalculation()).thenReturn(6);
        when(cellSettingsMock.getTargetThroughputR()).thenReturn(7.0D);
        when(cellSettingsMock.getDeltaGfsOptimizationThreshold()).thenReturn(8.0D);
        when(cellSettingsMock.getTargetSourceCoverageBalanceRatioThreshold()).thenReturn(9.0D);
        when(cellSettingsMock.getSourceTargetSamplesOverlapThreshold()).thenReturn(10.0D);
        when(cellSettingsMock.getTargetSourceContiguityRatioThreshold()).thenReturn(11.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate()).thenReturn(12.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1()).thenReturn(13.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForErabPercentageLost()).thenReturn(14.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForErabPercentageLostForQci1()).thenReturn(15.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForCellHoSuccRate()).thenReturn(16.0D);
        when(cellSettingsMock.getLoadBalancingThresholdForCellAvailability()).thenReturn(17.0D);
        when(cellSettingsMock.getOptimizationSpeed()).thenReturn("optimizationSpeed");
        when(cellSettingsMock.getOptimizationSpeedFactorTable()).thenReturn("optimizationSpeedSpeedFactorTable");
        when(cellSettingsMock.getBandwidthToStepSizeTable()).thenReturn("bandwidthToStepSizeTable");
        when(cellSettingsMock.getloadBalancingThresholdForEndcUsers()).thenReturn(18.0D);
        when(cellSettingsMock.getNumCallsCellHourlyReliabilityThresholdInHours()).thenReturn(1);
        when(cellSettingsMock.getSyntheticCountersCellReliabilityThresholdInRops()).thenReturn(1);
        when(cellSettingsMock.getEssEnabled()).thenReturn(true);
        when(cellSettingsMock.getMinNumSamplesForTransientCalculation()).thenReturn(19);
        when(cellSettingsMock.getSigmaForTransientCalculation()).thenReturn(20);
        when(cellSettingsMock.getUlPuschSinrRatioThreshold()).thenReturn(0.8D);
        when(cellSettingsMock.getMinTargetUlPuschSinr()).thenReturn(5);
        when(cellSettingsMock.getPercentageBadRsrpRatioThreshold()).thenReturn(1.2D);
        when(cellSettingsMock.getMinConnectedUsers()).thenReturn(10);
        when(cellSettingsMock.getExclusionList()).thenReturn("exclusionList");
        when(cellSettingsMock.getMinimumSourceRetained()).thenReturn(20);
        when(cellSettingsMock.getMinRopsForAppCovReliability()).thenReturn(21);
        when(cellSettingsMock.getMinNumCqiSamples()).thenReturn(22);
        when(cellSettingsMock.getSectorId()).thenReturn(23L);

        when(databaseAccessMock.executeBatchInsert(anyString(), anyList())).thenReturn(1);

        objectUnderTest.insertCellSettings(allCellSettings);

        verify(databaseAccessMock).executeBatchInsert(anyString(), anyList());

        inOrder.verify(cellSettingsMock).getId();
        inOrder.verify(cellSettingsMock).getOssId();
        inOrder.verify(cellSettingsMock).getFdn();
        inOrder.verify(cellSettingsMock).getExecutionId();
        inOrder.verify(cellSettingsMock).getConfigurationId();
        inOrder.verify(cellSettingsMock).getQosForCapacityEstimation();
        inOrder.verify(cellSettingsMock).getPercentileForMaxConnectedUser();
        inOrder.verify(cellSettingsMock).getMinNumCellForCdfCalculation();
        inOrder.verify(cellSettingsMock).getTargetThroughputR();
        inOrder.verify(cellSettingsMock).getDeltaGfsOptimizationThreshold();
        inOrder.verify(cellSettingsMock).getTargetSourceCoverageBalanceRatioThreshold();
        inOrder.verify(cellSettingsMock).getSourceTargetSamplesOverlapThreshold();
        inOrder.verify(cellSettingsMock).getTargetSourceContiguityRatioThreshold();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForErabPercentageLost();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForErabPercentageLostForQci1();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForCellHoSuccRate();
        inOrder.verify(cellSettingsMock).getLoadBalancingThresholdForCellAvailability();
        inOrder.verify(cellSettingsMock).getOptimizationSpeed();
        inOrder.verify(cellSettingsMock).getOptimizationSpeedFactorTable();
        inOrder.verify(cellSettingsMock).getBandwidthToStepSizeTable();
        inOrder.verify(cellSettingsMock).getloadBalancingThresholdForEndcUsers();
        inOrder.verify(cellSettingsMock).getNumCallsCellHourlyReliabilityThresholdInHours();
        inOrder.verify(cellSettingsMock).getSyntheticCountersCellReliabilityThresholdInRops();
        inOrder.verify(cellSettingsMock).getEssEnabled();
        inOrder.verify(cellSettingsMock).getMinNumSamplesForTransientCalculation();
        inOrder.verify(cellSettingsMock).getSigmaForTransientCalculation();
        inOrder.verify(cellSettingsMock).getUlPuschSinrRatioThreshold();
        inOrder.verify(cellSettingsMock).getMinTargetUlPuschSinr();
        inOrder.verify(cellSettingsMock).getPercentageBadRsrpRatioThreshold();
        inOrder.verify(cellSettingsMock).getMinConnectedUsers();
        inOrder.verify(cellSettingsMock).getExclusionList();
        inOrder.verify(cellSettingsMock).getMinimumSourceRetained();
        inOrder.verify(cellSettingsMock).getMinRopsForAppCovReliability();
        inOrder.verify(cellSettingsMock).getMinNumCqiSamples();
        inOrder.verify(cellSettingsMock).getSectorId();
        Mockito.verifyNoMoreInteractions(cellSettingsMock);
    }

    @Test
    public void whenExceptionHappensOnInsertingCellSettings_thenRetry() throws SQLException {
        final Logger loggerMock = mock(Logger.class);
        Whitebox.setInternalState(CellSettingsDaoImpl.class, "LOGGER", loggerMock);

        final List<CellSettings> allCellSettings = Collections.singletonList(new CellSettings());

        when(databaseAccessMock.executeBatchInsert(anyString(), anyList())).thenThrow(SQLException.class)
                .thenReturn(1);

        objectUnderTest.insertCellSettings(allCellSettings);

        verify(databaseAccessMock, times(MAX_RETRY_ATTEMPTS)).executeBatchInsert(anyString(), anyList());
        verify(loggerMock).warn(eq("Failed to execute insert : {}"), anyString(), any());
    }

    @Test
    public void whenRetrieveNamedSetting_thenRetrieveSetting() throws SQLException {
        final String expectedQuery =
                "SELECT settingsName FROM cell_configuration WHERE execution_id = 'executionId' AND oss_id = 999 AND fdn = 'FDN'";
        final String value = "value";
        when(databaseAccessMock.executeQuery(eq(expectedQuery), any(SettingsHandler.class), any(PreparedStatementHandler.class)))
                .thenReturn(value);

        assertThat(objectUnderTest.retrieveNamedSetting("settingsName", 999, "FDN", "executionId"))
                .isEqualTo(value);
    }

    @Test
    public void whenExceptionHappensOnRetrievingNamedSetting_thenThrowSQLException() throws SQLException {
        final Exception expected = new ArrayIndexOutOfBoundsException("test");
        when(databaseAccessMock.executeQuery(anyString(), any(SettingsHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(expected);

        thrown.expect(SQLException.class);
        thrown.expectCause(is(expected));

        objectUnderTest.retrieveNamedSetting("settingsName", 999, "FDN", "executionId");
    }

    @Test
    public void whenGetSettingsForCellPerFlmExecution_thenReturnSetting() throws SQLException {
        final String expectedQuery = "SELECT fdn, oss_id, execution_id, qos_for_capacity_estimation, percentile_for_max_connected_user, " +
                "min_num_cell_for_cdf_calculation, target_throughput_r, delta_gfs_optimization_threshold, " +
                "target_source_coverage_balance_ratio_threshold, source_target_samples_overlap_threshold, " +
                "target_source_contiguity_ratio_threshold, lb_threshold_for_initial_erab_estab_succ_rate, " +
                "lb_threshold_for_initial_erab_estab_succ_rate_for_qci1, lb_threshold_for_erab_percentage_lost, " +
                "lb_threshold_for_erab_percentage_lost_for_qci1, lb_threshold_for_cell_ho_succ_rate, lb_threshold_for_cell_availability, " +
                "optimization_speed, optimization_speed_factor_table, bandwidth_to_step_size_table, " +
                "lb_threshold_for_endc_users, num_calls_cell_hourly_reliability_threshold_in_hours, " +
                "synthetic_counters_cell_reliability_threshold_in_rops, ess_enabled, min_num_samples_for_transient_calculation, " +
                "sigma_for_transient_calculation, uplink_pusch_sinr_ratio_threshold, min_target_uplink_pusch_sinr, " +
                "percentage_bad_rsrp_ratio_threshold, min_connected_users, " +
                "exclusion_list FROM cell_configuration WHERE execution_id = 'executionId'";
        final Map settingsMap = mock(Map.class);
        when(databaseAccessMock.executeQuery(eq(expectedQuery), any(ResultHandler.class), any(PreparedStatementHandler.class)))
                .thenReturn(settingsMap);
        assertThat(objectUnderTest.getSettingsForCellPerFlmExecution("executionId")).isSameAs(settingsMap);
    }

    @Test
    public void whenExceptionThrownOnGetSettingsForCellPerFlmExecution_thenThrowSqlException() throws SQLException {
        final Exception expected = new IllegalStateException("test");
        when(databaseAccessMock.executeQuery(anyString(), any(ResultHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(expected);

        thrown.expect(SQLException.class);
        thrown.expectCause(is(expected));
        objectUnderTest.getSettingsForCellPerFlmExecution("id");
    }
}
