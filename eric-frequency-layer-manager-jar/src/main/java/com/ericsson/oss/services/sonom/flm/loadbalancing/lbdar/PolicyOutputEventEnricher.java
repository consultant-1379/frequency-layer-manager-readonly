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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.EUtranFrequency;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class is used to enrich PolicyOutputEvent with cm and kpi data so that can be processed with ProfileChangesCalculator.
 */
public class PolicyOutputEventEnricher {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyOutputEventEnricher.class);
    private final CmNodeObjectsStore cmNodeObjectStore;
    private final CmSectorCellStore cmCellStore;
    private final CellKpiStore cellKpiStore;

    public PolicyOutputEventEnricher(final CmNodeObjectsStore cmNodeObjectStore, final CmSectorCellStore cmCellStore,
                                     final CellKpiStore cellKpiStore) {
        this.cmNodeObjectStore = cmNodeObjectStore;
        this.cmCellStore = cmCellStore;
        this.cellKpiStore = cellKpiStore;
    }

    /**
     * This methods adds cm and kpi data to {@link PolicyOutputEvent} and returns this {@link EnrichedPolicyOutputEvent}.
     * 
     * @param policyOutputEvent
     *            a {@link PolicyOutputEvent} object
     * @return returns an {@link EnrichedPolicyOutputEvent} object
     */
    public EnrichedPolicyOutputEvent enrich(final PolicyOutputEvent policyOutputEvent) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
            "Enriching PolicyOutputEvent"));
        }
        final Map<TopologyObjectId, Cell> cmCellData = collectCmCellData(policyOutputEvent);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                String.format("Collected %d cells", cmCellData.size())));
        }
        final Map<TopologyObjectId, IdleModePrioAtRelease> profiles = collectProfiles(policyOutputEvent, cmCellData);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                String.format("Collected %d profiles", profiles.size())));
        }
        final Map<TopologyObjectId, CellKpis> cellKpis = collectKpis(cmCellData.values());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                String.format("Collected %d kpis", cellKpis.size())));
        }
        final Node node = cmNodeObjectStore.getNodeForCellFdn(policyOutputEvent.getLoadBalancingQuanta().getSourceCellFdn(),
                policyOutputEvent.getLoadBalancingQuanta().getSourceCellOssId());
        final Map<TopologyObjectId, Integer> frequencyToCarrier = collectFrequencyToCarrier(cmCellData.values());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                    String.format("Collected %d frequencies to carrier entries", frequencyToCarrier.size())));
        }

        // collect the cell to frequency mapping only if there were duplicate carriers
        Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency = new HashMap<>();
        if (frequencyToCarrier.values().stream().collect(Collectors.toSet()).size() < frequencyToCarrier.values().size()) {
            duplicateCarrierCellToFrequency = collectDuplicateCarrierCellToFrequency(cmCellData.values(), frequencyToCarrier, policyOutputEvent);
            LOGGER.info(LoggingFormatter.formatMessage(policyOutputEvent.getExecutionId(), policyOutputEvent.getSectorId(),
                    String.format("Found %d duplicated carriers in the sector's node list of frequencies.", duplicateCarrierCellToFrequency.size())));
        }
        return new EnrichedPolicyOutputEvent(policyOutputEvent, node, cmCellData, enrichProfiles(profiles, frequencyToCarrier), cellKpis,
                frequencyToCarrier, duplicateCarrierCellToFrequency);
    }

    private Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> enrichProfiles(final Map<TopologyObjectId, IdleModePrioAtRelease> profiles,
            final Map<TopologyObjectId, Integer> frequencyIdToCarrier) {
        return profiles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new EnrichedIdleModePrioAtRelease(entry.getValue(),
                        collectOnlyFdns(frequencyIdToCarrier))));
    }

    private Map<String, Integer> collectOnlyFdns(final Map<TopologyObjectId, Integer> frequencyToCarrier) {
        return frequencyToCarrier.entrySet().stream()
                .collect(Collectors.toMap(freqIdToCarrier -> freqIdToCarrier.getKey().getFdn(), Map.Entry::getValue));
    }

    private Set<TopologyObjectId> collectCellIds(final ProposedLoadBalancingQuanta loadBalancingQuanta) {
        final Set<TopologyObjectId> cellIds = new HashSet<>();
        cellIds.add(new TopologyObjectId(loadBalancingQuanta.getSourceCellFdn(), loadBalancingQuanta.getSourceCellOssId()));
        cellIds.addAll(loadBalancingQuanta.getTargetCells().stream()
                .map(targetCell -> new TopologyObjectId(targetCell.getTargetCellFdn(), targetCell.getTargetCellOssId()))
                .collect(Collectors.toList()));
        return cellIds;
    }

    private Map<TopologyObjectId, Cell> collectCmCellData(final PolicyOutputEvent policyOutputEvent) {
        final TopologySector sector = cmCellStore.getFullSector(policyOutputEvent.getSectorId());
        if (sector != null) {
            return sector.getAssociatedCells().stream()
                    .map(cell -> Pair.of(cell.getTopologyObjectId(), cell))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        }
        return new HashMap<>();
    }

    private Map<TopologyObjectId, IdleModePrioAtRelease> collectProfiles(final PolicyOutputEvent policyOutputEvent,
            final Map<TopologyObjectId, Cell> cmCellData) {
        final Set<TopologyObjectId> cellIds = collectCellIds(policyOutputEvent.getLoadBalancingQuanta());

        return cellIds.stream()
                .map(cellId -> Pair.of(cellId, cmCellData.get(cellId)))
                .filter(cellIdToCell -> cellIdToCell.getRight() != null)
                .map(cellIdToCell -> Pair.of(cellIdToCell.getLeft(), cellIdToCell.getRight().getIdleModePrioAtReleaseRef()))
                .filter(cellIdToProfileRef -> !StringUtils.isEmpty(cellIdToProfileRef.getRight()))
                .map(cellIdToProfileRef -> Pair.of(
                        TopologyObjectId.of(cellIdToProfileRef.getRight(), cellIdToProfileRef.getLeft().getOssId()),
                        cmNodeObjectStore.getIdleModePrioAtRelease(cellIdToProfileRef.getRight(),
                                cellIdToProfileRef.getLeft().getOssId())))
                .filter(profileIdToProfile -> profileIdToProfile.getRight() != null)
                .distinct()
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Map<TopologyObjectId, Integer> collectFrequencyToCarrier(final Collection<Cell> cells) {
        return cells.stream()
                .map(cell -> cmNodeObjectStore.getNodeForCellFdn(cell.getFdn(), cell.getOssId()))
                .filter(Objects::nonNull)
                .distinct()
                .map(node -> cmNodeObjectStore.getEutranFrequencies(node.getFdn(), node.getOssId()))
                .flatMap(Collection::stream)
                .map(frequency -> Pair.of(frequency.getTopologyObjectId(), frequency.getArfcnValueEUtranDl()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Map<TopologyObjectId, CellKpis> collectKpis(final Collection<Cell> cells) {

        return cells.stream()
                .map(cell -> Pair.of(cell.getTopologyObjectId(), cellKpiStore.getKpisForCell(cell.getTopologyObjectId().getFdn(),
                        cell.getTopologyObjectId().getOssId())))
                .filter(cellKpiPair -> cellKpiPair.getRight() != null)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Map<TopologyObjectId, TopologyObjectId> collectDuplicateCarrierCellToFrequency(
    final Collection<Cell> cells, final Map<TopologyObjectId, Integer> frequencyToCarrier, final PolicyOutputEvent poe) {

        final Set<Integer> duplicateCarriers = findDuplicates(frequencyToCarrier.values());
        LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
            String.format("Found more than one frequency for the following carrier values: '%s'", duplicateCarriers)));

        final Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency = new HashMap<>();
        for (final Cell cell : cells) {
            if (duplicateCarriers.contains(cell.getCarrier())) {
                collectDuplicatesForNode(duplicateCarrierCellToFrequency, cell, poe);
            }
        }
        return duplicateCarrierCellToFrequency;
    }

    private void collectDuplicatesForNode(final Map<TopologyObjectId, TopologyObjectId> duplicateCarrierCellToFrequency, final Cell cell, 
                                          final PolicyOutputEvent poe) {
        final Node node = cmNodeObjectStore.getNodeForCellFdn(cell.getFdn(), cell.getOssId());
        if (node == null) {
            LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                String.format("Node not found for cell: '%s'", cell.getFdn())));
        } else {
            for (final EUtranFrequency eUtranFrequency : cmNodeObjectStore.getEutranFrequencies(node.getFdn(), node.getOssId())) {
                if (eUtranFrequency.getArfcnValueEUtranDl() == cell.getCarrier()) {
                    LOGGER.info(LoggingFormatter.formatMessage(poe.getExecutionId(), poe.getSectorId(),
                        String.format(
                                "For duplicate carrier %d, storing frequency FDN from the cell's node: cell FDN '%s', frequency FDN: '%s'",
                                cell.getCarrier(), cell.getFdn(), eUtranFrequency.getFdn())));
                    duplicateCarrierCellToFrequency.put(cell.getTopologyObjectId(), eUtranFrequency.getTopologyObjectId());
                }
            }
        }
    }

    private <T> Set<T> findDuplicates(final Collection<T> collection) {
        final Set<T> duplicates = new LinkedHashSet<>();
        final Set<T> uniques = new HashSet<>();
        for (final T t : collection) {
            if (!uniques.add(t)) {
                duplicates.add(t);
            }
        }
        return duplicates;
    }
}
