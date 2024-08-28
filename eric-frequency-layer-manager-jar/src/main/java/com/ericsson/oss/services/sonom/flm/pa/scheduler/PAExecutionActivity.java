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

package com.ericsson.oss.services.sonom.flm.pa.scheduler;

import java.util.Map;

import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Class triggered by the quartz scheduler.
 */
public class PAExecutionActivity extends Activity {
    static final String PA_EXECUTION_CONTEXT = "paExecution";
    static final String FLM_EXECUTION_CONTEXT = "flmExecution";
    private static final Logger LOGGER = LoggerFactory.getLogger(PAExecutionActivity.class);

    private PAExecutionExecutor paExecutionExecutor;

    /**
     * Default constructor. "Any class extending {@link Activity} <b>must</b> have a no parameter constructor in order to support the Quartz
     * {@link org.quartz.Scheduler}. Any activity related data should be passed in the activityContext {@link Map} in the constructor."
     */
    public PAExecutionActivity() {
        super();
    }

    /**
     * Constructor with the params.
     *
     * @param activityName
     *            {@link String} name of the activity.
     * @param context
     *            {@link Map} of {@link String} and {@link Object} params used inside the overridden method run.
     */
    PAExecutionActivity(final String activityName, final Map<String, Object> context) {
        super(activityName, context);
    }

    @Override
    public void run(final Map<String, Object> context) {
        final PAExecution paExecution = (PAExecution) context.get(PA_EXECUTION_CONTEXT);
        final Execution flmExecution = (Execution) context.get(FLM_EXECUTION_CONTEXT);
        LOGGER.info("PA Execution triggered for '{}'", paExecution);
        this.paExecutionExecutor = new PAExecutionExecutor(paExecution, flmExecution);
        this.paExecutionExecutor.executeActivity();
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (paExecutionExecutor == null) {
            throw new UnableToInterruptJobException("PAExecutionExecutor is null, cannot terminate activity.");
        }
        paExecutionExecutor.terminateActivity();
    }
}
