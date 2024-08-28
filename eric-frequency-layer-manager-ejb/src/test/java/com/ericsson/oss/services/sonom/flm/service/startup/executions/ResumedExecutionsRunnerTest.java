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

package com.ericsson.oss.services.sonom.flm.service.startup.executions;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.ericsson.oss.services.sonom.flm.executions.ExecutionDbHandler;
import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.service.startup.util.ExecutionBuilder;

/**
 * Class to test Parallel Resumed {@link FlmExecution}.
 */
@RunWith(PowerMockRunner.class)
public class ResumedExecutionsRunnerTest {

    private static final int MAX_NUMBER_OF_THREADS_TEST = 10;

    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST);
    private final ExecutionDbHandler executionDbHandler = new ExecutionDbHandler();

    @Mock
    private FlmExecution flmExecution;
    @Mock
    private FlmAlgorithmExecutor executor;

    @Test
    public void whenParallelExecutionsAreResumed_thenExecuteActivityIsCalledRequiredNumberOfTimes() throws InterruptedException {
        Whitebox.setInternalState(ResumedExecutionsRunner.class, "FLM_EXECUTION", flmExecution);
        when(flmExecution.getNewFlmAlgorithmExecutor(any())).thenReturn(executor);
        final List<Callable<Void>> tasks = IntStream.range(0, 2)
                .mapToObj(i -> new ExecutionBuilder().build())
                .map(execution -> (new ResumedExecutionsRunner(execution, executionDbHandler, false)))
                .collect(toList());
        executorService.invokeAll(tasks);
        executorService.shutdown();
        verify(executor, times(2)).executeActivity();
    }
}
