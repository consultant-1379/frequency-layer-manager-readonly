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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.flm.kpi.SectorReferenceCellHandler;
import com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.MoreThanOneLogger")
public class AdditionalParametersUtilsTest {
    private static final Long SECTOR_ID = 1234L;

    @Test
    public void whenCalculatingAdditionalParametersForReferenceCellAndRequiredSectorIdsAreEmpty_thenReturnedAdditionalParametersEmpty() throws Exception {
        final Logger loggerMock = mock(Logger.class);
        final SectorReferenceCellHandler sectorReferenceCellHandlerMock = mock(SectorReferenceCellHandler.class);

        Whitebox.setInternalState(AdditionalParametersUtils.class, "LOGGER", loggerMock);

        when(sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.emptySet());

        final Map<String, String> actual = AdditionalParametersUtils.getAdditionalParametersForReferenceCell(
                sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation());

        assertThat(actual).isEmpty();

        verify(loggerMock).info("No calculation of reference cell required.");
        verify(sectorReferenceCellHandlerMock).getSectorIdsRequiringReferenceCellRecalculation();
        verifyNoMoreInteractions(sectorReferenceCellHandlerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForReferenceCellAndRequiredSectorIdsAreNotEmpty_thenReturnedAdditionalParametersContainParameter() throws Exception {
        final Logger loggerMock = mock(Logger.class);
        final SectorReferenceCellHandler sectorReferenceCellHandlerMock = mock(SectorReferenceCellHandler.class);

        Whitebox.setInternalState(AdditionalParametersUtils.class, "LOGGER", loggerMock);

        when(sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.singleton(SECTOR_ID));

        final Map<String, String> actual = AdditionalParametersUtils.getAdditionalParametersForReferenceCell(
                sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation());

        assertThat(actual).containsEntry(AdditionalKpiParameters.SECTORS_WITHOUT_REF_CELL.getKey(),
                                         String.format("kpi_db://kpi_cell_sector_1440.sector_id in (%s)", SECTOR_ID));

        verify(loggerMock).info("Sending calculation request for reference cells.");
        verify(sectorReferenceCellHandlerMock).getSectorIdsRequiringReferenceCellRecalculation();
        verifyNoMoreInteractions(sectorReferenceCellHandlerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForSignalRangeAndRequiredSectorIdsAreEmpty_thenReturnedAdditionalParametersContainsKeyWithNoValue() throws Exception {
        final SectorReferenceCellHandler sectorReferenceCellHandlerMock = mock(SectorReferenceCellHandler.class);

        when(sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.emptySet());

        final Map<String, String> actual = AdditionalParametersUtils.getAdditionalParametersForSignalRange(
                sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation());

        assertThat(actual).containsEntry(AdditionalKpiParameters.SECTORS_FOR_SIGNAL_RANGE_RECALCULATION.getKey(), StringUtils.EMPTY);

        verify(sectorReferenceCellHandlerMock).getSectorIdsRequiringReferenceCellRecalculation();
        verifyNoMoreInteractions(sectorReferenceCellHandlerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForSignalRangeAndRequiredSectorIdsAreNotEmpty_thenReturnedAdditionalParametersContainParameter() throws Exception {
        final SectorReferenceCellHandler sectorReferenceCellHandlerMock = mock(SectorReferenceCellHandler.class);

        when(sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation()).thenReturn(Collections.singleton(SECTOR_ID));

        final Map<String, String> actual = AdditionalParametersUtils.getAdditionalParametersForSignalRange(
                sectorReferenceCellHandlerMock.getSectorIdsRequiringReferenceCellRecalculation());

        assertThat(actual).containsEntry(AdditionalKpiParameters.SECTORS_FOR_SIGNAL_RANGE_RECALCULATION.getKey(),
                                         String.format("OR kpi_cell_sector_1440.sector_id in (%s)", SECTOR_ID));

        verify(sectorReferenceCellHandlerMock).getSectorIdsRequiringReferenceCellRecalculation();
        verifyNoMoreInteractions(sectorReferenceCellHandlerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForGlobalSettingsAndGlobalSettingsAreEmpty_thenExceptionIsThrown() {
        final Logger loggerMock = mock(Logger.class);
        final Execution executionMock = mock(Execution.class);

        Whitebox.setInternalState(AdditionalParametersUtils.class, "LOGGER", loggerMock);

        final Throwable thrown = catchThrowable(() -> AdditionalParametersUtils.getAdditionalParametersForGlobalSettings(Collections.emptyMap()));

        assertThat(thrown).isExactlyInstanceOf(FlmAlgorithmException.class)
                          .hasMessage("Error creating Customized Global Settings KPI parameters");

        verify(loggerMock).error("Error creating Customized Global Settings Parameters - KPI calculation will not be successful");
        verifyNoMoreInteractions(executionMock, loggerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForGlobalSettingsAndGlobalSettingsAreNull_thenExceptionIsThrown() {
        final Logger loggerMock = mock(Logger.class);
        final Execution executionMock = mock(Execution.class);

        Whitebox.setInternalState(AdditionalParametersUtils.class, "LOGGER", loggerMock);

        final Throwable thrown = catchThrowable(() -> AdditionalParametersUtils.getAdditionalParametersForGlobalSettings(null));

        assertThat(thrown).isExactlyInstanceOf(FlmAlgorithmException.class)
                          .hasMessage("Error creating Customized Global Settings KPI parameters");

        verify(loggerMock).error("Error creating Customized Global Settings Parameters - KPI calculation will not be successful");
        verifyNoMoreInteractions(executionMock, loggerMock);
    }

    @Test
    public void whenCalculatingAdditionalParametersForGlobalSettingsAndGlobalSettingsContainValue_thenReturnedAdditionalParametersContainParameter() throws FlmAlgorithmException {
        final Execution executionMock = mock(Execution.class);

        final Map<String, String> actual = AdditionalParametersUtils.getAdditionalParametersForGlobalSettings(
                Collections.singletonMap("sectorIdParameter", String.valueOf(SECTOR_ID)));

        assertThat(actual).containsEntry("param.sector_id_parameter", String.valueOf(SECTOR_ID));

        verifyNoMoreInteractions(executionMock);
    }
}