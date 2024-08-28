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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.KpiSectorDbConstants;

/**
 * Creates a {@link Set} of Sector Id's represented as {@link Long}.
 */
public class KpiSectorIdSetHandler implements ResultHandler<Set<Long>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KpiSectorIdSetHandler.class);

    @Override
    public Set<Long> populate(final ResultSet resultSet) throws SQLException {
        final Set<Long> kpiSectorIds = new HashSet<>();

        while (resultSet.next()) {
            kpiSectorIds.add(resultSet.getLong(KpiSectorDbConstants.SECTOR_ID_COLUMN));
        }

        LOGGER.debug("Populated set of KPI sector IDs: {}", kpiSectorIds);

        return kpiSectorIds;
    }
}
