/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.stores;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmSectorCellRetriever;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.google.common.collect.Iterables;

/**
 * Unit tests for {@link CmSectorCellStore} class.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ CmSectorCellStore.class,
                  CmSectorCellRetriever.class,
                  CmCellGroupRetriever.class })
@PowerMockIgnore({ "javax.management.MBeanServer",
                   "javax.management.ObjectName",
                   "javax.management.ObjectInstance",
                   "javax.net.ssl.SSLContext",
                   "javax.net.ssl.SSLSocketFactory",
                   "org.apache.http.ssl.SSLContexts" })
@SuppressWarnings("PMD.MoreThanOneLogger")
public class CmSectorCellStoreTest {
    private static final String OUTDOOR = "outdoor";
    private static final String UNDEFINED = "undefined";
    private static final Cell CELL1 = new Cell(1L, 1, "cell1Fdn", 1400, OUTDOOR, UNDEFINED);
    private static final Cell CELL2 = new Cell(2L, 1, "cell2Fdn", 1400, OUTDOOR, UNDEFINED);
    private static final Cell CELL3 = new Cell(3L, 1, "cell3Fdn", 1400, OUTDOOR, UNDEFINED);
    private static final Cell CELL4 = new Cell(4L, 1, "cell4Fdn", 1400, OUTDOOR, UNDEFINED);
    private static final Cell CELL5 = new Cell(5L, 1, "cell5Fdn", 1400, OUTDOOR, UNDEFINED);
    private static final String EXECUTION_ID = "EXECUTION_ID";
    private static final String LOGGER = "LOGGER";
    private static final String GROUP_1_NAME = "group_1";
    private static final Group GROUP_1 = new Group(GROUP_1_NAME);
    private static final String COLLECTING_INCLUDED_CELLS_TOOK = "Collecting included cells took {}.";
    private static final String COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST = "Collected {} cell(s) from the inclusion list.";

    private TopologySector sector0;
    private TopologySector sector1;
    private TopologySector sector2;

    @Mock
    private CmSectorCellRetriever cmSectorCellRetrieverMock;

    @Mock
    private CmCellGroupRetriever cmCellGroupRetrieverMock;

    @Mock
    private Execution executionMock;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    static {
        System.setProperty("CM_SERVICE_HOSTNAME", "CM_SERVICE_HOSTNAME");
        System.setProperty("CM_SERVICE_PORT", "8080");
    }

    @Before
    public void setUp() {
        sector0 = new TopologySector(0L, Collections.emptyList());
        sector1 = new TopologySector(1L, Arrays.asList(CELL1, CELL2));
        sector2 = new TopologySector(2L, Arrays.asList(CELL3, CELL4));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(cmCellGroupRetrieverMock, cmCellGroupRetrieverMock, executionMock);
    }

    @Test
    public void whenConstructorIsCalled_thenRetrieversAreCreated() throws Exception {
        PowerMockito.whenNew(CmSectorCellRetriever.class).withNoArguments().thenReturn(cmSectorCellRetrieverMock);
        PowerMockito.whenNew(CmCellGroupRetriever.class).withNoArguments().thenReturn(cmCellGroupRetrieverMock);
        
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.emptyList());
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(executionMock);

        softly.assertThat(objectUnderTest.getCells()).isEmpty();
        softly.assertThat(objectUnderTest.getSectorsWithInclusionListCells()).isEmpty();
        softly.assertThat(objectUnderTest.getCellForCellFdn("randomFdn", 1)).isNull();

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
        PowerMockito.verifyNew(CmCellGroupRetriever.class).withNoArguments();
        PowerMockito.verifyNew(CmSectorCellRetriever.class).withNoArguments();
    }

    @Test
    public void whenRetrieverReturnsEmptyListAndInclusionListIsNotApplied_thenNoCellOrSectorsCollected() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.emptyList());
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.getCells()).isEmpty();
        softly.assertThat(objectUnderTest.getFullSectors()).isEmpty();

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
    }

    @Test
    public void whenRetrieverReturnsOneSectorWithNoCellsAndInclusionListIsNotApplied_thenOneSectorAndNoCellsAreStored() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.singletonList(sector0));
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.getCells()).isEmpty();
        softly.assertThat(objectUnderTest.getFullSectors()).containsExactlyInAnyOrder(sector0);
        softly.assertThat(objectUnderTest.getSectorsWithInclusionListCells()).containsExactlyInAnyOrder(sector0);

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
    }

    @Test
    public void whenRetrieverReturnsMultipleSectorsWithMultipleCellsAndInclusionListIsNotApplied_thenSameSectorsAndCellsAreStored()
            throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final Logger loggerStoreMock = mock(Logger.class);
        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        assertSectorsAndCells(objectUnderTest,
                              Arrays.asList(CELL1, CELL2, CELL3, CELL4),
                              Arrays.asList(sector1, sector2),
                              Arrays.asList(sector1, sector2));

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
        verify(loggerStoreMock).info("Inclusion list is not applied.");
    }

    @Test
    public void whenInclusionListIsNull_thenInclusionListIsNotApplied() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.emptyList());
        when(executionMock.getInclusionList()).thenReturn(null);

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isFalse();

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
    }

    @Test
    public void whenInclusionListIsEmpty_thenInclusionListIsNotApplied() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.emptyList());
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isFalse();

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
    }

    @Test
    public void whenInclusionListIsApplied_thenOnlyIncludedCellsAreUsed() throws ExecutionException, InterruptedException, FlmAlgorithmException {
        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock = mock(Future.class);
        final Logger loggerMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerMock);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(GROUP_1));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock);
        when(futureMock.get()).thenReturn(Arrays.asList(CELL1, CELL4, CELL5));

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isTrue();
        assertSectorsAndCells(objectUnderTest,
                              Arrays.asList(CELL1, CELL4),
                              Arrays.asList(sector1, sector2),
                              Arrays.asList(new TopologySector(sector1.getSectorId(), Collections.singletonList(CELL1)),
                                            new TopologySector(sector2.getSectorId(), Collections.singletonList(CELL4))));

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getId();
        verify(executionMock, times(2)).getInclusionList();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock).get();
        verifyElapsedTimeIsLogged(loggerMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verify(loggerMock).info(eq(COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST), anyInt());
        verifyNoMoreInteractions(futureMock, loggerMock);
    }

    @Test
    public void whenCheckExceptionHappensOnInclusionCellFetching_thenCheckedExceptionIsConvertedToUncheckedException() throws ExecutionException,
            InterruptedException {

        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock = mock(Future.class);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(GROUP_1));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock);
        doThrow(InterruptedException.class).when(futureMock).get();

        final Throwable thrown = catchThrowable(() -> new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock));

        softly.assertThat(thrown)
              .isExactlyInstanceOf(RuntimeException.class)
              .hasRootCauseExactlyInstanceOf(InterruptedException.class);

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock).get();
        verifyNoMoreInteractions(futureMock);
    }

    @Test
    public void whenGroupsOfInclusionListContainNoCells_thenLoggedAndExceptionIsThrown() throws ExecutionException, InterruptedException {
        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock = mock(Future.class);
        final Logger loggerMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerMock);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(GROUP_1));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock);
        when(futureMock.get()).thenReturn(Collections.emptyList());

        final Throwable thrown = catchThrowable(() -> new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock));

        softly.assertThat(thrown)
              .isExactlyInstanceOf(FlmAlgorithmException.class)
              .hasMessage("Groups found in inclusion list contain no cells");

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock).get();
        verify(loggerMock).warn("Execution stopped as no cells were found in the specified inclusion list.");
        verify(loggerMock).warn("Group {} in the inclusion list is empty or does not contains cells.", GROUP_1_NAME);
        verifyElapsedTimeIsLogged(loggerMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verifyNoMoreInteractions(futureMock, loggerMock);
    }

    @Test
    public void whenAGroupOfInclusionListContainsNoCells_thenLogged() throws ExecutionException, InterruptedException, FlmAlgorithmException {
        final String group2 = "group_2";

        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock1 = mock(Future.class);
        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock2 = mock(Future.class);
        final Logger loggerMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerMock);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.singletonList(sector1));
        when(executionMock.getInclusionList()).thenReturn(Arrays.asList(GROUP_1, new Group(group2)));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock1);
        when(futureMock1.get()).thenReturn(Collections.emptyList());
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(group2, EXECUTION_ID)).thenReturn(futureMock2);
        when(futureMock2.get()).thenReturn(Collections.singletonList(CELL1));

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isTrue();
        assertSectorsAndCells(objectUnderTest,
                              Collections.singletonList(CELL1),
                              Collections.singletonList(sector1),
                              Collections.singletonList(new TopologySector(sector1.getSectorId(), Collections.singletonList(CELL1))));

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock, times(2)).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock1).get();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(group2, EXECUTION_ID);
        verify(futureMock2).get();
        verify(loggerMock).warn("Group {} in the inclusion list is empty or does not contains cells.", GROUP_1_NAME);
        verifyElapsedTimeIsLogged(loggerMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verify(loggerMock).info(eq(COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST), anyInt());
        verifyNoMoreInteractions(futureMock1, futureMock1, loggerMock);
    }

    @Test
    public void whenInclusionListIsApplied_thenCachedCellsAreFilteredAndEmptySectorsAreErasedAndProperlyLogged() throws ExecutionException,
            InterruptedException, FlmAlgorithmException {
        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock1 = mock(Future.class);
        final Logger loggerLoggingFormatterMock = mock(Logger.class);
        final Logger loggerStoreMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);
        Whitebox.setInternalState(LoggingFormatter.class, LOGGER, loggerLoggingFormatterMock);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Collections.singletonList(sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(GROUP_1));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock1);
        when(futureMock1.get()).thenReturn(Arrays.asList(CELL1, CELL2));

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isTrue();
        assertSectorsAndCells(objectUnderTest,
                              Collections.emptyList(),
                              Collections.singletonList(sector2),
                              Collections.emptyList());

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock, times(2)).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock1).get();
        verifyLoggerFormatter(loggerLoggingFormatterMock, sector2);
        verifyElapsedTimeIsLogged(loggerStoreMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verify(loggerStoreMock).info(eq(COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST), anyInt());
        verifyNoMoreInteractions(futureMock1, loggerStoreMock, loggerLoggingFormatterMock);
    }

    @Test
    public void whenInclusionListIsNotApplied_thenStoresCacheIsReturnedOnGetAllMediatedCells() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector0, sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final Logger loggerStoreMock = mock(Logger.class);
        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.getAllMediatedCells()).containsExactlyInAnyOrder(CELL1, CELL2, CELL3, CELL4);
        softly.assertThat(objectUnderTest.getAllMediatedCells()).containsExactlyInAnyOrder(Iterables.toArray(objectUnderTest.getCells(), Cell.class));
        softly.assertThat(objectUnderTest.isInclusionListApplied()).isFalse();
        assertSectorsAndCells(objectUnderTest,
                              Arrays.asList(CELL1, CELL2, CELL3, CELL4),
                              Arrays.asList(sector0, sector1, sector2),
                              Arrays.asList(sector0, sector1, sector2));

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
        verify(loggerStoreMock).info("Inclusion list is not applied.");
    }

    @Test
    public void whenInclusionListIsApplied_thenGetAllMediatedCellsFetchesCells() throws ExecutionException, InterruptedException,
            FlmAlgorithmException {
        final String group1 = GROUP_1_NAME;

        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock1 = mock(Future.class);
        final Logger loggerLoggingFormatterMock = mock(Logger.class);
        final Logger loggerStoreMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);
        Whitebox.setInternalState(LoggingFormatter.class, LOGGER, loggerLoggingFormatterMock);

        final TopologySector sector0Copy = TopologySector.newInstance(sector0);
        final TopologySector sector1Copy = TopologySector.newInstance(sector1);
        final TopologySector sector2Copy = TopologySector.newInstance(sector2);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector0, sector1, sector2))
                                                  .thenReturn(Arrays.asList(sector0Copy, sector1Copy, sector2Copy));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(new Group(group1)));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(group1, EXECUTION_ID)).thenReturn(futureMock1);
        when(futureMock1.get()).thenReturn(Arrays.asList(CELL1, CELL2));

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isTrue();
        softly.assertThat(objectUnderTest.getAllMediatedCells()).containsExactlyInAnyOrder(CELL1, CELL2, CELL3, CELL4);
        assertSectorsAndCells(objectUnderTest,
                              Arrays.asList(CELL1, CELL2),
                              Arrays.asList(sector0, sector1, sector2),
                              Collections.singletonList(new TopologySector(sector1.getSectorId(), Arrays.asList(CELL1, CELL2))));

        verify(cmSectorCellRetrieverMock, times(2)).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock, times(3)).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(group1, EXECUTION_ID);
        verify(futureMock1).get();
        verifyLoggerFormatter(loggerLoggingFormatterMock, sector0);
        verifyLoggerFormatter(loggerLoggingFormatterMock, sector2);
        verifyElapsedTimeIsLogged(loggerStoreMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verifyElapsedTimeIsLogged(loggerStoreMock, "Fetching all mediated cells took {}.");
        verify(loggerStoreMock).info(eq(COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST), anyInt());
        verifyNoMoreInteractions(loggerLoggingFormatterMock, loggerStoreMock, futureMock1);
    }

    @Test
    public void whenInclusionListIsNotApplied_thenStoresCacheIsReturnedOnGetIdsOfAllIncludedCells() throws FlmAlgorithmException {
        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector0, sector1, sector2));
        when(executionMock.getInclusionList()).thenReturn(Collections.emptyList());

        final Logger loggerStoreMock = mock(Logger.class);
        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.getAllIncludedCellIds()).containsExactlyInAnyOrder(
                new CellIdentifier(CELL1.getOssId(), CELL1.getFdn()),
                new CellIdentifier(CELL2.getOssId(), CELL2.getFdn()),
                new CellIdentifier(CELL3.getOssId(), CELL3.getFdn()),
                new CellIdentifier(CELL4.getOssId(), CELL4.getFdn()));
        softly.assertThat(objectUnderTest.getAllIncludedCellIds().stream()
                .map(CellIdentifier::getFdn).collect(Collectors.toSet())).containsExactlyInAnyOrderElementsOf(
                Stream.of(CELL1, CELL2, CELL3, CELL4)
                        .map(Cell::getFdn).collect(Collectors.toSet()));
        softly.assertThat(objectUnderTest.isInclusionListApplied()).isFalse();

        verify(cmSectorCellRetrieverMock).retrieve();
        verify(executionMock).getInclusionList();
        verify(loggerStoreMock).info("Inclusion list is not applied.");
    }

    @Test
    public void whenInclusionListIsApplied_thenGetIdsOfAllIncludedCellsFetchesCells() throws ExecutionException, InterruptedException,
            FlmAlgorithmException {
        @SuppressWarnings("unchecked")
        final Future<List<Cell>> futureMock1 = mock(Future.class);
        final Logger loggerLoggingFormatterMock = mock(Logger.class);
        final Logger loggerStoreMock = mock(Logger.class);

        Whitebox.setInternalState(CmSectorCellStore.class, LOGGER, loggerStoreMock);
        Whitebox.setInternalState(LoggingFormatter.class, LOGGER, loggerLoggingFormatterMock);

        final TopologySector sector0Copy = TopologySector.newInstance(sector0);
        final TopologySector sector1Copy = TopologySector.newInstance(sector1);
        final TopologySector sector2Copy = TopologySector.newInstance(sector2);

        when(cmSectorCellRetrieverMock.retrieve()).thenReturn(Arrays.asList(sector0, sector1, sector2))
                .thenReturn(Arrays.asList(sector0Copy, sector1Copy, sector2Copy));
        when(executionMock.getInclusionList()).thenReturn(Collections.singletonList(GROUP_1));
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(cmCellGroupRetrieverMock.retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID)).thenReturn(futureMock1);
        when(futureMock1.get()).thenReturn(Arrays.asList(CELL1, CELL2));

        final CmSectorCellStore objectUnderTest = new CmSectorCellStore(cmSectorCellRetrieverMock, cmCellGroupRetrieverMock, executionMock);

        softly.assertThat(objectUnderTest.isInclusionListApplied()).isTrue();
        softly.assertThat(objectUnderTest.getAllIncludedCellIds()).containsExactlyInAnyOrder(
                new CellIdentifier(CELL1.getOssId(), CELL1.getFdn()),
                new CellIdentifier(CELL2.getOssId(), CELL2.getFdn()));

        verify(cmSectorCellRetrieverMock, times(1)).retrieve();
        verify(executionMock, times(2)).getInclusionList();
        verify(executionMock, times(3)).getId();
        verify(cmCellGroupRetrieverMock).retrieveGroupEvaluation(GROUP_1_NAME, EXECUTION_ID);
        verify(futureMock1).get();
        verifyLoggerFormatter(loggerLoggingFormatterMock, sector0);
        verifyLoggerFormatter(loggerLoggingFormatterMock, sector2);
        verifyElapsedTimeIsLogged(loggerStoreMock, COLLECTING_INCLUDED_CELLS_TOOK);
        verify(loggerStoreMock).info(eq(COLLECTED_CELL_S_FROM_THE_INCLUSION_LIST), anyInt());
        verifyNoMoreInteractions(loggerLoggingFormatterMock, loggerStoreMock, futureMock1);
    }

    private void assertSectorsAndCells(final CmSectorCellStore cmSectorCellStore,
                                       final List<? extends Cell> cells,
                                       final List<? extends TopologySector> fullSectors,
                                       final List<? extends TopologySector> partialSectors) {
        softly.assertThat(cmSectorCellStore.getCells()).containsExactlyInAnyOrder(Iterables.toArray(cells, Cell.class));
        cells.forEach(cell -> softly.assertThat(cmSectorCellStore.getCellForCellFdn(cell.getFdn(), cell.getOssId())).isEqualTo(cell));

        softly.assertThat(cmSectorCellStore.getFullSectors()).containsExactlyInAnyOrder(Iterables.toArray(fullSectors, TopologySector.class));
        fullSectors.forEach(sector -> softly.assertThat(cmSectorCellStore.getFullSector(sector.getSectorId())).isEqualTo(sector));

        softly.assertThat(cmSectorCellStore.getSectorsWithInclusionListCells()).containsExactlyInAnyOrder(Iterables.toArray(partialSectors, TopologySector.class));
        partialSectors.forEach(sector -> softly.assertThat(cmSectorCellStore.getSectorWithInclusionListCells(sector.getSectorId())).isEqualTo(sector));
    }

    private void verifyLoggerFormatter(final Logger loggerLoggingFormatterMock, final TopologySector sector2) {
        verify(loggerLoggingFormatterMock).info("Execution_ID: {}, Sector_ID: {}, Exclusion_Reason: {}",
                                                EXECUTION_ID,
                                                sector2.getSectorId().toString(),
                                                "Once the inclusion list is applied, all cells under the sector are out " +
                                                        "of optimization scope.");
    }

    private void verifyElapsedTimeIsLogged(final Logger loggerMock, final String message) {
        final ArgumentCaptor<String> timeElapsed = ArgumentCaptor.forClass(String.class);
        verify(loggerMock).info(eq(message), timeElapsed.capture());

        softly.assertThat(timeElapsed).isNotNull();
    }
}