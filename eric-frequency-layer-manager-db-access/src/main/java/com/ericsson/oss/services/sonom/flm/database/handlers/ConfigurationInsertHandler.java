/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants;

/**
 * Handles the {@link ResultSet} for an inserted {@link com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration}.
 */
public class ConfigurationInsertHandler implements ResultHandler<Integer> {

    /**
     * Gets the ID of the newly inserted {@link com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration} record from the
     * {@link ResultSet}.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the ID from.
     * @return The ID of the new {@link com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration}.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public Integer populate(final ResultSet resultSet) throws SQLException {
        int configurationId = 0;
        while (resultSet.next()) {
            configurationId = resultSet.getInt(ConfigurationDbConstants.ID);
        }
        return configurationId;
    }
}
