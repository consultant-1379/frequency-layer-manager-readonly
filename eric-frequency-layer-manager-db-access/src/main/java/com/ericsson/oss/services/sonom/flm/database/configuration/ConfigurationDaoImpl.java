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
package com.ericsson.oss.services.sonom.flm.database.configuration;

import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.CONFIGURATION_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.CUSTOMIZED_DEFAULT_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.CUSTOMIZED_GLOBAL_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_ENABLED;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_ENABLE_PA;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_GROUPS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_INCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_OPEN_LOOP;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.DEFAULT_WEEKEND_DAYS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.ENABLED;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.ENABLE_PA;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.GROUPS;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.ID;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.INCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.NAME;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.OPEN_LOOP;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.SCHEDULE;
import static com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDbConstants.WEEKEND_DAYS;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.DatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.ConfigurationInsertHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.ModifySequenceValueHandler;
import com.ericsson.oss.services.sonom.flm.database.handlers.SequenceCurrentValueHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementCreator;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Class to implement methods of {@link ConfigurationDao}.
 */
public class ConfigurationDaoImpl implements ConfigurationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDaoImpl.class);
    private static final String FAILED_TO_EXECUTE_QUERY = "Failed to execute query: {} - {}";
    private static final String FAILED_TO_EXECUTE_DELETE_REQUEST = "Failed to execute delete request: {} - {}";
    private static final String ID_SEQUENCE_NAME = "configuration_settings_id_seq";
    private static final String MAX_LBDAR_STEPSIZE_NAME = "maxLbdarStepsize";
    private static final String PA_KPI_SETTINGS = "paKpiSettings";

    private static final Gson GSON = new Gson();
    private DatabaseAccess databaseAccess = new FlmDatabaseAccess(); // NOPMD cannot be final or mockito can't inject mock for testing

    @Override
    public Configuration create(final Configuration configuration) throws SQLException {
        setupConfigurationDefaultValues(configuration);
        final Object[] parameters = { configuration.getName(), configuration.isEnabled(), configuration.getSchedule(),
                configuration.isOpenLoop(), GSON.toJson(configuration.getCustomizedGlobalSettings()),
                GSON.toJson(configuration.getCustomizedDefaultSettings()), GSON.toJson(configuration.getGroups()),
                GSON.toJson(configuration.getInclusionList()), GSON.toJson(configuration.getExclusionList()),
                configuration.getWeekendDays(), configuration.isEnablePA() };
        final String query = String.format(
                "insert into %s (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) values (?,?,?,?,?,?,?,?,?,?,?) RETURNING id ",
                CONFIGURATION_SETTINGS, NAME, ENABLED, SCHEDULE, OPEN_LOOP, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS,
                INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS, ENABLE_PA);

        return insertConfiguration(configuration, parameters, query);
    }

    @Override
    public Configuration createWithId(final Configuration configuration) throws SQLException {
        setupConfigurationDefaultValues(configuration);
        final Object[] parameters = { configuration.getId(), configuration.getName(), configuration.isEnabled(), configuration.getSchedule(),
                configuration.isOpenLoop(), GSON.toJson(configuration.getCustomizedGlobalSettings()),
                GSON.toJson(configuration.getCustomizedDefaultSettings()), GSON.toJson(configuration.getGroups()),
                GSON.toJson(configuration.getInclusionList()), GSON.toJson(configuration.getExclusionList()), configuration.getWeekendDays(),
                configuration.isEnablePA() };
        final String query = String.format(
                "insert into %s (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) values (?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id ",
                CONFIGURATION_SETTINGS, ID, NAME, ENABLED, SCHEDULE, OPEN_LOOP, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS,
                INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS, ENABLE_PA);
        final Configuration insertedConfiguration = insertConfiguration(configuration, parameters, query);
        alignSequenceIfNeeded(insertedConfiguration);

        return insertedConfiguration;
    }

    @Override
    public int update(final Configuration configuration) throws SQLException {
        setupConfigurationDefaultValues(configuration);
        final Object[] parameters = new Object[] {
                configuration.getName(), configuration.isEnabled(), configuration.getSchedule(), configuration.isOpenLoop(),
                GSON.toJson(configuration.getCustomizedGlobalSettings()), GSON.toJson(configuration.getCustomizedDefaultSettings()),
                GSON.toJson(configuration.getGroups()), GSON.toJson(configuration.getInclusionList()), GSON.toJson(configuration.getExclusionList()),
                configuration.getWeekendDays(), configuration.isEnablePA(), configuration.getId()
        };
        final String query = String.format(
                "update %s set %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ? where %s = ? ",
                CONFIGURATION_SETTINGS, NAME, ENABLED, SCHEDULE, OPEN_LOOP, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS,
                INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS, ENABLE_PA, ID);
        LOGGER.debug("Executing parameterized update, {}", query);
        try {
            return databaseAccess.executeUpdate(query, parameters);
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to execute update statement: {} - {}", query, e.getMessage());
            throw e;
        }
    }

    @Override
    public Configuration get(final Integer configurationId) throws SQLException {
        return getConfigurationByParameter(ID, configurationId);
    }

    @Override
    public Configuration get(final String name) throws SQLException {
        return getConfigurationByParameter(NAME, name);
    }

    @Override
    public List<Configuration> getAll() throws SQLException {
        final String query = String.format("SELECT %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s FROM %s ORDER BY %s ", ID, NAME, ENABLED, SCHEDULE,
                OPEN_LOOP, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS, INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS, ENABLE_PA,
                CONFIGURATION_SETTINGS,
                ID);
        try {
            final List<Configuration> configurations = databaseAccess.executeQuery(query,
                    new ConfigurationHandler());
            LOGGER.debug("Retrieved configurations {}", configurations);
            return configurations;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    @Override
    public boolean delete(final Integer configurationId) throws SQLException {
        final Object[] parameters = new Object[] { configurationId };
        final String deleteQuery = String.format("DELETE FROM %s where %s = ? ", CONFIGURATION_SETTINGS, ID);
        try {
            return databaseAccess.executeUpdate(deleteQuery, parameters) > 0;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(FAILED_TO_EXECUTE_DELETE_REQUEST, deleteQuery, e);
            throw e;
        }
    }

    private void setupConfigurationDefaultValues(final Configuration configuration) {
        configuration.setEnabled(configuration.isEnabled() == null ? DEFAULT_ENABLED : configuration.isEnabled());
        configuration.setOpenLoop(configuration.isOpenLoop() == null ? DEFAULT_OPEN_LOOP : configuration.isOpenLoop());
        configuration.setCustomizedGlobalSettings(configuration.getCustomizedGlobalSettings() == null
                ? GSON.fromJson(DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, new TypeToken<Map<String, String>>() {
                }.getType())
                : configuration.getCustomizedGlobalSettings());
        configuration.setCustomizedDefaultSettings(configuration.getCustomizedDefaultSettings() == null
                ? GSON.fromJson(DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS, new TypeToken<Map<String, String>>() {
                }.getType())
                : configuration.getCustomizedDefaultSettings());
        configuration.setGroups(configuration.getGroups() == null ? GSON.fromJson(DEFAULT_GROUPS, new TypeToken<List<CustomizedGroup>>() {
        }.getType()) : configuration.getGroups());
        configuration.setInclusionList(configuration.getInclusionList() == null ? GSON.fromJson(DEFAULT_INCLUSION_LIST, new TypeToken<List<Group>>() {
        }.getType()) : configuration.getInclusionList());
        configuration.setExclusionList(configuration.getExclusionList() == null ? GSON.fromJson(DEFAULT_EXCLUSION_LIST, new TypeToken<List<Group>>() {
        }.getType()) : configuration.getExclusionList());
        configuration.setWeekendDays(configuration.getWeekendDays() == null ? DEFAULT_WEEKEND_DAYS : configuration.getWeekendDays());
        configuration.setEnablePA(configuration.isEnablePA() == null ? DEFAULT_ENABLE_PA : configuration.isEnablePA());

        fillMissingMapEntries(configuration.getCustomizedGlobalSettings(),
                              GSON.fromJson(DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, new TypeToken<Map<String, String>>() {
                              }.getType()));
        fillMissingMapEntries(configuration.getCustomizedDefaultSettings(), GSON.fromJson(DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS,
                new TypeToken<Map<String, String>>() {
                }.getType()));
        fillMissingMaxLbdarStepsizeEntries(configuration.getCustomizedGlobalSettings(),
                                           GSON.fromJson(DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS, new TypeToken<Map<String, String>>() {
                                           }.getType()));
        fillMissingPaKpiSettings(configuration.getCustomizedGlobalSettings(), GSON.fromJson(DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS,
                new TypeToken<Map<String, String>>() {
        }.getType()));
    }

    private void fillMissingMapEntries(final Map<String, String> destination, final Map<String, String> defaults) {
        defaults.forEach((key, value) -> destination.merge(key, value, (destinationVal, defaultsVal) -> destinationVal));
    }

    private void fillMissingMaxLbdarStepsizeEntries(final Map<String, String> customizedGlobalSettings,
                                                      final Map<String, String> defaultCustomizedGlobalSettings) {
        final JsonArray maxLbdarStepsizeArray = GSON.fromJson(customizedGlobalSettings.get(MAX_LBDAR_STEPSIZE_NAME), JsonArray.class);
        final JsonArray maxLbdarStepsizeDefaultArray = GSON.fromJson(defaultCustomizedGlobalSettings.get(MAX_LBDAR_STEPSIZE_NAME), JsonArray.class);
        for (int i = 0; i < maxLbdarStepsizeDefaultArray.size(); i++) {
            final JsonObject defaultCategory = maxLbdarStepsizeDefaultArray.get(i).getAsJsonObject();
            final int defaultBW = defaultCategory.get("BW").getAsInt();
            if (!doesMaxLbdarStepsizeHaveBW(maxLbdarStepsizeArray, defaultBW)) {
                maxLbdarStepsizeArray.add(defaultCategory);
            }
        }
        customizedGlobalSettings.put(MAX_LBDAR_STEPSIZE_NAME, GSON.toJson(maxLbdarStepsizeArray));
    }

    private void fillMissingPaKpiSettings(final Map<String, String> customizedGlobalSettings,
                                          final Map<String, String> defaultCustomizedGlobalSettings) {
        final JsonObject paKpiSettings = GSON.fromJson(customizedGlobalSettings.get(PA_KPI_SETTINGS), JsonObject.class);
        final JsonObject paKpiDefaultSettings = GSON.fromJson(defaultCustomizedGlobalSettings.get(PA_KPI_SETTINGS), JsonObject.class);
        if (paKpiSettings != null) {
            for (final String key : paKpiDefaultSettings.keySet()) {
                if (!paKpiSettings.has(key)) {
                    paKpiSettings.add(key, paKpiDefaultSettings.get(key));
                }
            }
            customizedGlobalSettings.put(PA_KPI_SETTINGS, paKpiSettings.toString());
        }
    }

    private boolean doesMaxLbdarStepsizeHaveBW(final JsonArray maxLbdarStepsizeArray, final int bwToFind) {
        for (int i = 0; i < maxLbdarStepsizeArray.size(); i++) {
            final JsonObject category = maxLbdarStepsizeArray.get(i).getAsJsonObject();
            final int currentBW = category.get("BW").getAsInt();
            if (currentBW == bwToFind) {
                return true;
            }
        }
        return false;
    }

    private void alignSequenceIfNeeded(final Configuration insertedConfiguration) throws SQLException {
        final Integer seqIdx = getCurrentIndexOfSequence();
        if (seqIdx < insertedConfiguration.getId()) {
            setIndexOfSequence(insertedConfiguration.getId());
        }
    }

    private Configuration insertConfiguration(final Configuration configuration, final Object[] parameters, final String query) throws SQLException {
        try {
            final int id = databaseAccess.executeInsert(query, new ConfigurationInsertHandler(), parameters);
            configuration.setId(id);
            return configuration;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Failed to execute create statement: {} - {}", query, e.getMessage());
            throw e;
        }
    }

    private Integer getCurrentIndexOfSequence() throws SQLException {
        final String query = String.format("SELECT last_value FROM %s;", ID_SEQUENCE_NAME);
        try {
            final Integer sequenceLastValue = databaseAccess.executeQuery(query,
                    new SequenceCurrentValueHandler());
            LOGGER.debug("The last value for {} is: {}", ID_SEQUENCE_NAME, sequenceLastValue);

            return sequenceLastValue;
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private void setIndexOfSequence(final Integer sequenceNumber) throws SQLException {
        final String query = String.format("SELECT setval ('%s',%s);", ID_SEQUENCE_NAME, sequenceNumber.toString());
        try {
            databaseAccess.executeQuery(query, new ModifySequenceValueHandler());
            LOGGER.debug("The last value for {} is: {}", ID_SEQUENCE_NAME, sequenceNumber);

        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    private Configuration getConfigurationByParameter(final Object parameter, final Object value) throws SQLException {
        final PreparedStatementHandler preparedStatementHandler = new PreparedStatementCreator() {
            private static final int CONFIGURATION_PARAMETER = 1;

            @Override
            public PreparedStatement addParameters(final PreparedStatement preparedStatement) throws SQLException {
                if (value instanceof Integer) {
                    preparedStatement.setInt(CONFIGURATION_PARAMETER, (Integer) value);
                } else if (value instanceof String) {
                    preparedStatement.setString(CONFIGURATION_PARAMETER, (String) value);
                }
                return preparedStatement;
            }
        };

        final String query = String.format("SELECT %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s FROM %s where %s = ? ", ID, NAME, ENABLED, SCHEDULE,
                OPEN_LOOP, CUSTOMIZED_GLOBAL_SETTINGS, CUSTOMIZED_DEFAULT_SETTINGS, GROUPS, INCLUSION_LIST, EXCLUSION_LIST, WEEKEND_DAYS,
                ENABLE_PA, CONFIGURATION_SETTINGS, parameter);

        try {
            final List<Configuration> configurations = databaseAccess.executeQuery(query,
                    new ConfigurationHandler(), preparedStatementHandler);
            if (configurations.isEmpty()) {
                LOGGER.info("No configuration available for {} {}", parameter, value);
                return null;
            } else {
                LOGGER.info("Retrieved configuration {}, for {}", parameter, configurations.get(0));
                return configurations.get(0);
            }
        } catch (final SQLException e) { //NOSONAR Exception suitably logged
            LOGGER.warn(FAILED_TO_EXECUTE_QUERY, query, e);
            throw e;
        }
    }

    @Override
    public int updateSettingsOnUpgrade(final Configuration configuration) throws SQLException {
        fillMissingPaKpiSettings(configuration.getCustomizedGlobalSettings(), GSON.fromJson(DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS,
                new TypeToken<Map<String, String>>() {
                }.getType()));
        fillMissingMapEntries(configuration.getCustomizedDefaultSettings(), GSON.fromJson(DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS,
                new TypeToken<Map<String, String>>() {
                }.getType()));
        return update(configuration);
    }
}
