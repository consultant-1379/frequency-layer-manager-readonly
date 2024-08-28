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
package com.ericsson.oss.services.sonom.flm.database.optimization;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbCommands;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDbConstants;
import com.ericsson.oss.services.sonom.flm.database.flm.FlmServiceUnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.runner.UnitTestDatabaseRunner;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbCommands;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit test for {@link OptimizationsWithOverlapInfoDaoImpl} class.
 */
@RunWith(OrderedTestRunner.class)
public class OptimizationsWithOverlapInfoDaoImplTest {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final OptimizationsWithOverlapInfoDao OVERLAP_INFO_DAO_WITH_H2_DB = new OptimizationsWithOverlapInfoDaoImpl(MAX_RETRY_ATTEMPTS,
            WAIT_PERIOD_IN_SECONDS);
    private static final UnitTestDatabaseRunner UNIT_TEST_DATABASE_RUNNER = new FlmServiceUnitTestDatabaseRunner();
    private static final String EXECUTION_ID = "FLM_2";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() throws SQLException {
        dropAndCreateTables();
        insertTestData();
    }

    @Test
    public void whenOptimizationsWithOverlapInfoAreRetrieved_thenOverlapInfoReturned() throws SQLException {
        final List<Pair<PolicyOutputEvent, OverlapInfo>> result = OVERLAP_INFO_DAO_WITH_H2_DB.getOptimizationsWithOverlapInfo(EXECUTION_ID);

        assertThat(result).hasSize(5);

        final OverlapInfo pair1Value = assertKeyHasSectorIdAndReturnValue(result.get(0), 11111);
        softly.assertThat(pair1Value.getOverlappingExecutions()).isEqualTo("FLM_4");
        softly.assertThat(pair1Value.getOverlappingExecutions()).doesNotContain("FLM_8");
        softly.assertThat(pair1Value.getOverlappingFlag()).isEqualTo(OverlapInfo.OverlappingFlag.OVERLAP_LOG_NEEDED);

        final OverlapInfo pair2value = assertKeyHasSectorIdAndReturnValue(result.get(1), 12345);
        softly.assertThat(pair2value.getOverlappingExecutions()).isEqualTo("FLM_1,FLM_4,FLM_5");
        softly.assertThat(pair2value.getOverlappingExecutions()).doesNotContain("FLM_8,FLM_9");
        softly.assertThat(pair2value.getOverlappingFlag()).isEqualTo(OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED);

        final OverlapInfo pair3value = assertKeyHasSectorIdAndReturnValue(result.get(2), 66666);
        softly.assertThat(pair3value.getOverlappingExecutions()).isNull();
        softly.assertThat(pair3value.getOverlappingFlag()).isEqualTo(OverlapInfo.OverlappingFlag.NOT_OVERLAPPING);

        final OverlapInfo pair4value = assertKeyHasSectorIdAndReturnValue(result.get(3), 66667);
        softly.assertThat(pair4value.getOverlappingExecutions()).isNull();
        softly.assertThat(pair4value.getOverlappingFlag()).isEqualTo(OverlapInfo.OverlappingFlag.NOT_OVERLAPPING);

        final OverlapInfo pair5value = assertKeyHasSectorIdAndReturnValue(result.get(4), 98765);
        softly.assertThat(pair5value.getOverlappingExecutions()).isEqualTo("FLM_3,FLM_4");
        softly.assertThat(pair5value.getOverlappingFlag()).isEqualTo(OverlapInfo.OverlappingFlag.OVERLAP_DROP_NEEDED);
    }

    private OverlapInfo assertKeyHasSectorIdAndReturnValue(final Pair<PolicyOutputEvent, OverlapInfo> pair, final int sectorId) {
        final PolicyOutputEvent pairKey = pair.getKey();
        softly.assertThat(pairKey.getSectorId()).isEqualTo(sectorId);
        return pair.getValue();
    }

    private void insertTestData() {
        insertExecutions();
        insertOptimizations();
        insertCellConfigurations();
    }

    private void dropAndCreateTables() {
        dropTables();
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.createExecutionTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(OptimizationsDbCommands.createOptimizationTable());
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(CellSettingsDbCommands.createTable(CellSettingsDbConstants.CELL_CONFIGURATION));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(CellSettingsDbCommands.createTable(CellSettingsDbConstants.CELL_CONFIGURATION_HISTORY));
    }

    private void dropTables() {
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(ExecutionDbConstants.FLM_EXECUTIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(OptimizationsDbConstants.FLM_OPTIMIZATIONS));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(CellSettingsDbConstants.CELL_CONFIGURATION));
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(ExecutionDbCommands.dropTable(CellSettingsDbConstants.CELL_CONFIGURATION_HISTORY));
    }

    private void insertOptimizations() {
        // Test data
        // Sector    Scenario
        // ------    --------
        // 11111     Overlaps with execution FLM_4 within 23hr window. Included in exclusion_list for FLM_8.
        // 12345     Overlaps with execution FLM_1 within 15min window and FLM_4,FLM_5 within 23hr window. Included in exclusion_list for FLM_9, FLM_8.
        // 66666     Does not overlap but is in the historical DB for FLM_7 outside of the 23hr window
        // 66667     Does not overlap but is in the configuration settings DB for FLM_7 outside of the 23hr window
        // 98765     Overlaps with execution FLM_3 within 15min window and FLM4 with 23hr window
        final String query = "INSERT INTO public.flm_optimizations (execution_id,sector_id,lbq,created) VALUES\n" +
                "\t ('FLM_2',12345,'{\"lbq\":\"1\"}','2021-05-17 12:02:19.562'),\n" +
                "\t ('FLM_2',98765,'{\"lbq\":\"2\"}','2021-05-17 12:02:19.786'),\n" +
                "\t ('FLM_1',55555,'{\"lbq\":\"3\"}','2021-05-17 12:02:19.822'),\n" +
                "\t ('FLM_2',66666,'{\"lbq\":\"4\"}','2021-05-17 12:02:19.853'),\n" +
                "\t ('FLM_2',66667,'{\"lbq\":\"5\"}','2021-05-17 12:02:19.854'),\n" +
                "\t ('FLM_2',11111,'{\"lbq\":\"6\"}','2021-05-17 12:02:19.874'),\n" +
                "\t ('FLM_1',11111,'{\"sourceCellFdn\":\"\",\"sourceCellOssId\":-1,\"sourceUsersMove\":\"\"," +
                "\"targetCells\":[{\"targetCellFdn\":\"\",\"targetCellOssId\":-1,\"targetUsersMove\":\"\"}]}','2021-05-17 12:02:19.874');";

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(Collections.singletonList(query));
    }

    @Test
    public void whenTablesCannotBeRead_thenExceptionIsThrown() throws SQLException {
        dropTables();
        thrown.expect(SQLException.class);
        OVERLAP_INFO_DAO_WITH_H2_DB.getOptimizationsWithOverlapInfo(EXECUTION_ID);
    }

    private void insertExecutions() {
        final String query = "INSERT INTO public.flm_executions (id,configuration_id,start_time,state,state_modified_time," +
                "additional_execution_information,schedule,retry_attempts,calculation_id,customized_global_settings," +
                "customized_default_settings,\"groups\",num_sectors_to_evaluate_for_optimization,num_optimization_elements_sent," +
                "num_optimization_elements_received,num_optimization_lbqs,num_changes_written_to_cm_db," +
                "num_changes_not_written_to_cm_db,open_loop,inclusion_list,exclusion_list,weekend_days,enable_pa,full_execution) VALUES\n" +
                "\t ('FLM_1',1,'2021-04-21 10:00:00','SUCCEEDED','2021-04-21 12:02:19.376','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_2',1,'2021-04-21 10:05:00','LOAD_BALANCING','2021-04-21 12:02:19.787','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_3',1,'2021-04-21 10:10:00','SUCCEEDED','2021-04-21 12:12:19.829','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_4',1,'2021-04-21 11:00:00','SUCCEEDED','2021-04-21 12:58:19.410','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_5',1,'2021-04-20 11:10:00','SUCCEEDED','2021-04-20 13:06:19.948','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_6',1,'2021-04-19 11:10:00','SUCCEEDED','2021-04-19 13:07:19.323','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_7',1,'2021-04-19 11:00:00','SUCCEEDED','2021-04-19 12:02:19.647','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_8',1,'2021-04-21 11:05:00','KPI_PROCESSING_GROUP_1','2021-04-21 12:02:19.647','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,false,'[]','[]','Monday',false,false),\n"
                + "\t ('FLM_9',1,'2021-04-20 11:10:00','SUCCEEDED','2021-04-20 12:02:19.647','','0 0 0 1 1 ? 2200',3,'calcId','null','null','null',11112,11111,232,232,232,0,true,'[]','[]','Monday',false,false);";

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(Collections.singletonList(query));
    }

    private void insertCellConfigurations() {
        final String query = "INSERT INTO public.cell_configuration (id,oss_id,fdn,execution_id,configuration_id,qos_for_capacity_estimation," +
                "percentile_for_max_connected_user,min_num_cell_for_cdf_calculation,target_throughput_r,delta_gfs_optimization_threshold," +
                "target_source_coverage_balance_ratio_threshold,source_target_samples_overlap_threshold,target_source_contiguity_ratio_threshold," +
                "lb_threshold_for_initial_erab_estab_succ_rate,lb_threshold_for_initial_erab_estab_succ_rate_for_qci1," +
                "lb_threshold_for_erab_percentage_lost,lb_threshold_for_erab_percentage_lost_for_qci1,lb_threshold_for_cell_ho_succ_rate," +
                "lb_threshold_for_cell_availability,optimization_speed,optimization_speed_factor_table,bandwidth_to_step_size_table," +
                "lb_threshold_for_endc_users,ess_enabled,num_calls_cell_hourly_reliability_threshold_in_hours,synthetic_counters_cell_reliability_threshold_in_rops,exclusion_list,min_rops_for_app_cov_reliability,min_num_cqi_samples,"
                +
                "min_num_samples_for_transient_calculation,sigma_for_transient_calculation,sector_id) " +
                "VALUES\n" +
                "\t (1,23,'fdn','FLM_1',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (2,23,'fdn','FLM_1',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (3,23,'fdn','FLM_1',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,56789),\n"
                + "\t (4,23,'fdn','FLM_1',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,56789),\n"
                + "\t (5,23,'fdn','FLM_2',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (6,23,'fdn','FLM_2',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (7,23,'fdn','FLM_2',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (8,23,'fdn','FLM_2',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (9,23,'fdn','FLM_3',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12121),\n"
                + "\t (10,23,'fdn','FLM_3',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12121),\n"
                + "\t (11,23,'fdn','FLM_3',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (12,23,'fdn','FLM_3',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (13,23,'fdn','FLM_4',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (14,23,'fdn','FLM_4',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (15,23,'fdn','FLM_4',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (16,23,'fdn','FLM_4',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (17,23,'fdn','FLM_4',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,11111),\n"
                + "\t (18,23,'fdn','FLM_8',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,'exclusion_group_1',18,19,20,21,11111),\n"
                + "\t (19,23,'fdn','FLM_8',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,'exclusion_group_1',18,19,20,21,12345);";

        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(Collections.singletonList(query));
        final String historyQuery = "INSERT INTO public.cell_configuration_history\n" +
                "(id, oss_id, fdn, execution_id, configuration_id, qos_for_capacity_estimation, " +
                "percentile_for_max_connected_user, min_num_cell_for_cdf_calculation, target_throughput_r, " +
                "delta_gfs_optimization_threshold, target_source_coverage_balance_ratio_threshold, " +
                "source_target_samples_overlap_threshold, target_source_contiguity_ratio_threshold, " +
                "lb_threshold_for_initial_erab_estab_succ_rate, lb_threshold_for_initial_erab_estab_succ_rate_for_qci1, " +
                "lb_threshold_for_erab_percentage_lost, lb_threshold_for_erab_percentage_lost_for_qci1, " +
                "lb_threshold_for_cell_ho_succ_rate, lb_threshold_for_cell_availability, optimization_speed, " +
                "optimization_speed_factor_table, bandwidth_to_step_size_table, lb_threshold_for_endc_users, " +
                "ess_enabled, num_calls_cell_hourly_reliability_threshold_in_hours, synthetic_counters_cell_reliability_threshold_in_rops, exclusion_list, min_rops_for_app_cov_reliability, min_num_cqi_samples, "
                +
                "min_num_samples_for_transient_calculation, sigma_for_transient_calculation, sector_id)\n" +
                "VALUES" +
                "\t (1,23,'fdn','FLM_5',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (2,23,'fdn','FLM_5',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (3,23,'fdn','FLM_5',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,56789),\n"
                + "\t (4,23,'fdn','FLM_5',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,56789),\n"
                + "\t (5,23,'fdn','FLM_6',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (6,23,'fdn','FLM_6',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,12345),\n"
                + "\t (7,23,'fdn','FLM_6',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (8,23,'fdn','FLM_6',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,98765),\n"
                + "\t (9,23,'fdn','FLM_7',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,NULL,18,19,20,21,66666),\n"
                + "\t (10,23,'fdn','FLM_9',24,1.0,2,3,6.0,7.0,8.0,9.0,10.0,11.0,12.0,13.0,14.0,15.0,16.0,'speed','table','table',22.0,false,1,1,'exclusion_group_1',18,19,20,21,12345);";
        UNIT_TEST_DATABASE_RUNNER.executeSqlCommands(Collections.singletonList(historyQuery));
    }

}