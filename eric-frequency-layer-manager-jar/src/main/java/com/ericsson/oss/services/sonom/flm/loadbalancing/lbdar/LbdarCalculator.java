/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021-2022
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.LoadBalancingCalculator;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * This class calculates a list of {@link ChangeElement}s for a given list of {@link PolicyOutputEvent}s.
 */
public class LbdarCalculator implements LoadBalancingCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LbdarCalculator.class);
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected exception occurred - ";
    private static final String EXCEPTION_SLOGAN = "Exception was %s";

    private PolicyOutputEventEnricher policyOutputEnricher;
    private ProfileChangesEnricher profileChangesEnricher;
    private final PolicyOutputEventValidator policyOutputEventValidator;
    private ProfileChangesCalculator profileChangeCalculator;
    private ChangeElementCalculator changeElementCalculator;
    private ReversionElementCalculator reversionElementCalculator;
    private EnrichedPolicyOutputEventValidator enrichedPolicyOutputEventValidator;
    private ProfileChangesValidator profileChangesValidator;

    public LbdarCalculator(final Execution execution,
            final CmStore cmStore,
            final CellKpiStore cellKpiStore,
            final Map<String, String> customizedGlobalSettings,
            final List<PolicyOutputEvent> policyOutputEvents) throws FlmAlgorithmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "Creating LBDAR Calculator"));
        }
        final Collection<TopologyObjectId> cellIds = collectCellIds(policyOutputEvents);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Number of %d cellIds were collected for initializing CmNodeObjectStore", cellIds.size())));
        }
        final Collection<Long> sectorIds = collectSectorIds(policyOutputEvents);
        cmStore.updateFrequenciesAndProfiles(cellIds, sectorIds);
        policyOutputEnricher = new PolicyOutputEventEnricher(cmStore.getCmNodeObjectsStore(cellIds, sectorIds),
                cmStore.getCmSectorCellStore(), cellKpiStore);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(execution.getId(), "PolicyOutputEventEnricher has been created"));
        }
        profileChangesEnricher = new ProfileChangesEnricher(cmStore.getCmNodeObjectsStore(cellIds, sectorIds));
        profileChangeCalculator = new ProfileChangesCalculatorImpl(customizedGlobalSettings, cmStore.getCmNodeObjectsStore(cellIds, sectorIds));
        changeElementCalculator = new ChangeElementCalculatorImpl(execution.getId(), execution.getConfigurationId(), execution.isOpenLoop());
        reversionElementCalculator = new ReversionElementCalculatorImpl(cmStore.getCmSectorCellStore(),
                cmStore.getCmNodeObjectsStore(cellIds, sectorIds));
        enrichedPolicyOutputEventValidator = new EnrichedPolicyOutputEventValidator();
        policyOutputEventValidator = new PolicyOutputEventValidator();
        profileChangesValidator = new ProfileChangesValidator();
    }

    // used for testing only
    public LbdarCalculator(final Execution execution,
            final CmStore cmStore,
            final CellKpiStore cellKpiStore,
            final Map<String, String> customizedGlobalSettings,
            final List<PolicyOutputEvent> policyOutputEvents,
            final LbdarDao lbdarDao) throws FlmAlgorithmException {

        this(execution, cmStore, cellKpiStore, customizedGlobalSettings, policyOutputEvents);

        final Collection<TopologyObjectId> cellIds = collectCellIds(policyOutputEvents);
        final Collection<Long> sectorIds = collectSectorIds(policyOutputEvents);
        profileChangeCalculator = new ProfileChangesCalculatorImpl(customizedGlobalSettings, cmStore.getCmNodeObjectsStore(cellIds, sectorIds),
                lbdarDao);
    }

    @Override
    public List<Pair<ChangeElement, ChangeElement>> calculateChanges(final List<PolicyOutputEvent> policyOutputEvents) {
        LOGGER.info("Starting to calculate LBDAR Profile Changes for {} PolicyOutputEvents", policyOutputEvents.size());

        final List<ProfileChanges> profileChanges = policyOutputEvents.stream()
                .filter(this::validateWrapper)
                .map(policyWrapper(policyOutputEnricher::enrich))
                .filter(Objects::nonNull)
                .filter(this::validateWrapper)
                .map(this::enrichedPolicyWrapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        LOGGER.info("Calculated {} Profile Changes ", profileChanges.size());

        final List<Pair<ChangeElement, ChangeElement>> changeElements = profileChanges.stream()
                .map(profileWrapper(profileChangesEnricher::enrich))
                .filter(Objects::nonNull)
                .filter(profileChangesValidator::validate)
                .sorted(this::getSourceUsersToMoveComparator)
                .map(enrichedProfileWrapper(changeElementCalculator::calculateChangeElement))
                .filter(Objects::nonNull)
                .map(pair(reversionElementCalculator::calculateReversionElement))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        LOGGER.info("Calculated total of {} ChangeElements", changeElements.size());
        return changeElements;
    }

    private Collection<TopologyObjectId> collectCellIds(final Collection<PolicyOutputEvent> policyOutputEvents) {
        return policyOutputEvents.stream()
                .flatMap(policyOutputEvent -> {
                    final Collection<TopologyObjectId> cellIds = new ArrayList<>();
                    final ProposedLoadBalancingQuanta lbq = policyOutputEvent.getLoadBalancingQuanta();
                    cellIds.add(TopologyObjectId.of(lbq.getSourceCellFdn(), lbq.getSourceCellOssId()));
                    cellIds.addAll(lbq.getTargetCells().stream()
                            .map(targetCell -> TopologyObjectId.of(targetCell.getTargetCellFdn(), targetCell.getTargetCellOssId()))
                            .collect(Collectors.toList()));
                    return cellIds.stream();
                })
                .collect(Collectors.toList());
    }

    private Collection<Long> collectSectorIds(final List<PolicyOutputEvent> policyOutputEvents) {
        return policyOutputEvents.stream()
                .map(PolicyOutputEvent::getSectorId)
                .collect(Collectors.toList());
    }

    /**
     * This method is used in tests only.
     * 
     * @param policyOutputEnricher
     *            a {@link PolicyOutputEventEnricher} object
     */
    void setPolicyOutputEnricher(final PolicyOutputEventEnricher policyOutputEnricher) {
        this.policyOutputEnricher = policyOutputEnricher;
    }

    /**
     * This method is used in tests only
     * 
     * @param profileChangesEnricher
     *            a {@link ProfileChangesEnricher} object
     */
    void setProfileChangesEnricher(final ProfileChangesEnricher profileChangesEnricher) {
        this.profileChangesEnricher = profileChangesEnricher;
    }

    /**
     * This method is used in tests only
     * 
     * @param enrichedPolicyOutputEventValidator
     *            a {@link EnrichedPolicyOutputEventValidator} object
     */
    void setEnrichedPolicyOutputEventValidator(final EnrichedPolicyOutputEventValidator enrichedPolicyOutputEventValidator) {
        this.enrichedPolicyOutputEventValidator = enrichedPolicyOutputEventValidator;
    }

    /**
     * This method is used in tests only
     * 
     * @param profileChangesValidator
     *            a {@link ProfileChangesValidator} object
     */
    void setProfileChangesValidator(final ProfileChangesValidator profileChangesValidator) {
        this.profileChangesValidator = profileChangesValidator;

    }

    private boolean validateWrapper(final PolicyOutputEvent policyOutputEvent) {
        try {
            policyOutputEventValidator.isValid(policyOutputEvent);
            return true;
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            logFilteredSector(policyOutputEvent.getExecutionId(), String.valueOf(policyOutputEvent.getSectorId()),
                    UNEXPECTED_ERROR_MESSAGE + e.getMessage());
            LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
            return false;
        }
    }

    private boolean validateWrapper(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        try {
            return enrichedPolicyOutputEventValidator.isValid(enrichedPolicyOutputEvent);
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    UNEXPECTED_ERROR_MESSAGE + e.getMessage());
            LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
            return false;
        }
    }

    private <T extends PolicyOutputEvent, R, E extends Exception> Function<T, R> policyWrapper(
            final FunctionWithException<T, R, E> functionWithException) {
        return arg -> {
            try {
                return functionWithException.apply(arg);
            } catch (final Exception e) { //NOSONAR Exception suitably logged
                logFilteredSector(arg.getExecutionId(), String.valueOf(arg.getSectorId()), UNEXPECTED_ERROR_MESSAGE + e.getMessage());
                LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
                return null;
            }
        };
    }

    private <T extends ProfileChanges, R, E extends Exception> Function<T, R> profileWrapper(
            final FunctionWithException<T, R, E> functionWithException) {
        return arg -> {
            try {
                return functionWithException.apply(arg);
            } catch (final Exception e) { //NOSONAR Exception suitably logged
                logFilteredSector(arg.getExecutionId(), String.valueOf(arg.getSectorId()), UNEXPECTED_ERROR_MESSAGE + e.getMessage());
                LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
                return null;
            }
        };
    }

    private <T extends EnrichedProfileChanges, R, E extends Exception> Function<T, R> enrichedProfileWrapper(
            final FunctionWithException<T, R, E> functionWithException) {
        return arg -> {
            try {
                return functionWithException.apply(arg);
            } catch (final Exception e) { //NOSONAR Exception suitably logged
                final ProfileChanges profileChanges = arg.getProfileChanges();
                logFilteredSector(profileChanges.getExecutionId(), String.valueOf(profileChanges.getSectorId()),
                        UNEXPECTED_ERROR_MESSAGE + e.getMessage());
                LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
                return null;
            }
        };
    }

    private ProfileChanges enrichedPolicyWrapper(final EnrichedPolicyOutputEvent en) {
        try {
            return profileChangeCalculator.calculateProfileChanges(en);
        } catch (final LbdarException e) { //NOSONAR Exception suitably logged
            return null;
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            final PolicyOutputEvent policyOutputEvent = en.getPolicyOutputEvent();
            logFilteredSector(policyOutputEvent.getExecutionId(), String.valueOf(policyOutputEvent.getSectorId()),
                    UNEXPECTED_ERROR_MESSAGE + e.getMessage());
            LOGGER.warn(String.format(EXCEPTION_SLOGAN, e), e);
            return null;
        }
    }

    private <T, R, E extends Exception> Function<T, Pair<T, R>> pair(final FunctionWithException<T, R, E> functionWithException) {
        return arg -> {
            try {
                final R r = functionWithException.apply(arg);
                return ImmutablePair.of(arg, r);
            } catch (final Exception e) {
                LOGGER.warn("Failed to create reversion for {}", arg, e);
                return null;
            }
        };
    }

    /**
     * this method is used in tests only
     *
     * @param changeElementCalculator
     *            a {@link ChangeElementCalculator} object
     */
    void setChangeElementCalculator(final ChangeElementCalculator changeElementCalculator) {
        this.changeElementCalculator = changeElementCalculator;
    }

    /**
     * this method is used in tests only
     *
     * @param reversionElementCalculator
     *            a {@link ReversionElementCalculator} object
     */
    void setReversionElementCalculator(final ReversionElementCalculator reversionElementCalculator) {
        this.reversionElementCalculator = reversionElementCalculator;
    }

    /**
     * This method is used in test only
     *
     * @param profileChangeCalculator
     *            an instance of {@link ProfileChangesCalculator}
     */
    void setProfileChangeCalculator(final ProfileChangesCalculator profileChangeCalculator) {
        this.profileChangeCalculator = profileChangeCalculator;
    }

    /**
     * Comparator for sorting {@link EnrichedProfileChanges} by source users to move in a decreasing order
     * 
     * @param profileChanges1
     *            first {@link EnrichedProfileChanges} object to compare
     * @param profileChanges2
     *            second {@link EnrichedProfileChanges} object to compare
     * @return result of comparison
     */
    private int getSourceUsersToMoveComparator(final EnrichedProfileChanges profileChanges1,
            final EnrichedProfileChanges profileChanges2) {
        return profileChanges2.getProfileChanges().getSourceUsersMove().compareTo(profileChanges1.getProfileChanges().getSourceUsersMove());
    }
}