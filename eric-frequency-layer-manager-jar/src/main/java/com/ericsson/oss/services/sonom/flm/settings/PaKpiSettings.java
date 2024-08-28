/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021-2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.Range;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * DAO for PAKPISettings, which represents and process PA KPIs.
 */
public class PaKpiSettings {

    private final Map<String, Data> dataMap = new HashMap<>();

    private final Gson gSon = new GsonBuilder().registerTypeAdapter(Boolean.class, new TypeAdapter<Boolean>() {

        @Override
        public void write(final JsonWriter jsonWriter, final Boolean logicalValue) throws IOException {
            if (logicalValue == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(logicalValue);
            }
        }

        @Override
        public Boolean read(final JsonReader jsonReader) throws IOException {
            final JsonToken peek = jsonReader.peek();
            if (JsonToken.BOOLEAN != peek) {
                throw new IllegalArgumentException("not boolean");
            }
            return jsonReader.nextBoolean();
        }
    }).create();

    /**
     * Get the specified KPI.
     *
     * @param kpis - get this specified KPI
     * @return the specified KPI
     */
    public Data get(final PaKpi kpis) {
        return dataMap.get(kpis.key);
    }

    /**
     * Process the Json primitive structure.
     *
     * @param string Json primitive that contains the PA KPIs
     */
    public void put(final String string) {
        for (final Map.Entry<String, JsonElement> entry : gSon.fromJson(string, JsonObject.class).entrySet()) {
            for (final PaKpi kpi : PaKpi.values()) {
                if (kpi.key.equals(entry.getKey())) {
                    dataMap.put(entry.getKey(), gSon.fromJson(entry.getValue(), Data.class));
                }
            }
        }
    }

    /**
     * Get the PA KPI Settings' Data size.
     *
     * @return int PA KPI Settings' Data size
     */
    public int getDataSize() {
        return dataMap.size();
    }

    /**
     * Representation of the PA KPI-s.
     */
    public enum PaKpi {
        CELLHOSR("cellHandoverSuccessRate", Range.between(80.0, 99.9), Range.between(90.0, 99.99)),
        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR("initialAndAddedERabEstabSrHourly", Range.between(80.0, 99.9), Range.between(90.0, 99.99)),
        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_QCI1("initialAndAddedERabEstabSrQci1Hourly", Range.between(80.0, 99.9), Range.between(90.0, 99.99)),
        E_RAB_RETAINABILITY_PERCENTAGE_LOST("eRabRetainabilityPercentageLostHourly", Range.between(80.0, 99.9), Range.between(0.01, 10.00)),
        E_RAB_RETAINABILITY_PERCENTAGE_LOST_QCI1("eRabRetainabilityPercentageLostQci1Hourly", Range.between(80.0, 99.9), Range.between(0.01, 10.00)),
        DLP_DCP_UE_THROUGHPUT("avgDlPdcpThroughputSector", Range.between(80.0, 99.9), Range.between(0.0, 0.0)),
        ULP_DCP_UE_THROUGHPUT("avgUlPdcpThroughputSector", Range.between(80.0, 99.9), Range.between(0.0, 0.0)),
        UL_PUSCH_SINR("ulPuschSinrHourly", Range.between(80.0, 99.9), Range.between(1.0, 25.0));

        private final String key;
        private final Range<Double> confidenceRange;
        private final Range<Double> relevanceRange;

        /**
         * Create the PA KPI with the specified values.
         *
         * @param key             the name of the PA KPI
         * @param confidenceRange of the PA KPI
         * @param relevanceRange  of the PA KPI
         */
        PaKpi(final String key, final Range<Double> confidenceRange, final Range<Double> relevanceRange) {
            this.key = key;
            this.confidenceRange = confidenceRange;
            this.relevanceRange = relevanceRange;
        }

        /**
         * Provide the confidence range.
         *
         * @return confidence range
         */
        public Range<Double> getConfidenceRange() {
            return confidenceRange;
        }

        /**
         * Provide the relevance range.
         *
         * @return relevance range
         */
        public Range<Double> getRelevanceRange() {
            return relevanceRange;
        }

        boolean isInRange(final Double value, final Supplier<Range<Double>> range) {
            return range.get().contains(value);
        }
    }

    class Data {

        @SerializedName("enableKPI")
        Boolean enableKpi;
        @SerializedName("confidenceInterval")
        Double confidenceInterval;
        @SerializedName("relevanceThreshold")
        Double relevanceThreshold;

        @Override
        public String toString() {
            return "Data{" +
                    "enableKpi=" + enableKpi +
                    ", confidenceInterval=" + confidenceInterval +
                    ", relevanceThreshold=" + relevanceThreshold +
                    '}';
        }
    }
}
