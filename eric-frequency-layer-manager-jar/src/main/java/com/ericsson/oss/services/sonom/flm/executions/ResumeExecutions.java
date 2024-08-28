/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.executions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Class to filter list of {@link Execution}(s) to those that need to be resumed. This class assumes the {@link Execution}(s) have been ordered by the
 * DB query that returns executions to be resumed.
 */
public final class ResumeExecutions {

    public static final int MAX_RETRY_ATTEMPTS = 3;
    private final List<Execution> executions;

    public ResumeExecutions(final List<Execution> executions) {
        this.executions = Collections.unmodifiableList(executions);
    }

    /**
     * Find the subset of {@link Execution}(s) that need to be resumed.
     *
     * @return The list of {@link Execution}(s) that need to be resumed.
     */
    public List<Execution> findExecutionsToResume() {
        final List<Integer> configurationIds = new ArrayList<>();

        return executions.stream()
                .filter(execution -> {
                    if (!configurationIds.contains(execution.getConfigurationId()) && execution.getRetryAttempts() < MAX_RETRY_ATTEMPTS) {
                        configurationIds.add(execution.getConfigurationId());
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}
