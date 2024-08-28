/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.settings;

import static com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmScheduleOperation.CREATE;
import static com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmScheduleOperation.DELETE;
import static com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmScheduleOperation.UPDATE;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDao;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDaoImpl;
import com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmExecutionScheduler;
import com.ericsson.oss.services.sonom.flm.scheduler.FlmAlgorithmScheduleOperation;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsExceptionCode;
import com.google.gson.Gson;

/**
 * Class that is used to load and update flm settings.
 */
public class FlmConfigurationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmConfigurationHandler.class);
    private static final int MAXIMUM_CONFIGURATIONS = 10;
    private ConfigurationDao configurationDao = new ConfigurationDaoImpl(); // NOPMD cannot be final or mockito can't inject mock for testing

    /**
     * Create new Configuration in the database.
     *
     * @param configurationJson
     *            the JSON string of settings to update
     * @return The newly created {@link Configuration}
     * @throws SQLException
     *             throws this exception if activity is not scheduled
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if the configuration has duplicated settings
     */
    public Configuration createConfiguration(final String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException {
        FlmConfigurationValidator.validateFlmConfiguration(configurationJson);
        LOGGER.debug("Successfully validated configuration json : {}", configurationJson);

        final Configuration configuration = new Gson().fromJson(configurationJson, Configuration.class);
        final Configuration newConfiguration;

        synchronized (this) {
            validateDaoBeforeCreation(configuration.getName(), configuration.getId());
            if (configuration.getId() == null) {
                newConfiguration = configurationDao.create(configuration);
            } else {
                newConfiguration = configurationDao.createWithId(configuration);
            }
        }

        LOGGER.debug("Successfully created the record in db with id '{}'", newConfiguration.getId());
        updateFlmAlgorithmSchedules(newConfiguration, CREATE);
        return newConfiguration;
    }

    /**
     * Create new Configuration in the database with given id.
     *
     * @param configurationId
     *            create configuration with this id (ignored if it's null)
     * @param configurationJson
     *            the JSON string of settings to update
     * @return The newly created {@link Configuration}
     * @throws SQLException
     *             throws this exception if activity is not scheduled
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if the configuration has duplicated settings
     */
    public Configuration createConfiguration(final Integer configurationId, final String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException {
        FlmConfigurationValidator.validateFlmConfiguration(configurationJson);
        LOGGER.debug("Successfully validated configuration json : {}", configurationJson);

        final Configuration configuration = new Gson().fromJson(configurationJson, Configuration.class);
        FlmConfigurationValidator.validateConfigurationId(configuration.getId(), configurationId);
        final Configuration newConfiguration;

        synchronized (this) {
            validateDaoBeforeCreation(configuration.getName(), configurationId);
            newConfiguration = configurationDao.createWithId(configuration);
        }

        LOGGER.debug("Successfully created the record in db with id '{}'", newConfiguration.getId());
        updateFlmAlgorithmSchedules(newConfiguration, CREATE);
        return newConfiguration;
    }

    /**
     * Apply update to the database.
     *
     * @param configurationJson
     *            the JSON string of settings to update
     * @param configurationId
     *            the id of configuration
     * @return boolean
     * @throws SQLException
     *             throws this exception if activity is not scheduled
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if new name already in use
     */
    public boolean updateConfiguration(final Integer configurationId, final String configurationJson)
            throws SQLException, ConfigurationSettingsJsonValidationException, ActivitySchedulerException, FlmConfigurationSettingsException {

        FlmConfigurationValidator.validateFlmConfiguration(configurationJson);
        final Configuration configuration = new Gson().fromJson(configurationJson, Configuration.class);
        FlmConfigurationValidator.validateConfigurationId(configuration.getId(), configurationId);
        LOGGER.debug("Successfully validated configuration json : {}", configurationJson);

        synchronized (this) {
            validateForNameConflict(configuration);

            final int updatedRecords = configurationDao.update(configuration);
            if (updatedRecords == 0) {
                LOGGER.warn("Failed to update record in the table");
                return false;
            }
            final Configuration updatedConfiguration = configurationDao.get(configurationId);
            LOGGER.debug("Successfully updated the record in db");
            updateFlmAlgorithmSchedules(updatedConfiguration, UPDATE);
        }

        return true;
    }

    /**
     * Get all configurations for the FLM algorithm.
     *
     * @return {@link List} of {@link String} containing the current configuration
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public List<Configuration> getConfigurations() throws SQLException {
        return configurationDao.getAll();
    }

    /**
     * Get a single configuration for the FLM algorithm.
     *
     * @param configId
     *            {@link Integer} the wanted configuration id.
     * @return {@link Configuration} the configuration or <code>null</code> if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public Configuration getConfiguration(final Integer configId) throws SQLException {
        return configurationDao.get(configId);
    }

    /**
     * Get a single configuration for the FLM algorithm.
     *
     * @param name
     *            {@link String} the wanted configuration name.
     * @return {@link Configuration} the configuration or <code>null</code> if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    public Configuration getConfiguration(final String name) throws SQLException {
        return configurationDao.get(name);
    }

    /**
     * Delete a single configuration for the FLM algorithm.
     *
     * @param configId
     *            {@link Integer} the configuration id of the configuration to delete
     * @return {@link boolean} true if successful false if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occur during the delete operation
     */
    public boolean deleteConfiguration(final Integer configId) throws SQLException {
        final Configuration configuration = getConfiguration(configId);
        synchronized (this) {
            final boolean result = configurationDao.delete(configId);
            if (result) {
                try {
                    updateFlmAlgorithmSchedules(configuration, DELETE);
                } catch (ActivitySchedulerException e) {
                    LOGGER.warn("Failed to remove schedule for configuration '{}'", configuration, e);
                }
            }
            return result;
        }
    }

    /**
     * Method to validate name conflict on configuration update.
     * <p>
     * Conflict occurs when configuration with given name already exists, and that configuration has different id than the one
     * we want to update, to let update records without updating the name column.
     * @param configurationToUpdate
     *             {@link Configuration } to validate
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     * @throws FlmConfigurationSettingsException
     *             throws this exception if new name already in use
     */
    private void validateForNameConflict(final Configuration configurationToUpdate) throws SQLException, FlmConfigurationSettingsException {
        final Configuration persistedConfiguration = configurationDao.get(configurationToUpdate.getName());

        if (Objects.nonNull(persistedConfiguration) && !Objects.equals(persistedConfiguration.getId(), configurationToUpdate.getId())) {
            throw new FlmConfigurationSettingsException(FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS);
        }
    }

    private void validateDaoBeforeCreation(final String configurationName, final Integer configurationId)
            throws FlmConfigurationSettingsException, SQLException {
        if (configurationDao.get(configurationName) != null) {
            throw new FlmConfigurationSettingsException(FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_NAME_ALREADY_EXISTS);
        }
        if (configurationId != null && configurationDao.get(configurationId) != null) {
            throw new FlmConfigurationSettingsException(FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_ID_ALREADY_EXISTS);
        }
        if (configurationDao.getAll().size() >= MAXIMUM_CONFIGURATIONS) {
            throw new FlmConfigurationSettingsException(FlmConfigurationSettingsExceptionCode.CONFIGURATION_SETTINGS_LIMIT_REACHED);
        }
    }

    void updateFlmAlgorithmSchedules(final Configuration configuration, final FlmAlgorithmScheduleOperation operation)
            throws ActivitySchedulerException {
        if (isConfigurationEnabled(configuration)) {
            switch (operation) {
                case CREATE:
                    createFlmAlgorithmExecutionSchedule(configuration);
                    break;
                case UPDATE:
                    updateFlmAlgorithmExecutionSchedule(configuration);
                    break;
                case DELETE:
                    deleteFlmAlgorithmExecutionSchedule(configuration);
                    break;
                default:
                    break;
            }
        } else {
            if (FlmAlgorithmExecutionScheduler.checkIfScheduleExists(configuration)) {
                deleteFlmAlgorithmExecutionSchedule(configuration);
            }
        }
    }

    private boolean isConfigurationEnabled(final Configuration configuration) {
        return (configuration.isEnabled() != null && configuration.isEnabled());
    }

    void createFlmAlgorithmExecutionSchedule(final Configuration configuration) throws ActivitySchedulerException {
        FlmAlgorithmExecutionScheduler.createSchedule(configuration);
        LOGGER.info("Successfully created the schedule: '{}', for configuration: '{}'", configuration.getSchedule(),
                configuration.getName());
    }

    void updateFlmAlgorithmExecutionSchedule(final Configuration configuration) throws ActivitySchedulerException {
        FlmAlgorithmExecutionScheduler.updateSchedule(configuration);
        LOGGER.info("Successfully updated the schedule: '{}', for configuration: '{}'", configuration.getSchedule(),
                configuration.getName());
    }

    void deleteFlmAlgorithmExecutionSchedule(final Configuration configuration) throws ActivitySchedulerException {
        FlmAlgorithmExecutionScheduler.deleteSchedule(configuration);
        LOGGER.info("Successfully deleted the schedule: '{}', for configuration: '{}'", configuration.getSchedule(),
                configuration.getName());
    }

}
