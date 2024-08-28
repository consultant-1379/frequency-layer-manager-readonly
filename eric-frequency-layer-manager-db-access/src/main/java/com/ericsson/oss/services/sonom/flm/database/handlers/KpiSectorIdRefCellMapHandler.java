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
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;

/**
 * Creates a {@link java.util.HashMap} of sector ids and their corresponding reference cells.
 */
public class KpiSectorIdRefCellMapHandler implements ResultHandler<HashMap<Long, String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KpiSectorIdRefCellMapHandler.class);

    @Override
    public HashMap<Long, String> populate(final ResultSet resultSet) throws SQLException {
        final HashMap<Long, String> kpiSectorIdsAndRefCell = new HashMap<>();

        while (resultSet.next()) {
            kpiSectorIdsAndRefCell.put(resultSet.getLong(KpiSectorDbConstants.SECTOR_ID_COLUMN),
                    resultSet.getString(KpiSectorDbConstants.REFERENCE_CELL_SECTOR_FDN_COLUMN));
        }

        LOGGER.debug("Populated map of KPI sector IDs and corresponding reference cells: {}", kpiSectorIdsAndRefCell);

        return kpiSectorIdsAndRefCell;
    }
}
