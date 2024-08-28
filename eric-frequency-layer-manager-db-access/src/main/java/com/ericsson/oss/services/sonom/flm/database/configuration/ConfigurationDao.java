/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2022
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

import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;

/**
 * Interface defining the <code>ConfigurationDao</code>.
 */
public interface ConfigurationDao {

    /**
     * This method creates an FLM configuration in the database.
     *
     * @param configuration
     *            {@link Configuration} the configuration which is to be create.
     * @return Configuration The configuration that was inserted into the database (ID field filled out)
     * @throws SQLException
     *             SqlException thrown when there is issue creating the resource.
     */
    Configuration create(Configuration configuration) throws SQLException;

    /**
     * This method updates the FLM configuration in the database.
     *
     * @param configuration
     *            {@link Configuration} the configuration which is to be updated.
     * @return int number of successful rows updated otherwise zero.
     * @throws SQLException
     *             SqlException thrown when there is issue updating database.
     */
    int update(Configuration configuration) throws SQLException;

    /**
     * Get a single configuration for the FLM algorithm based on configuration id.
     *
     * @param configurationId
     *            {@link Integer} the wanted configuration id.
     * @return {@link Configuration} containing the configuration or <code>null</code> if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    Configuration get(Integer configurationId) throws SQLException;

    /**
     * Get a single configuration for the FLM algorithm based on configuration name.
     *
     * @param name
     *            {@link String} the configuration name
     * @return {@link Configuration} containing the configuration or <code>null</code> if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    Configuration get(String name) throws SQLException;

    /**
     * This method creates an FLM configuration with an ID in the database(reinstantiation).
     *
     * @param configuration
     *            {@link Configuration} configuration to be re-instantiated
     * @return {@link Configuration} The configuration that was inserted into the database
     * @throws SQLException
     *             SqlException thrown when there is issue creating the resource.
     */
    Configuration createWithId(Configuration configuration) throws SQLException;

    /**
     * Get all configurations for the FLM algorithm.
     *
     * @return {@link List} of {@link Configuration} containing the current configuration or <code>null</code> if the configuration does not exist
     * @throws SQLException
     *             this exception will be raised if any error occurs during database retrieval
     */
    List<Configuration> getAll() throws SQLException;

    /**
     * Delete a single configuration for the FLM algorithm based on configuration id.
     *
     * @param configurationId
     *            {@link Integer} the wanted configuration id for deletion.
     * @return {@link Boolean} whether the configuration is deleted or not
     * @throws SQLException
     *             this exception will be raised if any error occurs deleting the configuration from the database
     */
    boolean delete(Integer configurationId) throws SQLException;

    /**
     * This method updates custom default and custom global settings
     * of an existing configuration during upgrade.
     *
     * @param configuration
     *            {@link Configuration} the configuration which is to be updated.
     * @return int number of successful rows updated otherwise zero.
     * @throws SQLException
     *             SqlException thrown when there is issue updating database.
     */
    int updateSettingsOnUpgrade(Configuration configuration) throws SQLException;
}
