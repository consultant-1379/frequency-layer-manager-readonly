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

import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.CURRENT_DATE;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.END_DATE_TIME;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.END_TIMESTAMP;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.PREVIOUS_EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.RECALCULATION_DATE;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.SECTORS_FOR_SIGNAL_RANGE_RECALCULATION;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.SECTORS_WITHOUT_REF_CELL;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.START_DATE_TIME;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.START_TIMESTAMP;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.TRANSIENT_NUM_DAYS;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.WEEKEND_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationRequestPayload;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

public class RequestPayloadBuilderTest {

    private static final Gson GSON = new Gson();

    @Test
    public void whenAdditionalParametersAllValuesAreUsed() {
        assertThat(AdditionalKpiParameters.values()).containsExactlyInAnyOrder(CURRENT_DATE, START_DATE_TIME, END_DATE_TIME, RECALCULATION_DATE,
                EXECUTION_ID, SECTORS_FOR_SIGNAL_RANGE_RECALCULATION, SECTORS_WITHOUT_REF_CELL, TRANSIENT_NUM_DAYS, PREVIOUS_EXECUTION_ID,  WEEKEND_DAYS, START_TIMESTAMP, END_TIMESTAMP);
    }

    @Test
    public void whenRequestPayloadIsBuiltFromKpiCalculationRequestPayload_thenItIsBuiltCorrectly() {
        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put("key1", "value1");

        final Map<String, String> additionalParameters = Maps.newHashMap();
        additionalParameters.put("key3", "value3");

        final List<String> kpiDefinitionNames = Arrays.asList("definition1", "definition2");
        final String source = "test_source";
        final String actualJson = RequestPayloadBuilder.from(new KpiCalculationRequestPayload(source, kpiDefinitionNames, parameters))
                .addAdditionalParameter("key2", "value2")
                .addAdditionalParameter(additionalParameters)
                .addAdditionalParameter(CURRENT_DATE, "currentDate")
                .addAdditionalParameter(START_DATE_TIME.getKey(), "startDateTime")
                .addAdditionalParameter(END_DATE_TIME.getKey(), "endDateTime")
                .addAdditionalParameter(RECALCULATION_DATE, "recalculationDate")
                .addAdditionalParameter(EXECUTION_ID, "executionId")
                .addAdditionalParameter(START_TIMESTAMP, "startTimestamp")
                .addAdditionalParameter(END_TIMESTAMP, "endTimestamp")
                .addAdditionalParameter(WEEKEND_DAYS, "weekendDays")
                .build();

        final KpiCalculationRequestPayload kpiCalculationRequestPayload = GSON.fromJson(actualJson, KpiCalculationRequestPayload.class);

        final Map<String, String> expectedEntries = Stream.of(entry(CURRENT_DATE.getKey(), "currentDate"),
                entry(START_DATE_TIME.getKey(), "startDateTime"),
                entry(END_DATE_TIME.getKey(), "endDateTime"),
                entry(RECALCULATION_DATE.getKey(), "recalculationDate"),
                entry(EXECUTION_ID.getKey(), "executionId"),
                entry(START_TIMESTAMP.getKey(), "startTimestamp"),
                entry(END_TIMESTAMP.getKey(), "endTimestamp"),
                entry(WEEKEND_DAYS.getKey(), "weekendDays"),
                entry("key2", "value2"))
                .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));

        expectedEntries.putAll(parameters);
        expectedEntries.putAll(additionalParameters);

        assertThat(kpiCalculationRequestPayload.getSource()).isEqualTo(source);
        assertThat(kpiCalculationRequestPayload.getKpiNames()).containsExactlyInAnyOrder(kpiDefinitionNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        assertThat(kpiCalculationRequestPayload.getParameters()).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}