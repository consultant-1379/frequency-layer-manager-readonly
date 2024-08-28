/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class to store and represent a {@link Group} which has some customized settings set.
 */
@JsonPropertyOrder({ "name", "customizedGroupSettings" })
public class CustomizedGroup extends Group {
    private static final long serialVersionUID = 6456959391194624472L;
    private Map<String, String> customizedGroupSettings;

    public CustomizedGroup(final String name, final Map<String, String> customizedGroupSettings) {
        super(name);
        this.customizedGroupSettings = customizedGroupSettings;
    }

    public Map<String, String> getCustomizedGroupSettings() {
        return customizedGroupSettings;
    }

    public void setCustomizedGroupSettings(final Map<String, String> customizedGroupSettings) {
        this.customizedGroupSettings = customizedGroupSettings;
    }

    @Override
    public String toString() {
        return String.format("%s:: {name: '%s', customizedGroupSettings: '%s'}",
                getClass().getSimpleName(),
                name, customizedGroupSettings);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustomizedGroup group = (CustomizedGroup) o;
        return name.equals(group.name) &&
                customizedGroupSettings.equals(group.customizedGroupSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, customizedGroupSettings);
    }
}
