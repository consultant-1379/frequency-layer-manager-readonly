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
package com.ericsson.oss.services.sonom.flm.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.service.api.settings.CustomizedGroup;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;

/**
 * Utility class to construct {@link Configuration} instances.
 */
public class ConfigurationBuilder {

    private final Configuration configuration;

    public ConfigurationBuilder() {
        configuration = new Configuration();
    }

    public ConfigurationBuilder(final Configuration configuration) {
        this.configuration = configuration;
    }

    public ConfigurationBuilder withId(final Integer id) {
        configuration.setId(id);
        return this;
    }

    public ConfigurationBuilder withName(final String name) {
        configuration.setName(name);
        return this;
    }

    public ConfigurationBuilder withEnabled(final Boolean enabled) {
        configuration.setEnabled(enabled);
        return this;
    }

    public ConfigurationBuilder withSchedule(final String schedule) {
        configuration.setSchedule(schedule);
        return this;
    }

    public ConfigurationBuilder withOpenLoop(final Boolean openLoop) {
        configuration.setOpenLoop(openLoop);
        return this;
    }

    public ConfigurationBuilder withCustomizedGlobalSettings(final Map<String, String> customizedGlobalSettings) {
        configuration.setCustomizedGlobalSettings(customizedGlobalSettings);
        return this;
    }

    public ConfigurationBuilder withCustomizedDefaultSettings(final Map<String, String> customizedDefaultSettings) {
        configuration.setCustomizedDefaultSettings(customizedDefaultSettings);
        return this;
    }

    public ConfigurationBuilder withGroups(final List<CustomizedGroup> groups) {
        configuration.setGroups(groups);
        return this;
    }

    public ConfigurationBuilder withInclusionList(final List<Group> inclusionList) {
        configuration.setInclusionList(inclusionList);
        return this;
    }

    public ConfigurationBuilder withExclusionList(final List<Group> exclusionList) {
        configuration.setExclusionList(exclusionList);
        return this;
    }

    public ConfigurationBuilder withWeekendDays(final String weekendDays) {
        configuration.setWeekendDays(weekendDays);
        return this;
    }

    public ConfigurationBuilder withNumberOfGroups(final int numOfGroups) {
        final List<CustomizedGroup> groups = new ArrayList<>();
        for (int i = 1; i <= numOfGroups; i++) {
            final double valueOfR = Math.floor((i * 2000.0) / numOfGroups) + 1;
            groups.add(new CustomizedGroup("group" + i, Collections.singletonMap("targetThroughputR(Mbps)", String.valueOf(valueOfR))));
        }
        configuration.setGroups(groups);
        return this;
    }

    public ConfigurationBuilder withEnablePA(final Boolean enablePA) {
        configuration.setEnablePA(enablePA);
        return this;
    }

    public Configuration build() {
        return configuration;
    }
}
