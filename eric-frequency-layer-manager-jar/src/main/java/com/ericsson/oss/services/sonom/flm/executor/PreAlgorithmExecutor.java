/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.executor;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.PARTIALLY_SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SUCCEEDED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.SCHEDULED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.STARTED;
import static com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState.TERMINATING;
import static io.github.resilience4j.retry.RetryConfig.custom;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionSummary;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

/**
 * Pre execution handler.
 * Used to check and stop any {@link PAExecution}s {@code SCHEDULED} or {@code STARTED} for the previous {@link Execution}.
 * Handler will not allow the {@link Execution} to continue until previous {@link PAExecution}s are in a final state.
 */
public class PreAlgorithmExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreAlgorithmExecutor.class);
    private static final List<PAExecutionState> RUNNING_PA_STATES = Arrays.asList(SCHEDULED, STARTED, TERMINATING);
    private static final String RETRY_CONFIG_NAME = "preAlgorithmExecutor";
    private static final Integer RETRY_MAX_ATTEMPTS = 120;
    private static final Integer RETRY_WAIT_DURATION = 30;
    private static final Long TIMEOUT_OFFSET = TimeUnit.HOURS.toMillis(1);
    private static final String EXHAUSTED_RETRIES_MESSAGE = "Exhausted retries waiting for PA executions for previous execution {} to terminate";

    private final Execution execution;
    private final ExecutionDao executionDao;
    private final PAExecutionDao paExecutionDao;

    PreAlgorithmExecutor(final Execution execution, final ExecutionDao executionDao) {
        this(execution, executionDao, new PAExecutionDaoImpl(3, 10));
    }

    PreAlgorithmExecutor(final Execution execution, final ExecutionDao executionDao, final PAExecutionDao paExecutionDao) {
        this.execution = execution;
        this.executionDao = executionDao;
        this.paExecutionDao = paExecutionDao;
    }

    void runPreExecutionSteps() throws FlmAlgorithmException {
        if (execution.getState() != ExecutionState.getInitialState() || !execution.isFullExecution()) {
            return;
        }

        try {
            final ExecutionSummary previousExecution = executionDao.getExecutionsInStates(SUCCEEDED, PARTIALLY_SUCCEEDED)
                    .stream()
                    .filter(Execution::isEnablePA)
                    .filter(otherExecution -> otherExecution.getConfigurationId().equals(execution.getConfigurationId()))
                    .max(Comparator.comparing(Execution::getStartTime))
                    .orElse(null);

            if (previousExecution == null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                            "No previous successful execution with PA enabled for configuration {}. Continuing with execution."),
                            execution.getConfigurationId());
                }
                return;
            }
            final Long timeout = System.currentTimeMillis() + TIMEOUT_OFFSET;
            final Retry retry = Retry.of(RETRY_CONFIG_NAME, retryConfig(timeout));
            final CheckedFunction0<Boolean> decoratedRetry = Retry
                    .decorateCheckedSupplier(retry, () -> allPreviousPaExecutionsFinished(previousExecution));

            final boolean result = Try.of(decoratedRetry)
                    .getOrElseThrow(() -> {
                        LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), EXHAUSTED_RETRIES_MESSAGE), previousExecution.getId());

                        return new FlmAlgorithmException(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES);
                    });

            if (!result) {
                if (LOGGER.isErrorEnabled()) {

                    LOGGER.error(LoggingFormatter.formatMessage(execution.getId(), EXHAUSTED_RETRIES_MESSAGE), previousExecution.getId());
                }
                final List<PAExecution> prePAExecutions = paExecutionDao.getPAExecutions(previousExecution.getId());
                prePAExecutions.forEach(prePAExecution -> {
                    if (prePAExecution.getState() == TERMINATING) {
                        LOGGER.info("Current state is: {}. Updating state to failed for PA execution {}",
                                    prePAExecution.getState(),
                                    prePAExecution.getId());
                        prePAExecution.setState(PAExecutionState.FAILED);
                        try {
                            paExecutionDao.update(prePAExecution);
                        } catch (final SQLException e) {
                            LOGGER.error("Error updating state to FAILED for executions. {}", prePAExecution.getId());
                        }
                        LOGGER.info("Updated state is: {}  for PA execution {} ", prePAExecution.getState(), prePAExecution.getId());
                    }
                });
                throw new FlmAlgorithmException(FlmServiceExceptionCode.EXHAUSTED_TERMINATING_PA_RETRIES);
            }
        } catch (final SQLException e) {
            LOGGER.error("Error fetching previous successful executions. {}", e.getLocalizedMessage());
            throw new FlmAlgorithmException(FlmServiceExceptionCode.EXECUTION_STATE_RETRIEVAL_ERROR, e);
        }
    }

    private boolean allPreviousPaExecutionsFinished(final ExecutionSummary prevExecution) throws ActivitySchedulerException, SQLException {
        final Map<PAExecutionState, List<PAExecution>> nonFinalExecutions = paExecutionDao.getPAExecutions(prevExecution.getId())
                .stream()
                .filter(paExecution -> RUNNING_PA_STATES.contains(paExecution.getState()))
                .collect(Collectors.groupingBy(PAExecution::getState, Collectors.mapping(Function.identity(), Collectors.toList())));

        if (nonFinalExecutions.isEmpty()) {
            return true;
        }

        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                    "There are PA Executions for previous execution {} that are in state {}."),
                    prevExecution.getId(), getRunningPaStatesString());
        }

        if (nonFinalExecutions.containsKey(SCHEDULED) && !nonFinalExecutions.get(SCHEDULED).isEmpty()) {
            PAExecutionsScheduler.cancelScheduledExecutions(nonFinalExecutions.get(SCHEDULED));
        }
        if (nonFinalExecutions.containsKey(STARTED) && !nonFinalExecutions.get(STARTED).isEmpty()) {
            PAExecutionsScheduler.terminateRunningExecution(nonFinalExecutions.get(STARTED));
        }

        return false;
    }

    private static String getRunningPaStatesString() {
        final int lastPosition = RUNNING_PA_STATES.size() - 1;
        return RUNNING_PA_STATES.stream()
                .map(PAExecutionState::name)
                .limit(lastPosition)
                .collect(Collectors.joining(", ", "", " or "))
                .concat(RUNNING_PA_STATES.get(lastPosition).name());
    }

    private static RetryConfig retryConfig(final Long timeout) {
        return custom()
                .retryOnResult(retryPredicate(timeout))
                .retryExceptions(SQLException.class, ActivitySchedulerException.class)
                .maxAttempts(RETRY_MAX_ATTEMPTS)
                .waitDuration(Duration.ofSeconds(RETRY_WAIT_DURATION))
                .build();
    }

    private static Predicate<Object> retryPredicate(final Long timeout) {
        return s -> s.equals(false) && System.currentTimeMillis() < timeout;
    }
}
