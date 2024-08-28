/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021-2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateBalancedDistribution;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.calculateCValue;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.FormulaSolver.leakageSimulation;
import static com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper.getTopologyFromTarget;
import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LeakageCell;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.MissingDataCollector;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.ProfileChangesCalculatorImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;

/**
 * This class is used by {@link ProfileChangesCalculatorImpl} to detects and handle the user leakage. When extra users will be moved from source to
 * target, the leakage impact should be checked and when it is significant, it should be handled by distribution value change at target cell.
 */
public class LeakageDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeakageDetector.class);
    private static final String MISSING_KPIS_MESSAGE = "No optimization is possible for sector because leakage cell is missing kpis";
    private static final String FAILED_TO_PERSIST_LEAKAGE_CELLS_MESSAGE = "Change excluded as leakage cells could not be persisted to the DB" +
            " with value: %s";
    private static final String TARGET_CELL_EQUALS_NULL_OR_ZERO_MESSAGE = "Change excluded as Leakage resolution not possible, " +
            "due to the leakage cell distribution " +
            "value for the target cell carrier, being null or zero.";

    private final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent;
    private final ProfileChangeCalculatorSettings configs;
    private final ProfileChangeCalculationHelper helper;
    private final MissingDataCollector missingDataCollector;
    private final LbdarDao lbdarDao;

    public LeakageDetector(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final ProfileChangeCalculatorSettings configs,
            final ProfileChangeCalculationHelper helper,
            final CmNodeObjectsStore profileStore,
            final LbdarDao lbdarDao) {
        this.enrichedPolicyOutputEvent = enrichedPolicyOutputEvent.copyMe();
        this.configs = configs;
        this.helper = helper;
        missingDataCollector = new MissingDataCollector(profileStore, helper);
        this.lbdarDao = lbdarDao;
    }

    /**
     * Detect and handling the leakage from target cell to 3rd cells.
     *
     * @param profileChanges
     *            the profile changes which were prepared so far.
     * @param targetCellId
     *            id of the Target cell where the leakage should be investigated and handled.
     * @return a completed map of profile changes with the result of leakage handling
     * @throws LbdarException
     *             when leakage resolution is not possible
     * @throws LbdarUnexpectedException
     *             a unexpected scenario has arisen that prevents leakage resolution
     */
    public Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> detectLeakage(
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges, final TopologyObjectId targetCellId)
            throws LbdarException, LbdarUnexpectedException {
        final int sourceCarrier = helper.getSourceCellCarrier();
        final int targetCarrier = enrichedPolicyOutputEvent.getCellCmData().get(targetCellId).getCarrier();
        final EnrichedIdleModePrioAtRelease profile = enrichedPolicyOutputEvent.getProfileFromCellId(targetCellId);
        final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo = helper.getSelectedDistributionInfo(profile, targetCellId);

        final Set<LeakageCell> leakageCells = new HashSet<>();
        for (final Integer carrier : distributionInfo.getCarriers()) {
            if (!Arrays.asList(sourceCarrier, targetCarrier).contains(carrier)) {
                final Optional<TopologyObjectId> optionalThirdCellLeakage = detectLeakageInFreq(profileChanges, targetCellId, carrier);
                optionalThirdCellLeakage
                        .ifPresent(thirdCellLeakage -> leakageCells.add(new LeakageCell(thirdCellLeakage.getFdn(), thirdCellLeakage.getOssId())));
            }
        }
        if (!leakageCells.isEmpty()) {
            persistLeakageCells(leakageCells);
        }

        return profileChanges;
    }

    private Optional<TopologyObjectId> detectLeakageInFreq(final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges,
            final TopologyObjectId targetCellId, final int thirdCellCarrier) throws LbdarException, LbdarUnexpectedException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    String.format("Detecting leakage for target cell %s and carrier %d", targetCellId.getFdn(), thirdCellCarrier)));
        }
        final TopologyObjectId thirdCellId = getThirdCellId(thirdCellCarrier);
        final Cell thirdCell = enrichedPolicyOutputEvent.getCell(thirdCellId);
        validateCellExistence(thirdCellId, thirdCell);
        validateProfileReferenceAtCell(thirdCell);
        validateCellKpiExistence(thirdCellId);
        validateCellUsers(thirdCellId);

        final TopologyObjectId thirdProfileId = TopologyObjectId.of(thirdCell.getIdleModePrioAtReleaseRef(),
                thirdCellId.getOssId());
        if (!isProfileInStoreAlready(thirdProfileId)) {
            missingDataCollector.updateInputDataWithMissingThirdProfileData(enrichedPolicyOutputEvent, thirdCell);
        }
        final Optional<EnrichedIdleModePrioAtRelease> optionalProfileChange = calculateLeakage(profileChanges, targetCellId, thirdCellId);
        optionalProfileChange.ifPresent(profileChange -> {
            profileChanges.put(targetCellId, profileChange);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                        enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                        String.format("Profile change due to leakage handling at cell %s", targetCellId)));
            }
        });
        if (profileChanges.containsKey(targetCellId)) {
            return Optional.of(thirdCellId);
        } else {
            return Optional.empty();
        }

    }

    private TopologyObjectId getThirdCellId(final int carrier) throws LbdarException {
        final TopologyObjectId thirdCellId = enrichedPolicyOutputEvent.getCellByCarrier(carrier);
        if (thirdCellId == null) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format("Failed to find leakage cell for carrier %d", carrier));
            throw new LbdarException(String.format("Failed to find leakage cell for carrier %d", carrier));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    String.format("Leakage cell found %s", thirdCellId.getFdn())));
        }
        return thirdCellId;
    }

    private void validateCellKpiExistence(final TopologyObjectId cellId) throws LbdarException {
        if (!enrichedPolicyOutputEvent.getCellKpis().containsKey(cellId)) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format(MISSING_KPIS_MESSAGE + " at %s",
                            cellId.getFdn()));
            throw new LbdarException(String.format(MISSING_KPIS_MESSAGE + " at %s",
                    cellId.getFdn()));
        }
    }

    private void validateCellUsers(final TopologyObjectId cellId) throws LbdarException {
        if (enrichedPolicyOutputEvent.getCellKpis().get(cellId).getSubscriptionRatio() == 0.0d) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format("Cell %s can't be used as leakage cell because subscription ratio is zero",
                            cellId.getFdn()));
            throw new LbdarException(String.format("Cell %s can't be used as leakage cell because subscription ratio is zero",
                    cellId.getFdn()));
        }
        if (enrichedPolicyOutputEvent.getCellKpis().get(cellId).getConnectedUsers() == 0.0d) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format("Cell %s can't be used as leakage cell because connected user number is zero",
                            cellId.getFdn()));
            throw new LbdarException(String.format("Cell %s can't be used as leakage cell because connected user number is zero",
                    cellId.getFdn()));
        }
    }

    private void validateCellExistence(final TopologyObjectId thirdCellId, final Cell thirdCell) throws LbdarException {
        if (thirdCell == null) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format("Cell object is not found for potential leakage cell of %s", thirdCellId.getFdn()));
            throw new LbdarException(String.format("Cell object is not found for potential leakage cell of %s", thirdCellId.getFdn()));
        }
    }

    private void validateProfileReferenceAtCell(final Cell thirdCell) throws LbdarException {
        if (StringUtils.isEmpty(thirdCell.getIdleModePrioAtReleaseRef())) {
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format("Profile is not associated with leakage cell %s", thirdCell.getFdn()));
            throw new LbdarException(String.format("Profile is not associated with leakage cell %s", thirdCell.getFdn()));
        }
    }

    private Optional<EnrichedIdleModePrioAtRelease> calculateLeakage(final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> profileChanges,
            final TopologyObjectId targetCellId,
            final TopologyObjectId thirdCellId) throws LbdarException, LbdarUnexpectedException {

        final EnrichedIdleModePrioAtRelease targetProfile = Optional.ofNullable(profileChanges
                .get(targetCellId)).orElse(enrichedPolicyOutputEvent.getProfileFromCellId(targetCellId));
        final EnrichedIdleModePrioAtRelease thirdPartyProfile = enrichedPolicyOutputEvent.getProfileFromCellId(thirdCellId);

        final EnrichedDistributionInfo targetDist = helper.getSelectedDistributionInfo(targetProfile, targetCellId);
        final EnrichedDistributionInfo thirdPartyDist = helper.getSelectedDistributionInfo(thirdPartyProfile, thirdCellId);

        final long targetUsersFromLbq = enrichedPolicyOutputEvent.getUsersToMoveFromCellId(targetCellId);
        final long thirdUsersFromLbq = getUsersToMoveFromLoadBalancingQuanta(thirdCellId);

        final long targetConnectedUsers = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(targetCellId).getConnectedUsers());
        final long targetUsers = targetConnectedUsers + targetUsersFromLbq;
        final long thirdConnectedUsers = Math.round(enrichedPolicyOutputEvent.getCellKpis().get(thirdCellId).getConnectedUsers());
        final long thirdUsers = thirdConnectedUsers + thirdUsersFromLbq;

        final int thirdCellCarrier = enrichedPolicyOutputEvent.getCell(thirdCellId).getCarrier();
        final int targetCellCarrier = enrichedPolicyOutputEvent.getCell(targetCellId).getCarrier();
        final float targetToThirdDist = targetDist
                .getDistributionOfFrequency(thirdCellCarrier);
        final Float thirdDistToTarget = thirdPartyDist
                .getDistributionOfFrequency(targetCellCarrier);
        validateNoPushBackFromThirdDistToTarget(thirdDistToTarget, enrichedPolicyOutputEvent);
        if (significantLeakage(targetUsers, thirdUsers, targetToThirdDist, thirdDistToTarget)) {
            final long newTargetUsers = leakageSimulation(targetUsers, targetToThirdDist, thirdUsers,
                    thirdDistToTarget, 0);
            if (configs.leakageThresholdsAreBreached(targetUsersFromLbq, targetUsers, thirdConnectedUsers, newTargetUsers,
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId())) {
                validateLeakageResolutionPossibility(targetToThirdDist, thirdDistToTarget, enrichedPolicyOutputEvent);
                final float c = configs.cValueIsOverrode() ? 1
                        : calculateCValue(targetConnectedUsers, targetToThirdDist, thirdConnectedUsers, thirdDistToTarget);
                final float newTargetToThirdDist = calculateBalancedDistribution(thirdUsers, thirdDistToTarget, c, targetUsers);
                return targetToThirdDist > newTargetToThirdDist
                        ? Optional.of(helper.updateDistributionOfProfile(targetProfile, targetCellId, thirdCellId, newTargetToThirdDist))
                        : Optional.empty();
            }
        }
        return Optional.empty();
    }

    private int getUsersToMoveFromLoadBalancingQuanta(final TopologyObjectId cellId) {
        return enrichedPolicyOutputEvent.getTargetCells().stream()
                .filter(t -> getTopologyFromTarget(t).equals(cellId))
                .mapToInt(t -> Integer.parseInt(t.getTargetUsersMove()))
                .findFirst()
                .orElse(0);
    }

    private boolean isProfileInStoreAlready(final TopologyObjectId profileId) {
        return enrichedPolicyOutputEvent.getProfiles().containsKey(profileId);
    }

    private static boolean significantLeakage(final long targetUsers, final long thirdPartyUsers,
            final Float targetToThirdDist, final Float thirdDistToTarget) {
        return targetUsers * targetToThirdDist > thirdPartyUsers * thirdDistToTarget;
    }

    private void validateLeakageResolutionPossibility(final Float targetToThirdDist,
            final Float thirdDistToTarget, final EnrichedPolicyOutputEvent changesCalculationInput)
            throws LbdarException {
        if (thirdDistToTarget == null || isDistributionValueZero(thirdDistToTarget)) {
            logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                    TARGET_CELL_EQUALS_NULL_OR_ZERO_MESSAGE);
            throw new LbdarException(TARGET_CELL_EQUALS_NULL_OR_ZERO_MESSAGE);
        }
        if (targetToThirdDist > configs.getExistingHighPush()) {
            logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                    "Change excluded due to high push to leakage cell");
            throw new LbdarException("Change excluded due to high push to leakage cell");
        }
    }

    private void validateNoPushBackFromThirdDistToTarget(final Float thirdDistToTarget,
            final EnrichedPolicyOutputEvent changesCalculationInput)
            throws LbdarException {
        if (thirdDistToTarget == null) {
            logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                    TARGET_CELL_EQUALS_NULL_OR_ZERO_MESSAGE);
            throw new LbdarException(TARGET_CELL_EQUALS_NULL_OR_ZERO_MESSAGE);

        }
    }

    private boolean isDistributionValueZero(final Float distribution) {
        return BigDecimal.valueOf(0.0f).round(new MathContext(2)).equals(BigDecimal.valueOf(distribution));
    }

    private void persistLeakageCells(final Set<LeakageCell> leakageCells) throws LbdarException {
        try {
            lbdarDao.insertLeakageCells(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId(),
                    leakageCells);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    String.format(FAILED_TO_PERSIST_LEAKAGE_CELLS_MESSAGE,
                            leakageCells));
            throw new LbdarException(String.format(FAILED_TO_PERSIST_LEAKAGE_CELLS_MESSAGE,
                    leakageCells), e);
        }
    }
}
