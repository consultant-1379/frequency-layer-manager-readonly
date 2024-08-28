/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.startup.bra;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.bro.BackupRestoreSubscriber;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.flm.service.startup.executions.ExecutionsController;

/**
 * Implements {@code FlmBackupRestoreController}. Subscribes to the <code>bro-notification</code> topic.
 */
@Stateless(name = "flmBackupRestoreController")
public class FlmBackupRestoreController implements BackupRestoreController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmBackupRestoreController.class);

    @EJB(name = "flmExecutionController")
    private ExecutionsController executionsController;

    @Asynchronous
    @Override
    public void listen() {
        final FlmBackupRestoreConsumer flmBackupRestoreConsumer;
        try {
            flmBackupRestoreConsumer = new FlmBackupRestoreConsumer(executionsController);
            BackupRestoreSubscriber.subscribe(flmBackupRestoreConsumer);
        } catch (final KafkaConsumerInstantiationException e) {
            LOGGER.warn("Failed to create FLM Backup and Restore consumer", e);
        }
    }
}
