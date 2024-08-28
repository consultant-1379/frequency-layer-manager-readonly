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

package com.ericsson.oss.services.sonom.flm.pa.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.env.Environment;
import com.ericsson.oss.services.sonom.common.kafka.consumer.KafkaRecordHandler;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDaoImpl;
import com.ericsson.oss.services.sonom.flm.messagehandler.ExecutionConsumerHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.KafkaConsumerWrapper;
import com.ericsson.oss.services.sonom.flm.messagehandler.PAExecutionConsumerHandlerImpl;
import com.ericsson.oss.services.sonom.flm.messagehandler.exceptions.BadSetupException;
import com.ericsson.oss.services.sonom.flm.optimization.ExecutionConsumerController;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * The PAExecutionConsumerController singleton is responsible to create and hold an object of {@link PAExecutionConsumerHandlerImpl} class for PA.
 */
public class PAExecutionConsumerController {
    private static final String NUMBER_OF_KAFKA_CONSUMERS = Environment.getEnvironmentValue("KAFKA_CLIENT_NUMBER", "10");
    private static final int PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS = 30;
    private static final int PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionConsumerController.class);

    private static PAExecutionConsumerController instance;

    private ExecutionConsumerHandler paExecutionConsumerHandler;
    private List<PAOutputEventDao> paOutputEventDaos;
    private int numberOfKafkaConsumers;

    //For unit testing only
    PAExecutionConsumerController(final KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaConsumerWrapper,
            final List<PAOutputEventDao> paOutputEventDaos) {
        setNumberOfKafkaConsumer();
        initPADaosIfNull(paOutputEventDaos);
        createExecutionConsumerHandler(kafkaConsumerWrapper);
    }

    private PAExecutionConsumerController(final KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaCustomerWrapper) {
        setNumberOfKafkaConsumer();
        initPADaosIfNull();
        createExecutionConsumerHandler(kafkaCustomerWrapper);
    }

    /**
     * Gets or creates an PAExecutionConsumerController object (singleton).
     *
     * @return A {@link ExecutionConsumerController} object
     */
    public static PAExecutionConsumerController getOrCreate() {
        if (instance == null) {
            instance = new PAExecutionConsumerController(null);
        }
        return instance;
    }

    /**
     * Gets an ExecutionConsumerHandler object.
     *
     * @return A {@link ExecutionConsumerHandler} object
     */
    public ExecutionConsumerHandler getPAExecutionConsumerHandler() {
        return paExecutionConsumerHandler;
    }

    private void setNumberOfKafkaConsumer() {
        try {
            numberOfKafkaConsumers = Integer.parseInt(NUMBER_OF_KAFKA_CONSUMERS);
        } catch (final NumberFormatException e) { //NOSONAR Exception Suitably logged.
            LOGGER.error("Wrong user input: KAFKA_CLIENT_NUMBER is not a number {}", e.getMessage());
            throw e;
        }
    }

    private void createExecutionConsumerHandler(final KafkaConsumerWrapper<PaPolicyOutputEvent> kafkaConsumerWrapper) {
        try {
            LOGGER.debug("Creating Execution Consumer Handler");
            paExecutionConsumerHandler = new PAExecutionConsumerHandlerImpl(
                    getRecordHandlers(numberOfKafkaConsumers),
                    kafkaConsumerWrapper);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    paExecutionConsumerHandler.shutdown();
                }
            });
        } catch (final KafkaConsumerInstantiationException | BadSetupException e) { //NOSONAR Exception Suitably logged.
            LOGGER.error("Failed to instantiate Execution Consumer Handler {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private void initPADaosIfNull(final List<PAOutputEventDao> paOutputEventDaos) {
        if (this.paOutputEventDaos == null) {
            this.paOutputEventDaos = new ArrayList<>(paOutputEventDaos);
        }
    }

    private void initPADaosIfNull() {
        if (paOutputEventDaos == null) {
            paOutputEventDaos = new ArrayList<>();
            for (int i = 0; i < Integer.parseInt(NUMBER_OF_KAFKA_CONSUMERS); i++) {
                paOutputEventDaos.add(new PAOutputEventDaoImpl(PA_EXECUTION_DAO_MAX_RETRY_ATTEMPTS,
                        PA_EXECUTION_DAO_WAIT_PERIOD_IN_SECONDS) {
                });
            }
        }
    }

    private List<KafkaRecordHandler<PaPolicyOutputEvent>> getRecordHandlers(final int numberOfRecordHandlers) {
        return IntStream.range(0, numberOfRecordHandlers)
                .mapToObj(paOutputEventDaos::get)
                .map(PAExecutionKafkaRecordHandler::new)
                .collect(Collectors.toList());
    }
}
