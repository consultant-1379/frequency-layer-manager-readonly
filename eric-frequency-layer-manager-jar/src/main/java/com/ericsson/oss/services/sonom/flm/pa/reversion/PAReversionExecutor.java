/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.reversion;

import static com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants.DEGRADED;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus;
import com.ericsson.oss.services.sonom.cm.service.change.api.Metadata;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmRestClientCreator;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.loadbalancing.SourceOfChangeCalculator;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAStageExecutor;
import com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradedCellKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradedSectorKpi;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementRetriever;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSender;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSenderImpl;
import com.ericsson.oss.services.sonom.flm.util.metadata.CellKpiMetadata;
import com.ericsson.oss.services.sonom.flm.util.metadata.FlmMetadata;
import com.ericsson.oss.services.sonom.flm.util.metadata.SectorKpiMetadata;
import com.google.gson.Gson;

/**
 * PAReversionExecutor is used to revert the optimizations done by FLM for sectors which are evaluated to be degraded.
 */
public class PAReversionExecutor implements PAStageExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PAReversionExecutor.class);
    private static final Gson GSON = new Gson();
    private PAExecution paExecution;
    private PAOutputEventDao paOutputEventDao;
    private ChangeElementRetriever changeElementRetriever;
    private ChangeElementSender changeElementSender;
    private PAExecutionLatch latch;

    /**
     * Constructor for PAReversionExecutor.
     *
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param paOutputEventDao
     *            {@link PAOutputEventDao} instance to get {@link PaPolicyOutputEvent} for the {@link PAExecution}
     * @param flmConfigurationId
     *            {@link Execution} configuration id of the FLM execution
     * @param latch
     *            {@link PAExecutionLatch} instance to check for an interruption signal
     */
    public PAReversionExecutor(final PAExecution paExecution, final PAOutputEventDao paOutputEventDao,
            final Integer flmConfigurationId, final PAExecutionLatch latch) {
        this.paExecution = paExecution;
        this.latch = latch;
        this.paOutputEventDao = paOutputEventDao;
        final CmRestExecutor cmRestExecutor = new CmRestClientCreator(PAConstants.MAX_RETRY_ATTEMPTS,
                PAConstants.SECONDS_TO_WAIT).getClientWithRetry();
        final String sourceOfChange = SourceOfChangeCalculator.calculateSourceOfChange(flmConfigurationId);
        changeElementRetriever = new ChangeElementRetriever(paExecution, cmRestExecutor, sourceOfChange);
        changeElementSender = new ChangeElementSenderImpl();
    }

    /**
     * Constructor used for unit testing PAReversionExecutor.
     */
    PAReversionExecutor() {

    }

    @Override
    public Object execute() throws PAExecutionException {
        try {
            final Set<Long> degradedSectorIds = getAllDegradedSectorIds();

            if (degradedSectorIds.isEmpty()) {
                LOGGER.info("No degraded sectors retrieved for PA Execution ID: {}.", paExecution.getId());
                return null;
            }

            LOGGER.info("{} degraded sectors retrieved for PA Execution ID: {}.", degradedSectorIds.size(), paExecution.getId());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("List of degraded sectors retrieved for PA Execution ID: {}, {}.", paExecution.getId(), degradedSectorIds);
            }

            latch.verifyNotInterruptedAndContinue();

            final List<ChangeElement> changeElements = changeElementRetriever.retrieveReversionChangeElementList();

            latch.verifyNotInterruptedAndContinue();
            final List<ChangeElement> filteredChangeElements = changeElements.stream()
                    .filter(changeElement -> degradedSectorIds.contains(Long.parseLong(changeElement.getChangeId()))
                            && (changeElement.getStatus().equals(ChangeElementStatus.FAILED.getValue())
                                    || changeElement.getStatus().equals(ChangeElementStatus.PENDING_APPROVAL.getValue())))
                    .collect(Collectors.toList());

            if (filteredChangeElements.isEmpty()) {
                LOGGER.info("No reversion change elements remaining for PA Execution ID '{}' after filtering", paExecution.getId());
                return null;
            }
            LOGGER.info("Retrieved {} filtered reversion change elements for PA Execution ID: {}.", filteredChangeElements.size(),
                    paExecution.getId());

            latch.verifyNotInterruptedAndContinue();
            final List<PaPolicyOutputEvent> listOfPaPolicyOutputEvents = getAllDegradedSectorKpis();

            for (final ChangeElement changeElement : filteredChangeElements) {
                latch.verifyNotInterruptedAndContinue();
                changeElement.setStatus(ChangeElementStatus.PROPOSED.getValue());
                if (!listOfPaPolicyOutputEvents.isEmpty()) {
                    final List<PaPolicyOutputEvent> changeElementsPaPolicyOutputEvent = listOfPaPolicyOutputEvents.stream()
                            .filter(paPolicyOutputEvent -> paPolicyOutputEvent.getSector().getSectorId().equals(changeElement.getChangeId()))
                            .collect(Collectors.toList());
                    changeElement.setMetadata(new Metadata(updateChangeElementWithMetadata(changeElement,
                            changeElementsPaPolicyOutputEvent, paExecution.getPaWindow())));
                }
            }
            LOGGER.info("Setting all degraded sectors reversion change elements status to PROPOSED");

            latch.verifyNotInterruptedAndContinue();
            changeElementSender.updateChangeElements(paExecution.getFlmExecutionId(), filteredChangeElements);
            LOGGER.info("Successfully set all degraded sectors reversion change elements status to PROPOSED");

        } catch (final PAExecutionInterruptedException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Interrupt signal received. Terminating PAReversionExecutor");
            throw e;
        } catch (final SQLException | FlmAlgorithmException e) {
            throw new PAExecutionException(e);
        }
        return null;
    }

    static String updateChangeElementWithMetadata(final ChangeElement changeElement, final List<PaPolicyOutputEvent> listOfPaPolicyOutputEvents,
            final Integer paWindow) {
        final FlmMetadata flmMetadata = new FlmMetadata(paWindow);
        listOfPaPolicyOutputEvents.forEach(paPolicyOutputEvent -> {
            final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps = retrieveListOfAffectedSectorKpisWithTimestamps(changeElement,
                    paPolicyOutputEvent);
            flmMetadata.addToListOfAffectedSectorKpisWithTimestamps(listOfAffectedSectorKpisWithTimestamps);

            final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps = retrieveListOfAffectedCellKpisWithTimestamps(paPolicyOutputEvent);
            flmMetadata.addToListOfAffectedCellKpisWithTimestamps(listOfAffectedCellKpisWithTimestamps);

        });
        return GSON.toJson(flmMetadata);
    }

    static List<SectorKpiMetadata> retrieveListOfAffectedSectorKpisWithTimestamps(final ChangeElement changeElement,
            final PaPolicyOutputEvent paPolicyOutputEvent) {
        final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps = new ArrayList<>();
        final String sectorId = changeElement.getChangeId();
        final Map<String, DegradedSectorKpi> degradedSectorKpis = paPolicyOutputEvent.getDegradationStatus().getDegradedSectorKpis();
        if (degradedSectorKpis != null) {
            degradedSectorKpis.forEach((sectorKpiName, degradedSectorKpi) -> {
                final Map<String, List<String>> sectorIdToDegradedTimestamps = degradedSectorKpi.getSectorIdToDegradedTimestamps();
                final List<String> listOfTimestampsForDegradedSectorKpi = sectorIdToDegradedTimestamps.get(sectorId);
                if (listOfTimestampsForDegradedSectorKpi != null) {
                    listOfAffectedSectorKpisWithTimestamps.add(new SectorKpiMetadata(sectorKpiName, sectorId, listOfTimestampsForDegradedSectorKpi));
                }
            });
        }
        return listOfAffectedSectorKpisWithTimestamps;
    }

    static List<CellKpiMetadata> retrieveListOfAffectedCellKpisWithTimestamps(final PaPolicyOutputEvent paPolicyOutputEvent) {
        final Map<String, DegradedCellKpi> degradedCellKpis = paPolicyOutputEvent.getDegradationStatus().getDegradedCellKpis();
        if (degradedCellKpis == null) {
            return Collections.emptyList();
        }

        final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps = new ArrayList<>();

        degradedCellKpis.forEach((cellKpiName, degradedCellKpi) -> {
            final Map<String, Map<String, List<String>>> ossIdToFdnToDegradedTimestamps = degradedCellKpi.getOssIdToFdnToDegradedTimestamps();
            Integer ossId = -1;
            String fdn = "";
            if (ossIdToFdnToDegradedTimestamps.keySet().iterator().hasNext()) {
                ossId = Integer.valueOf(ossIdToFdnToDegradedTimestamps.keySet().iterator().next());
            }
            final Map<String, List<String>> fdnToDegradedTimestamps = ossIdToFdnToDegradedTimestamps.get(String.valueOf(ossId));
            if (fdnToDegradedTimestamps.keySet().iterator().hasNext()) {
                fdn = fdnToDegradedTimestamps.keySet().iterator().next();
            }
            if (fdnToDegradedTimestamps.isEmpty()) {
                return;
            }
            final List<String> degradedTimestamps = fdnToDegradedTimestamps.get(String.valueOf(fdn));
            if (!degradedTimestamps.isEmpty()) {
                listOfAffectedCellKpisWithTimestamps
                        .add(new CellKpiMetadata(cellKpiName, fdn, ossId, degradedTimestamps));
            }
        });
        return listOfAffectedCellKpisWithTimestamps;
    }

    private Set<Long> getAllDegradedSectorIds() throws SQLException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = paOutputEventDao.getPaPolicyOutputEventById(paExecution.getId());
        return paPolicyOutputEvents.stream()
                .filter(paPolicyOutputEvent -> paPolicyOutputEvent.getDegradationStatus().getVerdict().equals(DEGRADED))
                .map(PaPolicyOutputEvent::getSector)
                .map(Sector::getSectorIdAsLong)
                .collect(Collectors.toSet());
    }

    private List<PaPolicyOutputEvent> getAllDegradedSectorKpis() throws SQLException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = paOutputEventDao.getPaPolicyOutputEventById(paExecution.getId());
        return paPolicyOutputEvents.stream()
                .filter(paPolicyOutputEvent -> paPolicyOutputEvent.getDegradationStatus().getVerdict().equals(DEGRADED))
                .collect(Collectors.toList());
    }

    PAReversionExecutor withPAExecution(final PAExecution paExecution) {
        this.paExecution = paExecution;
        return this;
    }

    PAReversionExecutor withPAOutputEventDao(final PAOutputEventDao paOutputEventDao) {
        this.paOutputEventDao = paOutputEventDao;
        return this;
    }

    PAReversionExecutor withChangeElementRetriever(final ChangeElementRetriever changeElementRetriever) {
        this.changeElementRetriever = changeElementRetriever;
        return this;
    }

    PAReversionExecutor withChangeElementSender(final ChangeElementSender changeElementSender) {
        this.changeElementSender = changeElementSender;
        return this;
    }

    PAReversionExecutor withPAExecutionLatch(final PAExecutionLatch latch) {
        this.latch = latch;
        return this;
    }

}