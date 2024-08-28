/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.CURRENT_DATE;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.END_DATE_TIME;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.END_TIMESTAMP;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.RECALCULATION_DATE;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.START_DATE_TIME;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.START_TIMESTAMP;
import static com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters.WEEKEND_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest.KpiCalculationRequestCreator;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationRequestPayload;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class KpiCalculationRequestTest {

    private static final Gson GSON = new Gson();
    private static final String TEST_KPIS = "kpiCalculationRequests/test_kpis.json";

    @Test
    public void whenDynamicParametersAreNotSet_thenTheyAreNotAddedToThePayload() throws IOException {
        final KpiCalculationRequest kpiCalculationRequest = KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, true)
                .loadGroupKpis(TEST_KPIS)
                .build();

        final KpiCalculationRequestPayload objectUnderTest = GSON.fromJson(kpiCalculationRequest.getRequestPayload(),
                KpiCalculationRequestPayload.class);

        assertThat(objectUnderTest.getKpiNames()).containsExactlyInAnyOrder("kpi_1");
        assertThat(objectUnderTest.getSource()).isEqualTo("FLM");
        assertThat(objectUnderTest.getParameters()).isEmpty();
    }

    @Test
    public void whenDynamicParametersAreSet_thenTheyAreAddedToThePayload() throws IOException {
        final KpiCalculationRequest kpiCalculationRequest = KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, true)
                .loadGroupKpis(TEST_KPIS)
                .withCurrentDate("currentDate")
                .withStartDateTime("startDateTime")
                .withEndDateTime("endDateTime")
                .withRecalculationDate("recalculationDate")
                .withExecutionId("executionId")
                .withStartTimeStamp("start_timestamp")
                .withEndTimeStamp("end_timestamp")
                .withWeekendDays("weekend_days")
                .withAdditionalParameters(Collections.singletonMap("additionalKey", "additionalValue"))
                .build();

        final KpiCalculationRequestPayload objectUnderTest = GSON.fromJson(kpiCalculationRequest.getRequestPayload(),
                KpiCalculationRequestPayload.class);

        final Map<String, String> expectedEntries = Stream.of(entry(CURRENT_DATE.getKey(), "currentDate"),
                entry(START_DATE_TIME.getKey(), "startDateTime"),
                entry(END_DATE_TIME.getKey(), "endDateTime"),
                entry(RECALCULATION_DATE.getKey(), "recalculationDate"),
                entry(EXECUTION_ID.getKey(), "executionId"),
                entry(START_TIMESTAMP.getKey(), "start_timestamp"),
                entry(END_TIMESTAMP.getKey(), "end_timestamp"),
                entry(WEEKEND_DAYS.getKey(), "weekend_days"),
                entry("additionalKey", "additionalValue"))
                .collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
        assertThat(objectUnderTest.getParameters()).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void whenLoadingGroupKpisFail_thenItIsSuitablyLoggedAndExceptionIsReThrown() {
        final Logger loggerMock = mock(Logger.class);
        Whitebox.setInternalState(KpiCalculationRequest.class, "LOGGER", loggerMock);

        final Throwable thrown = catchThrowable(() -> KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, true)
                .loadGroupKpis("file_doesNotExist.json")
                .build());

        assertThat(thrown).hasMessage("InputStream is null for path 'file_doesNotExist.json'");

        verify(loggerMock).warn(eq("Error loading resource through filepath: {}"),
                eq("file_doesNotExist.json"),
                any(IllegalArgumentException.class));
    }

    @Test
    public void whenExecutionStateIsInitialized_thenExecutionStateIsSet() throws IOException {
        final KpiCalculationRequest objectUnderTest = KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, true)
                .loadGroupKpis(TEST_KPIS)
                .build();

        assertThat(objectUnderTest.getExecutionState()).isEqualTo(ExecutionState.KPI_PROCESSING_GROUP_1);
    }

    @Test
    public void whenIsResumedIsInitializedToTrue_thenRequestIsResumed() throws IOException {
        final KpiCalculationRequest objectUnderTest = KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, true)
                .loadGroupKpis(TEST_KPIS)
                .build();

        assertThat(objectUnderTest.isResumed()).isTrue();
    }

    @Test
    public void whenIsResumedIsInitializedToFalse_thenRequestIsNotResumed() throws IOException {
        final KpiCalculationRequest objectUnderTest = KpiCalculationRequestCreator.create(ExecutionState.KPI_PROCESSING_GROUP_1, false)
                .loadGroupKpis(TEST_KPIS)
                .build();

        assertThat(objectUnderTest.isResumed()).isFalse();
    }

    @Test
    public void whenCreatingAnEmptyRequest_thenItContainsNoPayload() {
        final KpiCalculationRequest objectUnderTest = KpiCalculationRequestCreator.empty(ExecutionState.KPI_PROCESSING_GROUP_1, true);

        assertThat(objectUnderTest.getRequestPayload()).isEmpty();
    }
}
