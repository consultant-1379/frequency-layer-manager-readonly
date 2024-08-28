/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020
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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;

/**
 * Creates a {@link Map} of Sector Id's represented as {@link Long} to Sector Busy Hour.
 */
public class KpiSectorBusyHourHandler implements ResultHandler<Map<Long, String>> {

    @Override
    public Map<Long, String> populate(final ResultSet resultSet) throws SQLException {
        final Map<Long, String> kpiSectorBusyHours = new HashMap<>();

        while (resultSet.next()) {
            final long sectorId = resultSet.getLong(KpiSectorDbConstants.SECTOR_ID_COLUMN);
            final Timestamp sectorBusyHour = resultSet.getTimestamp("sector_busy_hour");
            if (!resultSet.wasNull()) {
                kpiSectorBusyHours.put(sectorId, sectorBusyHour.toString());
            }
        }
        return kpiSectorBusyHours;
    }
}
