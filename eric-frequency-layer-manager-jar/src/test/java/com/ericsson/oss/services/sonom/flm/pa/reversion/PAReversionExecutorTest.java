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

package com.ericsson.oss.services.sonom.flm.pa.reversion;

import static com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants.DEGRADED;
import static com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor.PAConstants.NOT_DEGRADED;
import static com.ericsson.oss.services.sonom.flm.pa.reversion.PAReversionExecutor.retrieveListOfAffectedCellKpisWithTimestamps;
import static com.ericsson.oss.services.sonom.flm.pa.reversion.PAReversionExecutor.retrieveListOfAffectedSectorKpisWithTimestamps;
import static com.ericsson.oss.services.sonom.flm.pa.reversion.PAReversionExecutor.updateChangeElementWithMetadata;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus;
import com.ericsson.oss.services.sonom.cm.service.change.api.Metadata;
import com.ericsson.oss.services.sonom.cm.service.change.api.ProposedChange;
import com.ericsson.oss.services.sonom.flm.ResourceLoader;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAOutputEventDao;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradationStatus;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.util.FlmMetadataVerifier;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementRetriever;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementSender;
import com.ericsson.oss.services.sonom.flm.util.metadata.CellKpiMetadata;
import com.ericsson.oss.services.sonom.flm.util.metadata.FlmMetadata;
import com.ericsson.oss.services.sonom.flm.util.metadata.SectorKpiMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Unit tests for {@link PAReversionExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAReversionExecutorTest {

    private static final String ID_ONE = "1";
    private static final String ID_TWO = "2";
    private static final Gson GSON = new Gson();
    private static final String E_RAB_RETAINABILITY_PERCENTAGE_LOST_HOURLY = "e_rab_retainability_percentage_lost_hourly";
    private static final String CELL_HANDOVER_SUCCESS_RATE_HOURLY = "cell_handover_success_rate_hourly";
    private static final String INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI_1_HOURLY = "initial_and_added_e_rab_establishment_sr_for_qci1_hourly";
    private static final String FLM_PA_TEST_EXECUTION_001 = "FLM_PA_TEST_EXECUTION-001";
    private static final String FLM_PA_TEST_EXECUTION_001_1 = "FLM_PA_TEST_EXECUTION-001_1";
    private static final String SECTOR_ID = "346592852364793107";
    private static final String ALG_FLM_1 = "alg_FLM_1";
    private static final String AVG_UL_PDCP_THROUGHPUT_SECTOR = "avg_ul_pdcp_throughput_sector";
    private static final String CHANGE_ID = "346592843081761068";
    private static final String CELL_1 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054167_OCEANSIDE_COAST,ManagedElement=054167_OCEANSIDE_COAST,ENodeBFunction=1,EUtranCellFDD=054167_1_4";
    private static final String CELL_2 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_054,MeContext=054582_78_JEFFERSON,ManagedElement=054582_78_JEFFERSON,ENodeBFunction=1,EUtranCellFDD=054582_3_4";
    private static final String ALG_FLM_11 = "alg_FLM_1";
    private static final String PROPOSED = "PROPOSED";
    private static final String LIST_OF_DIFFERENT_DEGRADATION_STATUSES = ResourceLoader
            .loadResource("listOfDegradationStatusesWithDifferentKpis.json");
    private static final String LIST_OF_DIFFERENT_PROPOSED_CHANGES = ResourceLoader.loadResource("listOfProposedChangesWithDifferentFdnValues.json");
    private static final String INVALID_CHANGE_ID = "96";
    private static final long LAST_MODIFIED = 1623674492700L;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private PAExecution paExecutionMock;

    @Mock
    private PAOutputEventDao paOutputEventDaoMock;

    @Mock
    private ChangeElementRetriever changeElementRetrieverMock;

    @Mock
    private ChangeElementSender changeElementSenderMock;

    @Mock
    private PAExecutionLatch latch;

    private PAReversionExecutor objectUnderTest;
    private final List<PaPolicyOutputEvent> listOfPaPolicyOutputEventsForMetadataTest = new ArrayList<>();
    private final List<ProposedChange> proposedChangesForMetadataTest = new ArrayList<>();

    @Before
    public void setUp() {
        objectUnderTest = new PAReversionExecutor()
                .withPAExecution(paExecutionMock)
                .withPAOutputEventDao(paOutputEventDaoMock)
                .withChangeElementRetriever(changeElementRetrieverMock)
                .withChangeElementSender(changeElementSenderMock)
                .withPAExecutionLatch(latch);

        final Type listOfProposedChangesObject = new TypeToken<ArrayList<ProposedChange>>() {
        }.getType();
        final Type listOfDegradationStatusObject = new TypeToken<ArrayList<DegradationStatus>>() {
        }.getType();

        final List<DegradationStatus> listOfDegradationStatusObjects = GSON.fromJson(LIST_OF_DIFFERENT_DEGRADATION_STATUSES,
                listOfDegradationStatusObject);

        final PaPolicyOutputEvent paPolicyOutputEventOne = new PaPolicyOutputEvent(
                FLM_PA_TEST_EXECUTION_001,
                FLM_PA_TEST_EXECUTION_001_1,
                1,
                SECTOR_ID,
                listOfDegradationStatusObjects.get(0));

        final PaPolicyOutputEvent paPolicyOutputEventTwo = new PaPolicyOutputEvent(
                FLM_PA_TEST_EXECUTION_001,
                FLM_PA_TEST_EXECUTION_001_1,
                1,
                CHANGE_ID,
                listOfDegradationStatusObjects.get(1));

        final PaPolicyOutputEvent paPolicyOutputEventThree = new PaPolicyOutputEvent(
                FLM_PA_TEST_EXECUTION_001,
                FLM_PA_TEST_EXECUTION_001_1,
                1,
                SECTOR_ID,
                listOfDegradationStatusObjects.get(2));

        final PaPolicyOutputEvent paPolicyOutputEventFour = new PaPolicyOutputEvent(
                FLM_PA_TEST_EXECUTION_001,
                FLM_PA_TEST_EXECUTION_001_1,
                1,
                SECTOR_ID,
                listOfDegradationStatusObjects.get(3));

        listOfPaPolicyOutputEventsForMetadataTest.add(paPolicyOutputEventOne);
        listOfPaPolicyOutputEventsForMetadataTest.add(paPolicyOutputEventTwo);
        listOfPaPolicyOutputEventsForMetadataTest.add(paPolicyOutputEventThree);
        listOfPaPolicyOutputEventsForMetadataTest.add(paPolicyOutputEventFour);

        proposedChangesForMetadataTest.addAll(GSON.fromJson(LIST_OF_DIFFERENT_PROPOSED_CHANGES, listOfProposedChangesObject));

    }

    @Test
    public void whenPaReversionExecutorIsExecuted_thenReversionsAreAppliedSuccessfully()
            throws PAExecutionException, SQLException, FlmAlgorithmException {

        final List<ChangeElement> changeElements = getChangeElementsForTest();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(2)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, times(1)).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verify(changeElementRetrieverMock).retrieveReversionChangeElementList();
        inOrder.verify(changeElementSenderMock).updateChangeElements(any(), anyList());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenRetrievingPaOutputEvents_andNoPaOutputEventsGetReturned_thenNoReversionsAreApplied()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(new ArrayList<>());
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(0)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, times(0)).updateChangeElements(any(), anyList());
    }

    @Test
    public void whenRetrievingPaOutputEvents_andThereAreNoDegradedSectors_thenNoReversionsAreApplied()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = new ArrayList<>(2);

        PaPolicyOutputEvent paPolicyOutputEvent = new PaPolicyOutputEvent(ID_ONE, ID_ONE, 0, ID_ONE,
                new DegradationStatus(NOT_DEGRADED, null, null));
        paPolicyOutputEvents.add(paPolicyOutputEvent);
        paPolicyOutputEvent = new PaPolicyOutputEvent(ID_ONE, ID_ONE, 0, ID_TWO,
                new DegradationStatus(NOT_DEGRADED, null, null));
        paPolicyOutputEvents.add(paPolicyOutputEvent);

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(paPolicyOutputEvents);
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(0)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, times(0)).updateChangeElements(any(), anyList());
    }

    @Test
    public void whenRetrievingChangeElements_andChangeElementRetrieverReturnsEmptyList_thenNoReversionsAreApplied()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = new ArrayList<>(2);
        final PaPolicyOutputEvent paPolicyOutputEvent = new PaPolicyOutputEvent(ID_ONE, ID_ONE, 0, ID_ONE,
                new DegradationStatus(DEGRADED, new HashMap<>(), new HashMap<>()));
        paPolicyOutputEvents.add(paPolicyOutputEvent);

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(paPolicyOutputEvents);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(new ArrayList<>());
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, times(0)).updateChangeElements(any(), anyList());
    }

    @Test
    public void whenRetrievingPaOutputEvents_andSqlExceptionOccurs_thenThrowPAExecutionException()
            throws PAExecutionException, SQLException {
        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenThrow(SQLException.class);
        thrown.expect(PAExecutionException.class);
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(0)).retrieveReversionChangeElementList();
    }

    @Test
    public void whenSendingUpdatedChangeElements_andFlmAlgorithmExceptionOccurs_thenThrowPAExecutionException()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        final List<PaPolicyOutputEvent> paPolicyOutputEvents = new ArrayList<>(1);

        final PaPolicyOutputEvent paPolicyOutputEvent = new PaPolicyOutputEvent(null, null,
                0, ID_ONE, new DegradationStatus(DEGRADED, null, null));
        paPolicyOutputEvents.add(paPolicyOutputEvent);

        final List<ChangeElement> changeElements = new ArrayList<>(1);

        final ChangeElement changeElement = new ChangeElement();
        changeElement.setChangeId(ID_ONE);
        changeElement.setStatus(ChangeElementStatus.PENDING_APPROVAL.getValue());
        changeElement.setChangeType(ChangeElement.ChangeType.REVERSION);
        changeElements.add(changeElement);

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(paPolicyOutputEvents);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);
        when(changeElementSenderMock.updateChangeElements(any(), anyList())).thenThrow(FlmAlgorithmException.class);
        thrown.expect(PAExecutionException.class);
        objectUnderTest.execute();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, times(1)).updateChangeElements(any(), anyList());
    }

    @Test
    public void whenUpdateChangeElementWithMetadataIsCalled_thenVerifyMetadataIsNotEmpty() {

        final ChangeElement changeElement = ChangeElement.createWithIdAndAttributes(1, FLM_PA_TEST_EXECUTION_001, ALG_FLM_11,
                PROPOSED, ALG_FLM_11, LAST_MODIFIED, proposedChangesForMetadataTest, CHANGE_ID);
        changeElement.setChangeType(ChangeElement.ChangeType.REVERSION);
        final Metadata testMetadata = new Metadata(updateChangeElementWithMetadata(changeElement, listOfPaPolicyOutputEventsForMetadataTest, 2));
        final FlmMetadata testFlmMetadata = GSON.fromJson(testMetadata.getValue(), FlmMetadata.class);
        final List<CellKpiMetadata> listOfAffectedCellKpisWithTimestamps = testFlmMetadata.getListOfAffectedCellKpisWithTimestamps();
        final List<SectorKpiMetadata> listOfAffectedSectorKpisWithTimestamps = testFlmMetadata.getListOfAffectedSectorKpisWithTimestamps();

        softly.assertThat(changeElement.metadataAsJson()).isNotEmpty();
        softly.assertThat(FlmMetadataVerifier.verifyListOfAffectedCellKpisWithTimestampsContains(E_RAB_RETAINABILITY_PERCENTAGE_LOST_HOURLY,
                listOfAffectedCellKpisWithTimestamps)).isTrue();
        softly.assertThat(FlmMetadataVerifier.verifyListOfAffectedCellKpisWithTimestampsContains(CELL_HANDOVER_SUCCESS_RATE_HOURLY,
                listOfAffectedCellKpisWithTimestamps)).isTrue();
        softly.assertThat(
                FlmMetadataVerifier.verifyListOfAffectedCellKpisWithTimestampsContains(
                        INITIAL_AND_ADDED_E_RAB_ESTABLISHMENT_SR_FOR_QCI_1_HOURLY, listOfAffectedCellKpisWithTimestamps))
                .isTrue();
        softly.assertThat(FlmMetadataVerifier.verifyListOfAffectedSectorKpisWithTimestampsContains(AVG_UL_PDCP_THROUGHPUT_SECTOR,
                listOfAffectedSectorKpisWithTimestamps))
                .isTrue();
        softly.assertThat(FlmMetadataVerifier.verifyListOfAffectedCellKpisWithTimestampsContainsFdn(CELL_1, listOfAffectedCellKpisWithTimestamps))
                .isTrue();
        softly.assertThat(FlmMetadataVerifier.verifyListOfAffectedCellKpisWithTimestampsContainsFdn(CELL_2, listOfAffectedCellKpisWithTimestamps))
                .isTrue();

    }

    @Test
    public void whenIdsDoNotMatch_verifyListOfAffectedSectorKpisIsEmpty() {
        final ChangeElement changeElement = ChangeElement.createWithIdAndAttributes(1, FLM_PA_TEST_EXECUTION_001, ALG_FLM_11,
                PROPOSED, ALG_FLM_11, LAST_MODIFIED, proposedChangesForMetadataTest, INVALID_CHANGE_ID);
        softly.assertThat(retrieveListOfAffectedSectorKpisWithTimestamps(changeElement, listOfPaPolicyOutputEventsForMetadataTest.get(0)))
                .isEmpty();
    }

    @Test
    public void whenIdsDoMatch_verifyListOfAffectedSectorKpisIsNotEmpty() {
        final ChangeElement changeElement = ChangeElement.createWithIdAndAttributes(1, FLM_PA_TEST_EXECUTION_001, ALG_FLM_11,
                PROPOSED, ALG_FLM_11, LAST_MODIFIED, proposedChangesForMetadataTest, CHANGE_ID);
        softly.assertThat(retrieveListOfAffectedSectorKpisWithTimestamps(changeElement, listOfPaPolicyOutputEventsForMetadataTest.get(1)))
                .isNotEmpty();
    }

    @Test
    public void whenFdnMatch_verifyListOfAffectedCellKpisIsNotEmpty() {

        softly.assertThat(
                retrieveListOfAffectedCellKpisWithTimestamps(listOfPaPolicyOutputEventsForMetadataTest.get(1)))
                .isNotEmpty();

    }

    @Test
    public void whenDegradedCellKpisIsEmpty_verifyEmptyListIsReturned() {

        softly.assertThat(
                retrieveListOfAffectedCellKpisWithTimestamps(listOfPaPolicyOutputEventsForMetadataTest.get(2)))
                .isEmpty();

    }

    @Test
    public void whenFdnToDegradedTimestampsIsEmpty_verifyEmptyListIsReturned() {

        softly.assertThat(
                retrieveListOfAffectedCellKpisWithTimestamps(listOfPaPolicyOutputEventsForMetadataTest.get(3)))
                .isEmpty();

    }

    @Test
    public void whenPaReversionExecutorIsExecuted_andInterruptSignalIsCalledAfterDegradedSectorsRetrieved_thenPaExecutionInterruptExceptionIsThrown()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);

        verifyExecutionFails();
        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, never()).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, never()).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenPaReversionExecutorIsExecuted_andInterruptSignalIsCalledAfterChangeElementsRetrieved_thenPaExecutionInterruptExceptionIsThrown()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        final List<ChangeElement> changeElements = getChangeElementsForTest();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);

        verifyExecutionFails();

        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, never()).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verify(changeElementRetrieverMock).retrieveReversionChangeElementList();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenPaReversionExecutorIsExecuted_andInterruptSignalIsCalledAfterChangeElementsFiltering_thenPaExecutionInterruptExceptionIsThrown()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        doNothing().doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        final List<ChangeElement> changeElements = getChangeElementsForTest();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);

        verifyExecutionFails();

        verify(paOutputEventDaoMock, times(1)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, never()).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verify(changeElementRetrieverMock).retrieveReversionChangeElementList();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenPaReversionExecutorIsExecuted_andInterruptSignalIsCalledDuringChangeElementUpdateLoop_thenPaExecutionInterruptExceptionIsThrown()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        doNothing().doNothing().doNothing().doThrow(PAExecutionInterruptedException.class).when(latch).verifyNotInterruptedAndContinue();

        final List<ChangeElement> changeElements = getChangeElementsForTest();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);

        verifyExecutionFails();

        verify(paOutputEventDaoMock, times(2)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, never()).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verify(changeElementRetrieverMock).retrieveReversionChangeElementList();
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenPaReversionExecutorIsExecuted_andInterruptSignalIsCalledBeforeChangeElementPersisting_thenPaExecutionInterruptExceptionIsThrown()
            throws PAExecutionException, SQLException, FlmAlgorithmException {
        doNothing().doNothing().doNothing().doNothing().doNothing().doThrow(PAExecutionInterruptedException.class).when(latch)
                .verifyNotInterruptedAndContinue();

        final List<ChangeElement> changeElements = getChangeElementsForTest();

        when(paOutputEventDaoMock.getPaPolicyOutputEventById(any())).thenReturn(listOfPaPolicyOutputEventsForMetadataTest);
        when(changeElementRetrieverMock.retrieveReversionChangeElementList()).thenReturn(changeElements);

        verifyExecutionFails();

        verify(paOutputEventDaoMock, times(2)).getPaPolicyOutputEventById(any());
        verify(changeElementRetrieverMock, times(1)).retrieveReversionChangeElementList();
        verify(changeElementSenderMock, never()).updateChangeElements(any(), anyList());

        final InOrder inOrder = inOrder(paOutputEventDaoMock, changeElementRetrieverMock, changeElementSenderMock);
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verify(changeElementRetrieverMock).retrieveReversionChangeElementList();
        inOrder.verify(paOutputEventDaoMock).getPaPolicyOutputEventById(any());
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyExecutionFails() throws PAExecutionException {
        try {
            objectUnderTest.execute();
            shouldHaveThrown(PAExecutionInterruptedException.class);
        } catch (final PAExecutionInterruptedException ignored) {
        }
    }

    private List<ChangeElement> getChangeElementsForTest() {
        final List<ChangeElement> changeElements = new ArrayList<>();
        final ChangeElement changeElementOne = ChangeElement.createWithIdAndAttributes(1,
                FLM_PA_TEST_EXECUTION_001, ALG_FLM_1,
                ChangeElementStatus.PENDING_APPROVAL.getValue(), ALG_FLM_1, LAST_MODIFIED,
                proposedChangesForMetadataTest, CHANGE_ID);
        changeElementOne.setChangeType(ChangeElement.ChangeType.REVERSION);
        changeElements.add(changeElementOne);
        final ChangeElement changeElementTwo = ChangeElement.createWithIdAndAttributes(2,
                FLM_PA_TEST_EXECUTION_001, ALG_FLM_1,
                ChangeElementStatus.PENDING_APPROVAL.getValue(), ALG_FLM_1,
                LAST_MODIFIED, proposedChangesForMetadataTest, CHANGE_ID);
        changeElementTwo.setChangeType(ChangeElement.ChangeType.REVERSION);
        changeElements.add(changeElementTwo);
        return changeElements;
    }
}