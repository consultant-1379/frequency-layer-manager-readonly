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

package com.ericsson.oss.services.sonom.flm.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;

import io.vavr.Tuple2;

/**
 * KPI combined view data access through KPI interface.
 */
public interface KpiCombinedViewDao {

    /**
     * Retrieve all KPIs with execution ID, KPI names and sector IDs provided between the given UTC timestamps.
     *
     * @param sectorIds
     *        A {@link List} of Sector IDs to retrieve busy hours for
     * @param executionId
     *        The executionId of the change elements
     * @param endDateTime
     *        fetch the KPIs with a timestamp less than this timestamp
     * @param startDateTime
     *        fetch the KPIs with a timestamp greater or equal to this timestamp
     * @param kpiNames
     *        A {@link List} of KPI names to retrieve
     * @return A {@link Map} of KPI values.
     * @throws SQLException
     *        thrown if an error occurred executing the query.
     */
    Tuple2<Map<Long, String>, Map<CellIdentifier, Map<String, Map<String, Object>>>> getKpis(List<String> sectorIds,
                String executionId, String endDateTime, String startDateTime, List<String> kpiNames) throws SQLException;
}
