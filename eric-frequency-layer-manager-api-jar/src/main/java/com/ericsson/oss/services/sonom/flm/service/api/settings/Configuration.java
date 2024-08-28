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
package com.ericsson.oss.services.sonom.flm.service.api.settings;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to store and represent Configuration Information.
 */
@JsonPropertyOrder({ "id", "name", "enabled", "schedule", "openLoop", "customizedGlobalSettings", "customizedDefaultSettings", "groups",
        "inclusionList", "exclusionList", "weekendDays", "enablePA" })
public class Configuration implements Serializable {

    private static final long serialVersionUID = 7413309072761023129L;

    private Integer id;
    private String name;
    private Boolean enabled;
    private String schedule;
    private Boolean openLoop;
    private Map<String, String> customizedGlobalSettings;
    private Map<String, String> customizedDefaultSettings;
    private List<CustomizedGroup> groups;
    private List<Group> inclusionList;
    private List<Group> exclusionList;
    private String weekendDays;
    private Boolean enablePA;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(final String schedule) {
        this.schedule = schedule;
    }

    public Boolean isOpenLoop() {
        return openLoop;
    }

    public void setOpenLoop(final Boolean openLoop) {
        this.openLoop = openLoop;
    }

    public Map<String, String> getCustomizedDefaultSettings() {
        return customizedDefaultSettings;
    }

    public void setCustomizedDefaultSettings(final Map<String, String> customizedDefaultSettings) {
        this.customizedDefaultSettings = customizedDefaultSettings;
    }

    public Map<String, String> getCustomizedGlobalSettings() {
        return customizedGlobalSettings;
    }

    public void setCustomizedGlobalSettings(final Map<String, String> customizedGlobalSettings) {
        this.customizedGlobalSettings = customizedGlobalSettings;
    }

    public List<CustomizedGroup> getGroups() {
        return groups; //NOSONAR it's a POJO
    }

    public void setGroups(final List<CustomizedGroup> groups) {
        this.groups = groups; //NOSONAR it's a POJO
    }

    public List<Group> getInclusionList() {
        return inclusionList; //NOSONAR it's a POJO
    }

    public void setInclusionList(final List<Group> inclusionList) {
        this.inclusionList = inclusionList; //NOSONAR it's a POJO
    }

    public List<Group> getExclusionList() {
        return exclusionList; //NOSONAR it's a POJO
    }

    public void setExclusionList(final List<Group> exclusionList) {
        this.exclusionList = exclusionList; //NOSONAR it's a POJO
    }

    public String getWeekendDays() {
        return weekendDays;
    }

    public void setWeekendDays(final String weekendDays) {
        this.weekendDays = weekendDays;
    }

    public Boolean isEnablePA() {
        return enablePA;
    }

    public void setEnablePA(final Boolean enablePA) {
        this.enablePA = enablePA;
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {id: '%s', name: '%s', enabled: '%s', schedule: '%s', openLoop: '%s', customizedGlobalSettings: '%s', " +
                        "customizedDefaultSettings: '%s', groups: '%s', inclusionList: '%s', exclusionList: '%s', weekendDays: '%s', enablePA: '%s'}",
                getClass().getSimpleName(),
                id, name, enabled, schedule, openLoop, customizedGlobalSettings, customizedDefaultSettings, groups, inclusionList, exclusionList,
                weekendDays, enablePA);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Configuration that = (Configuration) o;
        return Objects.equals(enabled, that.enabled) &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(schedule, that.schedule) &&
                Objects.equals(openLoop, that.openLoop) &&
                Objects.equals(customizedGlobalSettings, that.customizedGlobalSettings) &&
                Objects.equals(customizedDefaultSettings, that.customizedDefaultSettings) &&
                Objects.equals(groups, that.groups) &&
                Objects.equals(inclusionList, that.inclusionList) &&
                Objects.equals(exclusionList, that.exclusionList) &&
                Objects.equals(weekendDays, that.weekendDays) &&
                Objects.equals(enablePA, that.enablePA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, enabled, schedule, openLoop, customizedGlobalSettings, customizedDefaultSettings, groups, inclusionList,
                exclusionList, weekendDays, enablePA);
    }
}
