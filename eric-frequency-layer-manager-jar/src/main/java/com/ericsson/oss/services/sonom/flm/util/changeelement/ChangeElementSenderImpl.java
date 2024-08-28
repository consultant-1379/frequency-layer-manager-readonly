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

package com.ericsson.oss.services.sonom.flm.util.changeelement;

import static com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode.CHANGE_ELEMENTS_SENDING_ERROR;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.google.common.collect.Lists;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * ChangeElementSenderImpl is used for creating and updating {@link ChangeElement}s using {@link CmRestExecutor}.
 */
public class ChangeElementSenderImpl implements ChangeElementSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeElementSenderImpl.class);

    private static final String LOG_SENDING_CHANGE_ELEMENTS = "Sending Change Elements: %s";
    private static final String LOG_CHANGE_ELEMENTS_SENDING_STARTED = "Change element sending started with %d element(s), %d batch(es) created";
    private static final String LOG_SENDING_BATCH = "Sending #%d/%d batch";
    private static final String SENDING_RESULTS = "Sending results: %s";
    private static final String LOG_CHANGE_ELEMENTS_SENDING_FINISHED = "Change elements sending finished,"
            + " %d Optimization Elements in %d batch(es) sent successfully, %d batches already exists, %d batches failed";
    private static final String LOG_CHANGE_ELEMENT_ALREADY_EXISTS = "The sent change element is already exists in the DB, execution ID: {}";
    private static final String LOG_CM_SERVICE_RESPONSE = "Response from CM Service: %s";
    private static final String LOG_MAX_ATTEMPTS_REACHED = "Change Elements could not be sent after %d attempt(s), status code: %s";
    private static final String LOG_NON_RECOVERABLE_STATUS_RECEIVED = "Change Elements could not be sent, due to non recoverable status code: %s";

    private static final Set<Integer> RECOVERABLE_STATUS_CODES = new HashSet<>(
            Arrays.asList(HttpStatus.SC_NOT_FOUND, HttpStatus.SC_INTERNAL_SERVER_ERROR));

    // Note: this value is based on a worst case scenario measurement towards CM Service
    private static final int BATCH_SIZE = 700;

    private final CmRestExecutor cmRestExecutor;
    private int maxRetryAttempts = 10;
    private int secondsToWait = 30;

    /**
     * The {@link CmRestExecutor} instance is created here with the use of {@link Retry} with default 10 retry attempts and 30 seconds delay.
     */
    public ChangeElementSenderImpl() {
        cmRestExecutor = new CmRestExecutor.Builder().withRetry(getChangeRetry()).build();
    }

    /**
     * Will be used for testing purposes only.
     *
     * @param cmRestExecutor
     *            an instance of {@link CmRestExecutor}
     */
    public ChangeElementSenderImpl(final CmRestExecutor cmRestExecutor) {
        this.cmRestExecutor = cmRestExecutor;
    }

    /**
     * Will be used for testing purposes only.
     *
     * @param maxRetryAttempts
     *            number of attempts to connect to make
     * @param secondsToWait
     *            number of seconds to wait for connection to be established
     */
    ChangeElementSenderImpl(final int maxRetryAttempts, final int secondsToWait) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.secondsToWait = secondsToWait;
        cmRestExecutor = new CmRestExecutor.Builder().withRetry(getChangeRetry()).build();
    }

    @Override
    public Map<ChangeElementState, Integer> postChangeElements(final String executionId,
            final List<Pair<ChangeElement, ChangeElement>> changeElements) throws FlmAlgorithmException {
        final List<List<ChangeElement>> batchedList = getChangeElementsInBatches(changeElements);
        final List<SendingResult> sendingResults = sendChangeElements(executionId, batchedList, this::send);
        return processResults(executionId, sendingResults);
    }

    @Override
    public Map<ChangeElementState, Integer> updateChangeElements(final String executionId, final List<ChangeElement> changeElements)
            throws FlmAlgorithmException {
        final List<List<ChangeElement>> batchedList = Lists.partition(changeElements, BATCH_SIZE);
        final List<SendingResult> sendingResults = sendChangeElements(executionId, batchedList, this::sendUpdate);
        return processResults(executionId, sendingResults);
    }

    private List<SendingResult> sendChangeElements(final String executionId, final List<List<ChangeElement>> batchedList,
            final CheckedExceptionFunction2<String, List<ChangeElement>, SendingResult, FlmAlgorithmException> function)
            throws FlmAlgorithmException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(executionId, String.format(LOG_SENDING_CHANGE_ELEMENTS, batchedList)));
        }

        final int changeElementCount = batchedList.stream().mapToInt(List::size).sum();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                    String.format(LOG_CHANGE_ELEMENTS_SENDING_STARTED, changeElementCount, batchedList.size())));
        }

        final List<SendingResult> sendingResults = new ArrayList<>();
        final int numberOfBatches = batchedList.size();

        for (int i = 0; i < numberOfBatches; ++i) {
            final int actualBatchNumber = i + 1;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId,
                        String.format(LOG_SENDING_BATCH, actualBatchNumber, numberOfBatches)));
            }

            final SendingResult result = function.apply(executionId, batchedList.get(i));
            result.setBatchNumber(actualBatchNumber);
            sendingResults.add(result);
        }

        return sendingResults;
    }

    /**
     * Will be used for testing purposes only.
     *
     * @return constant batch size value
     */
    static int getBatchSize() {
        return BATCH_SIZE;
    }

    private static boolean retryResponse(final Object object) {
        if (Objects.isNull(object)) {
            LOGGER.warn("Failed to send changes (response was null), retrying");
            return true;
        }

        try (final RestResponse response = (RestResponse) object) {
            final int statusCode = response.getStatus();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Send change element returned status code {}", statusCode);
            }

            return responseIsRecoverable(statusCode);
        } catch (final Exception e) {
            LOGGER.warn("Failed to send changes (unknown status code), retrying", e);
            return true;
        }
    }

    private static boolean responseIsRecoverable(final int statusCode) {
        final boolean responseIsRecoverable = RECOVERABLE_STATUS_CODES.contains(statusCode);

        if (responseIsRecoverable) {
            LOGGER.warn("Failed to send changes (status code: {}), retrying", statusCode);
        }

        return responseIsRecoverable;
    }

    private List<List<ChangeElement>> getChangeElementsInBatches(final List<Pair<ChangeElement, ChangeElement>> changeElementPairs) {
        final Integer batchSizeForPairs = BATCH_SIZE / 2;
        final List<List<Pair<ChangeElement, ChangeElement>>> batchedPairs = Lists.partition(changeElementPairs, batchSizeForPairs);
        final List<List<ChangeElement>> batchedChangeElements = new ArrayList<>(batchedPairs.size() * 2);

        batchedPairs.forEach(list -> {
            final List<ChangeElement> changeElements = new ArrayList<>();
            list.forEach(pair -> {
                changeElements.add(pair.getKey());
                changeElements.add(pair.getValue());
            });
            batchedChangeElements.add(changeElements);
        });
        return batchedChangeElements;
    }

    private Map<ChangeElementState, Integer> processResults(final String executionId,
            final List<SendingResult> sendingResults) throws FlmAlgorithmException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                    String.format(SENDING_RESULTS, sendingResults)));
        }

        final int numberOfBatches = sendingResults.size();

        int sentChanges = 0;
        int sentBatches = 0;
        for (final SendingResult result : sendingResults) {
            if (result.isSent()) {
                sentBatches++;
                sentChanges += result.getNumberOfOptimizationsPerBatch();
            }
        }

        final int exists = (int) sendingResults.stream()
                .filter(SendingResult::isExists)
                .filter(result -> !result.isSent())
                .count();
        int failedBatches = 0;
        int failedChanges = 0;
        for (final SendingResult result : sendingResults) {
            if (!result.isSent() && !result.isExists()) {
                failedBatches++;
                failedChanges += result.getNumberOfOptimizationsPerBatch();
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId,
                    String.format(LOG_CHANGE_ELEMENTS_SENDING_FINISHED, sentChanges, sentBatches, exists, failedBatches)));
        }

        if (failedBatches == numberOfBatches) {
            throw new FlmAlgorithmException(CHANGE_ELEMENTS_SENDING_ERROR, "Could not send any batch of the received list");
        }

        final EnumMap<ChangeElementState, Integer> changeElementsMap = new EnumMap<>(ChangeElementState.class);
        changeElementsMap.put(ChangeElementState.SENT, sentChanges);
        changeElementsMap.put(ChangeElementState.EXISTS, exists);
        changeElementsMap.put(ChangeElementState.FAILED, failedChanges);

        return changeElementsMap;
    }

    private SendingResult sendUpdate(final String executionId, final List<ChangeElement> changeElements) throws FlmAlgorithmException {
        final int numberOfOptimizationsPerBatch = (int) changeElements.stream()
                .filter(c -> c.getChangeType().equals(ChangeElement.ChangeType.OPTIMIZATION)).count();
        try (final RestResponse response = cmRestExecutor.updateChangeElementsWithResponse(changeElements)) {
            final int responseStatus = response.getStatus();
            boolean sent = true;
            if (responseStatus != HttpStatus.SC_OK) {
                sent = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                            String.format(LOG_CM_SERVICE_RESPONSE, response)));
                }
                if (RECOVERABLE_STATUS_CODES.contains(responseStatus)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                                String.format(LOG_MAX_ATTEMPTS_REACHED, maxRetryAttempts, responseStatus)));
                    }
                } else {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                                String.format(LOG_NON_RECOVERABLE_STATUS_RECEIVED, responseStatus)));
                    }
                }
            }
            return new SendingResult(sent, false, numberOfOptimizationsPerBatch);
        } catch (final Exception e) {
            throw new FlmAlgorithmException(CHANGE_ELEMENTS_SENDING_ERROR, e);
        }
    }

    private SendingResult send(final String executionId, final List<ChangeElement> changeElements) throws FlmAlgorithmException {
        final int numberOfOptimizationsPerBatch = (int) changeElements.stream()
                .filter(c -> c.getChangeType().equals(ChangeElement.ChangeType.OPTIMIZATION)).count();
        try (final RestResponse response = cmRestExecutor.createChangeElementsWithResponse(changeElements)) {
            final int responseStatus = response.getStatus();
            boolean sent = true;
            boolean exists = false;
            if (responseStatus != HttpStatus.SC_CREATED) {
                sent = false;
                exists = logCMRestFailures(executionId, response, responseStatus);
            }
            return new SendingResult(sent, exists, numberOfOptimizationsPerBatch);
        } catch (final Exception e) {
            throw new FlmAlgorithmException(CHANGE_ELEMENTS_SENDING_ERROR, e);
        }
    }

    private boolean logCMRestFailures(final String executionId, final RestResponse response,
            final int responseStatus) {
        if (responseStatus == HttpStatus.SC_CONFLICT) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(executionId, LOG_CHANGE_ELEMENT_ALREADY_EXISTS));
            }
            return true;
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(LoggingFormatter.formatMessage(executionId,
                        String.format(LOG_CM_SERVICE_RESPONSE, response)));
            }
            if (RECOVERABLE_STATUS_CODES.contains(responseStatus)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                            String.format(LOG_MAX_ATTEMPTS_REACHED, maxRetryAttempts, responseStatus)));
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(executionId,
                            String.format(LOG_NON_RECOVERABLE_STATUS_RECEIVED, responseStatus)));
                }
            }
        }
        return false;
    }

    private Retry getChangeRetry() {
        final RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(maxRetryAttempts)
                .waitDuration(Duration.ofSeconds(secondsToWait))
                .retryOnResult(ChangeElementSenderImpl::retryResponse)
                .retryOnException(throwable -> {
                    LOGGER.warn("Failed to send changes (an exception occurred), retrying", throwable);
                    return true;
                })
                .build();

        return Retry.of("sendChangeElement", retryConfig);
    }

    private static class SendingResult {
        private final boolean sent;
        private final boolean exists;
        private int batchNumber;
        private final int numberOfOptimizationsPerBatch;

        SendingResult(final boolean sent, final boolean exists, final int numberOfOptimizationsPerBatch) {
            this.sent = sent;
            this.exists = exists;
            this.numberOfOptimizationsPerBatch = numberOfOptimizationsPerBatch;
        }

        void setBatchNumber(final int batchNumber) {
            this.batchNumber = batchNumber;
        }

        boolean isSent() {
            return sent;
        }

        boolean isExists() {
            return exists;
        }

        int getNumberOfOptimizationsPerBatch() {
            return numberOfOptimizationsPerBatch;
        }

        @Override
        public String toString() {
            return "{batchNumber: " + batchNumber + ", sent: " + sent + ", exists: " + exists + ", numberOfOptimizationsPerBatch: "
                    + numberOfOptimizationsPerBatch + "}";
        }
    }

    /**
     * Interface to wrap method throwing checked exception.
     * 
     * @param <T>
     *            parameter type
     * @param <U>
     *            parameter type
     * @param <R>
     *            return type
     * @param <E>
     *            exception type
     */
    @FunctionalInterface
    private interface CheckedExceptionFunction2<T, U, R, E extends Exception> {
        R apply(T t, U u) throws E;
    }

}