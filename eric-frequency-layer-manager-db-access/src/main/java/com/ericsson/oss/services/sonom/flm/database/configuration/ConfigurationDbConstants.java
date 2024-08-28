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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;

/**
 * Class to store column names for the configuration settings table.
 */
public final class ConfigurationDbConstants {

    public static final String CONFIGURATION_SETTINGS = "configuration_settings";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SCHEDULE = "schedule";
    public static final String ENABLED = "enabled";
    public static final String OPEN_LOOP = "open_loop";
    public static final String CUSTOMIZED_GLOBAL_SETTINGS = "customized_global_settings";
    public static final String CUSTOMIZED_DEFAULT_SETTINGS = "customized_default_settings";
    public static final String GROUPS = "groups";
    public static final String INCLUSION_LIST = "inclusion_list";
    public static final String EXCLUSION_LIST = "exclusion_list";
    public static final String WEEKEND_DAYS = "weekend_days";
    public static final String ENABLE_PA = "enable_pa";
    public static final Boolean DEFAULT_ENABLED = true;
    public static final Boolean DEFAULT_OPEN_LOOP = true;
    public static final String DEFAULT_CUSTOMIZED_GLOBAL_SETTINGS = loadResource("defaultCustomGlobalSettings.json");
    public static final String DEFAULT_CUSTOMIZED_DEFAULT_SETTINGS = loadResource("defaultCustomDefaultSettings.json");
    public static final String DEFAULT_GROUPS = "[]";
    public static final String DEFAULT_INCLUSION_LIST = "[]";
    public static final String DEFAULT_EXCLUSION_LIST = "[]";
    public static final String DEFAULT_WEEKEND_DAYS = "Saturday,Sunday";
    public static final Boolean DEFAULT_ENABLE_PA = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDbConstants.class);

    private ConfigurationDbConstants() {
    }

    private static String loadResource(final String filePath) {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(filePath);
        } catch (final IOException e) {
            LOGGER.error("Error loading resource through filepath: {}, exception: ", filePath, e);
            return "{}";
        }
    }
}
