/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020-2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.startup;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.optimization.ExecutionConsumerController;
import com.ericsson.oss.services.sonom.flm.service.database.DatabaseViewHandler;
import com.ericsson.oss.services.sonom.flm.service.metrics.MetricReporter;
import com.ericsson.oss.services.sonom.flm.service.startup.bra.BackupRestoreController;
import com.ericsson.oss.services.sonom.flm.service.startup.executions.ExecutionsController;
import com.ericsson.oss.services.sonom.flm.service.startup.pa.PAExecutionsController;

/**
 * Startup EJB.
 */
@Singleton
@Startup
public class FlmServiceStartupBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmServiceStartupBean.class);

    @EJB(name = "flmRequirementsValidator")
    private RequirementsValidator validator;

    @EJB(name = "flmExecutionController")
    private ExecutionsController executionsController;

    @EJB(name = "paExecutionsController")
    private PAExecutionsController paExecutionsController;

    @EJB(name = "flmBackupRestoreController")
    private BackupRestoreController backupRestoreController;

    @Inject
    private DatabaseViewHandler databaseViewHandler;

    @Inject
    private ConfigurationUpdater configurationUpdater;

    @PostConstruct
    void onStartUp() {
        LOGGER.info("Initializing son-frequency-layer-manager metrics...");
        MetricReporter.getInstance().initializeFlmMetrics();

        validator.validateCm();
        validator.validateKpis();
        LOGGER.info("Requirements sent for validation");

        databaseViewHandler.createExternalDatabaseRoleAndGrantAccess();

        // Update configuration with settings that are introduced
        // in newer version of FLM.
        configurationUpdater.updateSettingsOnUpgrade();

        ExecutionConsumerController.getOrCreate();
        // Below is asynchronous method call, which will not block (or wait) for wildfly to startup
        // All long running tasks can be added in this method
        executionsController.resumeOrScheduleExecution();

        //This is an asynchronous call so won't block. 
        paExecutionsController.schedulePAExecutions();

        backupRestoreController.listen();
    }
}
