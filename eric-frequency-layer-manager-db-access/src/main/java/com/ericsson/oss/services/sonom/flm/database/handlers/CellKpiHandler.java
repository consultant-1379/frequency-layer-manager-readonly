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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.KpiCellDbConstants;

/**
 * Creates a {@link List} of {@link CellKpi} to hold the values of KPIs per cell.
 */
public class CellKpiHandler implements ResultHandler<Map<CellKpi, CellKpis>> {

    private static final int INVALID_VALUE = -1;

    @Override
    public Map<CellKpi, CellKpis> populate(final ResultSet resultSet) throws SQLException {
        final Map<CellKpi, CellKpis> cellToKpis = new HashMap<>();
        while (resultSet.next()) {
            final CellKpis kpis = new CellKpis(getDoubleValueOrThrowException(resultSet, KpiCellDbConstants.CONNECTED_USERS),
                                               getDoubleValueOrThrowException(resultSet, KpiCellDbConstants.SUBSCRIPTION_RATIO),
                                               getIntValueOrThrowException(resultSet, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_HIGH_LOAD),
                                               getIntValueOrThrowException(resultSet, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD),
                                               getIntValueOrThrowException(resultSet, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_MEDIOUM_LOAD),
                                               getIntValueOrThrowException(resultSet, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD),
                                               getIntValueOrThrowException(resultSet, KpiCellDbConstants.PM_IDLE_MODE_REL_DISTR_LOW_LOAD));
            final CellKpi cell = new CellKpi(resultSet.getString(KpiCellDbConstants.FDN),
                    resultSet.getInt(KpiCellDbConstants.OSS_ID),
                    resultSet.getTimestamp(KpiCellDbConstants.LOCAL_TIMESTAMP).toString());

            cellToKpis.put(cell, kpis);
        }
        return cellToKpis;
    }

    private static int getIntValueOrThrowException(final ResultSet resultSet, final String kpiName) throws SQLException {
        final int kpiValue = resultSet.getInt(kpiName);
        return resultSet.wasNull()
                ? INVALID_VALUE
                : kpiValue;
    }

    private static double getDoubleValueOrThrowException(final ResultSet resultSet, final String kpiName) throws SQLException {
        final double kpiValue = resultSet.getDouble(kpiName);
        return resultSet.wasNull()
                ? INVALID_VALUE
                : kpiValue;
    }
}
