/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.ALGORITHM_FAILURE_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.util.TimeUtils.dayBefore;
import static com.ericsson.oss.services.sonom.flm.service.api.util.TimeUtils.weekBefore;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest.KpiCalculationRequestCreator;
import com.ericsson.oss.services.sonom.flm.kpi.util.AdditionalParametersUtils;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.util.WeekendDay;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.FlmExecutionHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;

/**
 * KPICalculationExecutor class used during KPI Calculations.
 */
public class KpiCalculationExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiCalculationExecutor.class);
    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE = Integer
            .parseInt(getEnvironmentValue("MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE", "120"));
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS = Integer
            .parseInt(getEnvironmentValue("RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS", "5"));
    private static final int EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 30;

    private final KpiRequestRetry kpiRequestRetry;
    private final RequestProcessor requestProcessor;
    private final PersistenceHandler persistenceHandler;
    private final Execution execution;
    private final MetricHelper flmMetricHelper;
    private final SectorReferenceCellHandler sectorReferenceCellHandler;
    private final FlmExecutionHandler flmExecutionHandler;

    private KpiCalculationStateHandler kpiCalculationStateHandler;
    private boolean isNonSettingsBasedKpiCalculationCompleted;

    public KpiCalculationExecutor(final CmStore cmStore, final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler) {
        final ExecutionDao executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        kpiRequestRetry = new KpiRequestRetry(new KpiActionForState(new KpiValidation()));
        requestProcessor = new RequestProcessor(execution, flmMetricHelper, executionDao, kpiRequestRetry);
        sectorReferenceCellHandler = new SectorReferenceCellHandler(cmStore);
        flmExecutionHandler = new FlmExecutionHandler();
        kpiCalculationStateHandler = new KpiCalculationStateHandler(
                MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE,
                RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS, true);
        isNonSettingsBasedKpiCalculationCompleted = false;
    }

    // required for Mockito JUnit
    public KpiCalculationExecutor(final CmStore cmStore, final Execution execution, final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler, final FlmExecutionHandler flmExecutionHandler) {
        final ExecutionDao executionDao = new ExecutionDaoImpl(EXECUTION_DAO_MAX_RETRY_ATTEMPTS, EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS);
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        kpiRequestRetry = new KpiRequestRetry(new KpiActionForState(new KpiValidation()));
        requestProcessor = new RequestProcessor(execution, flmMetricHelper, executionDao, kpiRequestRetry);
        sectorReferenceCellHandler = new SectorReferenceCellHandler(cmStore);
        this.flmExecutionHandler = flmExecutionHandler;
        kpiCalculationStateHandler = new KpiCalculationStateHandler(
                MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE,
                RETRY_WAIT_DURATION_KPI_CALCULATION_STATE_SECONDS, true);
        isNonSettingsBasedKpiCalculationCompleted = false;
    }

    // required for Mockito JUnit
    public KpiCalculationExecutor(final Execution execution,
            final MetricHelper flmMetricHelper,
            final RequestProcessor requestProcessor, final PersistenceHandler persistenceHandler,
            final KpiRequestRetry kpiRequestRetry,
            final SectorReferenceCellHandler sectorReferenceCellHandler, final FlmExecutionHandler flmExecutionHandler) {
        this.execution = execution;
        this.flmMetricHelper = flmMetricHelper;
        this.requestProcessor = requestProcessor;
        this.persistenceHandler = persistenceHandler;
        this.kpiRequestRetry = kpiRequestRetry;
        this.sectorReferenceCellHandler = sectorReferenceCellHandler;
        this.flmExecutionHandler = flmExecutionHandler;
        isNonSettingsBasedKpiCalculationCompleted = false;
    }

    protected void setKpiCalculationStateHandler(final KpiCalculationStateHandler kpiCalculationStateHandler) {
        this.kpiCalculationStateHandler = kpiCalculationStateHandler;
    }

    /**
     * Sets the flag for whether non-settings based KPIs have been calculated.
     */
    public void setIsNonSettingsBasedKpiCalculationCompleted() {
        isNonSettingsBasedKpiCalculationCompleted = true;
    }

    /**
     * Starts the non-settings based KPI calculations for processing groups 1-7.
     *
     * @param executionState
     *            The state of execution to start calculations from.
     * @param isResumed
     *            Whether calculation has been stopped and resumed again.
     * @param executionDate
     *            Date of execution.
     * @throws FlmAlgorithmException
     *             Error in overall algorithm execution.
     */
    public void nonSettingsBasedExecute(final ExecutionState executionState, final boolean isResumed, final String executionDate)
            throws FlmAlgorithmException {
        try {
            if (isResumed) {
                determineCurrentState(false);
            } else {
                execution.setState(executionState);
            }
            calculateNonSettingsBasedKpis(executionDate, isResumed);
        } catch (final Exception e) {
            throw new FlmAlgorithmException(ALGORITHM_FAILURE_ERROR, e);
        }
    }

    private void calculateNonSettingsBasedKpis(final String executionDate, final boolean isResumed)
            throws FlmAlgorithmException, IOException, SQLException {
        LOGGER.info("Starting non-settings based KPI Calculations");

        if (execution.getState() == ExecutionState.getFirstState()) {
            execution.setState(ExecutionState.KPI_PROCESSING_GROUP_1);
        }

        final long kpiCalculationStartTime = System.nanoTime();
        final WeekendDay weekendDay = WeekendDay.of(execution.getWeekendDays());

        final LocalDate executionLocalDate = LocalDate.parse(executionDate);

        final LocalDate kpiCalculationLocalDate = weekendDay.calculateLastBusinessDay(executionLocalDate);
        final LocalDateTime kpiCalculationStartLocalDateTime = weekendDay.calculateLastBusinessDay(dayBefore(executionLocalDate))
                .atTime(LocalTime.MIDNIGHT);
        final LocalDate kpiRecalculationLocalDate = weekendDay.calculateLastBusinessDay(weekBefore(executionLocalDate));
        final LocalDateTime kpiCalculationEndLocalDateTime = weekendDay.calculateLastBusinessDay(dayBefore(executionLocalDate))
                .atTime(LocalTime.MAX);

        final String kpiCalculation = kpiCalculationLocalDate.format(DateTimeFormatter.ISO_DATE);
        final String kpiCalculationStartDateTime = kpiCalculationStartLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        final String kpiCalculationEndDateTime = kpiCalculationEndLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME);

        final String kpiRecalculationDate = kpiRecalculationLocalDate.format(DateTimeFormatter.ISO_DATE);
        try {
            switch (execution.getState()) {
                case KPI_PROCESSING_GROUP_1:
                    writeSuitableLogForGroupState(1);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group1_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .withEndDateTime(kpiCalculationEndDateTime)
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_2:
                    writeSuitableLogForGroupState(2);
                    final Map<String, String> additionalParametersForReferenceCell = AdditionalParametersUtils
                            .getAdditionalParametersForReferenceCell(
                                    sectorReferenceCellHandler.getSectorIdsRequiringReferenceCellRecalculation());
                    requestProcessor.processRequest(additionalParametersForReferenceCell.isEmpty()
                            ? KpiCalculationRequest.KpiCalculationRequestCreator.empty(ExecutionState.KPI_PROCESSING_GROUP_2, isResumed)
                            : KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_2, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group2_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .withAdditionalParameters(additionalParametersForReferenceCell)
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_3:
                    writeSuitableLogForGroupState(3);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_3, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group3_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_4:
                    writeSuitableLogForGroupState(4);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_4, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group4_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_5:
                    writeSuitableLogForGroupState(5);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_5, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group5_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_6:
                    writeSuitableLogForGroupState(6);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_6, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group6_kpis.json")
                                    .withCurrentDate(kpiCalculation)
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .withEndDateTime(kpiCalculationEndDateTime)
                                    .withRecalculationDate(kpiRecalculationDate)
                                    .withAdditionalParameters(AdditionalParametersUtils.getAdditionalParametersForSignalRange(
                                            sectorReferenceCellHandler.getSectorIdsRequiringReferenceCellRecalculation()))
                                    .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_7:
                    writeSuitableLogForGroupState(7);
                    requestProcessor.processRequest(
                            KpiCalculationRequest.KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_7, isResumed)
                                    .loadGroupKpis("kpiCalculationRequests/group7_kpis.json")
                                    .withStartDateTime(kpiCalculationStartDateTime)
                                    .build()); //FALLTHROUGH
                case SETTINGS_PROCESSING:
                    execution.setCalculationId(null); // clear as all non-settings on-demand calls are completed
                    persistenceHandler.persistExecutionStatus(ExecutionState.SETTINGS_PROCESSING, isResumed);
                    execution.setState(ExecutionState.SETTINGS_PROCESSING);
                    isNonSettingsBasedKpiCalculationCompleted = true;
                    break;
                default:
                    LOGGER.info("All non-settings based KPI groups calculated");
                    break;
            }
        } finally {
            LOGGER.info("Non-settings based KPI Processing done");
            incrementFlmCountMetric(kpiCalculationStartTime,
                    FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS);
        }
    }

    /**
     * Starts the settings based KPI calculations for processing groups 8-14.
     *
     * @param executionState
     *            The state of execution to start calculations from.
     * @param executionDate
     *            Date of execution.
     * @param isResumed
     *            Whether calculation has been stopped and resumed again.
     * @param isFullExecution
     *            Whether it is an full Execution or not.
     * @throws FlmAlgorithmException
     *             Error in overall algorithm execution
     */
    public void settingsBasedExecute(final ExecutionState executionState, final boolean isResumed, final boolean isFullExecution,
            final String executionDate)
            throws FlmAlgorithmException {
        try {
            if (isResumed) {
                determineCurrentState(isFullExecution);
            } else {
                execution.setState(executionState);
            }
            calculateSettingsBasedKpis(executionDate, isResumed, isFullExecution);
        } catch (final Exception e) {
            throw new FlmAlgorithmException(ALGORITHM_FAILURE_ERROR, e);
        }
    }

    private void calculateSettingsBasedKpis(final String executionDate, final boolean isResumed, final boolean isFullExecution)
            throws FlmAlgorithmException, SQLException, IOException {
        final long kpiCalculationStartTime = System.nanoTime();
        try {
            if (ExecutionState.isStateAssociatedWithNonSettingsBasedKpiCalculations(execution.getState())) {
                LOGGER.error("Execution {} to execute settings based KPIs found in non-settings based KPI state {} Setting Execution to FAILED",
                        execution, execution.getState());
                persistenceHandler.persistExecutionStatus(ExecutionState.FAILED, isResumed);
                throw new FlmAlgorithmException(ALGORITHM_FAILURE_ERROR);
            }
            final WeekendDay weekendDay = WeekendDay.of(execution.getWeekendDays());

            final LocalDate executionLocalDate = LocalDate.parse(executionDate);
            final LocalDateTime executionLocalDateTime = executionLocalDate.atTime(0, 0, 0);

            final LocalDateTime kpiCalculationStartLocalDateTime = weekendDay.calculateLastBusinessDay(dayBefore(executionLocalDate))
                    .atTime(LocalTime.MIDNIGHT);
            final LocalDateTime kpiCalculationEndLocalDateTime = weekendDay.calculateLastBusinessDay(dayBefore(executionLocalDate))
                    .atTime(LocalTime.MAX);

            final String kpiCalculationStartDateTime = kpiCalculationStartLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            final String kpiCalculationEndDateTime = kpiCalculationEndLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME);

            final Optional<ExecutionSummary> previousExecutionSummary = flmExecutionHandler.getAllExecutionSummaries().stream()
                    .filter(executionSummary -> executionSummary.getStartTime().before(Timestamp.valueOf(executionLocalDateTime)))
                    .filter(executionSummary -> executionSummary.getConfigurationId().equals(execution.getConfigurationId()))
                    .filter(executionSummary -> executionSummary.getState() == ExecutionState.SUCCEEDED)
                    .max(Comparator.comparing(ExecutionSummary::getStartTime));

            final String previousExecutionId = previousExecutionSummary.isPresent() ? previousExecutionSummary.get().getId() : StringUtils.EMPTY;
            LOGGER.info("Most recent execution ID for current executions configuration is {}", previousExecutionId);
            switch (execution.getState()) {
                case KPI_PROCESSING_GROUP_8:
                    writeSuitableLogForGroupState(8);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_8, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group8_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_9:
                    writeSuitableLogForGroupState(9);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_9, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group9_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_10:
                    writeSuitableLogForGroupState(10);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_10, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group10_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .withPreviousExecutionId(previousExecutionId)
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_11:
                    writeSuitableLogForGroupState(11);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_11, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group11_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_12:
                    writeSuitableLogForGroupState(12);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_12, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group12_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .withAdditionalParameters(AdditionalParametersUtils.getAdditionalParametersForGlobalSettings(
                                    execution.getCustomizedGlobalSettings()))
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_13:
                    writeSuitableLogForGroupState(13);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_13, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group13_kpis.json")
                            .withExecutionId(execution.getId())
                            .withPreviousExecutionId(previousExecutionId)
                            .withAdditionalParameters(AdditionalParametersUtils.getAdditionalParametersForTransient())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_14:
                    writeSuitableLogForGroupState(14);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_14, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group14_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .withPreviousExecutionId(previousExecutionId)
                            .withAdditionalParameters(AdditionalParametersUtils.getAdditionalParametersForTransient())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_15:
                    writeSuitableLogForGroupState(15);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_15, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group15_kpis.json")
                            .withStartDateTime(kpiCalculationStartDateTime)
                            .withEndDateTime(kpiCalculationEndDateTime)
                            .withExecutionId(execution.getId())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_GROUP_16:
                    writeSuitableLogForGroupState(16);
                    requestProcessor.processRequest(KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_16, isResumed)
                            .loadGroupKpis("kpiCalculationRequests/group16_kpis.json")
                            .withExecutionId(execution.getId())
                            .build()); //FALLTHROUGH
                case KPI_PROCESSING_SUCCEEDED:
                    persistenceHandler.persistExecutionStatus(ExecutionState.KPI_PROCESSING_SUCCEEDED, isResumed);
                    execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState()));
                    break;
                default:
                    LOGGER.info("All KPI groups calculated");
                    break;
            }
        } finally {
            incrementFlmCountMetric(kpiCalculationStartTime,
                    FlmMetric.FLM_KPI_CALCULATION_TIME_IN_MILLIS);
        }
    }

    private void writeSuitableLogForGroupState(final int groupNumber) {
        LOGGER.info("Requesting KPI calculation group #{} for configuration Id {}, execution Id {}.",
                groupNumber, execution.getConfigurationId(), execution.getId());
    }

    private void determineCurrentState(final boolean isFullExecution) throws FlmAlgorithmException {
        if (Objects.nonNull(execution.getCalculationId())) { // NULL check for when resumed is true for Cell Settings Executor stage.
            if (isKpiCalculationLost()) {
                // calculation lost, resume from current state
                LOGGER.info("Calculation Lost, resuming from current state");
                return;
            }
            if (isKpiState(execution.getState()) && !isNonSettingsBasedKpiCalculationCompleted) {
                LOGGER.info("Moving from state: {}, to the next state.", execution.getState());
                execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState()));
            }
        } else {
            LOGGER.info("Calculation ID NULL, resuming from state: {}", execution.getState());
        }
    }

    private boolean isKpiCalculationLost() throws FlmAlgorithmException {
        try {
            kpiRequestRetry.getKpiCalculationState(kpiCalculationStateHandler, execution.getCalculationId());
        } catch (final FlmAlgorithmException e1) {
            if (kpiRequestRetry.getKpiRecalculationRequiredExceptionPredicate().test(e1)) {
                return true;
            }
            throw e1;
        }
        return false;
    }

    private void incrementFlmCountMetric(final Long requestStartTime, final FlmMetric flmMetricTime) {
        flmMetricHelper.incrementFlmMetric(flmMetricTime,
                flmMetricHelper.getTimeElapsedInMillis(requestStartTime));
    }

    /**
     * Determines if KPI is in the processing states.
     *
     * @param executionState
     *            executionState to check whether it is being processed.
     * @return returns whether KPI is being processed or not.
     */
    private static boolean isKpiState(final ExecutionState executionState) {
        return executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_1) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_2) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_3) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_4) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_5) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_6) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_7) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_8) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_9) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_10) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_11) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_12) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_13) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_14) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_15) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_GROUP_16) ||
                executionState.equals(ExecutionState.KPI_PROCESSING_SUCCEEDED);
    }
}
