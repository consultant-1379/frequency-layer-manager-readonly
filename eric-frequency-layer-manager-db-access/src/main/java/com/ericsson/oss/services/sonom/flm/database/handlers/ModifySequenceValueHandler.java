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
 * Handles the {@link ResultSet} for the actual sequence number.
 */
public class ModifySequenceValueHandler implements ResultHandler<Integer> {
    private static final String SETVAL = "setval";

    /**
     * Sets the actual sequence value from the database.
     *
     * @param resultSet
     *            The {@link ResultSet} to retrieve the sequence value.
     * @return The actual sequence value.
     * @throws SQLException
     *             In the case where an error occurs getting the value from the {@link ResultSet}
     */
    @Override
    public Integer populate(final ResultSet resultSet) throws SQLException {
        int lastSequenceValue = 0;
        while (resultSet.next()) {
            lastSequenceValue = resultSet.getInt(SETVAL);
        }
        return lastSequenceValue;
    }
}
