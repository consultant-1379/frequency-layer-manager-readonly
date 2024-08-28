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
package com.ericsson.oss.services.sonom.flm.database.configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.handlers.ResultHandler;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Creates a {@link List} of configuration's represented as {@link Configuration}.
 */
public class ConfigurationHandler implements ResultHandler<List<Configuration>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static final Gson GSON = new Gson();

    @Override
    public List<Configuration> populate(final ResultSet resultSet) throws SQLException {
        final List<Configuration> configurations = new ArrayList<>();

        while (resultSet.next()) {
            final Configuration configuration = new Configuration();
            configuration.setId(resultSet.getInt(ConfigurationDbConstants.ID));
            configuration.setName(resultSet.getString(ConfigurationDbConstants.NAME));
            configuration.setSchedule(resultSet.getString(ConfigurationDbConstants.SCHEDULE));
            configuration.setEnabled(resultSet.getBoolean(ConfigurationDbConstants.ENABLED));
            configuration.setOpenLoop(resultSet.getBoolean(ConfigurationDbConstants.OPEN_LOOP));
            configuration.setWeekendDays(resultSet.getString(ConfigurationDbConstants.WEEKEND_DAYS));
            configuration.setEnablePA(resultSet.getBoolean(ConfigurationDbConstants.ENABLE_PA));
            try {
                configuration.setCustomizedGlobalSettings(GSON.fromJson(resultSet.getString(
                        ConfigurationDbConstants.CUSTOMIZED_GLOBAL_SETTINGS), new TypeToken<Map<String, String>>() {
                        }.getType()));
                configuration.setCustomizedDefaultSettings(GSON.fromJson(resultSet.getString(
                        ConfigurationDbConstants.CUSTOMIZED_DEFAULT_SETTINGS), new TypeToken<Map<String, String>>() {
                        }.getType()));
                configuration.setGroups(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ConfigurationDbConstants.GROUPS), CustomizedGroup[].class)));
                configuration.setInclusionList(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ConfigurationDbConstants.INCLUSION_LIST), Group[].class)));
                configuration.setExclusionList(Arrays.asList(GSON.fromJson(
                        resultSet.getString(ConfigurationDbConstants.EXCLUSION_LIST), Group[].class)));

            } catch (final JsonSyntaxException e) {
                LOGGER.error("Failed to parse attribute for configuration: {}", configuration, e);
            }
            configurations.add(configuration);
        }
        LOGGER.debug("Populated list of configurations: {}", configurations);
        return configurations;
    }
}
