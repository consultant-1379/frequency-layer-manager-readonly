/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;

/**
 * Unit tests for {@link PolicyOutputEvent} class.
 */
public class PolicyOutputEventTest {
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String NAME_SPACE = "nameSpace";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final long SECTOR_ID = 1L;
    private static final String EXECUTION_ID = "1";

    private final ProposedLoadBalancingQuanta proposedLoadBalancingQuanta = new ProposedLoadBalancingQuanta("sourceCellFdn", 1, "100");
    private final List<OptimizationCell> optimizationCells = new ArrayList<>();

    private PolicyOutputEvent objectUnderTest;

    @Test
    public void whenPolicyOutputCreatedCorrectly_thenToStringIsOK() {
        objectUnderTest =
                new PolicyOutputEvent(NAME, VERSION, NAME_SPACE, SOURCE, TARGET, SECTOR_ID, EXECUTION_ID, proposedLoadBalancingQuanta, optimizationCells);
        assertThat(objectUnderTest.toString())
                .isEqualTo("PolicyOutputEvent:: {name: 'name',version: 'version', nameSpace: 'nameSpace', source: 'source', target: 'target', " +
                        "sectorId: '1', executionId: '1', loadBalancingQuanta: 'ProposedLoadBalancingQuanta:: {sourceCellFdn: 'sourceCellFdn', " +
                        "sourceCellOssId: 1, sourceUsersMove: '100', targetCells: '[]'}', optimizationCells: '[]'}");
    }

    @Test
    public void whenPolicyOutputCreatedCorrectlyWithLBQConstructor_thenToStringIsOK() {
        objectUnderTest = new PolicyOutputEvent(proposedLoadBalancingQuanta);
        assertThat(objectUnderTest.toString()).contains(proposedLoadBalancingQuanta.toString());
    }

    @Test
    public void whenPolicyOutputEventCreatedWithNullOptimizationCells_thenToStringIsOK() {
        objectUnderTest = new PolicyOutputEvent(NAME, VERSION, NAME_SPACE, SOURCE, TARGET, SECTOR_ID, EXECUTION_ID, proposedLoadBalancingQuanta, null);
        assertThat(objectUnderTest.toString()).contains("optimizationCells: '[]'");
    }

    @Test
    public void whenPolicyOutputEventCreatedWithNullLBQ_thenToStringIsOK() {
        objectUnderTest = new PolicyOutputEvent(NAME, VERSION, NAME_SPACE, SOURCE, TARGET, SECTOR_ID, EXECUTION_ID, null, optimizationCells);
        assertThat(objectUnderTest.toString()).contains("loadBalancingQuanta: 'null'");
    }
}
