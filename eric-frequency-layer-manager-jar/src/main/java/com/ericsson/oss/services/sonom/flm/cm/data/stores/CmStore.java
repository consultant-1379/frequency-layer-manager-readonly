/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.stores;

import java.util.Collection;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

/**
 * A class for access CM Stores.
 */
public class CmStore {

    private CmSectorCellStore cmSectorCellStore;
    private CmNodeObjectsStore cmNodeObjectsStore;
    private Execution execution;

    /**
     * This constructor should be used in production.
     * @param execution current execution holding all necessary information
     */
    public CmStore(final Execution execution) {
        this.execution = execution;
    }

    /**
     * This constructor should be used for testing only.
     * @param cmSectorCellStore a {@link CmSectorCellStore} object
     * @param cmNodeObjectsStore a {@link CmNodeObjectsStore} object
     */
    public CmStore(final CmSectorCellStore cmSectorCellStore, final CmNodeObjectsStore cmNodeObjectsStore) {
        this.cmSectorCellStore = cmSectorCellStore;
        this.cmNodeObjectsStore = cmNodeObjectsStore;
    }

    /**
     * Get the {@link CmSectorCellStore} to access cell and sector cache.
     * <p>
     * {@code inclusionList} contained in {@link Execution} is applied on the cache.
     * @return The instance of {@link CmSectorCellStore} for this execution.
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public CmSectorCellStore getCmSectorCellStore() throws FlmAlgorithmException {
        if (cmSectorCellStore == null) {
            cmSectorCellStore = new CmSectorCellStore(execution);
        }
        return cmSectorCellStore;
    }

    /**
     * Get the {@link CmNodeObjectsStore} to access cache for child objects of node.
     * @param cellIdsOfLBQs a collection of cell ids that should be used to filter which profiles and node data should be collected.
     * @param sectorIds a collection of sectors with non-empty LBQ
     * @return The instance of {@link CmNodeObjectsStore} for this execution
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public CmNodeObjectsStore getCmNodeObjectsStore(final Collection<TopologyObjectId> cellIdsOfLBQs,
                                                    final Collection<Long> sectorIds) throws FlmAlgorithmException {
        if (cmNodeObjectsStore == null) {
            cmNodeObjectsStore = new CmNodeObjectsStore(getCmSectorCellStore(), cellIdsOfLBQs, sectorIds);
        }
        return cmNodeObjectsStore;
    }

    /**
     * This method creates a new {@link CmNodeObjectsStore} and updates cell to node map with given Sector Ids.
     * @param sectorIds a collection of sectors with non-empty LBQ.
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public void createAndPopulateCmNodeObjectsStore(final Collection<Long> sectorIds) throws FlmAlgorithmException {
        cmNodeObjectsStore = new CmNodeObjectsStore(getCmSectorCellStore(), sectorIds);
    }

    /**
     * This method updates frequencies and profiles.
     * @param sectorIds a collection of sectors with non-empty LBQ.
     * @param cellIdsOfLBQs a collection of cell ids that should be used to filter which profiles and node data should be collected.
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public void updateFrequenciesAndProfiles(final Collection<TopologyObjectId> cellIdsOfLBQs,
                                             final Collection<Long> sectorIds) throws FlmAlgorithmException {
        cmNodeObjectsStore.updateFrequenciesAndProfiles(getCmSectorCellStore(), cellIdsOfLBQs, sectorIds);
    }
}
