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
package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a {@link Map} of {@link Map} to hold the values of settings per cell.
 */
public class CellFlmSettingsHandler implements ResultHandler<Map<Map<String, Integer>, Map<String, String>>> {

    private static final String FDN = "fdn";
    private static final String OSS_ID = "oss_id";
    private final List<String> settingNames;

    /**
     * Instantiates a new Cell flm settings handler.
     *
     * @param settingNames
     *            the setting names
     */
    public CellFlmSettingsHandler(final List<String> settingNames) {
        this.settingNames = new ArrayList<>(settingNames);
    }

    @Override
    public Map<Map<String, Integer>, Map<String, String>> populate(final ResultSet resultSet) throws SQLException {
        final Map<Map<String, Integer>, Map<String, String>> cellToSettings = new HashMap<>();
        while (resultSet.next()) {
            final Map<String, String> settings = new HashMap<>(2);
            for (final String setting : settingNames) {
                final String settingValue = resultSet.getString(setting);
                if (!resultSet.wasNull()) {
                    settings.put(setting, settingValue);
                }
            }
            final Map<String, Integer> cell = new HashMap<>();
            cell.put(resultSet.getString(FDN), resultSet.getInt(OSS_ID));
            cellToSettings.put(cell, settings);
        }
        return cellToSettings;
    }
}


