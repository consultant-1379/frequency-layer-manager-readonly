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
 *------------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;
import com.ericsson.oss.services.sonom.flm.database.utils.KpiDataFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

import io.vavr.Tuple;
import io.vavr.Tuple2;

/**
 *  Class to implement methods of {@link ResultHandler}.
 */
public class KpiHandler implements ResultHandler<Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>>> {
    private static final String LOCAL_TIMESTAMP = "local_timestamp";
    private static final Logger LOGGER = LoggerFactory.getLogger(KpiHandler.class);
    private final String fdn;
    private final String ossId;
    private final List<String> kpiNames;

    public KpiHandler(final String fdn, final String ossId, final List<String> kpiNames) {
        this.kpiNames = new ArrayList<>(kpiNames);
        this.fdn = fdn;
        this.ossId = ossId;
    }

    /**
     * Returns kpiSectorBusyHours values and cellToKpis values after executing a query.
     *
     * @param resultSet The result as {@link ResultSet} of the query.
     * @return {@link Tuple2} containing kpiSectorBusyHours values and cellToKpis values from the resultSet.
     * @throws SQLException thrown if a database access error occurs or result set is closed.
     */
    @Override
    public Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> populate(final ResultSet resultSet) throws SQLException {
        final Map<Long, String> kpiSectorBusyHours = new HashMap<>();
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellToKpis = new HashMap<>();

        while (resultSet.next()) {
            final long sectorId = resultSet.getLong(KpiSectorDbConstants.SECTOR_ID_COLUMN);
            final Timestamp sectorBusyHour = resultSet.getTimestamp("sector_busy_hour");
            final Map<String, Map<String, Object>> timestampToKpis = new HashMap<>();
            final Map<String, Object> kpis = new HashMap<>();

            if (!resultSet.wasNull()) {
                kpiSectorBusyHours.put(sectorId, sectorBusyHour.toString());
            }
            final CellIdentifier cellIdentifier = new CellIdentifier(resultSet.getInt(ossId), resultSet.getString(fdn));
            final String timestamp = resultSet.getTimestamp(LOCAL_TIMESTAMP).toString();
            for (final String kpi : kpiNames) {
                kpis.put(kpi, KpiDataFormatter.getObjectKpiValue(resultSet, kpi));
            }
            timestampToKpis.put(timestamp, kpis);
            if (cellToKpis.containsKey(cellIdentifier)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Retrieved additional KPIs for cell fdn '{}', oss_id '{}' at TimeStamp: '{}'", cellIdentifier.getFdn(),
                            cellIdentifier.getOssId(), timestamp);
                }
                cellToKpis.get(cellIdentifier).putAll(timestampToKpis);
            } else {
                cellToKpis.put(cellIdentifier, timestampToKpis);
            }

        }

        return Tuple.of(kpiSectorBusyHours, cellToKpis);
    }
}
