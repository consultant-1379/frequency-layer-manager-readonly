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
package com.ericsson.oss.services.sonom.flm.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.Environment;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDaoImpl;
import com.ericsson.oss.services.sonom.flm.messagehandler.ExecutionConsumerHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.messagehandler.PoeExecutionConsumerHandlerImpl;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * The ExecutionConsumerController singleton is responsible to create and hold an object of ExecutionConsumerHandler class.
 */
public class ExecutionConsumerController {
    private static final String NUMBER_OF_KAFKA_CONSUMERS = Environment.getEnvironmentValue("KAFKA_CLIENT_NUMBER", "10");
    private static final int OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS = 30;
    private static final int OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionConsumerController.class);

    private static ExecutionConsumerController instance;

    private ExecutionConsumerHandler executionConsumerHandler;
    private List<OptimizationsDao> optimizationsDaos;
    private int numberOfKafkaConsumers;

    //For unit testing only
    ExecutionConsumerController(final KafkaConsumerWrapper<PolicyOutputEvent> kafkaConsumerWrapper,
            final List<OptimizationsDao> optimizationsDaos) {
        setNumberOfKafkaConsumer();
        initOptimizationDaosIfNull(optimizationsDaos);
        createExecutionConsumerHandler(kafkaConsumerWrapper);
    }

    private ExecutionConsumerController(final KafkaConsumerWrapper<PolicyOutputEvent> kafkaCustomerWrapper) {
        setNumberOfKafkaConsumer();
        initOptimizationDaosIfNull();
        createExecutionConsumerHandler(kafkaCustomerWrapper);
    }

    /**
     * Gets or creates an ExecutionConsumerController object (singleton).
     *
     * @return A {@link ExecutionConsumerController} object
     */
    public static ExecutionConsumerController getOrCreate() {
        if (instance == null) {
            instance = new ExecutionConsumerController(null);
        }
        return instance;
    }

    /**
     * For unit testing only, do not call it, it is only public because a unit test out of this package calls it.
     *
     * @param kafkaCustomerWrapper
     *            kafka consumer wrapper
     * @return an instance of {@link ExecutionConsumerController}
     */
    public static ExecutionConsumerController getOrCreate(final KafkaConsumerWrapper<PolicyOutputEvent> kafkaCustomerWrapper) {
        if (instance == null) {
            instance = new ExecutionConsumerController(kafkaCustomerWrapper);
        }
        return instance;
    }

    /**
     * Gets an ExecutionConsumerHandler object.
     *
     * @return A {@link ExecutionConsumerHandler} object
     */
    public ExecutionConsumerHandler getExecutionConsumerHandler() {
        return executionConsumerHandler;
    }

    private void setNumberOfKafkaConsumer() {
        try {
            numberOfKafkaConsumers = Integer.parseInt(NUMBER_OF_KAFKA_CONSUMERS);
        } catch (final NumberFormatException e) { //NOSONAR Exception suitably logged.
            LOGGER.debug("Wrong user input: KAFKA_CLIENT_NUMBER is not a number! {}", e.getMessage());
            throw e;
        }
    }

    private void createExecutionConsumerHandler(final KafkaConsumerWrapper<PolicyOutputEvent> kafkaConsumerWrapper) {
        try {
            LOGGER.debug("Creating Execution Consumer Handler");
            executionConsumerHandler = new PoeExecutionConsumerHandlerImpl(
                    getRecordHandlers(numberOfKafkaConsumers),
                    kafkaConsumerWrapper);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    executionConsumerHandler.shutdown();
                }
            });
        } catch (final KafkaConsumerInstantiationException | BadSetupException e) { //NOSONAR Exception suitably logged.
            LOGGER.debug("Failed to instantiate Execution Consumer Handler {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private void initOptimizationDaosIfNull(final List<OptimizationsDao> optimizationsDaos) {
        if (this.optimizationsDaos == null) {
            this.optimizationsDaos = new ArrayList<>(optimizationsDaos);
        }
    }

    private void initOptimizationDaosIfNull() {
        if (optimizationsDaos == null) {
            optimizationsDaos = new ArrayList<>();
            for (int i = 0; i < Integer.parseInt(NUMBER_OF_KAFKA_CONSUMERS); i++) {
                optimizationsDaos.add(new OptimizationsDaoImpl(OPTIMIZATIONS_DAO_MAX_RETRY_ATTEMPTS,
                        OPTIMIZATIONS_DAO_WAIT_PERIOD_IN_SECONDS));
            }
        }
    }

    private List<KafkaRecordHandler<PolicyOutputEvent>> getRecordHandlers(final int numberOfRecordHandlers) {
        return IntStream.range(0, numberOfRecordHandlers)
                .mapToObj(optimizationsDaos::get)
                .map(ExecutionKafkaRecordHandler::new)
                .collect(Collectors.toList());
    }
}