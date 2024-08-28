/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * KPI sector data access.
 */
public interface KpiSectorDao {

    /**
     * Retrieve all KPI sector ids.
     *
     * @return A {@link Set} of Kpi Sector IDs of sectors with reference cells represented as @{@link Long} or an empty list if none found.
     * @throws SQLException thrown if an error occurred executing the query.
     */
    Set<Long> getKpiSectorIdsWithRefCell() throws SQLException;

    /**
     * Retrieve all KPI sector ids whose reference cell has been unavailable for the specified number of days.
     *
     * @param localDateTime Representing the time period for the days to determine the cell unavailability
     * @return A {@link Set} of Kpi Sector IDs as {@link Long}, where the reference cell of those sectors are unavailable for the specified number of
     *     days.
     * @throws SQLException thrown if an error occurred executing the query.
     */
    Set<Long> getKpiSectorIdsWithUnavailableRefCell(LocalDateTime localDateTime) throws SQLException;

    /**
     * Retrieve all KPI sector ids and their corresponding reference cell.
     *
     * @return A {@link Map} with Kpi Sector IDs as the key and their corresponding reference cell as the value.
     * @throws SQLException thrown if an error occurred executing the query.
     */
    Map<Long, String> getSectorIdsAndRefCell() throws SQLException;
}
