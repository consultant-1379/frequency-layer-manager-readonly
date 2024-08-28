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

package com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmRestClientCreator;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LeakageCell;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDaoImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.SourceOfChangeCalculator;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAStageExecutor;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementRetriever;

/**
 * PACellIdRetrieverExecutor is used to retrieve List of Sector ID and its corresponding source and target cells ({@link TopologyObjectId}) for PA.
 */
public class PACellIdRetrieverExecutor implements PAStageExecutor<Map<Long, List<TopologyObjectId>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PACellIdRetrieverExecutor.class);

    private final OptimizationsDao optimizationsDao;
    private final PAExecution paExecution;
    private final String sourceOfChange;
    private final ChangeElementRetriever changeElementRetriever;
    private final CmRestExecutor cmRestExecutor;
    private final LbdarDao lbdarDao;

    /**
     * Constructor for PACellIdRetrieverExecutor.
     *
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param flmExecution
     *            {@link Execution} instance is used calculate sourceOfChange
     */
    public PACellIdRetrieverExecutor(final PAExecution paExecution, final Execution flmExecution) {
        this.paExecution = paExecution;
        sourceOfChange = SourceOfChangeCalculator.calculateSourceOfChange(flmExecution.getConfigurationId());
        optimizationsDao = new OptimizationsDaoImpl(PAConstants.MAX_RETRY_ATTEMPTS, PAConstants.SECONDS_TO_WAIT);
        lbdarDao = new LbdarDaoImpl(PAConstants.MAX_RETRY_ATTEMPTS, PAConstants.SECONDS_TO_WAIT);

        cmRestExecutor = new CmRestClientCreator(PAConstants.MAX_RETRY_ATTEMPTS,
                PAConstants.SECONDS_TO_WAIT).getClientWithRetry();
        changeElementRetriever = new ChangeElementRetriever(paExecution, cmRestExecutor, sourceOfChange);
    }

    /**
     * This Constructor of PACellIdRetrieverExecutor only for unit test.
     *
     * @param paExecution
     *            {@link PAExecution} instance to get PA execution related information
     * @param sourceOfChange
     *            This parameter is used to filter parameter for REST call
     * @param changeElementRetriever
     *            {@link ChangeElementRetriever} instance to inject for unit test
     * @param optimizationsDao
     *            {@link OptimizationsDao} instance to inject for unit test
     */
    PACellIdRetrieverExecutor(final PAExecution paExecution, final String sourceOfChange, final CmRestExecutor cmRestExecutor,
            final ChangeElementRetriever changeElementRetriever, final OptimizationsDao optimizationsDao, final LbdarDao lbdarDao) {
        this.paExecution = paExecution;
        this.sourceOfChange = sourceOfChange;
        this.changeElementRetriever = changeElementRetriever;
        this.optimizationsDao = optimizationsDao;
        this.cmRestExecutor = cmRestExecutor;
        this.lbdarDao = lbdarDao;
    }

    @Override
    public Map<Long, List<TopologyObjectId>> execute() throws PAExecutionException {
        LOGGER.info("Retrieving sector id and corresponding source and target cells for Execution ID {}, sourceOfChange {}",
                paExecution.getFlmExecutionId(),
                sourceOfChange);
        final Map<Long, List<TopologyObjectId>> sectorCellMap;
        final List<ChangeElement> changeElementList = changeElementRetriever.retrieveChangeElementList();
        if (changeElementList.isEmpty()) {
            LOGGER.info("No change elements available for FLM Execution id: {}", paExecution.getFlmExecutionId());
            sectorCellMap = new HashMap<>();
            return sectorCellMap;
        }
        try {
            sectorCellMap = getSectorCellMap(changeElementList);
        } catch (final SQLException e1) {
            throw new PAExecutionException(e1);
        }
        return sectorCellMap;
    }

    private Map<Long, List<TopologyObjectId>> getSectorCellMap(final List<ChangeElement> retrievedChangeElements) throws SQLException {
        LOGGER.debug("Filtering changeID from list of change elements");
        final Set<String> sectorIdSet = retrievedChangeElements.stream()
                .map(ChangeElement::getChangeId).collect(Collectors.toSet());
        return getSectorCellIdList(sectorIdSet);
    }

    private Map<Long, List<TopologyObjectId>> getSectorCellIdList(final Set<String> sectorIdSet) throws SQLException {
        LOGGER.debug("Retrieving sector id and corresponding source and target cells from DB");
        final Map<Long, List<TopologyObjectId>> sectorCellMap = new HashMap<>();
        final List<PolicyOutputEvent> policyOutputEvents = optimizationsDao.getOptimizationsFiltered(paExecution.getFlmExecutionId());
        if (policyOutputEvents.isEmpty()) {
            LOGGER.info("No LBQ available in DB for FLM Execution id: {} and list of sector ids: {}", paExecution.getFlmExecutionId(), sectorIdSet);
            return sectorCellMap;
        }
        for (final PolicyOutputEvent event : policyOutputEvents) {
            final ProposedLoadBalancingQuanta lbq = event.getLoadBalancingQuanta();
            final Set<TopologyObjectId> cells = new HashSet<>();
            if (sectorIdSet.contains(event.getSectorId().toString())) {
                cells.add(new TopologyObjectId(lbq.getSourceCellFdn(), lbq.getSourceCellOssId()));
                for (final TargetCell targetCell : lbq.getTargetCells()) {
                    cells.add(new TopologyObjectId(targetCell.getTargetCellFdn(), targetCell.getTargetCellOssId()));
                }
                LOGGER.info("For FLM Execution id: {} and sector id: {}, the number of source and target cells for monitoring is: {}",
                        event.getExecutionId(),
                        event.getSectorId(),
                        cells.size());

                final Set<LeakageCell> leakageCells = lbdarDao.getLeakageCells(event.getExecutionId(), event.getSectorId());
                for (final LeakageCell leakageCell : leakageCells) {
                    cells.add(new TopologyObjectId(leakageCell.getFdn(), leakageCell.getOssId()));
                }
                LOGGER.info("For FLM Execution id: {} and sector id: {}, the number of leakage cells for monitoring is: {}",
                        event.getExecutionId(),
                        event.getSectorId(),
                        leakageCells.size());
            }
            if (!cells.isEmpty()) {
                sectorCellMap.put(event.getSectorId(), new ArrayList<>(cells));
            }
        }
        LOGGER.info("Total sectors eligible for performance assurance processing: {}", sectorCellMap.size());
        LOGGER.debug("Sectors eligible for performance assurance processing: {}", sectorCellMap.keySet());
        return sectorCellMap;
    }
}
