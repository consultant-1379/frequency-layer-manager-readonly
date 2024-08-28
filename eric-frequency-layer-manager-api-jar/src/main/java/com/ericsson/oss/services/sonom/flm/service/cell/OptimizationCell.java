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

package com.ericsson.oss.services.sonom.flm.service.cell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A POJO to hold the cell optimization.
 * @param <T> type of kpi
 */
public class OptimizationCell<T> {
    private final String fdn;
    private final int ossId;
    private final Map<String, String> kpis;
    private final Map<String, String> cmAttributes;
    private final Map<String, String> settings;

    public OptimizationCell(final String fdn, final int ossId, final Map<String, T> kpis,
            final Map<String, String> cmAttributes,
            final Map<String, String> settings) {
        this.fdn = fdn;
        this.ossId = ossId;
        this.cmAttributes = cmAttributes;
        this.settings = settings;
        this.kpis = policyInputNullRepresentation(kpis);
    }

    public OptimizationCell(final String fdn, final int ossId) {
        this(fdn, ossId, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public String getFdn() {
        return fdn;
    }

    public int getOssId() {
        return ossId;
    }

    public Map<String, String> getKpis() {
        return kpis;
    }

    public Map<String, String> getCmAttributes() {
        return cmAttributes;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void addKpi(final String kpiName, final String kpi) {
        kpis.put(kpiName, kpi);
    }

    public void addCmAttribute(final String cmAttributeName, final String cmAttribute) {
        cmAttributes.put(cmAttributeName, cmAttribute);
    }

    public void addSetting(final String settingName, final String setting) {
        settings.put(settingName, setting);
    }

    private Map<String, String> policyInputNullRepresentation(final Map<String, T> map) {
        final Map<String, String> convertedMap = new HashMap<>();
        for (final Map.Entry<String, T> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                convertedMap.put(entry.getKey(), "null");
            } else {
                convertedMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return convertedMap;
    }

    @Override
    public String toString() {
        return String.format(
                "%s:: {fdn: '%s', ossId: %d, kpis: '%s', cmAttributes: '%s', settings: '%s'}",
                getClass().getSimpleName(), fdn, ossId, kpis.toString(),
                cmAttributes.toString(), settings.toString());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OptimizationCell that = (OptimizationCell) o;
        return Objects.equals(fdn, that.getFdn())
                && Objects.equals(ossId, that.getOssId())
                && Objects.equals(kpis, that.getKpis()) && Objects.equals(cmAttributes, that.getCmAttributes())
                && Objects.equals(settings, that.getSettings());

    }

    @Override
    public int hashCode() {
        return Objects.hash(fdn, ossId, kpis, cmAttributes, settings);

    }

}
