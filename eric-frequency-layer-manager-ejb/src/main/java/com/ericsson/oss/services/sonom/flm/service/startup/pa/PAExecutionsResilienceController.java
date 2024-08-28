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

package com.ericsson.oss.services.sonom.flm.service.startup.pa;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDaoImpl;
import com.ericsson.oss.services.sonom.flm.pa.scheduler.PAExecutionsScheduler;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecutionState;

/**
 * Schedules PA executions again if they hadn't completed or were scheduled before the FLM container went down.
 */
@Stateless(name = "paExecutionsResilienceController")
public class PAExecutionsResilienceController implements PAExecutionsController {

        private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionsResilienceController.class);
        private static final int DAO_MAX_RETRY_ATTEMPTS = 10;
        private static final int DAO_WAIT_PERIOD_IN_SECONDS = 30;
        private final ExecutionDao executionDao;
        private final PAExecutionDao paExecutionDao;

        PAExecutionsResilienceController() {
                executionDao = new ExecutionDaoImpl(DAO_MAX_RETRY_ATTEMPTS, DAO_WAIT_PERIOD_IN_SECONDS);
                paExecutionDao = new PAExecutionDaoImpl(DAO_MAX_RETRY_ATTEMPTS, DAO_WAIT_PERIOD_IN_SECONDS);
        }

        @Override
        @Asynchronous
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public void schedulePAExecutions() {
                LOGGER.info("Checking for missed performance assurance schedules");
                final Map<String, List<PAExecution>> paExecutionsThatNeedToBeScheduled = getPAExecutionsInStatesStartedAndScheduled();
                LOGGER.info("Found '{}' FLM Execution(s) with missed PA Execution(s) that need to be scheduled",
                        paExecutionsThatNeedToBeScheduled.size());
                if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The following missed PA Execution(s) need to be scheduled '{}'", paExecutionsThatNeedToBeScheduled);
                }

                for (final Map.Entry<String, List<PAExecution>> entry : paExecutionsThatNeedToBeScheduled.entrySet()) {
                        final Execution flmExecution = getFlmExecution(entry.getKey());
                        if (flmExecution != null) {
                                //Send to the scheduler
                                PAExecutionsScheduler.scheduleExisting(flmExecution, paExecutionsThatNeedToBeScheduled.get(flmExecution.getId()));
                        }
                }
        }

        private Execution getFlmExecution(final String id) {
                Execution flmExecution = null;
                try {
                        flmExecution = executionDao.get(id);
                } catch (final SQLException e) {
                        LOGGER.error("Failed to retrieve FLM Execution with ID '{}' from the DB", id, e);
                }

                return flmExecution;
        }

        private Map<String, List<PAExecution>> getPAExecutionsInStatesStartedAndScheduled() {
                Map<String, List<PAExecution>> paExecutions = new HashMap<>();
                try {
                        paExecutions = paExecutionDao.getPAExecutionsInStates(PAExecutionState.STARTED,
                                PAExecutionState.SCHEDULED);
                } catch (final SQLException e) {
                        LOGGER.error("Failed to retrieve PA Executions in states '{}' '{}' on startup from the DB", PAExecutionState.STARTED,
                                PAExecutionState.SCHEDULED, e);
                }
                return paExecutions;
        }
}
