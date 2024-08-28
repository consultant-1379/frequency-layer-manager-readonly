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
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.utils.KpiDataFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

/**
 * Creates a {@link List} of hourly {@link CellIdentifier} to hold the values of KPIs per cell.
 */
public class CellHourlyFlmKpiHandler implements ResultHandler<Map<CellIdentifier, Map<String, Map<String, Object>>>> {
    private static final String LOCAL_TIMESTAMP = "local_timestamp";
    private static final Logger LOGGER = LoggerFactory.getLogger(CellHourlyFlmKpiHandler.class);
    private final String fdn;
    private final String ossId;
    private final String timestampColumn;
    private final List<String> kpiNames;

    public CellHourlyFlmKpiHandler(final String fdn, final String ossId, final List<String> kpiNames) {
        this(fdn, ossId, kpiNames, LOCAL_TIMESTAMP);
    }

    public CellHourlyFlmKpiHandler(final String fdn, final String ossId, final List<String> kpiNames, final String timestampColumn) {
        this.kpiNames = new ArrayList<>(kpiNames);
        this.fdn = fdn;
        this.ossId = ossId;
        this.timestampColumn = timestampColumn;
    }

    @Override
    public Map<CellIdentifier, Map<String, Map<String, Object>>> populate(final ResultSet resultSet) throws SQLException {
        final Map<CellIdentifier, Map<String, Map<String, Object>>> cellToKpis = new HashMap<>();
        while (resultSet.next()) {
            final Map<String, Map<String, Object>> timestampToKpis = new HashMap<>();
            final Map<String, Object> kpis = new HashMap<>();
            for (final String kpi : kpiNames) {
                kpis.put(kpi, KpiDataFormatter.getObjectKpiValue(resultSet, kpi));
            }
            final CellIdentifier cellIdentifier = new CellIdentifier(resultSet.getInt(ossId), resultSet.getString(fdn));
            final String timestamp = resultSet.getTimestamp(timestampColumn).toString();
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
        return cellToKpis;
    }
}
