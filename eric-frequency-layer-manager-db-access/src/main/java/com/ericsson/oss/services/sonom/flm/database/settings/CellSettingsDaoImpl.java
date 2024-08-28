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
package com.ericsson.oss.services.sonom.flm.database.settings;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getAllColumnNames;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getInsertStatementString;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getPolicyInputSettingsColumns;
import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getUpdateSetStatementString;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellFlmSettingsHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.SettingsHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.database.utils.DatabaseRetry;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CellSettings;

import io.vavr.CheckedFunction0;

/**
 * Class to implement methods of {@link CellSettingsDao}.
 */
public class CellSettingsDaoImpl implements CellSettingsDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingsDaoImpl.class);
    private static final String ERROR_MESSAGE_FAILED_TO_EXECUTE_INSERT = "Failed to execute insert : {}";
    private static final String CELL_CONFIGURATION = "cell_configuration";
    private static final PreparedStatementHandler NO_PARAMETERS_PREPARED_STATEMENT_HANDLER = new PreparedStatementCreator();

    private final DatabaseRetry databaseRetry;
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    public CellSettingsDaoImpl(final int maxRetryAttempts, final int retryWaitDuration) {
        databaseRetry = new DatabaseRetry(maxRetryAttempts, retryWaitDuration);
    }

    @Override
    public void insertCellSettings(final List<CellSettings> allCellSettings) throws SQLException {
        final CheckedFunction0<Integer> executionsInStateFunctionWithRetry = () -> performInsert(allCellSettings);
        databaseRetry.executeWithRetryAttempts(executionsInStateFunctionWithRetry);
    }

    private int performInsert(final List<CellSettings> allCellSettings) throws SQLException {
        final List<Object[]> parametersList = allCellSettings.stream()
                .map(extractField(CellSettings::getId,
                        CellSettings::getOssId,
                        CellSettings::getFdn,
                        CellSettings::getExecutionId,
                        CellSettings::getConfigurationId,
                        CellSettings::getQosForCapacityEstimation,
                        CellSettings::getPercentileForMaxConnectedUser,
                        CellSettings::getMinNumCellForCdfCalculation,
                        CellSettings::getTargetThroughputR,
                        CellSettings::getDeltaGfsOptimizationThreshold,
                        CellSettings::getTargetSourceCoverageBalanceRatioThreshold,
                        CellSettings::getSourceTargetSamplesOverlapThreshold,
                        CellSettings::getTargetSourceContiguityRatioThreshold,
                        CellSettings::getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRate,
                        CellSettings::getLoadBalancingThresholdForInitialAndAddedErabEstabSuccRateForQci1,
                        CellSettings::getLoadBalancingThresholdForErabPercentageLost,
                        CellSettings::getLoadBalancingThresholdForErabPercentageLostForQci1,
                        CellSettings::getLoadBalancingThresholdForCellHoSuccRate,
                        CellSettings::getLoadBalancingThresholdForCellAvailability,
                        CellSettings::getOptimizationSpeed,
                        CellSettings::getOptimizationSpeedFactorTable,
                        CellSettings::getBandwidthToStepSizeTable,
                        CellSettings::getloadBalancingThresholdForEndcUsers,
                        CellSettings::getNumCallsCellHourlyReliabilityThresholdInHours,
                        CellSettings::getSyntheticCountersCellReliabilityThresholdInRops,
                        CellSettings::getEssEnabled,
                        CellSettings::getMinNumSamplesForTransientCalculation,
                        CellSettings::getSigmaForTransientCalculation,
                        CellSettings::getUlPuschSinrRatioThreshold,
                        CellSettings::getMinTargetUlPuschSinr,
                        CellSettings::getPercentageBadRsrpRatioThreshold,
                        CellSettings::getMinConnectedUsers,
                        CellSettings::getExclusionList,
                        CellSettings::getMinimumSourceRetained,
                        CellSettings::getMinRopsForAppCovReliability,
                        CellSettings::getMinNumCqiSamples,
                        CellSettings::getSectorId))
                .collect(Collectors.toList());
        final String query = String.format("insert into %s (%s) values (%s) on conflict (id, execution_id) do update set %s;",
                CELL_CONFIGURATION, getAllColumnNames(), getInsertStatementString(), getUpdateSetStatementString());

        try {
            return databaseAccess.executeBatchInsert(query, parametersList);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(ERROR_MESSAGE_FAILED_TO_EXECUTE_INSERT, query, e);
            throw e;
        }
    }

    @SafeVarargs
    private static Function<CellSettings, Object[]> extractField(final Function<CellSettings, Object>... extractors) {
        return extractFrom -> Stream.of(extractors)
                .map(extractor -> extractor.apply(extractFrom))
                .toArray();
    }

    @Override
    public Map<Map<String, Integer>, Map<String, String>> getSettingsForCellPerFlmExecution(final String executionId) throws SQLException {
        final List<String> settings = new ArrayList<>(Arrays.asList(getPolicyInputSettingsColumns().split(",")));
        settings.add(EXCLUSION_LIST);
        final String settingsToSelect = StringUtils.join(settings, ", ");
        final String dbQuery = String.format("SELECT fdn, oss_id, execution_id, %s FROM %s WHERE execution_id = '%s'",
                settingsToSelect, CELL_CONFIGURATION, executionId);
        final CheckedFunction0<Map<Map<String, Integer>, Map<String, String>>> settingsForCells = () -> databaseAccess.executeQuery(dbQuery,
                new CellFlmSettingsHandler(settings),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(settingsForCells);
    }

    @Override
    public String retrieveNamedSetting(final String settingsName, final int ossId, final String fdn,
            final String executionId) throws SQLException {
        final String dbQuery = String.format("SELECT %s FROM %s " +
                "WHERE execution_id = '%s' " +
                "AND oss_id = %s " +
                "AND fdn = '%s'",
                settingsName, CELL_CONFIGURATION, executionId, ossId, fdn);
        final CheckedFunction0<String> settingsForCell = () -> databaseAccess.executeQuery(dbQuery, new SettingsHandler(settingsName),
                NO_PARAMETERS_PREPARED_STATEMENT_HANDLER);
        return databaseRetry.executeWithRetryAttempts(settingsForCell);
    }
}
