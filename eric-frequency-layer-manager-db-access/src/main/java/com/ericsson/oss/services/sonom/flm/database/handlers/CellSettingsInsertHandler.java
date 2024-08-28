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

/**
 * Handles the {@link ResultSet} for an inserted {@link com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings}.
 */
public class CellSettingsInsertHandler implements ResultHandler<Integer> {

    /**
     * Gets the OSS ID of the newly inserted {@link com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings} record from the
     * {@link ResultSet}.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the OSS ID from.
     * @return The OSS ID of the new {@link com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings}.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public Integer populate(final ResultSet resultSet) throws SQLException {
        int ossId = 0;
        while (resultSet.next()) {
            ossId = resultSet.getInt("oss_id");
        }
        return ossId;
    }
}
