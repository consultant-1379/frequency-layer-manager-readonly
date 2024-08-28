/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class is used as input to ProfileChangesCalculator. It contains a {@link PolicyOutputEvent} and some cm and kpi data that is related to cells
 * of policyOutputEvent
 */
public class EnrichedPolicyOutputEvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichedPolicyOutputEvent.class);
    private final PolicyOutputEvent policyOutputEvent;
    private final Map<TopologyObjectId, Cell> cellCmData;
    private final Map<Integer, TopologyObjectId> carrierToCellIdIndex = new HashMap<>();
    private final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profiles;
    private final Map<TopologyObjectId, Integer> frequencyToCarrier;
    private final Map<TopologyObjectId, CellKpis> cellKpis;
    private final Node sourceCellNode;
    private final Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency;

    public EnrichedPolicyOutputEvent(final PolicyOutputEvent policyOutputEvent,
            final Node sourceCellNode,
            final Map<TopologyObjectId, Cell> cellCmData,
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profiles,
            final Map<TopologyObjectId, CellKpis> cellKpis,
            final Map<TopologyObjectId, Integer> frequencyToCarrier,
            final Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency) {
        this.policyOutputEvent = policyOutputEvent;
        this.sourceCellNode = sourceCellNode;
        this.cellCmData = cellCmData;
        this.profiles = profiles;
        this.frequencyToCarrier = frequencyToCarrier;
        this.cellKpis = cellKpis;
        this.duplicateCarrierCellToFrequency = duplicateCarrierCellToFrequency;
        createCarrierToCellIndex();
    }

    public PolicyOutputEvent getPolicyOutputEvent() {
        return policyOutputEvent;
    }

    public Cell getCell(final TopologyObjectId cellId) {
        return cellCmData.get(cellId);
    }

    public Map<TopologyObjectId, Cell> getCellCmData() {
        return cellCmData;
    }

    public Node getSourceCellNode() {
        return sourceCellNode;
    }

    public Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> getProfiles() {
        return profiles;
    }

    /**
     * Gives back the profile connected to the given cell.
     * 
     * @param cellId
     *            is a {@link TopologyObjectId} we have to find the profile of this cell.
     * @return a profile
     */
    public EnrichedIdleModePrioAtRelease getProfileFromCellId(final TopologyObjectId cellId) {
        return profiles.getOrDefault(TopologyObjectId.of(cellCmData.get(cellId).getIdleModePrioAtReleaseRef(),
                cellId.getOssId()), null);
    }

    /**
     * Gives back the users to move for a given cell from the LBQ.
     * 
     * @param cellId
     *            id of the cell where we need the users to move from lbq
     * @return the users to move number or 0 if not found
     * @throws LbdarUnexpectedException
     *             when no usersToMove numbers can be found at cell or the usersToMove is zero or negative
     */
    public long getUsersToMoveFromCellId(final TopologyObjectId cellId) throws LbdarUnexpectedException {
        final long usersToMove;

        if (isItSourceCell(cellId)) {
            usersToMove = Long.parseLong(policyOutputEvent.getLoadBalancingQuanta().getSourceUsersMove());
        } else {
            usersToMove = getUsersToMoveForTargetCell(cellId);
        }
        if (usersToMove <= 0) {
            final String errorMessage = String.format("Value of usersToMove was not found or was zero/negative at cell %s", cellId.getFdn());
            throw new LbdarUnexpectedException(errorMessage);
        }
        return usersToMove;
    }

    public Map<TopologyObjectId, CellKpis> getCellKpis() {
        return cellKpis;
    }

    public Map<TopologyObjectId, TopologyObjectId> getDuplicateCarrierCellToFrequency() {
        return duplicateCarrierCellToFrequency;
    }

    public List<TargetCell> getTargetCells() {
        return policyOutputEvent.getLoadBalancingQuanta().getTargetCells();
    }

    /**
     * Creates a shallow copy from itself.
     * 
     * @return the copied object.
     */
    public EnrichedPolicyOutputEvent copyMe() {
        return new EnrichedPolicyOutputEvent(policyOutputEvent,
                sourceCellNode,
                new HashMap<>(cellCmData),
                new HashMap<>(profiles),
                new HashMap<>(cellKpis),
                new HashMap<>(frequencyToCarrier),
                new HashMap<>(duplicateCarrierCellToFrequency));
    }

    /**
     * Returns the FDN frequency of the given carrier.
     * 
     * @param carrier
     *            the carrier frequency number
     * @param cell
     *            cell associate with this carrier frequency
     * @return the fdn of the carrier frequency
     * @throws LbdarException
     *             if a cell's carrier cannot be uniquely identified
     */
    public String getFrequencyForCarrier(final int carrier, final TopologyObjectId cell) throws LbdarException {
        final String fdn = getFrequencyFromSourceProfile(carrier);
        if (fdn == null) {
            return getFrequencyFromNode(carrier, cell);
        } else {
            return fdn;
        }
    }

    /**
     * Return a cell id based on carrier. Only one cell should exists for given carrier.
     * 
     * @param carrier
     *            the carrier of a cell
     * @return the {@link TopologyObjectId} of a cell
     */
    public TopologyObjectId getCellByCarrier(final int carrier) {
        return carrierToCellIdIndex.get(carrier);
    }

    /**
     * Returns a map of {@link EUtranFrequency} {@link TopologyObjectId} to carrier.
     * 
     * @return a map from {@link TopologyObjectId} of {@link EUtranFrequency} to carrier
     */
    public Map<TopologyObjectId, Integer> getFrequencyToCarrier() {
        return frequencyToCarrier;
    }

    /**
     * It returns a {@link EUtranFrequency} fdn to carrier value map.
     * 
     * @return returns a map from frequency to carrier
     */
    public Map<String, Integer> getFrequencyFdnToCarrier() {
        return frequencyToCarrier.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getFdn(), Map.Entry::getValue));
    }

    /**
     * This method should be used for testing purposes only. It is adding a cell to the list of cells and updates carrier to cell index.
     * 
     * @param cellId
     *            A {@link TopologyObjectId} object of the cell
     * @param cell
     *            An instance of {@link Cell}.
     */
    public void addCell(final TopologyObjectId cellId, final Cell cell) {
        cellCmData.put(cellId, cell);
        createCarrierToCellIndex();
    }

    private void createCarrierToCellIndex() {
        carrierToCellIdIndex.clear();
        cellCmData.values()
                .forEach(cell -> carrierToCellIdIndex.put(cell.getCarrier(), cell.getTopologyObjectId()));
    }

    private long getUsersToMoveForTargetCell(final TopologyObjectId cellId) {
        return getTargetCells().stream()
                .filter(t -> TopologyObjectId.of(t.getTargetCellFdn(), t.getTargetCellOssId()).equals(cellId))
                .mapToLong(t -> Long.parseLong(t.getTargetUsersMove()))
                .findFirst()
                .orElse(0);
    }

    private boolean isItSourceCell(final TopologyObjectId cellId) {
        final ProposedLoadBalancingQuanta lbq = policyOutputEvent.getLoadBalancingQuanta();
        return cellId.equals(TopologyObjectId.of(lbq.getSourceCellFdn(), lbq.getSourceCellOssId()));
    }

    private String getFrequencyFromSourceProfile(final int carrier) {
        final EnrichedIdleModePrioAtRelease sourceProfile = getProfileFromCellId(
                TopologyObjectId.of(getPolicyOutputEvent().getSourceCellFdn(), getPolicyOutputEvent().getSourceCellOssdId()));
        return sourceProfile.getLowLoadDistributionInfo().getFrequency(carrier);
    }

    private String getFrequencyFromNode(final int carrier, final TopologyObjectId cell) throws LbdarException {
        String fdn = "";
        int countOfCarriers = 0;
        for (final Map.Entry<TopologyObjectId, Integer> freqToCarrier : this.getFrequencyToCarrier().entrySet()) {
            if (freqToCarrier.getValue() == carrier) {
                countOfCarriers++;
                fdn = freqToCarrier.getKey().getFdn();
            }
        }
        if (countOfCarriers != 1) {
            // If we have more than one carrier then check the duplicateCarrierCellToFrequency map for the frequency
            final TopologyObjectId cellFreq = duplicateCarrierCellToFrequency.get(cell);
            if (cellFreq != null) {
                LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                        String.format("Found frequency %s where there was a duplicate carrier %d in the sector's node list of frequencies.",
                                cellFreq.getFdn(), carrier)));
                return cellFreq.getFdn();
            }
            final String errorMessage = String.format(
                    "No optimization is possible for sector because carrier frequency for target cell " +
                            "OSS ID: %s FDN : %s could not be identified, carrier %s was found %s times.",
                    cell.getOssId(), cell.getFdn(), carrier, countOfCarriers);
            logFilteredSector(this.getPolicyOutputEvent().getExecutionId(), String.valueOf(this.getPolicyOutputEvent().getSectorId()), errorMessage);
            throw new LbdarException(errorMessage);
        }
        LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                String.format("Found frequency %s in the node frequency list for carrier %d.", fdn, carrier)));
        return fdn;
    }
}
