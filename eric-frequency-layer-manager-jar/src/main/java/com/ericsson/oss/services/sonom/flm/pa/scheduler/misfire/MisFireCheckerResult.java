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

package com.ericsson.oss.services.sonom.flm.pa.scheduler.misfire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Used by the {@link MisFireChecker} to store the results of its checks.
 */
public class MisFireCheckerResult {

    private final List<PAExecution> misfiresThatCanBeScheduledAgain = new ArrayList<>();
    private final List<PAExecution> misfiresThatCanNotBeScheduledAgain = new ArrayList<>();
    private final List<PAExecution> notMisfiredScheduleAgainAsNormal = new ArrayList<>();

    boolean addToMisfiresThatCanBeScheduled(final PAExecution paExecution) {
        return misfiresThatCanBeScheduledAgain.add(paExecution);
    }

    boolean addToMisfiresThanCanNotBeScheduled(final PAExecution paExecution) {
        return misfiresThatCanNotBeScheduledAgain.add(paExecution);
    }

    boolean addToNotMisfiredScheduleAgainAsNormal(final PAExecution paExecution) {
        return notMisfiredScheduleAgainAsNormal.add(paExecution);
    }

    public List<PAExecution> getMisfiresThatCanBeScheduled() {
        return Collections.unmodifiableList(misfiresThatCanBeScheduledAgain);
    }

    public List<PAExecution> getMisfiresThatCanNotBeScheduled() {
        return Collections.unmodifiableList(misfiresThatCanNotBeScheduledAgain);
    }

    public List<PAExecution> getNotMisfiredScheduleAgainAsNormal() {
        return Collections.unmodifiableList(notMisfiredScheduleAgainAsNormal);
    }

}
