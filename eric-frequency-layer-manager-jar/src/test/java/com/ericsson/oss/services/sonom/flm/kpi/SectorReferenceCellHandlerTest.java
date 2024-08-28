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

package com.ericsson.oss.services.sonom.flm.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.KpiSectorDao;

/**
 * Unit test for {@link SectorReferenceCellHandler} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class SectorReferenceCellHandlerTest {

    @Mock
    private CmStore cmStoreMock;

    @Mock
    private CmSectorCellStore cmSectorCellStore;

    @Mock
    private KpiSectorDao kpiSectorDaoMock;

    private SectorReferenceCellHandler objectUnderTest;
    private Collection<Cell> allCellsInCm;

    @Before
    public void setUp() {
        allCellsInCm = new ArrayList<>();
        objectUnderTest = new SectorReferenceCellHandler(kpiSectorDaoMock, cmStoreMock);
    }

    @Test
    public void whenThereAreTwoSectors_butOnlyOneHasAReferenceCell_thenOneSectorWithoutAReferenceCellShouldBeFound() throws Exception {
        final Integer[] cellsToCreate = { 2, 3 };
        createDataForTest(2, cellsToCreate);
        final Set<Long> sectorIdsRequiringReferenceCellRecalculation = objectUnderTest
                .getSectorIdsRequiringReferenceCellRecalculation();
        assertThat(sectorIdsRequiringReferenceCellRecalculation).containsExactly(2L);
    }

    @Test
    public void whenThereAreThreeSectors_butOnlyOneHasAReferenceCell_thenTwoSectorWithoutAReferenceCellShouldBeFound() throws Exception {
        final Integer[] cellsToCreate = { 2, 2, 1 };
        createDataForTest(3, cellsToCreate);
        final Set<Long> sectorIdsRequiringReferenceCellRecalculation = objectUnderTest
                .getSectorIdsRequiringReferenceCellRecalculation();
        assertThat(sectorIdsRequiringReferenceCellRecalculation).containsExactly(2L, 3L);
    }

    private void createDataForTest(final Integer cmSectorsToCreate, final Integer[] cmCellsToCreate) throws Exception {
        final Collection<TopologySector> allSectorsInCm = createCmSectors(cmSectorsToCreate, cmCellsToCreate);
        final Set<Long> sectorIdsWithRefCellInKpiService = Collections.singleton(1L);
        given(cmStoreMock.getCmSectorCellStore()).willReturn(cmSectorCellStore);
        given(cmSectorCellStore.getFullSectors()).willReturn(allSectorsInCm);
        given(cmSectorCellStore.getAllMediatedCells()).willReturn(allCellsInCm);
        given(kpiSectorDaoMock.getKpiSectorIdsWithRefCell()).willReturn(sectorIdsWithRefCellInKpiService);
    }

    private Collection<TopologySector> createCmSectors(final Integer cmSectorsToCreate, final Integer[] cmCellsToCreate) {
        final Collection<TopologySector> allSectorsInCm = new ArrayList<>();
        for (long i = 1; i <= cmSectorsToCreate; i++) {
            allSectorsInCm.add(new TopologySector(i, createCmCells(cmCellsToCreate[(int) i - 1])));
        }
        return allSectorsInCm;
    }

    private List<Cell> createCmCells(final Integer cmCellsToCreate) {
        final List<Cell> associatedCellsInCm = new ArrayList<>();
        for (long i = 1; i <= cmCellsToCreate; i++) {
            associatedCellsInCm.add(new Cell(i, 1, null, 1400, "outdoor", "undefined"));
        }
        this.allCellsInCm.addAll(associatedCellsInCm);
        return associatedCellsInCm;
    }
}
