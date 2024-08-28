/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;

/**
 * Unit tests for {@link ProfileChangesEnricher} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileChangesEnricherTest {
    private static final FeatureState ALL_TRUE = new FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final int OSS_ID = 1;
    @Mock
    CmNodeObjectsStore mockCmNodeObjectsStore;

    private ProfileChangesEnricher objectUnderTest;

    @Test
    public void whenNodeFoundForCellInStore_thenNodeIsFilledForEnrichedProfilesChanges() {
        final Map<TopologyObjectId, Cell> cells = new HashMap<>();
        final TopologyObjectId cellId = TopologyObjectId.of("cellFdn", OSS_ID);
        cells.put(cellId, new Cell(1L, OSS_ID, "cellFdn", 1400, "output", "undefined"));
        objectUnderTest = new ProfileChangesEnricher(mockCmNodeObjectsStore);

        when(mockCmNodeObjectsStore.getNodeForCellFdn("cellFdn", 1))
                .thenReturn(new Node(1L, "nodeFdn", OSS_ID, ALL_TRUE, "ERBS"));
        when(mockCmNodeObjectsStore.getNumberOfProfilesUsedByNode("nodeFdn", 1)).thenReturn(4);

        final EnrichedProfileChanges result = objectUnderTest.enrich(new ProfileChanges("FLM_2_EXEC_ID", 1L, 100, Collections.emptyMap(), cells));
        assertThat(result.getNodeForCell(cellId)).isEqualTo(new Node(1L, "nodeFdn", OSS_ID, ALL_TRUE, "ERBS"));
        assertThat(result.getProfilesUsed(TopologyObjectId.of("nodeFdn", OSS_ID))).isEqualTo(4);
    }

    @Test
    public void whenNodeNotFoundForCellInStore_thenNodeIsNullInEnrichedProfilesChanges() {
        final Map<TopologyObjectId, Cell> cells = new HashMap<>();
        final TopologyObjectId cellId = TopologyObjectId.of("fdn1", OSS_ID);
        cells.put(cellId, new Cell(1L, OSS_ID, "fdn1", 1400, "output", "undefined"));
        objectUnderTest = new ProfileChangesEnricher(mockCmNodeObjectsStore);

        when(mockCmNodeObjectsStore.getNodeForCellFdn("fdn1", 1)).thenReturn(null);

        final EnrichedProfileChanges result = objectUnderTest.enrich(new ProfileChanges("FLM_2_EXEC_ID", 1L, 100, Collections.emptyMap(), cells));
        assertThat(result.getNodeForCell(cellId)).isNull();
    }
}