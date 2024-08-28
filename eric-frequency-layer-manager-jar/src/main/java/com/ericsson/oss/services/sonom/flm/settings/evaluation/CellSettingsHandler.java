/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.settings.evaluation;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.ENABLE_ESS_SETTING_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MINIMUM_SOURCE_RETAINED_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_CONNECTED_USERS_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_CQI_SAMPLES_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.OPTIMIZATION_SPEED_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.TARGET_THROUGHPUT_R_ATTR_NAME;
import static com.ericsson.oss.services.sonom.flm.settings.CellSettingsAttributeNames.UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDao;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDaoImpl;
import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDao;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handler class which gets Cell configuration settings from cm topology service, applies settings to cells.
 */
public class CellSettingsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsHandler.class);
    private static final int BATCH_SIZE = 500;
    private static final String OSF_TABLE = "OSF_TABLE";
    private static final String BW_STEP_SIZE_TABLE = "BW_STEP_SIZE_TABLE";
    private static final String NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS_ENV = "NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS";
    private static final String SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS = "SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS";
    private static final String COMMA = ",";

    private final CmCellGroupRetriever cmCellGroupRetriever;
    private final CmStore cmStore;
    private final Execution execution;
    private final ConfigurationDao configurationDao;

    //Constructor for unit tests
    public CellSettingsHandler(final CmStore cmStore, final Execution execution, final CmCellGroupRetriever cmCellGroupRetriever,
            final ConfigurationDao configurationDao) {
        this.execution = execution;
        this.cmCellGroupRetriever = cmCellGroupRetriever;
        this.cmStore = cmStore;
        this.configurationDao = configurationDao;
    }

    public CellSettingsHandler(final CmStore cmStore, final Execution execution) {
        this(cmStore, execution, new CmCellGroupRetriever(), new ConfigurationDaoImpl());
    }

    /**
     * Retrieve Cells from cm topology service, applies settings to cells and persist in FLM database.
     *
     * @param cellSettingsDao
     *            Used to perform database insert.
     * @param executionId
     *            the id of the execution evaluating the cells
     * @throws SQLException
     *             if persistence to DB fails.
     * @throws ExecutionException
     *             if the computation threw an exception.
     * @throws InterruptedException
     *             if the current thread is interrupted.
     * @throws FlmAlgorithmException
     *             if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public void evaluateCellSettings(final CellSettingsDao cellSettingsDao,
            final String executionId) throws SQLException, InterruptedException, ExecutionException, FlmAlgorithmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(LoggingFormatter.formatMessage(executionId, "Retrieving list of cells from CM Topology and assign settings"));
        }
        final Configuration configuration = configurationDao.get(execution.getConfigurationId());
        final Map<String, String> customizedGlobalSettings = configuration.getCustomizedGlobalSettings();
        final Map<String, String> customizedDefaultSettings = configuration.getCustomizedDefaultSettings();

        final CellSettings defaultCellSettings = createDefaultCellSettings(customizedGlobalSettings, customizedDefaultSettings);

        final Collection<TopologySector> sectorList = cmStore.getCmSectorCellStore().getSectorsWithInclusionListCells();
        final Map<String, CellSettings> allCellSettings = getDefaultConfigurationSetting(sectorList, defaultCellSettings);

        final List<CustomizedGroup> groups = configuration.getGroups();
        evaluateConfigurationGroups(allCellSettings, groups, defaultCellSettings, executionId);

        final List<Group> exclusionList = configuration.getExclusionList();
        evaluateExclusionListGroups(exclusionList, executionId, allCellSettings);

        persistCellSettingsToDB(cellSettingsDao, allCellSettings);
    }

    private CellSettings createDefaultCellSettings(final Map<String, String> customizedGlobalSettings,
            final Map<String, String> customizedDefaultSettings) {
        final CellSettings defaultCellSettings = new CellSettings();
        defaultCellSettings.setQosForCapacityEstimation(Double.parseDouble(getCustomizedSettingValue(customizedGlobalSettings,
                DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, QOS_FOR_CAPACITY_ESTIMATION_ATTR_NAME)));
        defaultCellSettings
                .setPercentileForMaxConnectedUser(roundNumberToPrecisionOne(Double.parseDouble(getCustomizedSettingValue(customizedGlobalSettings,
                        DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, PERCENTILE_FOR_MAX_CONNECTED_USER_ATTR_NAME))));
        defaultCellSettings.setMinNumCellForCdfCalculation(Integer.parseInt(getCustomizedSettingValue(customizedGlobalSettings,
                DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, MIN_NUM_CELL_FOR_CDF_CALCULATION_ATTR_NAME)));
        defaultCellSettings.setTargetThroughputR(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, TARGET_THROUGHPUT_R_ATTR_NAME)));
        defaultCellSettings.setDeltaGfsOptimizationThreshold(getRoundedCustomizedDefaultSettingValue(customizedDefaultSettings,
                DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME));
        defaultCellSettings.setTargetSourceCoverageBalanceRatioThreshold(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME)));
        defaultCellSettings.setSourceTargetSamplesOverlapThreshold(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME)));
        defaultCellSettings.setTargetSourceContiguityRatioThreshold(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME)));
        defaultCellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(Double.parseDouble(getCustomizedSettingValue(
                customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME)));
        defaultCellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
                Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                        DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME)));
        defaultCellSettings.setLoadBalancingThresholdForErabPercentageLost(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME)));
        defaultCellSettings
                .setLoadBalancingThresholdForErabPercentageLostForQci1(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                        DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME)));
        defaultCellSettings.setLoadBalancingThresholdForCellHoSuccRate(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME)));
        defaultCellSettings.setLoadBalancingThresholdForCellAvailability(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME)));
        defaultCellSettings.setOptimizationSpeed(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, OPTIMIZATION_SPEED_ATTR_NAME));
        defaultCellSettings.setMinimumSourceRetained(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MINIMUM_SOURCE_RETAINED_NAME)));
        defaultCellSettings.setMinRopsForAppCovReliability(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME)));
        defaultCellSettings.setMinNumCqiSamples(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MIN_NUM_CQI_SAMPLES_ATTR_NAME)));
        defaultCellSettings.setMinNumSamplesForTransientCalculation(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME)));
        defaultCellSettings.setSigmaForTransientCalculation(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME)));
        defaultCellSettings.setUlPuschSinrRatioThreshold(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME)));
        defaultCellSettings.setMinTargetUlPuschSinr(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME)));
        defaultCellSettings.setPercentageBadRsrpRatioThreshold(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME)));
        defaultCellSettings.setMinConnectedUsers(Integer.parseInt(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, MIN_CONNECTED_USERS_ATTR_NAME)));
        defaultCellSettings.setloadBalancingThresholdForEndcUsers(Double.parseDouble(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME)));
        defaultCellSettings.setEssEnabled(Boolean.parseBoolean(getCustomizedSettingValue(customizedDefaultSettings,
                DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS,
                ENABLE_ESS_SETTING_ATTR_NAME)));
        return defaultCellSettings;
    }

    private void persistCellSettingsToDB(final CellSettingsDao cellSettingsDao, final Map<String, CellSettings> allCellSettings) throws SQLException {
        final List<CellSettings> cellSettings = new ArrayList<>(allCellSettings.values());
        for (final List<CellSettings> cellSettingsBatch : Lists.partition(cellSettings, BATCH_SIZE)) {
            cellSettingsDao.insertCellSettings(cellSettingsBatch);
        }
    }

    private Map<String, CellSettings> getDefaultConfigurationSetting(final Collection<TopologySector> sectorList,
            final CellSettings defaultCellSettings) {
        final Map<String, CellSettings> allCellSettings = new HashMap<>();
        for (final TopologySector sector : sectorList) {
            final Collection<Cell> cellList = sector.getAssociatedCells();
            for (final Cell cell : cellList) {
                final CellSettings cellSettings = new CellSettings();
                cellSettings.setId(cell.getCellId());
                cellSettings.setOssId(cell.getOssId());
                cellSettings.setFdn(cell.getFdn());
                cellSettings.setExecutionId(execution.getId());
                cellSettings.setConfigurationId(execution.getConfigurationId());
                cellSettings.setQosForCapacityEstimation(defaultCellSettings.getQosForCapacityEstimation());
                cellSettings.setPercentileForMaxConnectedUser(defaultCellSettings.getPercentileForMaxConnectedUser());
                cellSettings.setMinNumCellForCdfCalculation(defaultCellSettings.getMinNumCellForCdfCalculation());
                cellSettings.setTargetThroughputR(defaultCellSettings.getTargetThroughputR());
                cellSettings.setDeltaGfsOptimizationThreshold(defaultCellSettings.getDeltaGfsOptimizationThreshold());
                cellSettings.setTargetSourceCoverageBalanceRatioThreshold(defaultCellSettings.getTargetSourceCoverageBalanceRatioThreshold());
                cellSettings.setSourceTargetSamplesOverlapThreshold(defaultCellSettings.getSourceTargetSamplesOverlapThreshold());
                cellSettings.setTargetSourceContiguityRatioThreshold(defaultCellSettings.getTargetSourceContiguityRatioThreshold());
                cellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(
                        defaultCellSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate());
                cellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
                        defaultCellSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1());
                cellSettings.setLoadBalancingThresholdForErabPercentageLost(defaultCellSettings.getLoadBalancingThresholdForErabPercentageLost());
                cellSettings.setLoadBalancingThresholdForErabPercentageLostForQci1(
                        defaultCellSettings.getLoadBalancingThresholdForErabPercentageLostForQci1());
                cellSettings.setLoadBalancingThresholdForCellHoSuccRate(defaultCellSettings.getLoadBalancingThresholdForCellHoSuccRate());
                cellSettings.setLoadBalancingThresholdForCellAvailability(defaultCellSettings.getLoadBalancingThresholdForCellAvailability());
                cellSettings.setOptimizationSpeed(defaultCellSettings.getOptimizationSpeed());
                cellSettings.setMinimumSourceRetained(defaultCellSettings.getMinimumSourceRetained());
                cellSettings.setMinRopsForAppCovReliability(defaultCellSettings.getMinRopsForAppCovReliability());
                cellSettings.setMinNumCqiSamples(defaultCellSettings.getMinNumCqiSamples());
                cellSettings.setMinNumSamplesForTransientCalculation(defaultCellSettings.getMinNumSamplesForTransientCalculation());
                cellSettings.setSigmaForTransientCalculation(defaultCellSettings.getSigmaForTransientCalculation());
                cellSettings.setUlPuschSinrRatioThreshold(defaultCellSettings.getUlPuschSinrRatioThreshold());
                cellSettings.setMinTargetUlPuschSinr(defaultCellSettings.getMinTargetUlPuschSinr());
                cellSettings.setPercentageBadRsrpRatioThreshold(defaultCellSettings.getPercentageBadRsrpRatioThreshold());
                cellSettings.setloadBalancingThresholdForEndcUsers(defaultCellSettings.getloadBalancingThresholdForEndcUsers());
                cellSettings.setMinConnectedUsers(defaultCellSettings.getMinConnectedUsers());
                cellSettings.setEssEnabled(defaultCellSettings.getEssEnabled());
                cellSettings.setNumCallsCellHourlyReliabilityThresholdInHours(
                        Integer.parseInt(getEnvironmentValue(NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS_ENV, "20")));
                cellSettings.setSyntheticCountersCellReliabilityThresholdInRops(
                        Integer.parseInt(getEnvironmentValue(SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS, "72")));
                cellSettings.setBandwidthToStepSizeTable(getEnvironmentValue(BW_STEP_SIZE_TABLE));
                cellSettings.setOptimizationSpeedFactorTable(getEnvironmentValue(OSF_TABLE));
                cellSettings.setSectorId(sector.getSectorId());
                final String key = getCellKey(cell);
                allCellSettings.put(key, cellSettings);
            }
        }
        return allCellSettings;
    }

    private String getCellKey(final Cell cell) {
        return cell.getOssId() + "-" + cell.getFdn();
    }

    private void evaluateConfigurationGroups(final Map<String, CellSettings> allCellSettings, final List<CustomizedGroup> groups,
            final CellSettings defaultCellSettings, final String executionId) throws InterruptedException, ExecutionException {
        for (final CustomizedGroup group : groups) {
            final String groupName = group.getName();
            final Future<List<Cell>> cellListFutureGroupEvaluation = cmCellGroupRetriever.retrieveGroupEvaluation(groupName, executionId);
            final CellSettings groupSettings = mergeDefaultAndGroupSettings(group.getCustomizedGroupSettings(), defaultCellSettings);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Evaluating group '%s' containing '%d' objects",
                                groupName, cellListFutureGroupEvaluation.get().size())));
            }
            if (cellListFutureGroupEvaluation.get().isEmpty()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                            String.format("No cells exist in group '%s'", groupName)));
                }
            } else {
                for (final Cell cell : cellListFutureGroupEvaluation.get()) {
                    applyToCellSettings(allCellSettings, groupName, groupSettings, cell);
                }
            }

        }
    }

    private void applyToCellSettings(final Map<String, CellSettings> allCellSettings, final String groupName,
            final CellSettings groupSettings, final Cell cell) {
        final String key = getCellKey(cell);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(LoggingFormatter.formatMessage(execution.getId(),
                    String.format("Applying setting to oss id-fdn '%s'", key)));
        }
        final CellSettings cellSettings = allCellSettings.get(key);
        if (cellSettings == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(LoggingFormatter.formatMessage(execution.getId(),
                        String.format("Cell oss id-fdn '%s' in group name '%s' is not in the list of cells selected for optimization",
                                key, groupName)));
            }
        } else {
            cellSettings.setTargetThroughputR(groupSettings.getTargetThroughputR());
            cellSettings.setDeltaGfsOptimizationThreshold(groupSettings.getDeltaGfsOptimizationThreshold());
            cellSettings.setTargetSourceCoverageBalanceRatioThreshold(groupSettings.getTargetSourceCoverageBalanceRatioThreshold());
            cellSettings.setSourceTargetSamplesOverlapThreshold(groupSettings.getSourceTargetSamplesOverlapThreshold());
            cellSettings.setTargetSourceContiguityRatioThreshold(groupSettings.getTargetSourceContiguityRatioThreshold());
            cellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(
                    groupSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate());
            cellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
                    groupSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1());
            cellSettings.setLoadBalancingThresholdForErabPercentageLost(groupSettings.getLoadBalancingThresholdForErabPercentageLost());
            cellSettings.setLoadBalancingThresholdForErabPercentageLostForQci1(
                    groupSettings.getLoadBalancingThresholdForErabPercentageLostForQci1());
            cellSettings.setLoadBalancingThresholdForCellHoSuccRate(groupSettings.getLoadBalancingThresholdForCellHoSuccRate());
            cellSettings.setLoadBalancingThresholdForCellAvailability(groupSettings.getLoadBalancingThresholdForCellAvailability());
            cellSettings.setOptimizationSpeed(groupSettings.getOptimizationSpeed());
            cellSettings.setMinimumSourceRetained(groupSettings.getMinimumSourceRetained());
            cellSettings.setMinRopsForAppCovReliability(groupSettings.getMinRopsForAppCovReliability());
            cellSettings.setMinNumCqiSamples(groupSettings.getMinNumCqiSamples());
            cellSettings.setMinNumSamplesForTransientCalculation(groupSettings.getMinNumSamplesForTransientCalculation());
            cellSettings.setSigmaForTransientCalculation(groupSettings.getSigmaForTransientCalculation());
            cellSettings.setUlPuschSinrRatioThreshold(groupSettings.getUlPuschSinrRatioThreshold());
            cellSettings.setMinTargetUlPuschSinr(groupSettings.getMinTargetUlPuschSinr());
            cellSettings.setPercentageBadRsrpRatioThreshold(groupSettings.getPercentageBadRsrpRatioThreshold());
            cellSettings.setMinConnectedUsers(groupSettings.getMinConnectedUsers());
            cellSettings.setloadBalancingThresholdForEndcUsers(groupSettings.getloadBalancingThresholdForEndcUsers());
            cellSettings.setEssEnabled(groupSettings.getEssEnabled());
        }
    }

    //Exposed for testing
    protected void evaluateExclusionListGroups(final List<Group> exclusionList, final String executionId,
            final Map<String, CellSettings> allCellSettings) throws ExecutionException, InterruptedException {
        for (final Group group : exclusionList) {
            final String groupName = group.getName();
            final Future<List<Cell>> cellListFutureGroupEvaluation = cmCellGroupRetriever.retrieveGroupEvaluation(groupName, executionId);
            LOGGER.info("Evaluating exclusionList group '{}' with execution id '{}' containing '{}' objects", groupName, execution.getId(),
                    cellListFutureGroupEvaluation.get().size());
            if (cellListFutureGroupEvaluation.get().isEmpty()) {
                LOGGER.warn("No cells exist in group '{}'", groupName);
            } else {
                for (final Cell cell : cellListFutureGroupEvaluation.get()) {
                    final String key = getCellKey(cell);
                    final CellSettings cellSettings = allCellSettings.get(key);
                    if (cellSettings != null) {
                        cellSettings.setExclusionList(buildExclusionListWithGroupAppended(cellSettings, groupName));
                        LOGGER.debug("Applying exclusionList '{}' to cell with oss id-fdn '{}' with execution id '{}'", groupName, key,
                                execution.getId());
                    }
                }
            }
        }
    }

    //Exposed for testing
    protected static String buildExclusionListWithGroupAppended(final CellSettings cellSettings, final String groupName) {
        final StringBuilder exclusionList = new StringBuilder();
        final String currentExclusionList = cellSettings.getExclusionList();
        if (currentExclusionList != null) {
            exclusionList.append(currentExclusionList);
            exclusionList.append(COMMA);
        }
        exclusionList.append(groupName);
        return exclusionList.toString();
    }

    protected static String getCustomizedSettingValue(final Map<String, String> customSettings, final String customSettingDefaultValues,
            final String customSettingName) {
        final JsonObject jsonDefaultSettings = JsonParser.parseString(customSettingDefaultValues).getAsJsonObject();
        String settingValue = jsonDefaultSettings.get(customSettingName).getAsString();

        if (ObjectUtils.isNotEmpty(customSettings.get(customSettingName))) {
            settingValue = customSettings.get(customSettingName);
        }
        return settingValue;
    }

    protected static double getRoundedCustomizedDefaultSettingValue(final Map<String, String> customizedDefaultSettings,
            final String customDefaultSettingName) {
        final JsonObject jsonDefaultSettings = JsonParser.parseString(DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS).getAsJsonObject();
        double settingValue = roundNumberToPrecisionOne(jsonDefaultSettings.get(customDefaultSettingName).getAsDouble());
        if (ObjectUtils.isNotEmpty(customizedDefaultSettings.get(customDefaultSettingName))) {
            settingValue = roundNumberToPrecisionOne(Double.parseDouble(customizedDefaultSettings.get(customDefaultSettingName)));
        }
        return settingValue;
    }

    private static CellSettings mergeDefaultAndGroupSettings(final Map<String, String> groupCustomizedSettings,
            final CellSettings defaultCellSettings) {
        final CellSettings groupCellSettings = new CellSettings();

        groupCellSettings.setTargetThroughputR(getSettingDoubleValueFromGroup(groupCustomizedSettings, TARGET_THROUGHPUT_R_ATTR_NAME)
                .orElse(defaultCellSettings.getTargetThroughputR()));

        groupCellSettings.setDeltaGfsOptimizationThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, DELTA_GFS_OPTIMIZATION_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getDeltaGfsOptimizationThreshold()));

        groupCellSettings.setTargetSourceCoverageBalanceRatioThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getTargetSourceCoverageBalanceRatioThreshold()));

        groupCellSettings.setSourceTargetSamplesOverlapThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getSourceTargetSamplesOverlapThreshold()));

        groupCellSettings.setTargetSourceContiguityRatioThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getTargetSourceContiguityRatioThreshold()));

        groupCellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate()));

        groupCellSettings.setLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1(
                getSettingDoubleValueFromGroup(groupCustomizedSettings,
                        LOAD_BALANCING_THRESHOLD_FOR_INITIAL_AND_ADDED_ERAB_ESTAB_SUCC_RATE_FOR_QCI1_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1()));

        groupCellSettings.setLoadBalancingThresholdForErabPercentageLost(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForErabPercentageLost()));

        groupCellSettings.setLoadBalancingThresholdForErabPercentageLostForQci1(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForErabPercentageLostForQci1()));

        groupCellSettings.setLoadBalancingThresholdForCellHoSuccRate(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_CELL_HO_SUCC_RATE_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForCellHoSuccRate()));

        groupCellSettings.setLoadBalancingThresholdForCellAvailability(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_CELL_AVAILABILITY_ATTR_NAME)
                        .orElse(defaultCellSettings.getLoadBalancingThresholdForCellAvailability()));

        groupCellSettings.setOptimizationSpeed(
                getSettingStringValueFromGroup(groupCustomizedSettings, OPTIMIZATION_SPEED_ATTR_NAME)
                        .orElse(defaultCellSettings.getOptimizationSpeed()));

        groupCellSettings.setMinimumSourceRetained(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MINIMUM_SOURCE_RETAINED_NAME)
                        .orElse(defaultCellSettings.getMinimumSourceRetained()));

        groupCellSettings.setMinRopsForAppCovReliability(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MIN_ROPS_FOR_APP_COV_RELIABILITY_ATTR_NAME)
                        .orElse(defaultCellSettings.getMinRopsForAppCovReliability()));

        groupCellSettings.setMinNumCqiSamples(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MIN_NUM_CQI_SAMPLES_ATTR_NAME)
                        .orElse(defaultCellSettings.getMinNumCqiSamples()));

        groupCellSettings.setMinNumSamplesForTransientCalculation(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION_ATTR_NAME)
                        .orElse(defaultCellSettings.getMinNumSamplesForTransientCalculation()));

        groupCellSettings.setSigmaForTransientCalculation(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, SIGMA_FOR_TRANSIENT_CALCULATION_ATTR_NAME)
                        .orElse(defaultCellSettings.getSigmaForTransientCalculation()));

        groupCellSettings.setUlPuschSinrRatioThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, UPLINK_PUSCH_SINR_RATIO_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getUlPuschSinrRatioThreshold()));

        groupCellSettings.setMinTargetUlPuschSinr(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MIN_TARGET_UPLINK_PUSCH_SINR_ATTR_NAME)
                        .orElse(defaultCellSettings.getMinTargetUlPuschSinr()));

        groupCellSettings.setPercentageBadRsrpRatioThreshold(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD_ATTR_NAME)
                        .orElse(defaultCellSettings.getPercentageBadRsrpRatioThreshold()));

        groupCellSettings.setMinConnectedUsers(
                getSettingIntegerValueFromGroup(groupCustomizedSettings, MIN_CONNECTED_USERS_ATTR_NAME)
                        .orElse(defaultCellSettings.getMinConnectedUsers()));

        groupCellSettings.setloadBalancingThresholdForEndcUsers(
                getSettingDoubleValueFromGroup(groupCustomizedSettings, LOAD_BALANCING_THRESHOLD_FOR_ENDC_USERS_ATTR_NAME)
                        .orElse(defaultCellSettings.getloadBalancingThresholdForEndcUsers()));

        groupCellSettings.setEssEnabled(
                getSettingBooleanValueFromGroup(groupCustomizedSettings, ENABLE_ESS_SETTING_ATTR_NAME)
                        .orElse(defaultCellSettings.getEssEnabled()));

        return groupCellSettings;
    }

    private static <T> Optional<T> getSettingValueFromGroup(final Map<String, String> settings, final String customSettingName,
            final Function<String, T> mapper) {
        if (settings.containsKey(customSettingName)) {
            final String result = settings.get(customSettingName);
            return Optional.of(mapper.apply(result));
        }
        return Optional.empty();
    }

    protected static Optional<Double> getSettingDoubleValueFromGroup(final Map<String, String> settings, final String customSettingName) {
        return getSettingValueFromGroup(settings, customSettingName, Double::parseDouble);
    }

    protected static Optional<Integer> getSettingIntegerValueFromGroup(final Map<String, String> settings, final String customSettingName) {
        return getSettingValueFromGroup(settings, customSettingName, Integer::parseInt);
    }

    protected static Optional<String> getSettingStringValueFromGroup(final Map<String, String> settings, final String customSettingName) {
        return Optional.ofNullable(settings.get(customSettingName));
    }

    protected static Optional<Boolean> getSettingBooleanValueFromGroup(final Map<String, String> settings, final String customSettingName) {
        return getSettingValueFromGroup(settings, customSettingName, Boolean::parseBoolean);
    }

    protected static Double roundNumberToPrecisionOne(final double number) {
        final DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        final String numberValue = df.format(number);
        return Double.parseDouble(numberValue);
    }
}
