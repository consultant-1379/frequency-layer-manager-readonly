/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LeakageCell;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Creates a {@link Set} of {@link LeakageCell}s.
 */
public class LeakageCellSetHandler implements ResultHandler<List<LeakageCell>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeakageCellSetHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public List<LeakageCell> populate(final ResultSet resultSet) throws SQLException {
        List<LeakageCell> leakageCells = Collections.emptyList();

        while (resultSet.next()) {
            try {
                final String dbLeakageCell = resultSet.getString(LbdarDbConstants.LEAKAGE_CELLS);
                leakageCells = GSON.fromJson(dbLeakageCell,
                        new TypeToken<List<LeakageCell>>() {
                        }.getType());
            } catch (final JsonSyntaxException e) {
                LOGGER.error("Failed to parse leakage cell", e);
            }
            LOGGER.debug("Populated list of leakage cells: {}", leakageCells);
        }
        return leakageCells;
    }
}