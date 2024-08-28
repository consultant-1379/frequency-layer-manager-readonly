/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.settings;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDao;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Gets Cell settings per FLM execution.
 */
public class CellFlmSettingsRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellFlmSettingsRetriever.class);

    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final int WAIT_PERIOD_IN_SECONDS = 30;
    private static final CellSettingsDao CELL_SETTINGS_DAO = new CellSettingsDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    /**
     * Retrieve cell settings map.
     *
     * @param executionId
     *            the execution id
     * @return the map
     * @throws SQLException
     *             the sql exception
     */
    public Map<CellIdentifier, Map<String, String>> retrieveCellSettings(final String executionId) throws SQLException {
        try {
            final Map<CellIdentifier, Map<String, String>> cellSettings = new HashMap<>();
            final Map<Map<String, Integer>, Map<String, String>> cellSettingsMap = CELL_SETTINGS_DAO.getSettingsForCellPerFlmExecution(executionId);

            for (final Map.Entry<Map<String, Integer>, Map<String, String>> entry : cellSettingsMap.entrySet()) {
                final String fdn = entry.getKey().keySet().toArray()[0].toString();
                final Integer ossId = entry.getKey().get(fdn);
                cellSettings.put(new CellIdentifier(ossId, fdn), entry.getValue());
            }
            return cellSettings;
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve cell settings for execution Id '{}'", executionId, e);
            throw e;
        }
    }

    /**
     * Retrieves given setting value for OSS ID FDN Execution ID combination.
     * 
     * @param settingsName
     *            the name of the setting to be read.
     * @param ossId
     *            Required OSS ID.
     * @param fdn
     *            Required FDN.
     * @param executionId
     *            required execution ID.
     * @return persisted value for given setting.
     * @throws SQLException
     *             thrown when issue encountered reading from database.
     */
    public String retrieveGivenCellSettingValue(final String settingsName, final int ossId, final String fdn, final String executionId)
            throws SQLException {
        try {
            return CELL_SETTINGS_DAO.retrieveNamedSetting(settingsName, ossId, fdn, executionId);
        } catch (final SQLException e) {
            LOGGER.warn("Failed to retrieve cell settings for execution ID '{}'", executionId, e);
            throw e;
        }
    }
}
