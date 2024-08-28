/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.NO_SECTORS_FOUND;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.OPTIMIZATION_ELEMENT_RECEIVED_NO_POLICYOUTPUTEVENT;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.OPTIMIZATION_EXECUTOR_ERROR;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.FlmPolicyInputEventHandler;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.executor.StageExecutor;
import com.ericsson.oss.services.sonom.flm.messagehandler.ExecutionConsumerHandler;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellKpiCollection;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.ExecutionDates;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.settings.CellSettingCollection;

/**
 * This class collects all the sectors and cells for optimization, sends them to Policy Engine, receives the outcome of Policy Engine and handles them
 * via ExecutionConsumerHandler.
 */
public class OptimizationExecutor implements StageExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptimizationExecutor.class);
    private static final String FLM_INPUT_TOPIC = getEnvironmentValue("POLICY_INPUT_EVENT_KAFKA_TOPIC_NAME", "flmPolicyInputTopic");
    private static final int TIMEOUT_SEC = Integer.parseInt(getEnvironmentValue("KAFKA_CONSUME_MESSAGE_TIMEOUT_SEC", "300"));
    private static final int OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS = 10;
    private static final int OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS = 30;

    private CmStore cmStore;
    private Execution execution;
    private PersistenceHandler persistenceHandler;
    private final List<String> policyInputEvents = new ArrayList<>();
    private CellKpiCollection cellKpiCollection;
    private FlmPolicyInputEventHandler flmPolicyInputEventHandler;
    private MetricHelper flmMetricHelper;
    private PolicyInputEventGenerator inputEventGenerator;
    private CellSettingCollection cellSettingsCollection;
    private OptimizationsDao optimizationsDao;
    private ExecutionDao executionDao;
    private ExecutionConsumerHandler executionConsumerHandler;

    public OptimizationExecutor(final CmStore cmStore, final Execution execution, final PersistenceHandler persistenceHandler,
            final ExecutionDao executionDao) {
        this.execution = execution;
        this.persistenceHandler = persistenceHandler;
        flmPolicyInputEventHandler = new FlmPolicyInputEventHandler(executionDao);
        flmMetricHelper = new FlmMetricHelper();
        inputEventGenerator = new PolicyInputEventGenerator(cmStore);
        cellKpiCollection = new CellKpiCollection();
        this.cmStore = cmStore;
        cellSettingsCollection = new CellSettingCollection();
        optimizationsDao = new OptimizationsDaoImpl(OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS,
                OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS);
        this.executionDao = executionDao;
        executionConsumerHandler = ExecutionConsumerController.getOrCreate().getExecutionConsumerHandler();
    }

    // required for Mockito JUnit
    public OptimizationExecutor() {
        flmMetricHelper = new FlmMetricHelper();
    }

    private void processAllSectors(final String endDateTime, final String startDateTime)
            throws FlmAlgorithmException, SQLException, InterruptedException {
        final CmSectorCellStore cmSectorCellStore = cmStore.getCmSectorCellStore();
        final Collection<TopologySector> sectorList = cmSectorCellStore.getSectorsWithInclusionListCells();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                String.format("Found %d sectors to evaluate for optimization", sectorList.size())));
        }
        execution.setNumSectorsToEvaluateForOptimization(sectorList.size());

        final Map<CellIdentifier, Set<Long>> multiSectorCells = getMultiSectorCells(sectorList);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                String.format("Count of split cells: %d", multiSectorCells.size())));
        }

        if (sectorList.isEmpty()) {
            throw new FlmAlgorithmException(NO_SECTORS_FOUND);
        } else {
            final Map<CellIdentifier, Map<String, Object>> cellAllKpis = cellKpiCollection.collect(sectorList, execution.getId(), endDateTime,
                    startDateTime);
            final Map<CellIdentifier, Map<String, String>> cellAllSettings = cellSettingsCollection.collect(execution.getId());
            logCellsWithoutSectors(cmSectorCellStore, cellAllKpis);
            cmStore.createAndPopulateCmNodeObjectsStore(getSectorIdsAsLongCollection(sectorList));
            inputEventGenerator = createInputEventGenerator(cellAllKpis, cellAllSettings, multiSectorCells);
            final long policyInputEventGenerationStartTime = System.nanoTime();
            sectorList.forEach(this::processSector);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Successfully generated '%d' FLM policy input events", policyInputEvents.size())));
            }
            executionConsumerHandler.consumeMessagesForExecution(execution.getId(), policyInputEvents.size(), getAllSectorIds());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "Added execution to ExecutionConsumerHandler as needed"));
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), String.format("Sending records to '%s'", FLM_INPUT_TOPIC)));
            }
            flmPolicyInputEventHandler.sendToKafkaTopic(policyInputEvents, execution);
            flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_POLICY_INPUT_EVENT_GENERATION_IN_MILLIS,
                    flmMetricHelper.getTimeElapsedInMillis(policyInputEventGenerationStartTime));
        }
    }

    private Collection<Long> getSectorIdsAsLongCollection(final Collection<TopologySector> sectorList) {
        final Collection<Long> sectorCollection = new ArrayList<>();
        for (final TopologySector sector:sectorList) {
            sectorCollection.add(sector.getSectorId());
        }
        return sectorCollection;
    }

    private void logCellsWithoutSectors(final CmSectorCellStore cmSectorCellStore,
            final Map<CellIdentifier, Map<String, Object>> cellAllKpis) {
        findCellsWithoutSectors(cmSectorCellStore, cellAllKpis).forEach(ossAndFdn ->
                        LoggingFormatter.logFilteredCell(execution.getId(), String.valueOf(ossAndFdn.getOssId()), "<none>", ossAndFdn.getFdn(),
                                "Cell excluded from optimization because it is not in any Sector"));
    }

    private Set<CellIdentifier> findCellsWithoutSectors(final CmSectorCellStore cmSectorCellStore,
            final Map<CellIdentifier, Map<String, Object>> cellAllKpis) {
        final Set<CellIdentifier> allIncludedCells = cmSectorCellStore.getAllIncludedCellIds();
        final Set<CellIdentifier> cellsToBeOptimized = cellAllKpis.keySet();
        return allIncludedCells.stream()
                .filter(identifier -> !cellsToBeOptimized.contains(identifier))
                .collect(Collectors.toSet());
    }

    @Override
    public void execute(final ExecutionState executionState, final boolean isResumed, final boolean isFullExecution, final String executionDate)
            throws FlmAlgorithmException {
        final ExecutionDates executionDates = ExecutionDates.getExecutionDates(executionDate, execution.getWeekendDays());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), String.format("FLM execution running for KPIs from '%s' to '%s'",
                executionDates.getKpiExecutionStartDate(), executionDates.getKpiExecutionEndDate())));
        }
        try {
            switch (executionState) {
                case OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE:
                    processAllSectors(executionDates.getKpiExecutionEndDate(), executionDates.getKpiExecutionStartDate());
                    persistenceHandler.persistExecutionStatus(ExecutionState.OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, isResumed);
                    execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState())); //FALLTHROUGH
                case OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE:
                    receiveKafkaMessages();
                    persistenceHandler.persistExecutionStatus(ExecutionState.OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE, isResumed);
                    execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState())); //FALLTHROUGH
                case OPTIMIZATION_PROCESSING_SUCCEEDED:
                    persistenceHandler.persistExecutionStatus(ExecutionState.OPTIMIZATION_PROCESSING_SUCCEEDED, isResumed);
                    execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState()));
                    break;
                default:
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "All optimization processing done"));
                    }
                    break;
            }
        } catch (final Exception e) {
            executionConsumerHandler.removeExecution(execution.getId());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "Remove execution from ExecutionConsumerHandler as not needed"));
            }
            throw new FlmAlgorithmException(OPTIMIZATION_EXECUTOR_ERROR, e);
        }
    }

    PolicyInputEventGenerator createInputEventGenerator(final Map<CellIdentifier, Map<String, Object>> cellAllKpis,
            final Map<CellIdentifier, Map<String, String>> cellAllSettings, final Map<CellIdentifier, Set<Long>> multiSectorCells) {
        return new PolicyInputEventGenerator(cellAllKpis, cellAllSettings, multiSectorCells, cmStore);
    }

    private Map<CellIdentifier, Set<Long>> getMultiSectorCells(final Collection<TopologySector> sectorList) {
        final Map<CellIdentifier, Set<Long>> cellSectorMapForMultiSectorCells = new HashMap<>();
        sectorList.forEach(topologySector -> topologySector.getAssociatedCells()
                .forEach(cell -> cellSectorMapForMultiSectorCells
                        .computeIfAbsent(getCellIdentifier(cell), s -> new HashSet<>())
                        .add(topologySector.getSectorId())));
        cellSectorMapForMultiSectorCells.values().removeIf(longs -> longs.size() <= 1);
        return Collections.unmodifiableMap(cellSectorMapForMultiSectorCells);
    }

    private static CellIdentifier getCellIdentifier(final Cell cell) {
        return new CellIdentifier(cell.getOssId(), cell.getFdn());
    }

    private void processSector(final TopologySector sector) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(execution.getId(), sector.getSectorId(), String.format("Processing Sector with cells '%s'",
                    sector.getAssociatedCells().stream().map(Cell::getCellId).collect(Collectors.toList()))));
        }

        final PolicyInputEvent policyInputEvent = inputEventGenerator.generateInputEvent(sector, execution.getId());
        final String policyInputEventJson = policyInputEvent.toJson();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("PolicyInput event generated '%s'", policyInputEventJson)));
        }

        if (policyInputEvent.getOptimizationCells().size() < 2) {
            LoggingFormatter.logFilteredSector(execution.getId(), sector.getSectorId().toString(),
                            "Sector has less than two valid cells for optimization " +
                            "due to associated cells having unsupported installationType or are specified in " +
                            "the exclusion list, or cells exist in more than one sector.");
        } else {
            policyInputEvents.add(policyInputEventJson);
        }
    }

    public List<String> getPolicyInputEvents() {
        return new ArrayList<>(policyInputEvents);
    }

    private void receiveKafkaMessages() throws InterruptedException, SQLException, FlmAlgorithmException {
        if (executionConsumerHandler == null) {
            throw new FlmAlgorithmException(OPTIMIZATION_ELEMENT_RECEIVED_NO_POLICYOUTPUTEVENT);
        }

        final long policyOutputEventProcessingStartTime = System.nanoTime();
        final boolean noTimeOut = executionConsumerHandler.waitMessages(
                execution.getId(), TimeUnit.SECONDS.toMillis(TIMEOUT_SEC));
        final int numOfPolicyOutputEventsReceived = optimizationsDao.getNumberOfPolicyOutputEvents(execution.getId());
        execution.setNumOptimizationElementsReceived(numOfPolicyOutputEventsReceived);
        logKafkaMessagesReceivedResults(noTimeOut, numOfPolicyOutputEventsReceived);

        executionDao.update(execution);

        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_POLICY_OUTPUT_EVENT_PROCESSED,
                numOfPolicyOutputEventsReceived);
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_POLICY_OUTPUT_EVENT_PROCESSED_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(policyOutputEventProcessingStartTime));

        if (numOfPolicyOutputEventsReceived == 0) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(execution.getId(),
                "No PolicyOutputEvent has been persisted to database, execution status is set to FAILED"));
            }
            throw new FlmAlgorithmException(OPTIMIZATION_ELEMENT_RECEIVED_NO_POLICYOUTPUTEVENT);
        }
    }

    private void logKafkaMessagesReceivedResults(final boolean noTimeOut, final int numOfPolicyOutputEventsReceived) {
        if (noTimeOut) {
            if (numOfPolicyOutputEventsReceived == execution.getNumOptimizationElementsSent()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                            String.format("Received and processed all the %d PolicyOutputEvents", numOfPolicyOutputEventsReceived)));
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("All PolicyOutputEvent has been received, but not all processed, processed %d out of %d messages",
                                numOfPolicyOutputEventsReceived, execution.getNumOptimizationElementsSent())));
                }
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Receiving kafka messages has timed-out, processed %d out of %d messages",
                            numOfPolicyOutputEventsReceived, execution.getNumOptimizationElementsSent())));
            }
        }
    }

    private Set<Long> getAllSectorIds() throws SQLException {
        final List<PolicyOutputEvent> policyOutputEvents = optimizationsDao.getOptimizations(execution.getId());
        return policyOutputEvents.stream()
                .map(PolicyOutputEvent::getSectorId)
                .collect(Collectors.toSet());
    }

    //for unit testing only
    void setFlmPolicyInputEventHandler(final FlmPolicyInputEventHandler flmPolicyInputEventHandler) {
        this.flmPolicyInputEventHandler = flmPolicyInputEventHandler;
    }

    //for unit testing only
    void setOptimizationsDao(final OptimizationsDao optimizationsDao) {
        this.optimizationsDao = optimizationsDao;
    }

    //for unit testing only
    void setExecutionConsumerHandler(final ExecutionConsumerHandler executionConsumerHandler) {
        this.executionConsumerHandler = executionConsumerHandler;
    }

    //for unit testing only
    void setCmStore(final CmStore cmStore) {
        this.cmStore = cmStore;
    }

    //for unit testing only
    void setExecution(final Execution execution) {
        this.execution = execution;
    }

    //for unit testing only
    void setPersistenceHandler(final PersistenceHandler persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
    }

    //for unit testing only
    void setExecutionDao(final ExecutionDao executionDao) {
        this.executionDao = executionDao;
    }

    //for unit testing only
    void setCellKpiCollection(final CellKpiCollection cellKpiCollection) {
        this.cellKpiCollection = cellKpiCollection;
    }

    //for unit testing only
    void setCellSettingCollection(final CellSettingCollection cellSettingsCollection) {
        this.cellSettingsCollection = cellSettingsCollection;
    }

    //for unit testing only
    void setFlmMetricHelper(final FlmMetricHelper flmMetricHelper) {
        this.flmMetricHelper = flmMetricHelper;
    }
}
