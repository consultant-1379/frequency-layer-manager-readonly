/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.optimization.kpi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.optimization.testutils.TopologyBuilder;

/**
 * Unit tests for {@link TopologyIdentifierUtils} class.
 */
public class TopologyIdentifierUtilsTest {

    private static final String SECTOR_ID_ONE = "1";
    private static final String SECTOR_ID_TWO = "2";

    @Test
    public void whenTopologySectorsPassedToSectorIdBuilder_andSectorsArePresent_CorrectSectorIdListIsReturned() {
        assertThat(TopologyIdentifierUtils.getAllSectorIdsAsList(
                TopologyBuilder.buildListOfTopologySectors(TopologyBuilder.buildAssociatedCellsForSector())))
                .containsExactly(SECTOR_ID_ONE, SECTOR_ID_TWO);
    }
}
