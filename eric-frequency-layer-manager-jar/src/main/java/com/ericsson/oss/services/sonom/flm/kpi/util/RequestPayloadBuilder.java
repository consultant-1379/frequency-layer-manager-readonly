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

package com.ericsson.oss.services.sonom.flm.kpi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationRequestPayload;
import com.google.gson.Gson;

/**
 * Class for adding additional parameters to {@link KpiCalculationRequestPayload}.
 */
public final class RequestPayloadBuilder {
    private static final Gson GSON = new Gson();

    private final PayloadWrapper payloadWrapper;

    private RequestPayloadBuilder(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        payloadWrapper = new PayloadWrapper(kpiCalculationRequestPayload);
    }

    public static RequestPayloadBuilder from(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        return new RequestPayloadBuilder(kpiCalculationRequestPayload);
    }

    public RequestPayloadBuilder addAdditionalParameter(final String key, final String value) {
        payloadWrapper.addAdditionalParameter(key, value);
        return this;
    }

    public RequestPayloadBuilder addAdditionalParameter(final AdditionalKpiParameters additionalParameter, final String value) {
        payloadWrapper.addAdditionalParameter(additionalParameter, value);
        return this;
    }

    public RequestPayloadBuilder addAdditionalParameter(final Map<String, String> additionalParameters) {
        payloadWrapper.addAdditionalParameter(additionalParameters);
        return this;
    }

    /**
     * Builds {@link KpiCalculationRequestPayload}.
     * 
     * @return built {@link KpiCalculationRequestPayload}.
     */
    public String build() {
        return GSON.toJson(new KpiCalculationRequestPayload(payloadWrapper.getSource(),
                payloadWrapper.getKpiNames(),
                payloadWrapper.getParameters()));
    }

    /**
     * Enum to hold available KPI parameters.
     */
    public enum AdditionalKpiParameters {
        CURRENT_DATE("param.current_date"),
        START_DATE_TIME("param.start_date_time"),
        END_DATE_TIME("param.end_date_time"),
        RECALCULATION_DATE("param.recalculation_date"),
        EXECUTION_ID("param.execution_id"),
        SECTORS_FOR_SIGNAL_RANGE_RECALCULATION("param.sectors_for_signal_range_recalculation"),
        SECTORS_WITHOUT_REF_CELL("param.sectors_without_ref_cell"),
        TRANSIENT_NUM_DAYS("param.transient_num_days"),
        PREVIOUS_EXECUTION_ID("param.previous_execution_id"),
        START_TIMESTAMP("param.start_timestamp"),
        END_TIMESTAMP("param.end_timestamp"),
        WEEKEND_DAYS("param.weekend_days");

        private final String key;

        AdditionalKpiParameters(final String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private static final class PayloadWrapper {
        private final Map<String, String> parameters;
        private final String source;
        private final List<String> kpiNames;

        private PayloadWrapper(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
            parameters = new HashMap<>(kpiCalculationRequestPayload.getParameters());
            source = kpiCalculationRequestPayload.getSource();
            kpiNames = new ArrayList<>(kpiCalculationRequestPayload.getKpiNames());
        }

        private Map<String, String> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        private String getSource() {
            return source;
        }

        private List<String> getKpiNames() {
            return Collections.unmodifiableList(kpiNames);
        }

        private void addAdditionalParameter(final String key, final String value) {
            parameters.put(key, value);
        }

        private void addAdditionalParameter(final AdditionalKpiParameters additionalParameter, final String value) {
            parameters.put(additionalParameter.getKey(), value);
        }

        private void addAdditionalParameter(final Map<String, String> additionalParameters) {
            parameters.putAll(additionalParameters);
        }
    }
}