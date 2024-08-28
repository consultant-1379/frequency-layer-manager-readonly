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

package com.ericsson.oss.services.sonom.flm.pa.policy;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.messagehandler.ExecutionConsumerHandler;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionConsumerController;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAStageExecutor;
import com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiReader;
import com.ericsson.oss.services.sonom.flm.pa.policy.kpi.PAKpiRetriever;
import com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PACellIdRetrieverExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * PAPolicyExecutor is used to create and send the {@link PaPolicyInputEvent} to Policy Engine for PA.
 */
public class PAPolicyExecutor implements PAStageExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PAPolicyExecutor.class);
    private static final int TIMEOUT_SEC = Integer.parseInt(getEnvironmentValue("KAFKA_CONSUME_MESSAGE_TIMEOUT_SEC", "300"));
    private PAExecution paExecution;
    private FlmPaPolicyInputEventHandler flmPaPolicyInputEventHandler;
    private PAStageExecutor<Map<Long, List<TopologyObjectId>>> paCellIdRetrieverExecutor;
    private ExecutionConsumerHandler paExecutionConsumerHandler;
    private PAOutputEventDao paOutputEventDao;
    private PAKpiReader paKpiReader;
    private PAExecutionLatch latch;

    /**
     * Constructor for PAPolicyExecutor.
     *
     * @param executionDao
     *            {@link PAExecutionDao} instance for {@link FlmPaPolicyInputEventHandler}
     * @param flmExecution
     *            {@link Execution} instance to get FLM execution related information
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param paOutputEventDao
     *            {@link PAOutputEventDao} instance to get PA output event related information
     * @param latch
     *            {@link PAExecutionLatch} instance to check for an interruption signal
     */
    public PAPolicyExecutor(final PAExecutionDao executionDao, final Execution flmExecution,
            final PAExecution paExecution, final PAOutputEventDao paOutputEventDao, final PAExecutionLatch latch) {
        this.paExecution = paExecution;
        this.latch = latch;
        flmPaPolicyInputEventHandler = new FlmPaPolicyInputEventHandler(executionDao);
        paExecutionConsumerHandler = PAExecutionConsumerController.getOrCreate().getPAExecutionConsumerHandler();
        paCellIdRetrieverExecutor = new PACellIdRetrieverExecutor(paExecution, flmExecution);
        final PAKpiRetriever paKpiRetriever = new PAKpiRetriever();
        paKpiReader = new PAKpiReader(paExecution, flmExecution, paKpiRetriever, latch);
        this.paOutputEventDao = paOutputEventDao;
    }

    /**
     * Constructor used for unit testing PAPolicyExecutor.
     */
    PAPolicyExecutor() {
    }

    @Override
    public Object execute() throws PAExecutionException {
        final Map<Long, List<TopologyObjectId>> sectorsAndCells = paCellIdRetrieverExecutor.execute();
        if (sectorsAndCells.isEmpty()) {
            LOGGER.info("No Sectors and Cells retrieved for PA Execution {}.", paExecution.getId());
        } else {
            try {
                final List<Sector> sectors = paKpiReader.generateSectorList(sectorsAndCells);
                latch.verifyNotInterruptedAndContinue();

                final List<String> paPolicyInputEvents = generateInputEvents(sectors);
                latch.verifyNotInterruptedAndContinue();

                paExecutionConsumerHandler.consumeMessagesForExecution(paExecution.getId(), paPolicyInputEvents.size(), getAllSectorIds());
                latch.verifyNotInterruptedAndContinue();

                flmPaPolicyInputEventHandler.sendToKafkaTopic(paPolicyInputEvents, paExecution);
                latch.verifyNotInterruptedAndContinue();

                receiveKafkaMessages();
            } catch (final PAExecutionInterruptedException e) { //NOSONAR Exception suitably logged
                LOGGER.warn("Interrupt signal received. Terminating PAPolicyExecutor");
                throw e;
            } catch (final Exception e) {
                throw new PAExecutionException(e);
            }
        }
        return null;
    }

    private Set<Long> getAllSectorIds() throws SQLException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = paOutputEventDao.getPaPolicyOutputEventById(paExecution.getId());
        return paPolicyOutputEvents.stream()
                .map(PaPolicyOutputEvent::getSector)
                .map(Sector::getSectorIdAsLong)
                .collect(Collectors.toSet());
    }

    private List<String> generateInputEvents(final List<Sector> sectors) {
        final List<String> paPolicyInputEvents = new ArrayList<>();
        for (final Sector sector : sectors) {
            paPolicyInputEvents
                    .add(new PaPolicyInputEvent(paExecution.getFlmExecutionId(), paExecution.getId(), paExecution.getPaWindow(), sector).toJson());
        }
        return paPolicyInputEvents;
    }

    private void receiveKafkaMessages() throws PAExecutionException, InterruptedException {

        try {
            if (paExecutionConsumerHandler == null) {
                throw new PAExecutionException("Error receiving records from Policy Engine");

            }
            final String paExecutionId = paExecution.getId();
            final boolean noTimeOut = paExecutionConsumerHandler.waitMessages(
                    paExecutionId, TimeUnit.SECONDS.toMillis(TIMEOUT_SEC));
            final int numOfPAPolicyOutputEventsReceived = paOutputEventDao.getPaPolicyOutputEventById(paExecutionId).size();
            if (noTimeOut) {
                if (numOfPAPolicyOutputEventsReceived == paExecution.getNumPaPolicyInputEventsSent()) {
                    LOGGER.info("Received and processed all PAPolicyOutputEvents for the PA Execution {}", paExecutionId);
                } else {
                    LOGGER.warn("All PAPolicyOutputEvents have been received, but not all processed for the PA Execution {}, " +
                            "processed {} out of {} messages", paExecutionId, numOfPAPolicyOutputEventsReceived,
                            paExecution.getNumPaPolicyInputEventsSent());
                }
            } else {
                LOGGER.info("Receiving kafka messages has timed-out for the PA execution {}, processed {} out of {} messages",
                        paExecutionId, numOfPAPolicyOutputEventsReceived, paExecution.getNumPaPolicyInputEventsSent());
            }
        } catch (final SQLException e) {
            LOGGER.error("Failed to retrieve records from database ", e);
        }
    }

    static String timestampToUtcString(final Timestamp timestamp) {
        return timestamp.toLocalDateTime().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    PAPolicyExecutor withPaExecution(final PAExecution paExecution) {
        this.paExecution = paExecution;
        return this;
    }

    PAPolicyExecutor withFlmPaPolicyInputEventHandler(final FlmPaPolicyInputEventHandler flmPaPolicyInputEventHandler) {
        this.flmPaPolicyInputEventHandler = flmPaPolicyInputEventHandler;
        return this;
    }

    PAPolicyExecutor withPAStageExecutor(final PAStageExecutor<Map<Long, List<TopologyObjectId>>> paStageExecutor) {
        paCellIdRetrieverExecutor = paStageExecutor;
        return this;
    }

    PAPolicyExecutor withExecutionConsumerHandler(final ExecutionConsumerHandler paExecutionConsumerHandler) {
        this.paExecutionConsumerHandler = paExecutionConsumerHandler;
        return this;
    }

    PAPolicyExecutor withPAOutputEventDao(final PAOutputEventDao paOutputEventDao) {
        this.paOutputEventDao = paOutputEventDao;
        return this;
    }

    PAPolicyExecutor withPAKpiReader(final PAKpiReader paKpiReader) {
        this.paKpiReader = paKpiReader;
        return this;
    }

    PAPolicyExecutor withPAExecutionLatch(final PAExecutionLatch latch) {
        this.latch = latch;
        return this;
    }
}