/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
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

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_1;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.KPI_PROCESSING_GROUP_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.executions.util.ExecutionBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * Unit tests for {@link ResumeExecutions} class.
 */
public class ResumeExecutionsTest {

    @Test
    public void whenMultipleExecutions_thenKeepOnlyLastExecutionForEachConfigurationIdWithLessThan3Retries() {
        final String executionId = "FLM_1600701252";
        final List<Execution> executions = new ArrayList<>();

        executions.add(new ExecutionBuilder()
                .id(executionId + "_3")
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.930"))
                .state(KPI_PROCESSING_GROUP_2)
                .retryAttempts(3)
                .build()); // excluded because outside max retry attempts

        final Execution firstIncludedExecution = new ExecutionBuilder()
                .id(executionId + "_3")
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.922"))
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(firstIncludedExecution);
        executions.add(new ExecutionBuilder()
                .id(executionId + "_1")
                .configurationId(1)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.866"))
                .state(KPI_PROCESSING_GROUP_1)
                .build()); // excluded because not latest start time for this configuration ID
        final Execution secondIncludedExecution = new ExecutionBuilder()
                .id(executionId + "_4")
                .configurationId(2)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.923"))
                .state(KPI_PROCESSING_GROUP_2)
                .build();
        executions.add(secondIncludedExecution); // included because latest start time for this configuration ID
        executions.add(new ExecutionBuilder()
                .id(executionId + "_2")
                .configurationId(2)
                .startTime(Timestamp.valueOf("2020-05-07 10:53:15.87"))
                .state(KPI_PROCESSING_GROUP_1)
                .build()); // excluded because not latest start time for this configuration ID

        final ResumeExecutions resumeExecutions = new ResumeExecutions(executions);
        final List<Execution> result = resumeExecutions.findExecutionsToResume();

        assertThat(result).containsExactly(firstIncludedExecution, secondIncludedExecution);
    }
}