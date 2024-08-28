/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.startup;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDao;
import com.ericsson.oss.services.sonom.flm.database.configuration.ConfigurationDaoImpl;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;

/**
 * This class updates existing configurations during FLM startup and is intended to
 * update JSON fields that are part of configuration during an upgrade.
 */
public class ConfigurationUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdater.class);

    private final ConfigurationDao configDao = new ConfigurationDaoImpl();

    /**
     * During an upgrade, this method updates existing configurations with settings
     * that are introduced in newer version of FLM. Configuration is updated with
     * default value of newly introduced settings.
     */
    public void updateSettingsOnUpgrade() {

        List<Configuration> allConfigs = new LinkedList<>();
        try {
            allConfigs = configDao.getAll();
            LOGGER.info("Retrieved configurations {}", allConfigs);
        } catch (final SQLException e) {
            LOGGER.error("Error getting configuration ", e);
        }

        for (final Configuration config : allConfigs) {
            try {
                final int updatedRecords = configDao.updateSettingsOnUpgrade(config);
                if (updatedRecords == 0) {
                    LOGGER.error("Failed to update configuration {}", config);
                } else {
                    LOGGER.info("Updated configuration {}", config);
                }
            } catch (final SQLException e) {
                LOGGER.error("Error updating configuration ", e);
            }
        }
    }
}
