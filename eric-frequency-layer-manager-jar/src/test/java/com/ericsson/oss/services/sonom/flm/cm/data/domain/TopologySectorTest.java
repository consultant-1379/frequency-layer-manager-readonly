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

package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Unit tests for {@link TopologySector} class.
 */
public class TopologySectorTest {

    private static final String LTE_NR_SPECTRUM_SHARED = "undefined";
    private static final int OSS_ID = 1;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final Cell OUTDOOR_CELL = new Cell(1L, OSS_ID, "fdn1", null, "outdoor", LTE_NR_SPECTRUM_SHARED);
    private static final Cell OUTDOOR_INDOOR_CELL = new Cell(2L, OSS_ID, "fdn2", null, "outdoor_indoor", LTE_NR_SPECTRUM_SHARED);
    private static final Cell INDOOR_CELL = new Cell(3L, OSS_ID, "fdn3", null, "indoor", LTE_NR_SPECTRUM_SHARED);

    @Test
    public void whenTopologySectorIsCreated_IndoorCellsAreExcludedInOutdoorCellsCollection() {
        final TopologySector objectUnderTest = new TopologySector(12L, Arrays.asList(OUTDOOR_CELL, OUTDOOR_INDOOR_CELL, INDOOR_CELL));
        assertThat(objectUnderTest.getAssociatedCells()).containsExactlyInAnyOrder(OUTDOOR_CELL, OUTDOOR_INDOOR_CELL, INDOOR_CELL);
        assertThat(objectUnderTest.getAssociatedOutdoorCells()).containsExactlyInAnyOrder(OUTDOOR_CELL, OUTDOOR_INDOOR_CELL);
    }

    @Test
    public void whenNewInstanceIsCalled_thenShallowCopyIsReturned() {
        final TopologySector original = sectorWithThreeCells();
        final TopologySector shallowCopy = TopologySector.newInstance(original);

        softly.assertThat(original).isEqualTo(shallowCopy);

        softly.assertThat(original.getAssociatedCells()).isNotSameAs(shallowCopy.getAssociatedCells());
        softly.assertThat(original.getAssociatedOutdoorCells()).isNotSameAs(shallowCopy.getAssociatedOutdoorCells());

        softly.assertThat(original.getAssociatedCells()).containsExactlyInAnyOrder(Iterables.toArray(shallowCopy.getAssociatedCells(), Cell.class));
        softly.assertThat(original.getAssociatedOutdoorCells()).containsExactlyInAnyOrder(Iterables.toArray(shallowCopy.getAssociatedOutdoorCells(), Cell.class));

        //  Checking shallow-copy`s arrays are same by reference ensuring that underlying Cells were not re-instantiated
        final Map<Long, Cell> originalAssociatedCells = original.getAssociatedCells()
                                                                .stream()
                                                                .collect(Collectors.toMap(Cell::getCellId, Function.identity()));
        final Map<Long, Cell> originalAssociatedOutdoorCells = original.getAssociatedOutdoorCells()
                                                                       .stream()
                                                                       .collect(Collectors.toMap(Cell::getCellId, Function.identity()));

        shallowCopy.getAssociatedCells()
                   .forEach(cell -> softly.assertThat(originalAssociatedCells.get(cell.getCellId())).isNotNull()
                                                                                             .isSameAs(cell));
        shallowCopy.getAssociatedOutdoorCells()
                   .forEach(cell -> softly.assertThat(originalAssociatedOutdoorCells.get(cell.getCellId())).isNotNull()
                                                                                                    .isSameAs(cell));
    }

    @Test
    public void whenInclusionListIsApplied() {
        final TopologySector topologySector = sectorWithThreeCells();

        final Cell cell = new Cell(1L, OSS_ID, "fdn1", 1400, "outdoor", LTE_NR_SPECTRUM_SHARED);
        topologySector.applyInclusionList(Collections.singletonList(cell));

        assertThat(topologySector.getAssociatedCells()).containsExactly(cell);
        assertThat(topologySector.getAssociatedOutdoorCells()).containsExactly(cell);
    }

    @Test
    public void whenAssociatedCellsAreEmpty_thenTrueIsReturned() {
        final TopologySector objectUnderTest = new TopologySector(10L, Collections.emptyList());

        assertThat(objectUnderTest.isAssociatedCellsEmpty()).isTrue();
    }

    @Test
    public void whenAssociatedCellsAreNotEmpty_thenFalseIsReturned() {
        final TopologySector objectUnderTest = sectorWithThreeCells();

        assertThat(objectUnderTest.isAssociatedCellsEmpty()).isFalse();
    }

    private TopologySector sectorWithThreeCells() {
        final List<Cell> cellList = new ArrayList<>(3);
        cellList.add(new Cell(1L, OSS_ID, "fdn1", 1400, "outdoor", LTE_NR_SPECTRUM_SHARED));
        cellList.add(new Cell(2L, OSS_ID, "fdn2", 1400, "outdoor_indoor", LTE_NR_SPECTRUM_SHARED));
        cellList.add(new Cell(3L, OSS_ID, "fdn3", 1400, "indoor", LTE_NR_SPECTRUM_SHARED));
        return new TopologySector(12L, cellList);
    }

}
