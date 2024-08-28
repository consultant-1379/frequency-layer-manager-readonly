/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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

import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node.FeatureState;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.EnrichedProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.ProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.TestDataBuilder;

/**
 * Unit tests for {@link ChangeElementCalculatorImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeElementCalculatorImplTest {
    private static final String EXECUTION_ID_1 = "FLM_1";
    private static final String EXECUTION_ID_2 = "FLM_2";
    private static final String SOURCE_OF_CHANGE_1 = "alg_FLM_1";
    private static final String MODIFIED_BY_1 = "alg_FLM_1";
    private static final String SOURCE_OF_CHANGE_2 = "alg_FLM_2";
    private static final String MODIFIED_BY_2 = "alg_FLM_2";
    private static final String DEFAULT_SECTOR_ID = String.valueOf(1L);

    private static final FeatureState ALL_TRUE = new FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final TopologyObjectId C1 = new TopologyObjectId("cellFdn1", 1);
    private static final TopologyObjectId C2 = new TopologyObjectId("cellFdn2", 1);
    private static final TopologyObjectId C3 = new TopologyObjectId("cellFdn3", 1);
    private static final TopologyObjectId C4 = new TopologyObjectId("cellFdn4", 1);

    private static final Cell CELL1 =
            new Cell(1L, C1.getOssId(), C1.getFdn(), 1200, "idleModePrioAtReleaseFdn1", "cgi1", 10000, "outdoor", "undefined");
    private static final Cell CELL2 =
            new Cell(2L, C2.getOssId(), C2.getFdn(), 1200, "idleModePrioAtReleaseFdn2", "cgi2", 10000, "outdoor", "undefined");
    private static final Cell CELL3 =
            new Cell(3L, C3.getOssId(), C3.getFdn(), 1200, "idleModePrioAtReleaseFdn2", "cgi2", 10000, "outdoor", "undefined");
    private static final Cell CELL4 =
            new Cell(4L, C4.getOssId(), C4.getFdn(), 1200, "idleModePrioAtReleaseRef1", "cgi2", 10000, "outdoor", "undefined");

    private static final IdleModePrioAtRelease PROFILE1 = TestDataBuilder.getTestIdleModePrioAtRelease(1, "idleModePrioAtReleaseFdn1", 1,
            Arrays.asList("SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054968_I5_NOBEL_B,ManagedElement=054968_I5_NOBEL_B,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2325",
                    "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054968_I5_NOBEL_B,ManagedElement=054968_I5_NOBEL_B,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=5230"),
            Collections.emptySet());
    private static final IdleModePrioAtRelease PROFILE2 = TestDataBuilder.getTestIdleModePrioAtRelease(2, "idleModePrioAtReleaseFdn2", 1,
            Arrays.asList("SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054968_I5_NOBEL_B,ManagedElement=054968_I5_NOBEL_B,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=2325",
                    "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054968_I5_NOBEL_B,ManagedElement=054968_I5_NOBEL_B,ENodeBFunction=1,EUtraNetwork=1,EUtranFrequency=5230"),
            Collections.emptySet());

    private static final Node NODE1 = new Node(1L, "nodeFdn1", 1, ALL_TRUE, "ERBS");
    private static final Node NODE2 = new Node(2L, "nodeFdn2", 1, ALL_TRUE, "RadioNode");
    private static final String PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String PROPOSED = "PROPOSED";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void whenOneProfileChanged_thenChangeElementIsCreated() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);

        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges).addNode(C1, NODE1, 7).build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PENDING_APPROVAL);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(trim(result.proposedChangesAsJson())).isEqualTo(trim(ResourceLoader.loadResource("testProposedChangesOneChange.json")));
        softly.assertThat(result.getChangeId()).isEqualTo(DEFAULT_SECTOR_ID);
    }

    @Test
    public void whenProfileShared_thenChangeElementIsCreatedWithNoDeletionOfProfile() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_2, 2, false);

        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .reserveCellToProfile(CELL2, PROFILE1)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges)
                .addNode(C1, NODE1, 6)
                .build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_2);
        softly.assertThat(result.getStatus()).isEqualTo(PROPOSED);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_2);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_2);
        softly.assertThat(trim(result.proposedChangesAsJson())).isEqualTo(trim(ResourceLoader.loadResource("testProposedChangesSharedProfile.json")));
        softly.assertThat(result.getChangeId()).isEqualTo(DEFAULT_SECTOR_ID);
    }

    @Test
    public void whenProfileSharedAndNotEnoughProfiles_thenChangeElementIsNull() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);

        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .reserveCellToProfile(CELL2, PROFILE1)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges)
                .addNode(C1, NODE1, 7)
                .build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges);

        assertThat(result).isNull();
    }

    @Test
    public void whenMoreProfileChangesNotSharedOnSameNode_thenChangeElementIsCreated() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, false);

        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .addProfileChange(CELL2, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges)
                .addNode(C1, NODE1, 7)
                .addNode(C2, NODE1, 7)
                .build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PROPOSED);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(trim(result.proposedChangesAsJson())).isEqualTo(trim(ResourceLoader.loadResource("testProposedChangesMoreChanges.json")));
        softly.assertThat(result.getChangeId()).isEqualTo(DEFAULT_SECTOR_ID);
    }

    @Test
    public void whenMoreProfileChangesOnDifferentNodesShared_thenChangeElementIsCreated() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);

        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .reserveCellToProfile(CELL3, PROFILE1)
                .addProfileChange(CELL2, PROFILE2)
                .reserveCellToProfile(CELL4, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges)
                .addNode(C1, NODE1, 6)
                .addNode(C2, NODE2, 6)
                .build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PENDING_APPROVAL);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(trim(result.proposedChangesAsJson()))
                .isEqualTo(trim(ResourceLoader.loadResource("testProposedChangesMoreChangesShareDifferentNodes.json")));
        softly.assertThat(result.getChangeId()).isEqualTo(DEFAULT_SECTOR_ID);
    }

    @Test
    public void whenMultipleCallsOnSameNode_thenChangeElementIsCreated() {
        final ChangeElementCalculatorImpl objectUnderTest = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);

        final ProfileChanges profileChanges1 = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE1)
                .reserveCellToProfile(CELL2, PROFILE1)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges1 = new EnrichedProfileChangesBuilder(profileChanges1)
                .addNode(C1, NODE1, 6)
                .build();

        final ChangeElement result = objectUnderTest.calculateChangeElement(enrichedProfileChanges1);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PENDING_APPROVAL);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(trim(result.proposedChangesAsJson())).isEqualTo(trim(ResourceLoader.loadResource("testProposedChangesSharedProfile.json")));
        softly.assertThat(result.getChangeId()).isEqualTo(DEFAULT_SECTOR_ID);

        final ProfileChanges profileChanges2 = new ProfileChangesBuilder()
                .addProfileChange(CELL2, PROFILE2)
                .reserveCellToProfile(CELL3, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges2 = new EnrichedProfileChangesBuilder(profileChanges2)
                .addNode(C2, NODE1, 6)
                .build();

        final ChangeElement result2 = objectUnderTest.calculateChangeElement(enrichedProfileChanges2);

        assertThat(result2).isNull();
    }

    private String trim(final String text) {
        return text.replace("\r", "").replace("\n", "").replace(" ", "");
    }
}