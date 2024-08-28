/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;

/**
 * Utils class providing methods which return the identifiers of all topology objects in a {@link TopologySector}.
 */
public class TopologyIdentifierUtils {

    private TopologyIdentifierUtils() {
    }

    /**
     * Creates a {@link List} of all Sector IDs in a {@link List} of {@link TopologySector}.
     * 
     * @param sectors
     *            The {@link List} of {@link TopologySector}s
     * @return A {@link List} of Sector ID
     */
    public static List<String> getAllSectorIdsAsList(final Collection<TopologySector> sectors) {
        final List<String> ids = new ArrayList<>(sectors.size());
        sectors.forEach(s -> ids.add(s.getSectorId().toString()));
        return ids;
    }

}
