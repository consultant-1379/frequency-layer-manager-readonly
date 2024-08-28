/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.optimization;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.optimization.testutils.TopologyBuilder.buildAssociatedCellsForSector;
import static com.ericsson.oss.services.sonom.flm.optimization.testutils.TopologyBuilder.buildCellKpis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.optimization.testutils.TopologyBuilder;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link PolicyInputEventGenerator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PolicyInputEventGeneratorTest {

    private static final String EXECUTION_ID = "FLMExecution1";
    private final Map<String, Object> kpis = new HashMap<>(2);
    private final Map<CellIdentifier, Map<String, String>> cellSettings = new LinkedHashMap<>();

    private PolicyInputEventGenerator objectUnderTest;

    @Mock
    private CmStore mockCmStore;

    @Mock
    private CmNodeObjectsStore mockedCmNodeObjectsStore;


    @Before
    public void setUp() {
        kpis.put("kpiOne", 1.0);
        kpis.put("kpiTwo", 2.0);
    }

    @Test
    public void whenKpisExistForCell_thenPolicyInputEventIsGeneratedWithTheseKpis() throws FlmAlgorithmException {
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true, false, true, false, "ACTIVATED", false), "nodetype"));
        final List<Cell> cells = buildAssociatedCellsForSector();
        final TopologySector sector = TopologyBuilder.buildTopologySector(1L, cells);
        final Map<CellIdentifier, Set<Long>> multiSectorCells = new HashMap<>();
        objectUnderTest = new PolicyInputEventGenerator(buildCellKpis(cells, kpis), cellSettings,
                multiSectorCells,mockCmStore);
        assertThat(objectUnderTest.generateInputEvent(sector, EXECUTION_ID)).isEqualTo(TopologyBuilder.buildInputEvent(EXECUTION_ID, kpis, "1"));
    }

    @Test
    public void whenKpisDoNotExistForCell_thenPolicyInputEventIsGeneratedWithAnEmptyKpis() throws FlmAlgorithmException {
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true, false, true, false, "ACTIVATED", false), "nodetype"));

        final List<Cell> cells = buildAssociatedCellsForSector();
        final TopologySector sector = TopologyBuilder.buildTopologySector(1L, cells);
        final Map<CellIdentifier, Set<Long>> multiSectorCells = new HashMap<>();
        objectUnderTest = new PolicyInputEventGenerator(Collections.emptyMap(), cellSettings, multiSectorCells, mockCmStore);
        assertThat(objectUnderTest.generateInputEvent(sector, EXECUTION_ID)).isEqualTo(TopologyBuilder.buildInputEvent(EXECUTION_ID,
                Collections.emptyMap(), "1"));
    }

    @Test
    public void whenNoCellsExistInSector_PolicyInputEventIsBuiltWithEmptyCells() {
        final TopologySector sector = TopologyBuilder.buildTopologySector(1L, new ArrayList<>());
        final Map<CellIdentifier, Set<Long>> multiSectorCells = new HashMap<>();
        objectUnderTest = new PolicyInputEventGenerator(Collections.emptyMap(), cellSettings, multiSectorCells, mockCmStore);
        assertThat(objectUnderTest.generateInputEvent(sector, EXECUTION_ID)).isEqualTo(TopologyBuilder.buildInputEventWithoutCells(EXECUTION_ID));
    }


    @Test
    public void whenExclusionListIsPopulated_thenPolicyInputEventIsGeneratedWithNonExcludedCells() throws FlmAlgorithmException {
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true, false, true, false, "ACTIVATED", false), "nodetype"));
        final List<Cell> cells = buildAssociatedCellsForSector();
        final TopologySector sector = TopologyBuilder.buildTopologySector(1L, cells);
        final Cell cellTwo = cells.get(1);
        final Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put(EXCLUSION_LIST, "test_group1");
        cellSettings.put(new CellIdentifier(cellTwo.getOssId(), cellTwo.getFdn()), settingsMap);

        final Map<CellIdentifier, Set<Long>> multiSectorCells = new HashMap<>();
        objectUnderTest = new PolicyInputEventGenerator(buildCellKpis(cells, kpis), cellSettings, multiSectorCells, mockCmStore);
        assertThat(objectUnderTest.generateInputEvent(sector, EXECUTION_ID))
                .isEqualTo(TopologyBuilder.buildInputEventWithOneCell(EXECUTION_ID, kpis, "1"));
    }

    @Test
    public void whenSectorHasMultiSectorCells_thenPolicyInputEventIsGeneratedWithNonMultiSectorCells() throws FlmAlgorithmException {
        when(mockCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true, false, true, false, "ACTIVATED", false), "nodetype"));
        final List<Cell> cells = buildAssociatedCellsForSector();
        final TopologySector sector = TopologyBuilder.buildTopologySector(1L, cells);
        final Map<CellIdentifier, Set<Long>> multiSectorCells = new HashMap<>();
        multiSectorCells.put(new CellIdentifier(1, "fdnTwo"), new HashSet<>(1));
        objectUnderTest = new PolicyInputEventGenerator(buildCellKpis(cells, kpis), new HashMap<>(), multiSectorCells, mockCmStore);
        assertThat(objectUnderTest.generateInputEvent(sector, EXECUTION_ID))
                .isEqualTo(TopologyBuilder.buildInputEventWithOneCell(EXECUTION_ID, kpis, "1"));
    }
}
