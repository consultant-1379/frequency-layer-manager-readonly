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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;
import com.ericsson.oss.services.sonom.flm.database.utils.KpiDataFormatter;

/**
 * Creates a {@link Map} of hourly {@link String} to hold the values of KPIs per sector.
 */
public class SectorHourlyFlmKpiHandler implements ResultHandler<Map<Long, Map<String, Map<String, Object>>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SectorHourlyFlmKpiHandler.class);
    private final List<String> kpiNames;
    private final String timestampColumn;

    public SectorHourlyFlmKpiHandler(final List<String> kpiNames) {
        this(kpiNames, KpiSectorDbConstants.LOCAL_TIMESTAMP_COLUMN);
    }

    public SectorHourlyFlmKpiHandler(final List<String> kpiNames, final String timestampColumn) {
        this.kpiNames = new ArrayList<>(kpiNames);
        this.timestampColumn = timestampColumn;
    }

    @Override
    public Map<Long, Map<String, Map<String, Object>>> populate(final ResultSet resultSet) throws SQLException {
        final Map<Long, Map<String, Map<String, Object>>> sectorToKpis = new HashMap<>();

        while (resultSet.next()) {
            final Map<String, Map<String, Object>> timestampToKpis = new HashMap<>();
            final Map<String, Object> kpis = new HashMap<>();
            for (final String kpi : kpiNames) {
                kpis.put(kpi, KpiDataFormatter.getObjectKpiValue(resultSet, kpi));
            }

            final long sectorId = resultSet.getLong(KpiSectorDbConstants.SECTOR_ID_COLUMN);
            final String timestamp = resultSet.getTimestamp(timestampColumn).toString();
            timestampToKpis.put(timestamp, kpis);

            if (sectorToKpis.containsKey(sectorId)) {
                LOGGER.debug("Retrieved additional KPIs for sector id '{}' at TimeStamp: '{}'", sectorId, timestamp);
                sectorToKpis.get(sectorId).putAll(timestampToKpis);
            } else {
                LOGGER.debug("Retrieved KPIs for sector id '{}' at TimeStamp: '{}'", sectorId, timestamp);
                sectorToKpis.put(sectorId, timestampToKpis);
            }
        }

        return sectorToKpis;
    }
}
