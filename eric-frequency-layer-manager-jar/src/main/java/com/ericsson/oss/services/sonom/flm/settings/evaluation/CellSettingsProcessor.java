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

package com.ericsson.oss.services.sonom.flm.settings.evaluation;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;

/**
 * Processes cell settings during FLM algorithm executions.
 */
public class CellSettingsProcessor {

    private final CellSettingsDao cellSettingsDao;
    private final CellSettingsHandler cellSettingsHandler;
    private final Execution execution;

    public CellSettingsProcessor(final CmStore cmStore, final Execution execution,
            final CellSettingsDao cellSettingsDao) {
        this.cellSettingsDao = cellSettingsDao;
        cellSettingsHandler = new CellSettingsHandler(cmStore, execution);
        this.execution = execution;
    }

    /**
     * Evaluate and apply configurations to cell and store in FLM DB.
     *
     * @param executionState
     *            the execution status state.
     * @param executionDate
     *            the execution date.
     * @param isResumed
     *            <code>true</code> if resumed execution
     * @throws ExecutionException
     *             if the computation threw an exception.
     * @throws InterruptedException
     *             if the current thread is interrupted.
     * @throws SQLException
     *             if storing of cell configurations in DB fails.
     * @throws FlmAlgorithmException
     *             if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public void processApplySettingsToCell(final ExecutionState executionState, final String executionDate,
            final boolean isResumed) throws SQLException, InterruptedException, ExecutionException, FlmAlgorithmException {
        cellSettingsHandler.evaluateCellSettings(cellSettingsDao, execution.getId());
    }
}
