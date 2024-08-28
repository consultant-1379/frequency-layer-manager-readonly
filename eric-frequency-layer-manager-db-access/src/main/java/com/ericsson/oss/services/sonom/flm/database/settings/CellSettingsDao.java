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

package com.ericsson.oss.services.sonom.flm.database.settings;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;

/**
 * Interface defining the <code>CellSettingsDao</code>.
 */
public interface CellSettingsDao {

    /**
     * This method inserts the cell settings configurations, used in the execution, in the database.
     *
     * @param allCellSettings
     *            the {@link List} of each cell and it's settings.
     * @throws SQLException
     *             SqlException thrown when there is issue updating database.
     */
    void insertCellSettings(List<CellSettings> allCellSettings) throws SQLException;

    /**
     * This method gets the cell settings, used in the execution, in the database.
     *
     * @param executionId
     *            the ID of each execution.
     * @return the settings for cell per FLM execution
     * @throws SQLException
     *             thrown when issue encountered reading from database.
     */
    Map<Map<String, Integer>, Map<String, String>> getSettingsForCellPerFlmExecution(String executionId) throws SQLException;

    /**
     * Retrieves setting value for given OSS ID FDN Execution ID combination.
     * 
     * @param settingsName
     *            the name of the setting to be read.
     * @param ossId
     *            Required OSS ID.
     * @param fdn
     *            Required FDN.
     * @param executionId
     *            required execution ID.
     * @return persisted value for the given setting.
     * @throws SQLException
     *             thrown when issue encountered reading from database.
     */
    String retrieveNamedSetting(String settingsName, int ossId, String fdn, String executionId) throws SQLException;

}
