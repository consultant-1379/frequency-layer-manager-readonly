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

package com.ericsson.oss.services.sonom.flm.database.optimization;

import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

import java.util.ArrayList;
import java.util.List;

public class TestDataBuilders {

    private static final String CELL_FDN_ONE = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-1";
    private static final String CELL_FDN_TWO = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-2";
    private static final String CELL_FDN_THREE = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-3";
    private static final String CELL_FDN_FOUR = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=netsim_LTE02ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE02ERBS00001-4";
    private static final int TARGET_CELL_OSSID = 1;
    private static final String TARGET_USERS_MOVE = "400";
    private static final int SOURCE_CELL_OSSID = 1;
    private static final String SOURCE_USERS_MOVE = "400";

    private TestDataBuilders() {

    }

    public static class ProposedLbqBuilder {
        private final String sourceCellFdn;
        private final int sourceCellOssId;
        private final String sourceUsersMove;
        private List<TargetCell> targetCells;

        public ProposedLbqBuilder(final String sourceCellFdn, final int sourceCellOssId, final String sourceUsersMove) {
            this.sourceCellFdn = sourceCellFdn;
            this.sourceCellOssId = sourceCellOssId;
            this.sourceUsersMove = sourceUsersMove;
        }

        public ProposedLbqBuilder withTargetCells(final List<TargetCell> targetCells) {
            this.targetCells = targetCells;
            return this;
        }

        public ProposedLoadBalancingQuanta build() {
            return new ProposedLoadBalancingQuanta(this.sourceCellFdn, this.sourceCellOssId,
                    this.sourceUsersMove, targetCells);
        }
    }

    public static List<TargetCell> buildSampleTargetCells() {
        final List<TargetCell> targetCells = new ArrayList<>();
        final TargetCell targetCellOne = new TargetCell(CELL_FDN_ONE, TARGET_CELL_OSSID, TARGET_USERS_MOVE);
        final TargetCell targetCellTwo = new TargetCell(CELL_FDN_TWO, TARGET_CELL_OSSID, TARGET_USERS_MOVE);
        final TargetCell targetCellThree = new TargetCell(CELL_FDN_THREE, TARGET_CELL_OSSID, TARGET_USERS_MOVE);
        final TargetCell targetCellFour = new TargetCell(CELL_FDN_FOUR, TARGET_CELL_OSSID, TARGET_USERS_MOVE);
        targetCells.add(targetCellOne);
        targetCells.add(targetCellTwo);
        targetCells.add(targetCellThree);
        targetCells.add(targetCellFour);
        return targetCells;
    }

    public static ProposedLoadBalancingQuanta buildSampleLoadBalancingQuanta(final List<TargetCell> targetCells) {
        return new ProposedLbqBuilder(CELL_FDN_ONE, SOURCE_CELL_OSSID, SOURCE_USERS_MOVE)
                .withTargetCells(targetCells)
                .build();
    }

    public static class PolicyOutputEventBuilder {

        private ProposedLoadBalancingQuanta lbq;
        private final Long sectorId;
        private final String executionId;
        private List<OptimizationCell> optimizationCells;
        private String name;
        private String nameSpace;
        private String source;
        private String target;
        private String version;

        public PolicyOutputEventBuilder(final String executionId, final Long sectorId) {
            this.sectorId = sectorId;
            this.executionId = executionId;
        }

        public PolicyOutputEventBuilder withOptimizationCells(final List<OptimizationCell> optimizationCells) {
            this.optimizationCells = optimizationCells;
            return this;
        }

        public PolicyOutputEventBuilder withLbq(final ProposedLoadBalancingQuanta lbq) {
            this.lbq = lbq;
            return this;
        }

        public PolicyOutputEventBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public PolicyOutputEventBuilder withNameSpace(final String nameSpace) {
            this.nameSpace = nameSpace;
            return this;
        }

        public PolicyOutputEventBuilder withSource(final String source) {
            this.source = source;
            return this;
        }

        public PolicyOutputEventBuilder withTarget(final String target) {
            this.target = target;
            return this;
        }

        public PolicyOutputEventBuilder withVersion(final String version) {
            this.version = version;
            return this;
        }

        public PolicyOutputEvent build() {
            final PolicyOutputEvent policyOutputEvent = new PolicyOutputEvent(lbq);
            policyOutputEvent.setExecutionId(executionId);
            policyOutputEvent.setSectorId(sectorId);
            policyOutputEvent.setOptimizationCells(optimizationCells);
            policyOutputEvent.setName(name);
            policyOutputEvent.setNameSpace(nameSpace);
            policyOutputEvent.setSource(source);
            policyOutputEvent.setTarget(target);
            policyOutputEvent.setVersion(version);
            return policyOutputEvent;
        }
    }
}
