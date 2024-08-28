/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStoreImplTestUtils.BusyHourCellKpiListBuilder;
import com.ericsson.oss.services.sonom.flm.kpi.store.CellKpiStoreImplTestUtils.SectorBusyHourListBuilder;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellFlmKpiRetriever;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.SectorBusyHourRetriever;

@RunWith(MockitoJUnitRunner.class)
public class CellKpiStoreImplTest {

    private static final String CELL_FDN_ONE = "cellFdn_1_1";
    /*
    This is a date which only can be a weekday.
    Weekend day definition comes from the execution.
     */
    private static final String BUSY_HOUR_DATE = "2020-12-30";
    private static final List<String> BUSY_HOURS = Arrays.asList(
            "2020-12-29 15:00:00.0",
            "2020-12-29 16:00:00.0",
            "2020-12-29 17:00:00.0",
            "2020-12-29 18:00:00.0");
    private static final Collection<TopologySector> SECTOR_LIST = new CellKpiStoreImplTestUtils.SectorListBuilder()
            .withSectorNumber(4)
            .withCellNumberPerSector(4)
            .build();
    private static final int ONCE = 1;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private SectorBusyHourRetriever sectorBusyHourRetrieverMock;

    @Mock
    private CellFlmKpiRetriever cellKpiRetrieverMock;

    @Test
    public void whenBusyHourKpisRetrieved_thenCellKpisReturned() throws SQLException {
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST))
                .thenReturn(new SectorBusyHourListBuilder(BUSY_HOURS).build());
        final Map<CellKpi, CellKpis> busyHourCellKpis = new BusyHourCellKpiListBuilder(BUSY_HOURS).build();
        when(cellKpiRetrieverMock.retrieveNotVisibleCellHourlyKpis(anyString())).thenReturn(busyHourCellKpis);

        final CellKpiStoreImpl objectUnderTest = new CellKpiStoreImpl(sectorBusyHourRetrieverMock, cellKpiRetrieverMock, BUSY_HOUR_DATE, SECTOR_LIST);
        final CellKpis busyHourCellKpisForACell = objectUnderTest.getKpisForCell(CELL_FDN_ONE, 1);

        final CellKpi cellKpiToVerify = new CellKpi(CELL_FDN_ONE, 1, "2020-12-29 15:00:00.0");
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getConnectedUsers())
                .isEqualTo(busyHourCellKpisForACell.getConnectedUsers());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getSubscriptionRatio())
                .isEqualTo(busyHourCellKpisForACell.getSubscriptionRatio());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getPmIdleModeRelDistrHighLoad())
                .isEqualTo(busyHourCellKpisForACell.getPmIdleModeRelDistrHighLoad());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getPmIdleModeRelDistrMediumHighLoad())
                .isEqualTo(busyHourCellKpisForACell.getPmIdleModeRelDistrMediumHighLoad());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getPmIdleModeRelDistrMediumLoad())
                .isEqualTo(busyHourCellKpisForACell.getPmIdleModeRelDistrMediumLoad());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getPmIdleModeRelDistrLowMediumLoad())
                .isEqualTo(busyHourCellKpisForACell.getPmIdleModeRelDistrLowMediumLoad());
        softly.assertThat(busyHourCellKpis.get(cellKpiToVerify).getPmIdleModeRelDistrLowLoad())
                .isEqualTo(busyHourCellKpisForACell.getPmIdleModeRelDistrLowLoad());

        verify(sectorBusyHourRetrieverMock, times(ONCE)).populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST);
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T15:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T16:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T17:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T18:00:00");
    }

    @Test
    public void whenNotAllBusyHourKpisRetrieved_thenNullCellKpisReturned() throws SQLException {
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST))
                .thenReturn(new SectorBusyHourListBuilder(BUSY_HOURS).build());
        when(cellKpiRetrieverMock.retrieveNotVisibleCellHourlyKpis(anyString()))
                .thenReturn(new BusyHourCellKpiListBuilder(BUSY_HOURS).withKpis(generateStaticKpisWithInvalidValues())
                                                                      .build());

        final CellKpiStoreImpl objectUnderTest = new CellKpiStoreImpl(sectorBusyHourRetrieverMock, cellKpiRetrieverMock, BUSY_HOUR_DATE, SECTOR_LIST);
        final CellKpis busyHourCellKpisForACell = objectUnderTest.getKpisForCell(CELL_FDN_ONE, 1);

        assertThat(busyHourCellKpisForACell).isNull();

        verify(sectorBusyHourRetrieverMock, times(ONCE)).populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST);
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T15:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T16:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T17:00:00");
        verify(cellKpiRetrieverMock, times(ONCE)).retrieveNotVisibleCellHourlyKpis("2020-12-29T18:00:00");
    }

    @Test
    public void whenBusyHourKpisRetrievedAndIncorrectInputProvided_thenNullReturned() throws SQLException {
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST))
                .thenReturn(new SectorBusyHourListBuilder(BUSY_HOURS).build());
        when(cellKpiRetrieverMock.retrieveNotVisibleCellHourlyKpis(anyString()))
                .thenReturn(new BusyHourCellKpiListBuilder(BUSY_HOURS).build());

        final CellKpiStoreImpl objectUnderTest = new CellKpiStoreImpl(sectorBusyHourRetrieverMock, cellKpiRetrieverMock, BUSY_HOUR_DATE, SECTOR_LIST);

        // Non-existent fdn:
        softly.assertThat(objectUnderTest.getKpisForCell("dummyCellFdn", 1)).isNull();
        // Null fdn:
        softly.assertThat(objectUnderTest.getKpisForCell(null, 1)).isNull();
        // Non-existent ossId:
        softly.assertThat(objectUnderTest.getKpisForCell(CELL_FDN_ONE, 2)).isNull();
    }

    @Test
    public void whenBusyHourRetrieverThrowsException_thenExceptionIsThrown() throws SQLException {
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST)).thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        new CellKpiStoreImpl(sectorBusyHourRetrieverMock, cellKpiRetrieverMock, BUSY_HOUR_DATE, SECTOR_LIST);
    }

    @Test
    public void whenCellKpiRetrieverThrowsException_thenExceptionIsThrown() throws SQLException {
        when(sectorBusyHourRetrieverMock.populateSectorIdToBusyHour(BUSY_HOUR_DATE, SECTOR_LIST))
                .thenReturn(new SectorBusyHourListBuilder(BUSY_HOURS).build());
        when(cellKpiRetrieverMock.retrieveNotVisibleCellHourlyKpis(anyString())).thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        new CellKpiStoreImpl(sectorBusyHourRetrieverMock, cellKpiRetrieverMock, BUSY_HOUR_DATE, SECTOR_LIST);
    }

    private CellKpis generateStaticKpisWithInvalidValues() {
        return new CellKpis(-1, 100.0, -1,
                250, 300,
                150, -1);
    }
}