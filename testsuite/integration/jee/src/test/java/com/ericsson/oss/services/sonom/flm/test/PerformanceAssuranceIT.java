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
package com.ericsson.oss.services.sonom.flm.test;

import static com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions.assertThat;
import static com.ericsson.oss.services.sonom.common.test.util.IntegrationTestUtils.sleep;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.FAILED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SCHEDULED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.test.verification.ChangeElementVerifier.verifyDataFromFilteredResponse;
import static com.ericsson.oss.services.sonom.flm.test.verification.ChangeElementVerifier.verifyDataFromFilteredResponseContainsCellMetadataOrSectorMetadata;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.CELL_GUID_FLM_60_ASSERTION_FILE_FOR_PA;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.SECTOR_FLM_60_ASSERTION_FILE_FOR_PA;
import static com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier.verifyTableContentWithDynamicValues;
import static com.ericsson.oss.services.sonom.flm.test.verification.PAOutputEventsVerifier.assertSentAndReceivedPolicyOutputEventsCountIsEqual;
import static com.ericsson.oss.services.sonom.flm.test.verification.PAOutputEventsVerifier.verifyDegradationStatus;
import static java.lang.String.valueOf;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.TestExecutionTimeLogger;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.ericsson.oss.services.sonom.flm.test.verification.FlmPreAlgorithmVerifier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(OrderedTestRunner.class)
public class PerformanceAssuranceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceAssuranceIT.class);
    private static final String FLM_SERVICE_BASE_URI = "http://" + ServiceHostnameAndPortProvider.getFlmAlgorithmHostnameAndPort();
    private static final String CONTEXT_ROOT = "/son-om/algorithms/flm/v1";
    private static final String PA_EXECUTIONS_ENDPOINT = FLM_SERVICE_BASE_URI + CONTEXT_ROOT + "/executions/%s/pa";
    private static final String FLM_EXECUTION_ID = "FLM_PA_TEST_EXECUTION-001";
    private static final String PA_EXECUTION_ID_1 = "FLM_PA_TEST_EXECUTION-001_1";
    private static final String YESTERDAY = "<YESTERDAY>";

    private static Execution flmExecution;
    private static long paExecutionOffsetInMinutes;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    @Rule
    public TestExecutionTimeLogger testRuleLogTestRunData = new TestExecutionTimeLogger(System.out);

    @BeforeClass
    public static void init() {
        final LocalDateTime endOfPaWindow = LocalDate.now().atStartOfDay().minusHours(1);
        final LocalDateTime wantedPaStartExecutionTime = LocalDateTime.now().plusMinutes(2);
        paExecutionOffsetInMinutes = ChronoUnit.MINUTES.between(endOfPaWindow, wantedPaStartExecutionTime);

        System.setProperty("PA_EXECUTION_OFFSET_TIME_IN_MINUTES", valueOf(paExecutionOffsetInMinutes));
        System.setProperty("BOOTSTRAP_SERVER", "eric-data-message-bus-kf:9092");
        System.setProperty("PA_POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME", "flmPaPolicyInputTopic");
        System.setProperty("PA_POLICY_OUTPUT_EVENT_KAFKA_TOPIC_NAME", "flmPaPolicyOutputTopic");
        System.setProperty("KAFKA_CONSUME_MESSAGE_TIMEOUT_SEC", "300");
        System.setProperty("KAFKA_CLIENT_NUMBER", "10");

        flmExecution = buildFlmExecutionForTest();
    }

    // Verify the three PA windows are scheduled, as per settings (i.e. at times expected)
    // Verify the first PA window is executed successfully
    // Verify On Demand KPIs are calculated correctly
    // Verify that number policy input/output events match
    // Verify that policy output events contain expected degradations per sector
    // Verify that expected reversion elements are set to PROPOSED or are unchanged

    @Test
    @InSequence(1)
    public void whenPerformanceAssuranceIsTriggered_thenPaExecutionsScheduledSuccessfully() {
        PAExecutionsScheduler.createSchedule(flmExecution);
        final List<PAExecution> paExecutions = getPaExecutionsFromFlmExecutionId(flmExecution.getId());

        assertThat(paExecutions).hasSize(3);

        // Building up expected values for the 3 PA windows start and end times and schedules
        // In the PAExecutionsScheduler it uses the FLM execution start time and the values in PASettings to define these values
        // Note: Since SONP-49139 To Align with kpi service expectations pa windows start times are truncated to seconds and pa window end times are minus one second.
        final Instant flmExecutionStartTime = flmExecution.getStartTime().toInstant();
        final List<Timestamp> paWindowStartTimes = Arrays.asList(
                new Timestamp(flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(1, ChronoUnit.HOURS).toEpochMilli()),
                new Timestamp(flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(7, ChronoUnit.HOURS).toEpochMilli()),
                new Timestamp(flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(13, ChronoUnit.HOURS).toEpochMilli()));
        final List<Timestamp> paWindowEndTimes = Arrays.asList(
                new Timestamp(
                        flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(7, ChronoUnit.HOURS).minus(1, ChronoUnit.SECONDS).toEpochMilli()),
                new Timestamp(
                        flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(13, ChronoUnit.HOURS).minus(1, ChronoUnit.SECONDS).toEpochMilli()),
                new Timestamp(flmExecutionStartTime.truncatedTo(ChronoUnit.SECONDS).plus(19, ChronoUnit.HOURS).minus(1, ChronoUnit.SECONDS)
                        .toEpochMilli()));
        final List<String> paExecutionSchedules = Arrays.asList(
                makeCron(paWindowEndTimes.get(0).toInstant().plus(paExecutionOffsetInMinutes, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS)),
                makeCron(paWindowEndTimes.get(1).toInstant().plus(paExecutionOffsetInMinutes, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS)),
                makeCron(paWindowEndTimes.get(2).toInstant().plus(paExecutionOffsetInMinutes, ChronoUnit.MINUTES).plus(1, ChronoUnit.SECONDS)));

        for (final PAExecution paExecutionToVerify : paExecutions) {
            final int window = paExecutionToVerify.getPaWindow();

            LOGGER.info("Verifying PA Execution: {}", paExecutionToVerify.toString());

            softly.assertThat(paExecutionToVerify.getState())
                    .as("PA execution %d state", window)
                    .isEqualTo(SCHEDULED);
            softly.assertThat(paExecutionToVerify.getPaWindowStartTime())
                    .as("PA execution %d start time", window)
                    .isEqualTo(paWindowStartTimes.get(window - 1));
            softly.assertThat(paExecutionToVerify.getPaWindowEndTime())
                    .as("PA execution %d end time", window)
                    .isEqualTo(paWindowEndTimes.get(window - 1));
            softly.assertThat(paExecutionToVerify.getSchedule())
                    .as("PA execution %d schedule", window)
                    .isEqualTo(paExecutionSchedules.get(window - 1));
        }
    }

    @Test
    @InSequence(2)
    public void whenPaExecutionIsExecuted_thenPaExecutionIsSuccessful() {
        assertThat(waitForSuccessfulPaExecution(FLM_EXECUTION_ID)).isTrue();
    }

    @Test
    @InSequence(3)
    public void whenOnDemandKpisAreCalled_thenOnDemandKpisAreCalculatedCorrectly() {
        final LocalDate dateYesterday = LocalDate.now().minusDays(1);
        final String executionId = FLM_EXECUTION_ID;
        final FlmPreAlgorithmVerifier.ValueMap filteredValueMap = FlmPreAlgorithmVerifier.ValueMapBuilder.create()
                .addExecutionId(executionId)
                .applyFilter("execution_id = '%s'", executionId)
                .add(YESTERDAY, dateYesterday.toString())
                .build();
        verifyTableContentWithDynamicValues(
                FlmPreAlgorithmVerifier.DynamicContentDataSource.of(FlmPreAlgorithmVerifier.TableSource.CELL_GUID_FLM_60_VIEW,
                        CELL_GUID_FLM_60_ASSERTION_FILE_FOR_PA, filteredValueMap));
        verifyTableContentWithDynamicValues(
                FlmPreAlgorithmVerifier.DynamicContentDataSource.of(FlmPreAlgorithmVerifier.TableSource.SECTOR_FLM_60_VIEW,
                        SECTOR_FLM_60_ASSERTION_FILE_FOR_PA, filteredValueMap));
    }

    @Test
    @InSequence(4)
    public void whenPaExecutionsFinished_thenVerifyInputAndOutputEvents() {
        final List<PAExecution> paExecutions = getPaExecutionsFromFlmExecutionId(FLM_EXECUTION_ID);
        final PAExecution paExecution = getPaExecutionWithId(paExecutions, PA_EXECUTION_ID_1);

        if (paExecution != null) {
            LOGGER.info("Verifying that the number of PA policy input events sent matches the number of output events received");
            assertSentAndReceivedPolicyOutputEventsCountIsEqual(paExecution);

            LOGGER.info("Verifying the output events contents");
            verifyDegradationStatus(paExecution);
        }
    }

    @Test
    @InSequence(5)
    public void whenPaExecutionIsExecuted_thenExpectedReversionElementsAreProposed() {
        final Map<String, String> filters = new HashMap<>();
        filters.put(ChangeElement.EXECUTION_ID_KEY, FLM_EXECUTION_ID);
        filters.put(ChangeElement.CHANGE_TYPE_KEY, ChangeElement.ChangeType.REVERSION.name());

        verifyDataFromFilteredResponse(FLM_EXECUTION_ID, filters);
    }

    @Test
    @InSequence(6)
    public void whenPaExecutionIsExecuted_thenExpectedReversionElementsArePopulatedWithMetadata() {
        final Map<String, String> filters = new HashMap<>();
        filters.put(ChangeElement.EXECUTION_ID_KEY, FLM_EXECUTION_ID);
        filters.put(ChangeElement.CHANGE_TYPE_KEY, ChangeElement.ChangeType.REVERSION.name());

        verifyDataFromFilteredResponseContainsCellMetadataOrSectorMetadata(filters);
    }

    /**
     * Waiting for scheduled PA execution - usually with some delay - to start and to finish.
     *
     * @return {@code true} if execution succeeded otherwise {@code false}.
     */
    private static boolean waitForSuccessfulPaExecution(final String flmExecutionId) {
        final int maxRetries = 90;
        final int sleepTimeSeconds = 15;
        final int totalSleepTime = maxRetries * sleepTimeSeconds;

        LOGGER.info("Waiting for PA execution and KPI calculation for '{}' seconds, or until Execution succeeds", totalSleepTime);

        for (int retries = 1; retries <= maxRetries; retries++) {
            LOGGER.info("Try #{}", retries);
            final Response response = requestPaExecutionsWithFlmExecutionId(flmExecutionId);

            if (response.getStatus() == HttpStatus.SC_OK) {
                PAExecutionState paExecutionState = FAILED;

                final List<PAExecution> paExecutions = deserializePaExecutionsFromResponse(response, getPaExecutionsListType());
                final PAExecution firstPaExecution = getPaExecutionWithId(paExecutions, PA_EXECUTION_ID_1);
                if (firstPaExecution != null) {
                    paExecutionState = firstPaExecution.getState();
                    LOGGER.info("PAExecutionState: '{}'", paExecutionState);
                }

                if (FAILED.equals(paExecutionState)) {
                    return false;
                }

                if (SUCCEEDED.equals(paExecutionState)) {
                    return true;
                }
            }

            sleep(sleepTimeSeconds, TimeUnit.SECONDS);
        }

        return false;
    }

    private static PAExecution getPaExecutionWithId(final List<PAExecution> paExecutions, final String paExecutionId) {
        for (final PAExecution execution : paExecutions) {
            if (paExecutionId.equals(execution.getId())) {
                return execution;
            }
        }

        LOGGER.error("PA execution with id '{}' not found in list '{}'", paExecutionId, paExecutions);
        return null;
    }

    private static String makeCron(final Instant paExecutionInstant) {
        //Flm is using the local time zone so we follow suit here to keep aligned.
        final LocalDateTime ldt = Timestamp.from(paExecutionInstant).toLocalDateTime();

        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s",
                ldt.get(ChronoField.SECOND_OF_MINUTE),
                ldt.get(ChronoField.MINUTE_OF_HOUR),
                ldt.get(ChronoField.HOUR_OF_DAY),
                ldt.get(ChronoField.DAY_OF_MONTH),
                ldt.get(ChronoField.MONTH_OF_YEAR),
                "?",
                ldt.get(ChronoField.YEAR));
    }

    private static List<PAExecution> getPaExecutionsFromFlmExecutionId(final String flmExecutionId) {
        return deserializePaExecutionsFromResponse(requestPaExecutionsWithFlmExecutionId(flmExecutionId), getPaExecutionsListType());
    }

    private static <T> T deserializePaExecutionsFromResponse(final Response response, final Type type) {
        return new Gson().fromJson(response.readEntity(String.class), type);
    }

    private static Type getPaExecutionsListType() {
        return new TypeToken<ArrayList<PAExecution>>() {
        }.getType();
    }

    private static Response requestPaExecutionsWithFlmExecutionId(final String flmExecutionId) {
        return httpGetRequest(String.format(PA_EXECUTIONS_ENDPOINT, flmExecutionId));
    }

    private static Response httpGetRequest(final String uri) {
        return buildHttpClient(uri).get();
    }

    private static Invocation.Builder buildHttpClient(final String uri) {
        return ClientBuilder.newClient()
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON);
    }

    private static Execution buildFlmExecutionForTest() {
        final Timestamp flmExecutionStartTime = Timestamp.valueOf(LocalDate.now().atStartOfDay().minusHours(8).plusNanos(5000000)); //Add 5 millis to FLM start time SONP-49138
        final Execution flmExecution = new Execution();
        flmExecution.setId(FLM_EXECUTION_ID);
        flmExecution.setConfigurationId(1);
        flmExecution.setStartTime(flmExecutionStartTime);
        flmExecution.setState(ExecutionState.SUCCEEDED);
        flmExecution.setStateModifiedTime(flmExecutionStartTime);
        flmExecution.setSchedule(makeCron(flmExecutionStartTime.toInstant()));
        flmExecution.setRetryAttempts(0);
        flmExecution.setCalculationId("KPI_1619921309118");

        final Map<String, String> customizedGlobalSettings = new HashMap<>();
        customizedGlobalSettings.put("qosForCapacityEstimation", "0.5");
        customizedGlobalSettings.put("percentileForMaxConnectedUser", "40.0");
        customizedGlobalSettings.put("minNumCellForCDFCalculation", "20");
        customizedGlobalSettings.put("targetPushBack", "2");
        customizedGlobalSettings.put("overrideCCalculator", "No");
        customizedGlobalSettings.put("minLbdarStepsize", "1");
        customizedGlobalSettings.put("maxLbdarStepsize", "[{\"BW\":\"1400\", \"value\":\"1\"}, {\"BW\":\"3000\", \"value\":\"2\"}, " +
                "{\"BW\":\"5000\", \"value\":\"5\"}, {\"BW\":\"10000\", \"value\":\"20\"}, {\"BW\":\"15000\", \"value\":\"25\"}, " +
                "{\"BW\":\"20000\", \"value\":\"30\"}]");
        customizedGlobalSettings.put("leakageThirdCell", "10");
        customizedGlobalSettings.put("leakageLbqImpact", "20");
        customizedGlobalSettings.put("existingHighPush", "30");
        customizedGlobalSettings.put("paKpiSettings", "{" +
                "\"cellHandoverSuccessRate\":{\"enableKPI\":true,\"confidenceInterval\":\"98.10\",\"relevanceThreshold\":\"99.89\"}," +
                "\"initialAndAddedERabEstabSrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.10\",\"relevanceThreshold\":\"99.90\"}," +
                "\"initialAndAddedERabEstabSrQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"95.10\",\"relevanceThreshold\":\"99.91\"}," +
                "\"eRabRetainabilityPercentageLostHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"0.12\"}," +
                "\"eRabRetainabilityPercentageLostQci1Hourly\":{\"enableKPI\":true,\"confidenceInterval\":\"93.10\",\"relevanceThreshold\":\"0.01\"},"
                +
                "\"avgDlPdcpThroughputSector\":{\"enableKPI\":true,\"confidenceInterval\":\"90.40\"}," +
                "\"avgUlPdcpThroughputSector\":{\"enableKPI\":true,\"confidenceInterval\":\"92.70\"}," +
                "\"ulPuschSinrHourly\":{\"enableKPI\":true,\"confidenceInterval\":\"97.50\",\"relevanceThreshold\":\"15\"}}");
        customizedGlobalSettings.put("numberOfKpiDegradedHoursThreshold", "3");
        flmExecution.setCustomizedGlobalSettings(customizedGlobalSettings);

        final Map<String, String> customizedDefaultSettings = new HashMap<>();
        customizedDefaultSettings.put("targetThroughputR(Mbps)", "5.0");
        customizedDefaultSettings.put("deltaGFSOptimizationThreshold", "0.3");
        customizedDefaultSettings.put("targetSourceCoverageBalanceRatioThreshold", "0.9");
        customizedDefaultSettings.put("sourceTargetSamplesOverlapThreshold", "70.0");
        customizedDefaultSettings.put("targetSourceContiguityRatioThreshold", "0.9");
        customizedDefaultSettings.put("loadBalancingThresholdForInitialAndAddedErabEstabSuccRate", "98.0");
        customizedDefaultSettings.put("loadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1", "98.5");
        customizedDefaultSettings.put("loadBalancingThresholdForErabPercentageLost", "2.0");
        customizedDefaultSettings.put("loadBalancingThresholdForErabPercentageLostForQci1", "1.5");
        customizedDefaultSettings.put("loadBalancingThresholdForCellHoSuccRate", "70.0");
        customizedDefaultSettings.put("loadBalancingThresholdForCellAvailability", "70.0");
        customizedDefaultSettings.put("optimizationSpeed", "normal");
        customizedDefaultSettings.put("loadBalancingThresholdForEndcUsers", "50.0");
        customizedDefaultSettings.put("essEnabled", "true");
        customizedDefaultSettings.put("minimumSourceRetained", "20");
        customizedDefaultSettings.put("minRopsForAppCovReliability", "3");
        customizedDefaultSettings.put("minNumCqiSamples", "100");
        customizedDefaultSettings.put("minNumSamplesForTransientCalculation", "15");
        customizedDefaultSettings.put("sigmaForTransientCalculation", "3");
        customizedDefaultSettings.put("ulPuschSinrRatioThreshold", "0.8");
        customizedDefaultSettings.put("minTargetUlPuschSinr", "5");
        customizedDefaultSettings.put("percentageBadRsrpRatioThreshold", "1.2");
        flmExecution.setCustomizedDefaultSettings(customizedDefaultSettings);

        flmExecution.setGroups(Collections.emptyList());
        flmExecution.setNumSectorsToEvaluateForOptimization(29);
        flmExecution.setNumOptimizationElementsSent(28);
        flmExecution.setNumOptimizationElementsReceived(28);
        flmExecution.setNumOptimizationLbqs(0);
        flmExecution.setNumChangesWrittenToCmDb(0);
        flmExecution.setNumChangesNotWrittenToCmDb(0);
        flmExecution.setOpenLoop(true);
        flmExecution.setInclusionList(Collections.emptyList());
        flmExecution.setExclusionList(Collections.emptyList());
        flmExecution.setWeekendDays("");
        flmExecution.setEnablePA(true);
        flmExecution.setFullExecution(true);

        return flmExecution;
    }
}
