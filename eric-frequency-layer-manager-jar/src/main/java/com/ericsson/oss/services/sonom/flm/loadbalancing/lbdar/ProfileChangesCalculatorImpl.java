/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter.logFilteredSector;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDaoImpl;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.Balancer;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.DistributionChangeSaturator;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.LeakageDetector;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.MissingPushbackHandler;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.MissingSourcePushHandler;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculationHelper;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util.ProfileChangeCalculatorSettings;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.settings.CellFlmSettingsRetriever;

/**
 * Implementation of ProfileChangeCalculator. ProfileChangeCalculator returns a list of desired idleModePrioAtRelease ({@link ProfileChanges}) for the
 * given {@link EnrichedProfileChanges} object. The list can contain lbdar profiles for the source cell and for some target cells if needed
 */
public class ProfileChangesCalculatorImpl implements ProfileChangesCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileChangesCalculatorImpl.class);
    private static final String MINIMUM_SOURCE_RETAINED_NAME = "minimum_source_retained";
    private static final int LBDAR_TABLE_ACCESS_MAX_RETRY_ATTEMPTS = 10;
    private static final int LBDAR_TABLE_ACCESS_WAIT_PERIOD_IN_SECONDS = 30;
    private static CellFlmSettingsRetriever cellFlmSettingsRetriever = new CellFlmSettingsRetriever();

    private final LbdarDao lbdarDao;
    private final ProfileChangeCalculatorSettings configs;
    private final CmNodeObjectsStore profileStore;

    public ProfileChangesCalculatorImpl(final Map<String, String> customizedGlobalSettings,
            final CmNodeObjectsStore profileStore) {
        this.profileStore = profileStore;
        configs = new ProfileChangeCalculatorSettings(customizedGlobalSettings);
        lbdarDao = new LbdarDaoImpl(LBDAR_TABLE_ACCESS_MAX_RETRY_ATTEMPTS, LBDAR_TABLE_ACCESS_WAIT_PERIOD_IN_SECONDS);
    }

    // used only for test
    ProfileChangesCalculatorImpl(final Map<String, String> customizedGlobalSettings,
            final CmNodeObjectsStore profileStore, final LbdarDao lbdarDao) {
        this.profileStore = profileStore;
        configs = new ProfileChangeCalculatorSettings(customizedGlobalSettings);
        this.lbdarDao = lbdarDao;
    }

    public static void setCellFlmSettingsRetriever(final CellFlmSettingsRetriever cellFlmSettingsRetriever) { //required for mockito
        ProfileChangesCalculatorImpl.cellFlmSettingsRetriever = cellFlmSettingsRetriever;
    }

    @Override
    public ProfileChanges calculateProfileChanges(final EnrichedPolicyOutputEvent changesCalculationInput)
            throws LbdarException, LbdarUnexpectedException, SQLException {
        final String executionId = changesCalculationInput.getPolicyOutputEvent().getExecutionId();
        final Long sectorId = changesCalculationInput.getPolicyOutputEvent().getSectorId();
        final String sourceCellFdn = changesCalculationInput.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellFdn();
        final int sourceCellOssId = changesCalculationInput.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceCellOssId();
        final String minimumSourceRetainedFromDb = cellFlmSettingsRetriever.retrieveGivenCellSettingValue(MINIMUM_SOURCE_RETAINED_NAME,
                sourceCellOssId, sourceCellFdn, executionId);

        if (Objects.isNull(minimumSourceRetainedFromDb)) {
            final String errorMessage = String.format("Minimum source retained setting was null for given OSS ID: %s FDN : %s", sourceCellOssId,
                    sourceCellFdn);
            logFilteredSector(executionId, String.valueOf(sectorId), errorMessage);
            throw new LbdarException(errorMessage);
        }
        final int minimumSourceRetained = Integer.parseInt(minimumSourceRetainedFromDb);
        final String sourceUsersMove = changesCalculationInput.getPolicyOutputEvent().getLoadBalancingQuanta().getSourceUsersMove();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId, "Calculation started"));
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId,
                    String.format("Source Cell FDN: '%s', OSS_ID: '%d', Source Users Move: '%s'", sourceCellFdn, sourceCellOssId, sourceUsersMove)));
            for (final TargetCell targetCell : changesCalculationInput.getTargetCells()) {
                LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId,
                        String.format("Target Cell FDN: '%s', OSS_ID: '%d', Target Users Move: '%s'", targetCell.getTargetCellFdn(),
                                targetCell.getTargetCellOssId(), targetCell.getTargetUsersMove())));
            }
        }
        final ProfileChangeCalculationHelper helper = new ProfileChangeCalculationHelper(changesCalculationInput);

        //setting source push to 0% if missing
        final MissingSourcePushHandler sourcePushHandler = new MissingSourcePushHandler(helper);
        sourcePushHandler.replaceMissingSourcePushWithDefault();

        //setting pushback values to Target cells when it is missing
        final MissingPushbackHandler pushbackHandler = new MissingPushbackHandler(changesCalculationInput, configs, helper);
        Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> modifiedProfiles = pushbackHandler.replaceMissingPushbackWithDefault();
        //adjust distribution increase at source cell due to the pushback replacements
        if (!modifiedProfiles.isEmpty()) {
            modifiedProfiles = pushbackHandler.adjustDistributionAtSourceCell(modifiedProfiles);
        }

        //Basic level calculation, Source -> Target
        final Balancer balancer = new Balancer(changesCalculationInput, configs, helper);
        modifiedProfiles = balancer.solveEquilibrium(modifiedProfiles, minimumSourceRetained);

        //Leakage Detection (at each target cells)
        final LeakageDetector leakageDetector = new LeakageDetector(changesCalculationInput, configs, helper, profileStore, lbdarDao);
        for (final TopologyObjectId targetCellId : helper.getTargetCellsWithIncreasedUsers(modifiedProfiles)) {
            modifiedProfiles = leakageDetector.detectLeakage(modifiedProfiles, targetCellId);
        }

        //min and max lbdar StepSize validation
        final DistributionChangeSaturator saturator = new DistributionChangeSaturator(changesCalculationInput, configs, helper);
        for (final Map.Entry<TopologyObjectId, EnrichedIdleModePrioAtRelease> entry : modifiedProfiles.entrySet()) {
            saturator.saturateStepSize(entry).ifPresent(entry::setValue);
        }

        if (modifiedProfiles.isEmpty()) {
            logFilteredSector(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(changesCalculationInput.getPolicyOutputEvent().getSectorId()),
                    "No ProfileChanges created.");
            return null;
        }

        validateMinSourceRetention(helper, changesCalculationInput, modifiedProfiles, minimumSourceRetained);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, sectorId,
                    String.format("End of calculation, %d ProfileChanges results are prepared", modifiedProfiles.size())));
        }
        final Map<TopologyObjectId, Cell> cellCmData = filterCellCmData(changesCalculationInput.getCellCmData(), modifiedProfiles);

        return new ProfileChanges(changesCalculationInput.getPolicyOutputEvent().getExecutionId(),
                changesCalculationInput.getPolicyOutputEvent().getSectorId(),
                Integer.parseInt(changesCalculationInput
                        .getPolicyOutputEvent().getLoadBalancingQuanta().getSourceUsersMove()),
                convertToIdleModePrioAtRelease(modifiedProfiles), cellCmData);
    }

    private Map<TopologyObjectId, IdleModePrioAtRelease> convertToIdleModePrioAtRelease(
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> enrichedProfiles) {
        return enrichedProfiles.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getIdleModePrioAtReleaseCopy()));
    }

    private Map<TopologyObjectId, Cell> filterCellCmData(final Map<TopologyObjectId, Cell> cellCmData,
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> modifiedTargetProfiles) {
        return cellCmData
                .entrySet()
                .stream()
                .filter(entry -> modifiedTargetProfiles.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void validateMinSourceRetention(final ProfileChangeCalculationHelper helper,
            final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent,
            final Map<TopologyObjectId, EnrichedIdleModePrioAtRelease> modifiedProfiles, final int minimumSourceRetained) throws LbdarException {
        final EnrichedIdleModePrioAtRelease sourceProfile = modifiedProfiles.get(helper.getSourceCellId());
        final EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo distributionInfo = sourceProfile.getDistributionInfos()
                .get(helper.getSelectedThreshold(helper.getSourceCellId()));
        if (distributionInfo.getDistributionOfFrequency(helper.getSourceCellCarrier()) < minimumSourceRetained) {
            final String message = String.format("Retained level of users at Source cell is too low, %s threshold is breached.",
                    minimumSourceRetained);
            logFilteredSector(enrichedPolicyOutputEvent.getPolicyOutputEvent().getExecutionId(),
                    String.valueOf(enrichedPolicyOutputEvent.getPolicyOutputEvent().getSectorId()),
                    message);
            throw new LbdarException(message);
        }
    }
}