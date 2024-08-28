/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDao;

/**
 * Unit tests for {@link SectorsWithRefCellDeletedHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SectorsWithRefCellDeletedHandlerTest {

    private static final String FDN = "fdn";

    private SectorsWithRefCellDeletedHandler objectUnderTest;

    @Mock
    private CmSectorCellStore cmSectorCellStore;

    @Mock
    private KpiSectorDao kpiSectorDaoMock;

    @Before
    public void setUp() {
        objectUnderTest = new SectorsWithRefCellDeletedHandler(cmSectorCellStore, kpiSectorDaoMock);
    }

    @Test
    public void whenAllReferenceCellsFoundInCM_thenEmptyListReturned() throws SQLException {
        createDataForTest(0, 0);
        assertThat(objectUnderTest.find()).isEmpty();
    }

    @Test
    public void whenAllReferenceCellsFoundInCm_thenAnEmptyListReturned() throws SQLException {
        createDataForTest(5, 5);
        assertThat(objectUnderTest.find()).isEmpty();
    }

    @Test
    public void whenCellNotFoundInCm_thenSectorIncludedToHaveRefCellRecalculated() throws SQLException {
        createDataForTest(3, 5);
        assertThat(objectUnderTest.find()).containsExactlyInAnyOrder(4L, 5L);
    }

    private void createDataForTest(final Integer cmSectorsToCreate, final Integer sectorIdsToCreate) throws SQLException {
        final Collection<Cell> allSectorsInCm = createCmCells(cmSectorsToCreate);
        given(cmSectorCellStore.getAllMediatedCells()).willReturn(allSectorsInCm);

        final Map<Long, String> sectorIdsAndRefCell = new HashMap<>(sectorIdsToCreate);
        for (long i = 1; i <= sectorIdsToCreate; i++) {
            sectorIdsAndRefCell.put(i, FDN + i);
        }

        given(kpiSectorDaoMock.getSectorIdsAndRefCell()).willReturn(sectorIdsAndRefCell);
    }

    private static List<Cell> createCmCells(final Integer cmCellsToCreate) {
        final List<Cell> allCellsInCm = new ArrayList<>();
        for (long i = 1; i <= cmCellsToCreate; i++) {
            allCellsInCm.add(new Cell(i, 1, FDN + i, 1400, "outdoor", "undefined"));
        }
        return allCellsInCm;
    }
}