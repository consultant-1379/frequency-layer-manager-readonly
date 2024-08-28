/*
 * -----------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.CHANGE_ELEMENTS_SENDING_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.LOAD_BALANCING_ERROR;
import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.READING_CELL_KPIS_LOAD_BALANCER_ERROR;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.activation.kafka.ActivationPolicy;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.optimization.OverlapInfo;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.executor.StageExecutor;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStoreImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.retriever.PolicyOutputEventRetriever;
import com.ericsson.oss.services.sonom.flm.loadbalancing.retriever.PolicyOutputEventRetrieverImpl;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellFlmKpiRetriever;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.SectorBusyHourRetriever;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.api.util.WeekendDay;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSender;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSenderImpl;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementState;

/**
 * The class implements a {@link StageExecutor}. Will be added to FlmAlgorithmExecutor as another stage It should be able to create
 * {@link ChangeElement}s from the given list of PolicyOutputEvents that hold LoadBalancingQuantas It should be able to handle more types of Load
 * Balancing features not just LBDAR, but for now only LBDAR is supported
 */
public class LoadBalancingExecutor implements StageExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancingExecutor.class);

    private final CmStore cmStore;
    private final PersistenceHandler persistenceHandler;
    private final Execution execution;
    private final MetricHelper flmMetricHelper;
    private final PolicyOutputEventRetriever policyOutputEventRetriever;
    private final SectorBusyHourRetriever sectorBusyHourRetriever;
    private final CellFlmKpiRetriever cellFlmKpiRetriever;
    private final OverLappingFilter overLappingFilter;
    private final ChangeElementSender changeElementSender;
    private final ActivationPolicySender activationPolicySender;
    private LoadBalancingCalculatorFactory factory = new LoadBalancingCalculatorFactory();

    /**
     * This constructor will be used in production environment. Sets the required dependencies for the {@link ChangeElement} creation and instantiates
     * {@link ChangeElementSenderImpl} and {@link ActivationPolicySenderImpl} for sending them to the CM Service change DB and the
     * {@link ActivationPolicy} to ChangeMediation Kafka Topic in case of closed loop.
     *
     * @param execution
     *            {@link Execution}
     * @param cmStore
     *            {@link CmStore}
     * @param flmMetricHelper
     *            {@link MetricHelper} utility class for metrics
     * @param persistenceHandler
     *            {@link PersistenceHandler} PersistenceHandler
     */
    public LoadBalancingExecutor(final Execution execution,
            final CmStore cmStore,
            final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler) {
        this.execution = execution;
        this.cmStore = cmStore;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        this.sectorBusyHourRetriever = new SectorBusyHourRetriever();
        this.cellFlmKpiRetriever = new CellFlmKpiRetriever();
        this.overLappingFilter = new OverLappingFilter();
        this.policyOutputEventRetriever = new PolicyOutputEventRetrieverImpl();
        this.changeElementSender = new ChangeElementSenderImpl();
        this.activationPolicySender = new ActivationPolicySenderImpl();
    }

    /**
     * Will be used for testing purposes only.
     *
     * @param execution
     *            {@link Execution}
     * @param cmStore
     *            {@link CmStore}
     * @param flmMetricHelper
     *            {@link MetricHelper} utility class for metrics
     * @param persistenceHandler
     *            {@link PersistenceHandler} PersistenceHandler
     * @param sectorBusyHourRetriever
     *            {@link SectorBusyHourRetriever}
     * @param cellFlmKpiRetriever
     *            {@link CellFlmKpiRetriever}
     * @param policyOutputEventRetriever
     *            {@link PolicyOutputEventRetriever}
     * @param changeElementSender
     *            {@link ChangeElementSender}
     * @param activationPolicySender
     *            {@link ActivationPolicySender}
     */
    public LoadBalancingExecutor(final Execution execution, //NOSONAR this will be used only at tests
            final CmStore cmStore,
            final MetricHelper flmMetricHelper,
            final PersistenceHandler persistenceHandler,
            final SectorBusyHourRetriever sectorBusyHourRetriever,
            final CellFlmKpiRetriever cellFlmKpiRetriever,
            final PolicyOutputEventRetriever policyOutputEventRetriever,
            final ChangeElementSender changeElementSender,
            final ActivationPolicySender activationPolicySender) {
        this.execution = execution;
        this.cmStore = cmStore;
        this.flmMetricHelper = flmMetricHelper;
        this.persistenceHandler = persistenceHandler;
        this.sectorBusyHourRetriever = sectorBusyHourRetriever;
        this.cellFlmKpiRetriever = cellFlmKpiRetriever;
        this.overLappingFilter = new OverLappingFilter();
        this.policyOutputEventRetriever = policyOutputEventRetriever;
        this.changeElementSender = changeElementSender;
        this.activationPolicySender = activationPolicySender;
    }

    /**
     * Will be used for testing purposes only.
     *
     * @param factory
     *            an instance of {@link LoadBalancingCalculatorFactory}
     */
    public void setLoadBalancingCalculatorFactory(final LoadBalancingCalculatorFactory factory) {
        this.factory = factory;
    }

    @Override
    public void execute(final ExecutionState executionState, final boolean isResumed, final boolean isFullExecution, final String executionDate)
            throws FlmAlgorithmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "Starting LoadBalancing calculation"));
        }
        try {
            if (executionState == ExecutionState.LOAD_BALANCING) {
                createChangeElements(executionDate);
                persistenceHandler.persistExecutionStatus(ExecutionState.LOAD_BALANCING, isResumed);
                execution.setState(ExecutionState.getNextState(isFullExecution, execution.getState()));
            }
        } catch (final Exception e) {
            throw new FlmAlgorithmException(LOAD_BALANCING_ERROR, e);
        }
    }

    private void createChangeElements(final String executionDate) throws FlmAlgorithmException {
        final long changeElementsCreationStartTime = System.nanoTime(); // NOPMD needs to be defined here as is a timer
        final List<PolicyOutputEvent> policyOutputEvents = retrievePolicyOutputEvents();
        if (policyOutputEvents.isEmpty()) {
            return;
        }

        execution.setNumOptimizationLbqs(policyOutputEvents.size());

        final CellKpiStore cellKpiStore;
        try {
            cellKpiStore = loadCellKpiStoreIfNeeded(executionDate, policyOutputEvents);
        } catch (final SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), "Failed to read kpis for cells"));
            }
            throw new FlmAlgorithmException(READING_CELL_KPIS_LOAD_BALANCER_ERROR, e);
        }

        final Optional<LoadBalancingCalculator> lbCalculator = factory.create(LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR,
                execution, execution.getCustomizedGlobalSettings(), cmStore, cellKpiStore, policyOutputEvents);

        final List<Pair<ChangeElement, ChangeElement>> changeElements;
        if (lbCalculator.isPresent()) {
            changeElements = new ArrayList<>(lbCalculator.get().calculateChanges(policyOutputEvents));
        } else {
            throw new FlmAlgorithmException(LOAD_BALANCING_ERROR, "Failed to find LoadBalancingCalculator for type " +
                    LoadBalancingCalculatorFactory.LoadBalancingType.LBDAR);
        }

        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_LOAD_BALANCING_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(changeElementsCreationStartTime));

        if (!changeElements.isEmpty()) {
            final String executionId = execution.getId();
            sendChangeElementsAndUpdateCounter(changeElements, executionId);

            if (execution.isOpenLoop().equals(Boolean.FALSE)) {
                activationPolicySender.sendActivationPolicyToKafka(
                        executionId, SourceOfChangeCalculator.calculateSourceOfChange(execution.getConfigurationId()));
            }
        }
    }

    private void sendChangeElementsAndUpdateCounter(final List<Pair<ChangeElement, ChangeElement>> changeElements, final String executionId)
      throws FlmAlgorithmException {
        final long changeElementsSendingStartTime = System.nanoTime();

        try {
            updateExecutionsChangeElements(changeElementSender.postChangeElements(executionId, changeElements));
            flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT, changeElements.size());
        } catch (final Exception e) {
            throw new FlmAlgorithmException(CHANGE_ELEMENTS_SENDING_ERROR, e);
        } finally {
            flmMetricHelper.incrementFlmMetric(
                    FlmMetric.FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS,
                    flmMetricHelper.getTimeElapsedInMillis(changeElementsSendingStartTime));
        }
    }

    private List<PolicyOutputEvent> retrievePolicyOutputEvents() throws FlmAlgorithmException {
        final List<PolicyOutputEvent> policyOutputEvents;
        try {
            final List<Pair<PolicyOutputEvent, OverlapInfo>> policyOutputEventsWithOverlaps =
                    policyOutputEventRetriever.getPolicyOutputEvents(execution.getId());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Number of non-empty LBQ message is %d", policyOutputEventsWithOverlaps.size())));
            }
            policyOutputEvents = overLappingFilter.filterAndLogOverlapped(policyOutputEventsWithOverlaps);
            if (LOGGER.isDebugEnabled() && policyOutputEvents != null) {
                LOGGER.debug(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Number of PolicyOutputEvents retrieved from database : %d", policyOutputEvents.size())));
            }
        } catch (final SQLException e) {
            throw new FlmAlgorithmException(FlmServiceExceptionCode.OPTIMIZATION_ELEMENT_READING_ERROR, e);
        }

        if (overLappingFilter.allPolicyOutputEventsWereDropped()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "All of the sectors are overlapping and were dropped"));
            }
            throw new FlmAlgorithmException(FlmServiceExceptionCode.ALL_SECTORS_ARE_OVERLAPPING);
        }

        if (policyOutputEvents == null || policyOutputEvents.isEmpty()) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(), "No PolicyOutputEvents are retrieved from database"));
            }
            return Collections.emptyList();
        }
        return policyOutputEvents;
    }

    private CellKpiStore loadCellKpiStoreIfNeeded(final String executionDate,
            final List<PolicyOutputEvent> policyOutputEvents) throws SQLException, FlmAlgorithmException {
        final String queryDate = WeekendDay.of(execution.getWeekendDays())
                .calculateLastBusinessDay(LocalDate.parse(executionDate).minusDays(1))
                .format(DateTimeFormatter.ISO_DATE);
        final List<Long> sectorIds = policyOutputEvents.stream()
                .map(PolicyOutputEvent::getSectorId)
                .collect(Collectors.toList());
        final List<TopologySector> sectors = cmStore.getCmSectorCellStore().getFullSectors().stream()
                .filter(topologySector -> sectorIds.contains(topologySector.getSectorId()))
                .collect(Collectors.toList());
        if (sectorBusyHourRetriever == null || cellFlmKpiRetriever == null) {
            return new CellKpiStoreImpl(queryDate, sectors);
        } else {
            return new CellKpiStoreImpl(sectorBusyHourRetriever, cellFlmKpiRetriever, queryDate, sectors);
        }
    }

    private void updateExecutionsChangeElements(final Map<ChangeElementState, Integer> executionChangeElements) {
        execution.setNumChangesWrittenToCmDb(executionChangeElements.get(ChangeElementState.SENT));
        execution.setNumChangesNotWrittenToCmDb(executionChangeElements.get(ChangeElementState.FAILED));
    }
}
