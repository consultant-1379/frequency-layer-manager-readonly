/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.sonom.flm.service.api;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Remote;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;

/**
 * Interface defining the <code>flm-service</code>.
 */
@Remote
public interface FlmConfigurationService {

    /**
     * Method retrieves the configuration and cron schedule for the flm service given the configuration id.
     *
     * @param configurationId
     *            {@link Integer} configuration id.
     * @return {@link Configuration} containing the specified configuration
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    Configuration getConfiguration(Integer configurationId) throws SQLException;

    /**
     * Method retrieves the configuration and cron schedule for the flm service given the configuration name.
     *
     * @param name
     *            {@link String} configuration name.
     * @return {@link Configuration} containing the specified configuration
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    Configuration getConfiguration(String name) throws SQLException;

    /**
     * Method retrieves the configurations for the flm service which are currently being stored in the database.
     *
     * @return {@link List} of {@link Configuration} containing the current configuration
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    List<Configuration> getConfigurations() throws SQLException;

    /**
     * Method updates the flm service settings.
     * <p>
     * It takes a JSON object comprised of:
     * <p>
     * * configurationSettings object
     * <p>
     * And updates the relevant tables in the database
     *
     * @param configurationJson
     *            a {@link String} containing an algorithm's settings in JSON format
     * @param configurationId
     *            a {@link Integer} containing the id of Configuration
     * @return {@link Boolean} indicating if settings updated successfully or not
     * @throws SQLException
     *             throws this exception if there was a problem persisting the schedule
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if the configuration has duplicated settings
     */
    boolean putConfiguration(Integer configurationId, String configurationJson)
            throws SQLException, ConfigurationSettingsJsonValidationException, ActivitySchedulerException, FlmConfigurationSettingsException;

    /**
     * Method creates an flm service setting.
     * <p>
     * It takes a JSON object comprised of:
     * <p>
     * * configurationSettings object
     * <p>
     * And creates a new configuration setting in the database
     *
     * @param configurationJson
     *            a {@link String} containing an algorithm's settings in JSON format
     * @return {@link Configuration} containing the newly created configuration
     * @throws SQLException
     *             throws this exception if there was a problem persisting the schedule
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if the configuration has duplicated settings
     */
    Configuration postConfiguration(String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException;

    /**
     * Method creates an flm service setting.
     * <p>
     * It takes an id and a JSON object comprised of:
     * <p>
     * * configurationSettings object
     * <p>
     * And creates a new configuration setting in the database with the given id
     *
     * @param configurationId
     *            create configuration with this id (ignored if it's null)
     * @param configurationJson
     *            a {@link String} containing an algorithm's settings in JSON format
     * @return {@link Configuration} containing the newly created configuration
     * @throws SQLException
     *             throws this exception if there was a problem persisting the schedule
     * @throws ConfigurationSettingsJsonValidationException
     *             throws this exception if json validation fails
     * @throws ActivitySchedulerException
     *             throws this exception if activity is not scheduled
     * @throws FlmConfigurationSettingsException
     *             throws this exception if the configuration has duplicated settings
     */
    Configuration postConfiguration(Integer configurationId, String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException;

    /**
     * Method returns boolean with the answer of whether the configuration is deleted or not.
     *
     * @param id
     *            {@link Integer} configuration id.
     * @return {@link boolean} response
     * @throws SQLException
     *             throws this exception if there was a problem deleting the configuration from the database
     */
    boolean deleteConfiguration(Integer id) throws SQLException;
}
