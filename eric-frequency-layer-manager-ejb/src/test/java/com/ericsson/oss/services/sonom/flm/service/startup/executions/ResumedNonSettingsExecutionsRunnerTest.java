/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.ericsson.oss.services.sonom.flm.executor.FlmAlgorithmExecutor;
import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.startup.util.ExecutionBuilder;

/**
 * Class to test Parallel non settings Resumed {@link Execution}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ResumedExecutionsRunner.class, FlmAlgorithmExecutor.class })
public class ResumedNonSettingsExecutionsRunnerTest {

    private static final int MAX_NUMBER_OF_THREADS_TEST = 10;

    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS_TEST);

    @Mock
    private FlmExecution flmExecution;
    @Mock
    private FlmAlgorithmExecutor executor;
    @Mock
    private KpiCalculationExecutor kpiCalculationExecutor;

    @Test
    public void whenParallelDailyExecutionsAreTriggered_thenDailyExecutorIsCalled() throws InterruptedException {
        Whitebox.setInternalState(ResumedNonSettingsExecutionsRunner.class, "FLM_EXECUTION", flmExecution);
        when(flmExecution.getNewFlmAlgorithmExecutor(any())).thenReturn(executor);
        when(executor.getKpiCalculationExecutor()).thenReturn(kpiCalculationExecutor);
        mockStatic(FlmAlgorithmExecutor.class);
        when(FlmAlgorithmExecutor.calculateNonSettingsBasedKpis(any(KpiCalculationExecutor.class), any(String.class), any(Execution.class), eq(true)))
                .thenReturn(true);

        final List<Callable<Void>> tasks = IntStream.range(0, 2)
                .mapToObj(i -> new ExecutionBuilder().build())
                .map(ResumedNonSettingsExecutionsRunner::new)
                .collect(toList());
        executorService.invokeAll(tasks);
        executorService.shutdown();

        verifyStatic(FlmAlgorithmExecutor.class, times(2));
        FlmAlgorithmExecutor.calculateNonSettingsBasedKpis(any(KpiCalculationExecutor.class), any(String.class), any(Execution.class), eq(true));
    }
}
