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
package com.ericsson.oss.services.sonom.flm.test;

import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static com.ericsson.oss.services.sonom.common.test.util.IntegrationTestUtils.sleep;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmOptimizationsVerifier.assertCountOfFlmOptimizations;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmOptimizationsVerifier.assertSentAndReceivedMessagesCountIsEqual;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.CELL_GUID_FLM_60_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.CELL_SECTOR_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.CELL_SECTOR_FLM_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.ContentDatasource;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.DynamicContentDataSource;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_GUID_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_GUID_FLM_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_GUID_FLM_60_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_SECTOR_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_SECTOR_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_CELL_SECTOR_FLM_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_FREQBAND_BANDWIDTH_TARGET_THROUGHPUT_R_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_SECTOR_1440_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.KPI_SECTOR_ASSERTION_FILE;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.TableSource;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.ValueMapBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.JUnitSoftAssertions;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.TestExecutionTimeLogger;
import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.KpiDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.test.util.ConfigurationBuilder;
import com.ericsson.oss.services.sonom.flm.test.util.ExclusionLogParser;
import com.ericsson.oss.services.sonom.flm.test.util.ScheduledKpiCalculationHelper;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.ericsson.oss.services.sonom.flm.test.verification.CellConfigurationHistoryVerifier;
import com.ericsson.oss.services.sonom.flm.test.verification.CellConfigurationViewVerifier;
import com.ericsson.oss.services.sonom.flm.test.verification.CellSettingsVerifier;
import com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier;
import com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.ValueMap;
import com.ericsson.oss.services.sonom.flm.test.verification.ScheduledKpisVerifier;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@RunWith(OrderedTestRunner.class)
public class FlmAlgorithmIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlmAlgorithmIT.class);
    private static final Gson GSON = new Gson();
    private static final String SCHEME_AND_AUTHORITY = "http://" + ServiceHostnameAndPortProvider.getFlmAlgorithmHostnameAndPort();
    private static final String CONTEXT_ROOT = "/son-om/algorithms/flm/v1";
    private static final String BASE_URI = SCHEME_AND_AUTHORITY + CONTEXT_ROOT;
    private static final String CONFIGURATIONS = "/configurations";
    private static final String EXECUTIONS = "/executions";
    private static final String EXECUTION_ID_INVALID = "Invalid id, no execution found in database for requested id";
    private static final String USER_MESSAGE = "userMessage";
    private static final String DEVELOPER_MESSAGE = "developerMessage";
    private static final String ID_DOES_NOT_EXIST = "No execution exists in the database for requested id";
    private static final Set<String> COMPLETED_EXECUTION_IDS = new HashSet<>();
    private static final int DEFAULT_CONFIGURATION_ID = 1;
    private static final long CONFIGURATION_SCHEDULE_OFFSET = TimeUnit.SECONDS.toSeconds(5);
    private static final String MULTISECTOR_CELL_EXCLUSION_MESSAGE_SECOND_PART = "Cell_ID: SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054," +
            "MeContext=054004_CROWN_PALMFRONT,ManagedElement=054004_CROWN_PALMFRONT,ENodeBFunction=1,EUtranCellFDD=054004_1," +
            " Exclusion_Reason: Cell excluded from optimization due to presence in multiple sectors: 1554,370";
    private static final String EXECUTION_ID = "Execution_ID: ";
    private static final String SECTOR_ID = "Sector_ID: ";
    private static final String OSS_ID = "Oss_ID: 1";
    private static final String CELL_ID = "Cell_ID: ";
    private static final String EXCLUSION_REASON = "Exclusion_Reason: ";

    private static final String CM_CHANGE_ENDPOINT = "http://" + ServiceHostnameAndPortProvider.getCmServiceHostnameAndPort()
            + "/son-om/cm-change/v1" + "/changes";

    private static final int NUMBER_OF_CHANGES = 1;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    @Rule
    public TestExecutionTimeLogger testRuleLogTestRunData = new TestExecutionTimeLogger(System.out);

    @Test
    @InSequence(1)
    public void whenFlmAlgorithmIsTriggered_thenFlmExecutesSuccessfully() {
        final Configuration configuration = createConfiguration();

        final Response putResponse = ClientBuilder.newClient()
                .target(getFlmServiceUri(CONFIGURATIONS + '/' + DEFAULT_CONFIGURATION_ID))
                .request()
                .put(Entity.json(GSON.toJson(configuration)));
        assertThat(putResponse).isOK();
        LOGGER.info("Execution was successfully scheduled.");

        assertTrue("Timed out waiting for execution", waitForSuccessfulExecution());

        final String executionId = getExecutionIdFromTestExecution();
        assertNotNull(executionId);
        LOGGER.info("Execution with id {} finished.", executionId);

        verifyKpis(executionId);
        verifyFlmData();
    }

    @Test
    @InSequence(2)
    public void whenFlmAlgorithmIsTriggered_thenConfigurationSnapshotIsCreated() {
        LOGGER.info("Verifying that a started execution creates a snapshot of the configuration which triggered it.");
        final JsonObject execution = getExecutionFromTestExecution();
        assertNotNull(execution);
        final JsonObject configuration = getCorrespondingConfiguration(execution);

        assertEquals(configuration.get("customizedGlobalSettings").toString(), execution.get("customizedGlobalSettings").toString());
        assertEquals(configuration.get("customizedDefaultSettings").toString(), execution.get("customizedDefaultSettings").toString());
        assertEquals(configuration.get("groups").toString(), execution.get("groups").toString());
        assertEquals(configuration.get("openLoop").getAsBoolean(), execution.get("openLoop").getAsBoolean());
        assertEquals(configuration.get("inclusionList").toString(), execution.get("inclusionList").toString());
        assertEquals(configuration.get("exclusionList").toString(), execution.get("exclusionList").toString());
        assertEquals(configuration.get("weekendDays").toString(), execution.get("weekendDays").toString());
        assertEquals(configuration.get("enablePA").getAsBoolean(), execution.get("enablePA").getAsBoolean());
    }

    @Test
    @InSequence(3)
    public void whenFlmAlgorithmIsTriggered_thenOptimizationChangeElementsCanBeFoundInDataTable() {
        final String executionIdFromTestExecution = getExecutionIdFromTestExecution();
        final Response response = ClientBuilder.newClient()
                .target(CM_CHANGE_ENDPOINT + "?executionId=" + executionIdFromTestExecution)
                .request(MediaType.APPLICATION_JSON)
                .get();

        final List<ChangeElement> listOfOptimizationChangeElements = ((List<ChangeElement>) GSON.fromJson(response.readEntity(String.class),
                new TypeToken<ArrayList<ChangeElement>>() {
                }.getType()))
                        .stream()
                        .filter(changeElement -> changeElement.getChangeType() == ChangeElement.ChangeType.OPTIMIZATION)
                        .collect(Collectors.toList());
        Assertions.assertThat(listOfOptimizationChangeElements.size()).isEqualTo(NUMBER_OF_CHANGES);
        Assertions.assertThat(listOfOptimizationChangeElements.get(0).getExecutionId()).isEqualTo(executionIdFromTestExecution);
    }

    @Test
    @InSequence(4)
    public void whenGetAllExecutions_thenReturns200Ok() {
        LOGGER.info("Verifying that execution is in the database");
        final Response response = httpGetRequest(getFlmServiceUri(EXECUTIONS));
        assertThat(response).isOK();
        final JsonArray executionArray = deserializeFromResponse(response, JsonArray.class);
        assertThat(executionArray).hasSize(3);
    }

    @Test
    @InSequence(5)
    public void whenGetExecutionById_thenSingleExecutionReturned() {
        LOGGER.info("Verifying that execution is in the database by ID");
        final String executionId = getExecutionIdFromTestExecution();
        assertNotNull(executionId);

        final Response getResponse = httpGetRequest(getFlmServiceUri(EXECUTIONS + "/" + executionId));
        assertThat(getResponse).isOK();
        final JsonObject execution = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(execution.get("id").getAsString()).isEqualTo(executionId);
    }

    @Test
    @InSequence(6)
    public void whenFlmOptimizationFinished_thenSentElementCountMatchesReceivedElementCount() {
        LOGGER.info("Verifying that the number of optimization elements sent matches the number of optimization " +
                "elements received");
        final String executionId = getExecutionIdFromTestExecution();
        assertNotNull(executionId);

        assertSentAndReceivedMessagesCountIsEqual(executionId);
        LOGGER.info("Optimization elements sent matches the number of optimization element received verified");
    }

    @Test
    @InSequence(7)
    public void whenFlmOptimizationFinished_thenReceivedElementCountEqualsFlmOptimizationsTableElementCount() {
        LOGGER.info("Verifying that the number of optimization elements received matches count of the elements in " +
                "flm_optimizations table");
        final String executionId = getExecutionIdFromTestExecution();
        assertNotNull(executionId);

        assertCountOfFlmOptimizations(executionId);
        LOGGER.info("DB count matching received element count verified");
    }

    @Test
    @InSequence(8)
    public void whenIdDoesNotExist_thenExecutionByIdReturnsNotFound() {
        LOGGER.info("Verifying that there is no execution with ID 678");
        final Response getResponse = httpGetRequest(getFlmServiceUri(EXECUTIONS + "/678"));
        assertThat(getResponse).hasStatusCode(HttpStatus.SC_NOT_FOUND);
        final JsonObject response = deserializeFromResponse(getResponse, JsonObject.class);
        assertThat(response.get(USER_MESSAGE).getAsString()).isEqualTo(EXECUTION_ID_INVALID);
        assertThat(response.get(DEVELOPER_MESSAGE).getAsString()).isEqualTo(ID_DOES_NOT_EXIST);
    }

    @Test
    @InSequence(9)
    public void whenScheduledKpisAreExecutedKpisAreCalculatedCorrectly() throws ParseException, IOException, KpiModelVerificationException {
        deleteKpiCellSector60Data();

        final List<String> everyFifteenMinutesScheduledKpis = Arrays.asList(
                "avg_dl_pdcp_throughput_cell",
                "avg_ul_pdcp_ue_throughput_cell",
                "cell_exe_att",
                "cell_exe_succ",
                "cell_handover_success_rate",
                "cell_prep_att",
                "cell_prep_succ",
                "cgi",
                "connected_endc_users_hourly",
                "connected_users_hourly",
                "contiguity",
                "counter_cell_reliability_daily",
                "dl_pdcp_ue_throughput_den_cell",
                "dl_pdcp_ue_throughput_num_cell",
                "e_rab_retainability_percentage_lost",
                "e_rab_retainability_percentage_lost_qci1",
                "endc_spid115_ues",
                "initial_and_added_e_rab_establishment_sr",
                "initial_and_added_e_rab_establishment_sr_for_qci1",
                "intra_site",
                "num_calls_cell_hourly",
                "num_calls_cell_sector_hourly",
                "num_calls_daily",
                "num_calls_sector_hourly",
                "num_non_null_counters_count_app_coverage",
                "num_non_null_counters_count_pmactiveuedlsum",
                "num_non_null_counters_count_pmactiveuedlsumqci",
                "num_non_null_counters_count_pmpdcchcceactivity",
                "num_non_null_counters_count_pmpdcchcceutil",
                "num_non_null_counters_count_pmprbavaildl",
                "num_non_null_counters_count_pmprbuseddldtchfirsttransqci",
                "num_non_null_counters_count_pmprbuseddlfirsttrans",
                "num_non_null_counters_count_pmprbuseddlretrans",
                "num_non_null_counters_count_pmradiothpvoldl",
                "num_non_null_counters_count_pmradiothpvoldlscell",
                "num_non_null_counters_count_pmradiotxrankdistr",
                "num_non_null_counters_count_pmradiouerepcqidistr",
                "num_non_null_counters_count_pmradiouerepcqidistr2",
                "num_non_null_counters_count_pmrrcconnlevsamp",
                "num_non_null_counters_count_pmrrcconnlevsum",
                "out_succ_ho_interf",
                "out_succ_ho_intraf",
                "per_endc_den",
                "percentage_endc_users",
                "pm_erab_estab_att_added",
                "pm_erab_estab_att_added_ho_ongoing",
                "pm_erab_estab_att_added_ho_ongoing_qci1",
                "pm_erab_estab_att_added_qci1",
                "pm_erab_estab_att_init",
                "pm_erab_estab_att_init_qci1",
                "pm_erab_estab_succ_added",
                "pm_erab_estab_succ_added_qci1",
                "pm_erab_estab_succ_init_qci1",
                "pm_erab_rel_abnormal_enb",
                "pm_erab_rel_abnormal_enb_act",
                "pm_erab_rel_abnormal_enb_act_qci1",
                "pm_erab_rel_abnormal_enb_qci1",
                "pm_erab_rel_abnormal_mme_act",
                "pm_erab_rel_abnormal_mme_act_qci1",
                "pm_erab_rel_mme",
                "pm_erab_rel_mme_qci1",
                "pm_erab_rel_normal_enb",
                "pm_erab_rel_normal_enb_qci1",
                "pm_flex_rrc_conn_sum",
                "pm_flex_rrc_conn_sum_spid",
                "pm_rrc_conn_estab_att",
                "pm_rrc_conn_estab_att_reatt",
                "pm_rrc_conn_estab_fail_mme_ovl_mod",
                "pm_rrc_conn_estab_fail_mme_ovl_mos",
                "pm_rrc_conn_estab_succ",
                "pm_rrc_conn_lev_samp",
                "pm_rrc_conn_lev_sum",
                "pm_rrc_conn_samp",
                "pm_s1_sig_conn_estab_att",
                "pm_s1_sig_conn_estab_fail_mme_ovl_mos",
                "pm_s1_sig_conn_estab_succ",
                "rrc_conn_estab_att",
                "rrc_conn_estab_att_int",
                "rrc_conn_estab_att_reatt",
                "rrc_conn_estab_succ",
                "s1_sig_conn_estab_att",
                "s1_sig_conn_estab_succ",
                "source_cell_cgi_daily",
                "source_fdn",
                "ul_pdcp_ue_throughput_den_cell",
                "ul_pdcp_ue_throughput_num_cell");

        final List<String> onceHourlyScheduledKpis = Arrays.asList(
                "achievable_throughput_dist",
                "active_ues_dl",
                "active_ues_mbb",
                "active_ues_volte_dl",
                "available_prbs",
                "available_prbs_per_user",
                "avg_dl_pdcp_throughput_sector",
                "avg_dl_pdcp_throughput_sector_rss",
                "avg_ul_pdcp_throughput_sector",
                "avg_ul_pdcp_throughput_sector_rss",
                "bandwidth",
                "ca_adjustment_factor",
                "cce_per_subframe",
                "cell_exe_att_hourly",
                "cell_exe_succ_hourly",
                "cell_handover_success_rate_hourly",
                "cell_handover_success_rate_hourly_rss",
                "cell_prep_att_hourly",
                "cell_prep_succ_hourly",
                "connected_users",
                "counter_cell_reliability_hourly",
                "cqi",
                "cqi1",
                "cqi2",
                "cqi_sum",
                "dl_pdcp_ue_throughput_den_sector",
                "dl_pdcp_ue_throughput_num_sector",
                "dlprbstot",
                "double_count_factor_radio_tx_rank_distr",
                "e_rab_retainability_percentage_lost_hourly",
                "e_rab_retainability_percentage_lost_hourly_den",
                "e_rab_retainability_percentage_lost_hourly_num",
                "e_rab_retainability_percentage_lost_hourly_rss",
                "e_rab_retainability_percentage_lost_qci1_den",
                "e_rab_retainability_percentage_lost_qci1_hourly",
                "e_rab_retainability_percentage_lost_qci1_hourly_rss",
                "e_rab_retainability_percentage_lost_qci1_num",
                "erab_estab_att_hourly",
                "erab_estab_att_qci1_hourly",
                "erab_estab_succ_hourly",
                "erab_estab_succ_qci1_hourly",
                "freq_band",
                "initial_and_added_e_rab_establishment_sr_for_qci1_hourly",
                "initial_and_added_e_rab_establishment_sr_hourly",
                "initial_and_added_e_rab_establishment_sr_hourly_rss",
                "initial_and_added_e_rab_establishment_sr_hourly_rss",
                "initial_and_added_e_rab_establishment_sr_qci1_hourly_rss",
                "initial_and_added_e_rab_establishment_sr_qci1_hourly_rss",
                "interim_kpi_prbs_and_elements_per_user",
                "mac_level_data_vol_dl_rate",
                "no_of_trx_points",
                "num_active_ue_dl_sum_qci",
                "pdcch_cfi_mode",
                "pdcchcceutil",
                "pm_rrc_conn_estab_succ_hourly",
                "pm_s1_sig_conn_estab_succ_hourly",
                "pm_ue_throughput_time_dl",
                "pm_ue_throughput_time_ul",
                "pm_ue_throughput_vol_minus_lasttti_dl",
                "pm_ue_throughput_vol_ul",
                "pmpdcchcceactivity_sum_den",
                "pmpdcchcceutil_0_19_distribution_product",
                "pmpdcchcceutil_0_norm",
                "pmpdcchcceutil_1_19_distribution_product",
                "prb_pairs_available",
                "qci_samples_per_rop",
                "radio_tx_rank_distr",
                "resource_elements_per_prb",
                "resource_elements_per_prb_interim_mode_4_5",
                "resource_elements_per_prb_interim_mode_4_5_num_den",
                "resource_elements_per_prb_mode_4",
                "resource_elements_per_prb_mode_5",
                "rrc_conn_estab_att_hourly",
                "rrc_conn_estab_att_int_hourly",
                "rrc_conn_estab_succ_hourly",
                "s1_sig_conn_estab_att_hourly",
                "s1_sig_conn_estab_attm_hourly",
                "s1_sig_conn_estab_succ_hourly",
                "sub_ratio_denom",
                "sub_ratio_numerator",
                "subscription_ratio",
                "sum_active_ues_in_dl",
                "sum_active_ues_in_dl_per_no_of_rops",
                "tti_per_rop",
                "ul_pdcp_ue_throughput_den_sector",
                "ul_pdcp_ue_throughput_num_sector",
                "ul_pusch_sinr_hourly",
                "ul_pusch_sinr_hourly_denominator",
                "ul_pusch_sinr_hourly_interim",
                "ul_pusch_sinr_hourly_numerator",
                "uplink_pusch_sinr_hourly_rss",
                "volteprbsdl",
                "volteprbsdl_numerator_qci",
                "volteprbsdl_numerator_transmissions");

        final List<String> allScheduledKpis = new ArrayList<>();
        allScheduledKpis.addAll(everyFifteenMinutesScheduledKpis);
        allScheduledKpis.addAll(onceHourlyScheduledKpis);

        LOGGER.info("Converting scheduled KPIs to on demand");
        ScheduledKpiCalculationHelper.changeKpisToOnDemandKpis(allScheduledKpis);

        LOGGER.info("Requesting calculation of scheduled KPIs with calculation_frequency = '0 0/15 * * * ? *'");
        ScheduledKpiCalculationHelper.requestOnDemandCalculationAndPollState(everyFifteenMinutesScheduledKpis);

        LOGGER.info("Requesting calculation of scheduled KPIs with calculation_frequency = '0 30 0/1 * * ? *'");
        ScheduledKpiCalculationHelper.requestOnDemandCalculationAndPollState(onceHourlyScheduledKpis);

        ScheduledKpisVerifier.verifyIntermediateKpisAreCalculatedAndStoredInDatabase();
        ScheduledKpisVerifier.verifyVisibleKpisAreCalculatedAndStoredInDatabase();
        ScheduledKpisVerifier.verifyVisibleKpisAreCalculatedAndStoredInDatabaseForPAKPIs();
    }

    @Test
    @InSequence(10)
    public void whenFlmAlgorithmIsTriggered_andConfigurationIsDeleted_thenExecutionRemainsVisibleInDatabase() {
        LOGGER.info("Verifying that deleting a Configuration does not delete corresponding execution(s).");
        final JsonObject execution = getExecutionFromTestExecution();
        assertNotNull(execution);
        final JsonObject configuration = getCorrespondingConfiguration(execution);
        final Response deleteResponse = httpDeleteRequest(getFlmServiceUri(CONFIGURATIONS + "/" + configuration.get("id").getAsString()));
        assertThat(deleteResponse).isOK();
        final Response getResponse = httpGetRequest(getFlmServiceUri(EXECUTIONS + "/" + execution.get("id").getAsString()));
        assertThat(getResponse).isOK();
        // re-insert the deleted configuration for further tests
        final Response insertResponse = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/" + configuration.get("id").getAsString()), configuration);
        assertThat(insertResponse).hasStatusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @InSequence(11)
    public void whenFlmAlgorithmIsFinished_thenVerify_ExclusionLogs() {
        LOGGER.info("Verifying exclusion logs.");
        final String executionId = getExecutionIdFromTestExecution().trim();
        final List<String> expectedExclusionlogList = new ArrayList<>(18);
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "311, " + EXCLUSION_REASON
                + "Sector is excluded from optimization as none of the cells have complete information.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173296891163765388, " + EXCLUSION_REASON
                + "Sector not suitable for optimization as Goal Function Score Delta Threshold (deltaGFSOptimizationThreshold) is not met");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "195, " + EXCLUSION_REASON
                + "Sector is excluded from optimization as none of the cells have complete information.");
        expectedExclusionlogList
                .add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "1291, " + EXCLUSION_REASON + "Sector has less than two valid cells for optimization");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173290459927812150, " + EXCLUSION_REASON
                + "Sector is excluded as all source cells for the sector are excluded");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "1291, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054008_HELLERS_BEND,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=054008_2, "
                +
                EXCLUSION_REASON + "Cell excluded from optimization as installationType is: indoor");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "195, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054004_CROWN_PALMFRONT,ManagedElement=054004_CROWN_PALMFRONT,ENodeBFunction=1,EUtranCellFDD=054004_3_2, "
                +
                EXCLUSION_REASON + "Cell excluded due to missing kpi 'p_failing_r_mbps'.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "311, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054008_HELLERS_BEND,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=054008_1, "
                +
                EXCLUSION_REASON + "Cell excluded due to empty kpi 'distance_q1' value");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290459927812150, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054343_UTC,ManagedElement=054343_UTC,ENodeBFunction=1,EUtranCellFDD=054343_3_2, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to low Contiguity for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054343_UTC,ManagedElement=054343_UTC,ENodeBFunction=1,EUtranCellFDD=054343_3");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_3, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to low Coverage Balance for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1, "
                +
                EXCLUSION_REASON + "Source cell excluded as all target cells are screened out");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1_4, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to breach of Accessibility threshold for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1_2");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_3_4, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to breach of Accessibility for QCI1 threshold for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_3_9");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290459927812150, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054343_UTC,ManagedElement=054343_UTC,ENodeBFunction=1,EUtranCellFDD=054343_3_2, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to breach of E-RAB Retainability for QCI1 threshold for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054343_UTC,ManagedElement=054343_UTC,ENodeBFunction=1,EUtranCellFDD=054343_3_9");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_2_9, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to breach of Cell HO Success Rate threshold for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_3_2");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_2_4, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to low Cell Availability for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290088340418268, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054234_I5_NOBEL,ManagedElement=054234_I5_NOBEL,ENodeBFunction=1,EUtranCellFDD=054234_2_2, "
                +
                EXCLUSION_REASON + "Cell excluded due to unreliable KPI for App Coverage");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1_9, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to breach of E-RAB Retainability threshold for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_1_2");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173290088340418890, " +
                EXCLUSION_REASON + "Sector excluded due to inconsistent Target Throughput R(targetThroughputR(Mbps))");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102500, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054444_REGENTS_RD,ManagedElement=054444_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=054444_2_2, "
                +
                EXCLUSION_REASON + "Unexpected exception occurred - Cell excluded due to empty CM attribute 'bandwidth' value");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290088340418290, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054235_I5_NOBEL_1,ManagedElement=054235_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054235_1_2, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to empty Cell Availability for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054235_I5_NOBEL_1,ManagedElement=054235_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054235_1");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290459927814150, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054345_UTC,ManagedElement=054345_UTC,ENodeBFunction=1,EUtranCellFDD=054345_3_3, "
                +
                EXCLUSION_REASON + "Source cell excluded as source Contiguity value is zero");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290459927814150, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054345_UTC,ManagedElement=054345_UTC,ENodeBFunction=1,EUtranCellFDD=054345_3, "
                +
                EXCLUSION_REASON + "Source cell excluded as source Coverage Balance Distance value is zero");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173290088340418990, " +
                EXCLUSION_REASON
                + "Sector excluded due to inconsistent Goal Function Score Delta Optimization Threshold (deltaGFSOptimizationThreshold)");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173310348263161344, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054328_BORREGO_FIRE,ManagedElement=054328_BORREGO_FIRE,ENodeBFunction=1,EUtranCellFDD=054328_1_4, "
                +
                EXCLUSION_REASON + "Cell excluded as target due to high % EN-DC users for source cell");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173310348263161344, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054328_BORREGO_FIRE,ManagedElement=054328_BORREGO_FIRE,ENodeBFunction=1,EUtranCellFDD=054328_1_2, "
                +
                EXCLUSION_REASON + "Source cell excluded as all target cells are screened out due to high % EN-DC users");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173310348263161344, " +
                EXCLUSION_REASON + "Sector is excluded as all source cells for the sector are excluded due to high % EN-DC users");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "346594793184559314, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054113_STARVATION_MOUNTAIN,ManagedElement=054113_STARVATION_MOUNTAIN,ENodeBFunction=1,EUtranCellFDD=054113_2_4, "
                +
                EXCLUSION_REASON + "Cell excluded as target as cell is ESS.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "346594793184559314, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054113_STARVATION_MOUNTAIN,ManagedElement=054113_STARVATION_MOUNTAIN,ENodeBFunction=1,EUtranCellFDD=054113_2_2, "
                +
                EXCLUSION_REASON + "Source cell excluded as all target cells are screened out");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290088340413702, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054237_I5_NOBEL_1,ManagedElement=054237_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054237_1, "
                +
                EXCLUSION_REASON + "Cell excluded as source as Probability Transient High");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290088340413702, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054237_I5_NOBEL_1,ManagedElement=054237_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054237_2, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target as Probability Transient Low for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054237_I5_NOBEL_1,ManagedElement=054237_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054237_2_2");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290088340413702, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054237_I5_NOBEL_1,ManagedElement=054237_I5_NOBEL_1,ENodeBFunction=1,EUtranCellFDD=054237_2_2, "
                +
                EXCLUSION_REASON + "Source cell excluded as all target cells are screened out.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173290088340413702, " +
                EXCLUSION_REASON
                + "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "323, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054329_BORREGO_FIRE,ManagedElement=054329_BORREGO_FIRE,ENodeBFunction=1,EUtranCellFDD=LowConn1, "
                +
                EXCLUSION_REASON
                + "Source cell excluded as its connected users: 1 is below the threshold: 2.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "323, " +
                EXCLUSION_REASON
                + "Sector is excluded as all source cells for the sector are excluded, due to all their individual source or target cells being screened out.");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102300, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=053333_REGENTS_RD,ManagedElement=053333_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=053333_1, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to low absolute value of UL PUSCH SINR, for source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=053333_REGENTS_RD,ManagedElement=053333_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=053333_1_2. Where Target Cell UL PUSCH SINR value was: 4");
        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + OSS_ID + ", " + SECTOR_ID + "173290089656102300, " + CELL_ID
                + "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=053333_REGENTS_RD,ManagedElement=053333_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=053333_2, "
                +
                EXCLUSION_REASON
                + "Cell excluded as target due to poor DL coverage relative to source cell SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=053333_REGENTS_RD,ManagedElement=053333_REGENTS_RD,ENodeBFunction=1,EUtranCellFDD=053333_1_2. Where Target Source ratio was: 100");
        // Test will be added as part of
        //        expectedExclusionlogList.add(EXECUTION_ID + executionId + ", " + SECTOR_ID + "173310348263161344, " +
        //                EXCLUSION_REASON + "Sector is excluded from optimization as Sector Busy Hour is unreliable.");
        for (final String expectedLog : expectedExclusionlogList) {
            softly.assertThat(ExclusionLogParser.containsExpectedLog(expectedLog)).as("Following expected log missing: " + expectedLog).isTrue();
        }
    }

    @Test
    @InSequence(12)
    public void whenFlmAlgorithmIsFinished_thenVerify_MultiSectorCellExclusionLogs() {
        LOGGER.info("Verifying exclusion logs for multi sector cells");
        final String executionId = getExecutionIdFromTestExecution();
        String expectedExclusionLog = "Execution_ID: " + executionId
                + ", Oss_ID: 1, Sector_ID: 1554, " + MULTISECTOR_CELL_EXCLUSION_MESSAGE_SECOND_PART;
        LOGGER.info("Asserting Exclusion log: " + expectedExclusionLog);
        assertTrue(ExclusionLogParser.containsExpectedLog(expectedExclusionLog));

        expectedExclusionLog = "Execution_ID: " + executionId
                + ", Oss_ID: 1, Sector_ID: 370, " + MULTISECTOR_CELL_EXCLUSION_MESSAGE_SECOND_PART;
        LOGGER.info("Asserting Exclusion log: " + expectedExclusionLog);
        assertTrue(ExclusionLogParser.containsExpectedLog(expectedExclusionLog));
    }

    private static void verifyFlmData() {
        LOGGER.info("Verify when FLM Algorithm is finished content of 'cell_configuration' table is copied into 'cell_configuration_history' table.");
        CellConfigurationHistoryVerifier.verifyCellConfigurationHistoryTable();

        LOGGER.info("Verify 'cell_configuration' view contains the wanted columns.");
        CellConfigurationViewVerifier.verifyCellConfigurationViewColumns();
    }

    private static void verifyKpis(final String executionId) {
        final String executionIdValue1 = "FLM_1613653680007-150";
        final String executionIdValue2 = "FLM_1613653680007-151";
        final String executionIdValue3 = "FLM_PA_TEST_EXECUTION-001";

        final ValueMap filteredValueMap = ValueMapBuilder.create()
                .addExecutionId(executionId)
                .addLastBusinessDay()
                .applyFilter("execution_id = '%s'", executionId)
                .build();

        final ValueMap filteredValueMapKpiCellGuidFLM60 = ValueMapBuilder.from(filteredValueMap)
                .applyFilter("kpi_cell_guid_flm_60.execution_id not in ('%s', '%s', '%s') AND execution_id = '%s'",
                        executionIdValue1,
                        executionIdValue2,
                        executionIdValue3,
                        executionId)
                .build();

        final ValueMap filteredValueMapCellGuidFLM60KPIs = ValueMapBuilder.from(filteredValueMap)
                .applyFilter("cell_guid_flm_60_kpis.execution_id not in ('%s')",
                        executionIdValue3)
                .build();

        final ValueMap valueMap = ValueMapBuilder.from(filteredValueMap)
                .discardFilter()
                .build();

        LOGGER.info("Verify intermediate KPIs are calculated and stored in database.");
        FlmPreAlgorithmVerifier.verifyTableContent(ContentDatasource.of(TableSource.KPI_CELL_GUID_1440_TABLE, KPI_CELL_GUID_1440_ASSERTION_FILE))
                .thenVerifyTableContent(ContentDatasource.of(TableSource.KPI_CELL_SECTOR_1440_TABLE, KPI_CELL_SECTOR_1440_ASSERTION_FILE))
                .thenVerifyTableContent(ContentDatasource.of(TableSource.KPI_CELL_SECTOR_TABLE, KPI_CELL_SECTOR_ASSERTION_FILE))
                .thenVerifyTableContent(ContentDatasource.of(TableSource.KPI_SECTOR_TABLE, KPI_SECTOR_ASSERTION_FILE))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.KPI_CELL_GUID_FLM_60_TABLE,
                        KPI_CELL_GUID_FLM_60_ASSERTION_FILE, filteredValueMapKpiCellGuidFLM60))
                .thenVerifyTableContentWithDynamicValues(
                        DynamicContentDataSource.of(TableSource.KPI_FREQBAND_BANDWIDTH_TARGET_THROUGHPUT_R_1440_TABLE,
                                KPI_FREQBAND_BANDWIDTH_TARGET_THROUGHPUT_R_1440_ASSERTION_FILE,
                                filteredValueMap))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.KPI_SECTOR_1440_TABLE,
                        KPI_SECTOR_1440_ASSERTION_FILE,
                        valueMap))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.KPI_CELL_SECTOR_FLM_1440_TABLE,
                        KPI_CELL_SECTOR_FLM_1440_ASSERTION_FILE,
                        filteredValueMap))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.KPI_CELL_GUID_FLM_1440_TABLE,
                        KPI_CELL_GUID_FLM_1440_ASSERTION_FILE,
                        valueMap));

        LOGGER.info("Verify visible KPIs are calculated and stored in database.");
        FlmPreAlgorithmVerifier.verifyTableContent(ContentDatasource.of(TableSource.CELL_SECTOR_1440_VIEW, CELL_SECTOR_1440_ASSERTION_FILE))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.CELL_GUID_FLM_60_VIEW,
                        CELL_GUID_FLM_60_ASSERTION_FILE, filteredValueMapCellGuidFLM60KPIs))
                .thenVerifyTableContentWithDynamicValues(DynamicContentDataSource.of(TableSource.CELL_SECTOR_FLM_1440_VIEW,
                        CELL_SECTOR_FLM_1440_ASSERTION_FILE,
                        valueMap));

        LOGGER.info("Verify when FLM Algorithm is finished 'cell_configuration' table is cleaned.");
        CellSettingsVerifier.verifyCellSettingsAreStoredInDatabase(executionId);
    }

    private static Configuration createConfiguration() {
        final String cronExpression = CronMaker.now(CONFIGURATION_SCHEDULE_OFFSET);

        final Map<String, String> customizedGlobalSettings = new HashMap<>(4);
        customizedGlobalSettings.put("qosForCapacityEstimation", "0.4");
        customizedGlobalSettings.put("percentileForMaxConnectedUser", "80");
        customizedGlobalSettings.put("minNumCellForCDFCalculation", "1");
        customizedGlobalSettings.put("maxLbdarStepsize",
                "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, {\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, {\"BW\":\"20000\", \"value\":\"30\"}]");

        final Map<String, String> customizedDefaultSettings = new HashMap<>(4);
        customizedDefaultSettings.put("deltaGFSOptimizationThreshold", "0.3");
        customizedDefaultSettings.put("minimumSourceRetained", "20");
        customizedDefaultSettings.put("sourceTargetSamplesOverlapThreshold", "70");
        customizedDefaultSettings.put("targetSourceContiguityRatioThreshold", "0.9");
        customizedDefaultSettings.put("minConnectedUsers", "2");

        final List<CustomizedGroup> groups = new ArrayList<>(5);
        final String targetThroughputR = "targetThroughputR(Mbps)";
        final String deltaGFSOptimizationThreshold = "deltaGFSOptimizationThreshold";

        groups.add(new CustomizedGroup("test_group_node_no_cells", Collections.singletonMap(targetThroughputR, "7.0")));
        groups.add(new CustomizedGroup("test_group1", Collections.singletonMap(targetThroughputR, "7.0")));
        groups.add(new CustomizedGroup("test_group2", Collections.singletonMap(targetThroughputR, "7.0")));
        groups.add(new CustomizedGroup("test_group3", Collections.singletonMap(targetThroughputR, "5.0")));
        groups.add(new CustomizedGroup("test_group4", Collections.singletonMap(targetThroughputR, "5.0")));
        groups.add(new CustomizedGroup("test_group26", Collections.singletonMap(targetThroughputR, "7.0")));
        groups.add(new CustomizedGroup("test_group27", Collections.singletonMap(deltaGFSOptimizationThreshold, "0.1")));
        groups.add(new CustomizedGroup("test_groupLowConn", Collections.singletonMap(deltaGFSOptimizationThreshold, "0.1")));

        final List<Group> exclusionList = new ArrayList<>(4);
        exclusionList.add(new Group("test_group1"));
        exclusionList.add(new Group("test_group2"));
        exclusionList.add(new Group("test_group3"));
        exclusionList.add(new Group("test_group4"));

        final List<Group> inclusionList = new ArrayList<>(exclusionList);
        inclusionList.add(new Group("test_group5"));
        inclusionList.add(new Group("test_group6"));
        inclusionList.add(new Group("test_group7"));
        inclusionList.add(new Group("test_group8"));
        inclusionList.add(new Group("test_group9"));
        inclusionList.add(new Group("test_group10"));
        inclusionList.add(new Group("test_group11"));
        inclusionList.add(new Group("test_group12"));
        inclusionList.add(new Group("test_group13"));
        inclusionList.add(new Group("test_group14"));
        inclusionList.add(new Group("test_group15"));
        inclusionList.add(new Group("test_group16"));
        inclusionList.add(new Group("test_group17"));
        inclusionList.add(new Group("test_group18"));
        inclusionList.add(new Group("test_group19"));
        inclusionList.add(new Group("test_group26"));
        inclusionList.add(new Group("test_group27"));
        inclusionList.add(new Group("test_groupLowConn"));

        return new ConfigurationBuilder().withId(DEFAULT_CONFIGURATION_ID)
                .withSchedule(cronExpression)
                .withName("default")
                .withEnabled(true)
                .withCustomizedGlobalSettings(customizedGlobalSettings)
                .withCustomizedDefaultSettings(customizedDefaultSettings)
                .withOpenLoop(true)
                .withGroups(groups)
                .withExclusionList(exclusionList)
                .withInclusionList(inclusionList)
                .withWeekendDays(getDynamicWeekendDays())
                .withEnablePA(false)
                .build();
    }

    private static String getFlmServiceUri(final String resource) {
        return BASE_URI + resource;
    }

    private static Response httpGetRequest(final String uri) {
        return buildHttpClient(uri).get();
    }

    private static Response httpPutRequest(final String uri, final JsonElement element) {
        return buildHttpClient(uri).put(Entity.json(GSON.toJson(element)));
    }

    private static Response httpDeleteRequest(final String uri) {
        return buildHttpClient(uri).delete();
    }

    private static <E> E deserializeFromResponse(final Response response, final Type type) {
        return GSON.fromJson(response.readEntity(String.class), type);
    }

    private static Invocation.Builder buildHttpClient(final String uri) {
        return ClientBuilder.newClient()
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON);
    }

    /**
     * Waiting for scheduled execution - usually with some delay - to start and to finish.
     * <p>
     * {@link FlmAlgorithmIT#getExecutionIdFromTestExecution()} will return the latest execution thus we have to cache already finished executions in
     * order to not return the previous run`s execution response causing failure in waiting for the new execution to be scheduled and finished.
     *
     * @return {@code true} if execution succeeded otherwise {@code false}.
     */
    private static boolean waitForSuccessfulExecution() {
        final int maxRetries = 360;
        final int sleepTimeSeconds = 5;
        final int totalSleepTime = maxRetries * sleepTimeSeconds;

        LOGGER.info("Waiting for FLM service execution and kpi calculation for '{}' seconds, or until Execution succeeds", totalSleepTime);

        String executionId = null;
        for (int retries = 1; retries <= maxRetries; retries++) {
            LOGGER.info("Try #{}", retries);
            if (Objects.isNull(executionId) || COMPLETED_EXECUTION_IDS.contains(executionId)) {
                executionId = getExecutionIdFromTestExecution();
            }

            if (Objects.nonNull(executionId)) {
                if (COMPLETED_EXECUTION_IDS.contains(executionId)) {
                    LOGGER.info("An execution with id {} is already checked. Waiting for a new execution to start.", executionId);
                } else {
                    if (getExecutionStateResult(executionId)) {
                        return true;
                    }
                }
            }
            sleep(sleepTimeSeconds, TimeUnit.SECONDS);
        }

        return false;
    }

    private static boolean getExecutionStateResult(final String executionId) {
        final Response response = httpGetRequest(getFlmServiceUri(EXECUTIONS + '/' + executionId));
        if (response.getStatus() == HttpStatus.SC_OK) {
            final JsonObject execution = deserializeFromResponse(response, JsonObject.class);
            final String executionState = execution.get("state").getAsString();
            if ("SUCCEEDED".equals(executionState)) {
                COMPLETED_EXECUTION_IDS.add(executionId);
                return true;
            }
        }
        return false;
    }

    private static JsonObject getExecutionFromTestExecution() {
        final String executionId = getExecutionIdFromTestExecution();
        final Response response = httpGetRequest(getFlmServiceUri(EXECUTIONS + "/" + executionId));
        return deserializeFromResponse(response, JsonObject.class);
    }

    private static String getExecutionIdFromTestExecution() {
        final Response response = httpGetRequest(getFlmServiceUri(EXECUTIONS));
        final JsonArray executionArray = deserializeFromResponse(response, JsonArray.class);
        if (executionArray.size() != 0) {
            for (final JsonElement executionElement : executionArray) {
                final String executionId = executionElement.getAsJsonObject().get("id").getAsString();
                if (!"FLM_1613653680007-150".equalsIgnoreCase(executionId) && !"FLM_PA_TEST_EXECUTION-001".equalsIgnoreCase(executionId)) {
                    return executionId;
                }
            }
        }
        return null;
    }

    private static JsonObject getCorrespondingConfiguration(final JsonObject execution) {
        final int configurationId = execution.get("configurationId").getAsInt();
        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS + "/" + configurationId));
        assertThat(getResponse).isOK();
        return deserializeFromResponse(getResponse, JsonObject.class);
    }

    private static String getDynamicWeekendDays() {
        final LocalDate nowDate = LocalDate.now();
        final String twoDaysBefore = nowDate.minusDays(2).getDayOfWeek().name();
        final String oneDayBefore = nowDate.minusDays(1).getDayOfWeek().name();
        return String.format("%s,%s", twoDaysBefore, oneDayBefore);
    }

    private static void deleteKpiCellSector60Data() {
        final DatabaseAccess KPI_DB_ACCESS = new KpiDatabaseAccess();
        try {
            KPI_DB_ACCESS.executeUpdate("DELETE FROM kpi_cell_sector_60;", new Object[] {});
        } catch (final SQLException throwables) {
            LOGGER.error("Deleting data from kpi_cell_sector_60 table was unsuccessful.");
        }
    }
}
