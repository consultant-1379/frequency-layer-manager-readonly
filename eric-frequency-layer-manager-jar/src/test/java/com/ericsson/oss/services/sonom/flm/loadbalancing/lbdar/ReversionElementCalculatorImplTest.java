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
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ProposedChange;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarReversionException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.EnrichedProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.ProfileChangesBuilder;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.TestDataBuilder;

/**
 * Unit tests for {@link ReversionElementCalculatorImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReversionElementCalculatorImplTest {
    private static final String EXECUTION_ID_1 = "FLM_1";
    private static final String SOURCE_OF_CHANGE_1 = "alg_FLM_1";
    private static final String MODIFIED_BY_1 = "alg_FLM_1";
    private static final String CHANGE_ID = "123";
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String MODIFY = "MODIFY";

    private static final Node.FeatureState ALL_TRUE = new Node.FeatureState(true, true, true, true, "ACTIVATED", true);
    private static final TopologyObjectId C1 = new TopologyObjectId("cellFdn1", 1);

    private static final Cell CELL1 = new Cell(1L, C1.getOssId(), C1.getFdn(), 1200, "nodeFdn2,cgi2", "cgi1", 10000,
            "outdoor", "undefined");

    private static final IdleModePrioAtRelease profile1 = TestDataBuilder.getTestIdleModePrioAtRelease(1, "nodeFdn1,cgi1", 1,
            Arrays.asList("freqFdn1", "freqFdn2"),
            Collections.singleton("cell1"));
    private static final IdleModePrioAtRelease PROFILE2 = TestDataBuilder.getTestIdleModePrioAtRelease(2, "nodeFdn2,cgi2", 1,
            Arrays.asList("freqFdn1", "freqFdn2"),
            Collections.emptySet());

    private static final Node NODE1 = new Node(1L, "nodeFdn1", 1, ALL_TRUE, "ERBS");
    private static final String PENDING_APPROVAL = "PENDING_APPROVAL";

    @Mock
    CmNodeObjectsStore mockCmNodeObjectsStore;

    @Mock
    CmSectorCellStore mockCmSectorCellStore;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenCalculateReversionElementCalledOpenLoop_thenChangeElementIsCreated() throws LbdarReversionException {
        final ReversionElementCalculatorImpl objectUnderTest = new ReversionElementCalculatorImpl(mockCmSectorCellStore, mockCmNodeObjectsStore);
        final ChangeElementCalculatorImpl changeElementCalculator = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);
        Mockito.when(mockCmNodeObjectsStore.getIdleModePrioAtRelease(PROFILE2.getFdn(), CELL1.getOssId())).thenReturn(PROFILE2);
        Mockito.when(mockCmSectorCellStore.getCellForCellFdn(CELL1.getFdn(), CELL1.getOssId())).thenReturn(CELL1);
        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges).addNode(C1, NODE1, 7).build();
        final ChangeElement optmization = changeElementCalculator.calculateChangeElement(enrichedProfileChanges);
        optmization.setChangeId(CHANGE_ID);
        final ChangeElement result = objectUnderTest.calculateReversionElement(optmization);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PENDING_APPROVAL);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(result.getChangeId()).isEqualTo(CHANGE_ID);
        softly.assertThat(result.getChangeType()).isEqualTo(ChangeElement.ChangeType.REVERSION);
        result.getProposedChanges().forEach(this::verifyProposedChanges);
    }

    @Test
    public void whenCalculateReversionElementCalledClosedLoop_thenChangeElementIsCreated() throws LbdarReversionException {
        final ReversionElementCalculatorImpl objectUnderTest = new ReversionElementCalculatorImpl(mockCmSectorCellStore, mockCmNodeObjectsStore);
        final ChangeElementCalculatorImpl changeElementCalculator = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, false);
        Mockito.when(mockCmNodeObjectsStore.getIdleModePrioAtRelease(PROFILE2.getFdn(), CELL1.getOssId())).thenReturn(PROFILE2);
        Mockito.when(mockCmSectorCellStore.getCellForCellFdn(CELL1.getFdn(), CELL1.getOssId())).thenReturn(CELL1);
        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges).addNode(C1, NODE1, 7).build();
        final ChangeElement optmization = changeElementCalculator.calculateChangeElement(enrichedProfileChanges);
        optmization.setChangeId(CHANGE_ID);
        final ChangeElement result = objectUnderTest.calculateReversionElement(optmization);

        assertThat(result).isNotNull();
        softly.assertThat(result.getExecutionId()).isEqualTo(EXECUTION_ID_1);
        softly.assertThat(result.getStatus()).isEqualTo(PENDING_APPROVAL);
        softly.assertThat(result.getSourceOfChange()).isEqualTo(SOURCE_OF_CHANGE_1);
        softly.assertThat(result.getModifiedBy()).isEqualTo(MODIFIED_BY_1);
        softly.assertThat(result.getChangeId()).isEqualTo(CHANGE_ID);
        softly.assertThat(result.getChangeType()).isEqualTo(ChangeElement.ChangeType.REVERSION);
        result.getProposedChanges().forEach(this::verifyProposedChanges);
    }

    @Test
    public void whenCalculateReversionElementCalled_AndIdleModePrioAtReleaseIsNotFound_thenLbdarExceptionIsThrown() throws LbdarReversionException {
        final ReversionElementCalculatorImpl objectUnderTest = new ReversionElementCalculatorImpl(mockCmSectorCellStore, mockCmNodeObjectsStore);
        final ChangeElementCalculatorImpl changeElementCalculator = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);
        Mockito.when(mockCmNodeObjectsStore.getIdleModePrioAtRelease(PROFILE2.getFdn(), CELL1.getOssId())).thenReturn(null);
        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges).addNode(C1, NODE1, 7).build();
        final ChangeElement optmization = changeElementCalculator.calculateChangeElement(enrichedProfileChanges);
        thrown.expect(LbdarReversionException.class);
        thrown.expectMessage("Failed to get IdleModePrioAtRelease for Fdn:nodeFdn2,cgi2, OssId:1");
        objectUnderTest.calculateReversionElement(optmization);
    }

    @Test
    public void whenCalculateReversionElementCalled_AndCellIsNotFound_thenLbdarExceptionIsThrown() throws LbdarReversionException {
        final ReversionElementCalculatorImpl objectUnderTest = new ReversionElementCalculatorImpl(mockCmSectorCellStore, mockCmNodeObjectsStore);
        final ChangeElementCalculatorImpl changeElementCalculator = new ChangeElementCalculatorImpl(EXECUTION_ID_1, 1, true);
        Mockito.when(mockCmNodeObjectsStore.getIdleModePrioAtRelease(PROFILE2.getFdn(), CELL1.getOssId())).thenReturn(PROFILE2);
        Mockito.when(mockCmSectorCellStore.getCellForCellFdn(CELL1.getFdn(), CELL1.getOssId())).thenReturn(null);
        final ProfileChanges profileChanges = new ProfileChangesBuilder()
                .addProfileChange(CELL1, PROFILE2)
                .build();
        final EnrichedProfileChanges enrichedProfileChanges = new EnrichedProfileChangesBuilder(profileChanges).addNode(C1, NODE1, 7).build();
        final ChangeElement optmization = changeElementCalculator.calculateChangeElement(enrichedProfileChanges);
        thrown.expect(LbdarReversionException.class);
        thrown.expectMessage("Failed to get Cell for Fdn:cellFdn1, OssId:1");
        objectUnderTest.calculateReversionElement(optmization);
    }

    private void verifyProposedChanges(final ProposedChange proposedChange) {
        switch (proposedChange.getOperationType()) {
            case DELETE:
                softly.assertThat(proposedChange.getFdn()).as("Verify deleted profile fdn: '%s' is correct", proposedChange.getFdn()).isEqualTo(profile1.getFdn());
                break;
            case MODIFY:
                final String newProfileFdn = proposedChange.getParameterChanges().get(0).getParameterValue();
                softly.assertThat(proposedChange.getFdn()).as("Verify modified cell fdn: '%s' is correct", proposedChange.getFdn()).isEqualTo(CELL1.getFdn());
                softly.assertThat(newProfileFdn).as("Verify new profile fdn '%s' assigned to cell is correct", newProfileFdn).isEqualTo(PROFILE2.getFdn());
                break;
            case CREATE:
                softly.assertThat(proposedChange.getFdn()).as("Verify created profile fdn: '%s' is correct", proposedChange.getFdn()).isEqualTo(PROFILE2.getFdn());
                break;
            default:
                fail("Invalid OperationType assigned for the proposed change.");
                break;
        }
    }
}