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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.executions.util.ExecutionBuilder;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.flm.util.RowsUpdated;

/**
 * Unit tests for {@link ExecutionDbHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionDbHandlerTest {

    private static final String EXECUTION_ID = "FLM_1600701252-162";
    private final List<Execution> executions = new ArrayList<>();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private ExecutionDao executionDao; // NOPMD its an injected mock...

    @Mock
    private RowsUpdated rowsUpdated;

    @InjectMocks
    private ExecutionDbHandler executionDbHandler;

    @Test
    public void whenFailedStatus_thenSetFailedStatusPersistedToDbUnlessStateIsFinal() throws SQLException {
        final List<Execution> succeededExecution = createExecutionsForTest(); // successful execution should not be changed

        final int result = executionDbHandler.applyFailedState(executions.toArray(new Execution[0]));

        assertThat(result).isEqualTo(ExecutionState.getRestartableExecutionStates().length);

        executions.forEach(execution -> {
            if (succeededExecution.contains(execution)) {
                softly.assertThat(execution.getState())
                        .as("execution ID %s not %s: ", execution.getId(), execution.getState())
                        .satisfiesAnyOf(ExecutionState.SUCCEEDED::equals, ExecutionState.PARTIALLY_SUCCEEDED::equals);
                return;
            }

            softly.assertThat(execution.getState())
                    .as("execution ID %s not %s: ", execution.getId(), ExecutionState.FAILED)
                    .isEqualTo(ExecutionState.FAILED);
        });

        // ExecutionState count minus the final states because we subtract the executions in final state which should not be changed
        verify(rowsUpdated, times(ExecutionState.values().length - 3)).verifyRowsUpdated(anyInt(), any(Runnable.class), any(Runnable.class));
    }

    @Test
    public void whenRetryAttemptsIncremented_thenIncrementedValuePersistedToDb() {
        final List<Execution> succeededExecution = createExecutionsForTest(); // successful execution should not be changed

        int result = executionDbHandler.incrementRetryAttempts(executions.toArray(new Execution[0]));

        assertThat(result).isEqualTo(ExecutionState.values().length);

        result = executionDbHandler.incrementRetryAttempts(succeededExecution.toArray(new Execution[0]));
        assertThat(result).isEqualTo(2);

        executions.forEach(execution -> {
            if (succeededExecution.contains(execution)) {
                softly.assertThat(execution.getRetryAttempts())
                        .as("execution ID %s retryAttempts not incremented twice: ", execution.getId())
                        .isEqualTo(2);
                return;
            }
            softly.assertThat(execution.getRetryAttempts())
                    .as("execution ID %s retryAttempts not incremented once: ", execution.getId())
                    .isEqualTo(1);
        });

        // numberOfRowsUpdated - one time for each execution class is called with + 2 for retry :
        // [SONP-43050] - Will retry for KPI_PROCESSING_SUCCEEDED, CELL_SETTINGS_HISTORY
        verify(rowsUpdated, times(32)).verifyRowsUpdated(anyInt(), any(Runnable.class), any(Runnable.class));
    }

    private List<Execution> createExecutionsForTest() {
        int i = 0;
        for (final ExecutionState state : ExecutionState.values()) {
            executions.add(new ExecutionBuilder().id(EXECUTION_ID + "_" + i++).state(state).build());
        }

        return executions.stream().filter(execution -> execution.getState() == ExecutionState.SUCCEEDED ||
                execution.getState() == ExecutionState.PARTIALLY_SUCCEEDED).collect(Collectors.toList());
    }
}