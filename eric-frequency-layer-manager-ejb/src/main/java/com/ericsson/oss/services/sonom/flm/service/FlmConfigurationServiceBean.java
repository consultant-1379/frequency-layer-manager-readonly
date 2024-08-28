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

package com.ericsson.oss.services.sonom.flm.service;

import java.sql.SQLException;
import java.util.List;

import javax.ejb.Stateless;

import com.ericsson.oss.services.sonom.common.scheduler.ActivitySchedulerException;
import com.ericsson.oss.services.sonom.flm.service.api.FlmConfigurationService;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.ConfigurationSettingsJsonValidationException;
import com.ericsson.oss.services.sonom.flm.service.api.settings.exception.FlmConfigurationSettingsException;
import com.ericsson.oss.services.sonom.flm.settings.FlmConfigurationHandler;

/**
 * Implementation of {@link FlmConfigurationService}.
 */
@Stateless(name = "flmConfigurationServiceBean")
public class FlmConfigurationServiceBean implements FlmConfigurationService {

    private final FlmConfigurationHandler flmConfigurationHandler = new FlmConfigurationHandler();

    @Override
    public Configuration getConfiguration(final Integer configurationId) throws SQLException {
        return flmConfigurationHandler.getConfiguration(configurationId);
    }

    @Override
    public Configuration getConfiguration(final String name) throws SQLException {
        return flmConfigurationHandler.getConfiguration(name);
    }

    @Override
    public List<Configuration> getConfigurations() throws SQLException {
        return flmConfigurationHandler.getConfigurations();
    }

    @Override
    public boolean putConfiguration(final Integer configurationId, final String configurationJson)
            throws SQLException, ConfigurationSettingsJsonValidationException, ActivitySchedulerException, FlmConfigurationSettingsException {
        return flmConfigurationHandler.updateConfiguration(configurationId, configurationJson);
    }

    @Override
    public Configuration postConfiguration(final String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException {
        return flmConfigurationHandler.createConfiguration(configurationJson);
    }

    @Override
    public Configuration postConfiguration(final Integer configurationId, final String configurationJson)
            throws SQLException, ActivitySchedulerException, ConfigurationSettingsJsonValidationException, FlmConfigurationSettingsException {
        return flmConfigurationHandler.createConfiguration(configurationId, configurationJson);
    }

    @Override
    public boolean deleteConfiguration(final Integer id) throws SQLException {
        return flmConfigurationHandler.deleteConfiguration(id);
    }

}
