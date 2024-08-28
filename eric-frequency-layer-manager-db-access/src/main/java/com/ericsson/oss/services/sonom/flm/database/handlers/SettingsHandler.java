/*
 *
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
 *
 */

package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the {@link ResultSet} for getting a String setting.
 */
public class SettingsHandler implements ResultHandler<String> {

    private final String settingName;

    public SettingsHandler(final String settingName) {
        this.settingName = settingName;
    }

    /**
     * Gets the actual String setting value from the database.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the sequence value.
     * @return The actual string value.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public String populate(final ResultSet resultSet) throws SQLException {
        String settingValue = null;
        if (resultSet.next()) {
            settingValue = resultSet.getString(settingName);
        }
        return settingValue;
    }
}
