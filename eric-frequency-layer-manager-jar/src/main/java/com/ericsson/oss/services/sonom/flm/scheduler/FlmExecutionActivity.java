/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.scheduler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.Activity;
import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;

/**
 * Represents a FLM execution activity that can be scheduled, which is a subclass of {@link Activity}.
 */
public class FlmExecutionActivity extends Activity {

    public static final String FLM_ACTIVITY_CONFIGURATION = "flmActivityConfiguration";

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmExecutionActivity.class);

    /**
     * Default constructor is required by the scheduler while executing the {@link #run(Map)} method to instantiate {@link FlmExecutionActivity}.
     */
    public FlmExecutionActivity() {
        super();
    }

    /**
     * Instantiates a new FLM execution activity.
     *
     * @param activityName
     *            a unique name for the {@link Activity}
     * @param context
     *            a {@link Map} with everything that is needed during the execution of {@link #run(Map)}. The configuration to be used for FLM should
     *            be passed in with key {@link #FLM_ACTIVITY_CONFIGURATION}
     */
    public FlmExecutionActivity(final String activityName, final Map<String, Object> context) {
        super(activityName, context);
    }

    @Override
    public void run(final Map<String, Object> context) {
        final Configuration configuration = (Configuration) context.get(FLM_ACTIVITY_CONFIGURATION);
        LOGGER.info("Creating new FlmAlgorithmExecutor for execution");
        final FlmAlgorithmExecutor flmAlgorithmExecutor = new FlmAlgorithmExecutor(configuration);
        flmAlgorithmExecutor.executeActivity();
    }
}
