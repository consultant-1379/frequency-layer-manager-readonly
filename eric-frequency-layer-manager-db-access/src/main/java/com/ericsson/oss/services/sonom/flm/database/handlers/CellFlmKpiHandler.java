/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
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

import com.ericsson.oss.services.sonom.flm.database.utils.KpiDataFormatter;

/**
 * Creates a {@link List} of {@link CellKpi} to hold the values of KPIs per cell.
 */
public class CellFlmKpiHandler implements ResultHandler<Map<CellKpi, Map<String, Object>>> {

    private static final String LOCAL_TIMESTAMP = "local_timestamp";

    private final String fdn;
    private final String ossId;
    private final List<String> kpiNames;

    public CellFlmKpiHandler(final String fdn, final String ossId, final List<String> kpiNames) {
        this.kpiNames = new ArrayList<>(kpiNames);
        this.fdn = fdn;
        this.ossId = ossId;
    }

    @Override
    public Map<CellKpi, Map<String, Object>> populate(final ResultSet resultSet) throws SQLException {
        final Map<CellKpi, Map<String, Object>> cellToKpis = new HashMap<>();
        while (resultSet.next()) {
            final Map<String, Object> kpis = new HashMap<>(2);
            for (final String kpi : kpiNames) {
                final Object kpiValue = KpiDataFormatter.getObjectKpiValue(resultSet, kpi);
                kpis.put(kpi, kpiValue);
            }
            final CellKpi cell = new CellKpi(resultSet.getString(fdn), resultSet.getInt(ossId),
                    resultSet.getTimestamp(LOCAL_TIMESTAMP).toString());
            cellToKpis.put(cell, kpis);
        }
        return cellToKpis;
    }
}
