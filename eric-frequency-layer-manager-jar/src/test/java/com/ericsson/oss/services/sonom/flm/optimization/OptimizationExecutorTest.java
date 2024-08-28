/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.optimization;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.EXCLUSION_LIST;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.CELL_SETTINGS_HISTORY;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE;
import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Node;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import com.ericsson.oss.services.sonom.common.rest.utils.exception.RestExecutionException;
import com.ericsson.oss.services.sonom.flm.FlmPolicyInputEventHandler;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmStore;
import com.ericsson.oss.services.sonom.flm.database.execution.ExecutionDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDaoImpl;
import com.ericsson.oss.services.sonom.flm.executor.PersistenceHandler;
import com.ericsson.oss.services.sonom.flm.messagehandler.PoeExecutionConsumerHandlerImpl;
import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.optimization.kpi.CellKpiCollection;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.settings.CellSettingCollection;

/**
 * Unit tests for {@link OptimizationExecutor} class.
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
@RunWith(MockitoJUnitRunner.class)
public class OptimizationExecutorTest {

    public static final String FDN = "fdn";
    private static final String FDN_2 = "fdn2";
    public static final String EXECUTION_ID = "1";
    private static final String EXECUTION_DATE = LocalDate.of(2_021, 3, 31).toString();
    private static final String UNDEFINED = "undefined";
    private static final String OUTDOOR = "outdoor";

    @Mock
    private static FlmPolicyInputEventHandler FLM_POLICY_INPUT_EVENT_HANDLER;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private CmStore mockedCmStore;

    @Mock
    private CmSectorCellStore mockedCmSectorCellStore;

    @Mock
    private Execution executionMock;

    @Mock
    private PersistenceHandler persistenceHandlerMock;

    @Mock
    private CellKpiCollection mockedCellKpiCollection;

    @Mock
    private CellSettingCollection mockedCellSettingCollection;

    @Mock
    private PoeExecutionConsumerHandlerImpl executionConsumerHandlerImplMock;

    @Mock
    private OptimizationsDaoImpl optimizationsDaoMock;

    @Mock
    private ExecutionDao executionDaoMock;

    @Mock
    private FlmMetricHelper mockedFlmMetricHelper;

    @Mock
    private CmNodeObjectsStore mockedCmNodeObjectsStore;

    private OptimizationExecutor objectUnderTest;

    static {
        //Must be set so that Mockito can create the mock for OptimizationExecutor
        System.setProperty("CM_SERVICE_HOSTNAME", "localhost");
        System.setProperty("CM_SERVICE_PORT", "8080");
    }

    @Before
    public void setUp() {
        objectUnderTest = spy(new OptimizationExecutorBuilder()
                .cmStore(mockedCmStore)
                .execution(executionMock)
                .persistenceHandler(persistenceHandlerMock)
                .flmPolicyInputEventHandler(FLM_POLICY_INPUT_EVENT_HANDLER)
                .optimizationsDao(optimizationsDaoMock)
                .executionDao(executionDaoMock)
                .executionConsumerHandlerImpl(executionConsumerHandlerImplMock)
                .cellKpiCollection(mockedCellKpiCollection)
                .cellSettingCollection(mockedCellSettingCollection)
                .flmMetricHelper(mockedFlmMetricHelper)
                .build());
    }

    @Test
    public void whenExecutingOptimizationExecutorAndSectorsExist_thenNoErrorOccurs() throws SQLException, FlmAlgorithmException {
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(1, 1, 1));
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);
        when(mockedCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true,  true,  true,  true,  "UNDEFINED", true), "nodetype"));


        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);

        final String executionDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, executionDate);

        verify(mockedCellKpiCollection, times(2)).collect(any(), any(), any(), any());
        verify(mockedCmStore, times(2)).getCmSectorCellStore();
        verify(mockedCmSectorCellStore, times(2)).getSectorsWithInclusionListCells();
        verify(persistenceHandlerMock, times(2)).persistExecutionStatus(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false);
        verify(executionMock, atLeastOnce()).setState(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE);
    }

    @Test
    public void whenExecutingOptimizationExecutorAndSectorsExistWithNoOutdoorCells_thenNoPolicyEventIsCreated() throws SQLException,
            FlmAlgorithmException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(0, 0, 1));
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);

        assertThat(objectUnderTest.getPolicyInputEvents()).isEmpty();
    }

    @Test
    public void whenExecutingOptimizationExecutorAndOneSectorExistsWithOutdoorCells_thenPolicyEventIsCreated() throws SQLException,
            FlmAlgorithmException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(1, 1, 1));
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);
        when(mockedCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true,  true,  true,  true,  "ACTIVATED", true), "nodetype"));


        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);

        assertThat(objectUnderTest.getPolicyInputEvents()).hasSize(1);
    }

    @Test
    public void whenExecutingOptimizationExecutorOnSectorWithThreeCells_thenPolicyEventIsCreatedWithOnlyOutdoorCells() throws SQLException,
            FlmAlgorithmException, ParseException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(1, 1, 1));
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);
        when(mockedCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true,  true,  true,  true,  "DEACTIVATED", true), "nodetype"));


        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);

        final JSONParser parser = new JSONParser();
        final JSONObject json = (JSONObject) parser.parse(objectUnderTest.getPolicyInputEvents().get(0));
        assertThat(json.get("sectorId")).isEqualTo("12");
        assertThat(json.get("optimizationCells")).asList().hasSize(2);
    }

    @Test
    public void whenExecutingOptimizationExecutorSettingCollectionFails_thenExceptionThrown() throws SQLException, FlmAlgorithmException {
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(0, 0, 0));
        when(mockedCellSettingCollection.collect(executionMock.getId())).thenThrow(new SQLException());
        final String executionDate = DateTimeFormatter.ISO_DATE.format(LocalDate.now());

        thrown.expect(FlmAlgorithmException.class);
        thrown.expectCause(instanceOf(SQLException.class));

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, executionDate);
        verify(persistenceHandlerMock, never()).persistExecutionStatus(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false);
        verify(executionMock, never()).setState(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE);
    }

    @Test
    public void whenExecutingOptimizationExecutorAndReadingSectorsFails_thenExceptionThrown() throws FlmAlgorithmException {
        final String errorMsg = "Failed to get topology objects from eric-cm-topology-model-sn endpoint response";
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenThrow(new RestExecutionException(errorMsg));

        thrown.expect(FlmAlgorithmException.class);
        thrown.expectMessage(errorMsg);

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);
        verify(persistenceHandlerMock, never()).persistExecutionStatus(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false);
        verify(executionMock, never()).setState(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE);
    }

    @Test
    public void whenExecutingOptimizationExecutorAndExecutionStateIsNotRecognised_thenNoProcessingIsDone() throws FlmAlgorithmException {
        objectUnderTest.execute(CELL_SETTINGS_HISTORY, false, true, EXECUTION_DATE);

        verify(mockedCmStore, never()).getCmSectorCellStore();
        verify(executionMock, never()).setState(CELL_SETTINGS_HISTORY);

        verify(persistenceHandlerMock, never()).persistExecutionStatus(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false);
        verify(executionMock, never()).setState(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE);
    }

    @Test
    public void whenSendStateEnds_thenReceiveStateStarted() throws InterruptedException, SQLException, FlmAlgorithmException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(executionMock.getNumOptimizationElementsSent()).thenReturn(2);
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(generateSectorWithCells(0, 0, 1));
        when(executionConsumerHandlerImplMock.waitMessages(anyString(), anyLong())).thenReturn(true);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);
        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);
        verify(persistenceHandlerMock).persistExecutionStatus(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE, false);
    }

    @Test
    public void whenReceiveState_thenKafkaConsumersConsumesMessages() throws SQLException, FlmAlgorithmException, InterruptedException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(executionMock.getNumOptimizationElementsSent()).thenReturn(2);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE, false, true, EXECUTION_DATE);
        verify(executionConsumerHandlerImplMock).waitMessages(anyString(), anyLong());
        verify(mockedFlmMetricHelper).incrementFlmMetric(eq(FlmMetric.FLM_POLICY_OUTPUT_EVENT_PROCESSED), anyLong());
        verify(mockedFlmMetricHelper).incrementFlmMetric(eq(FlmMetric.FLM_POLICY_OUTPUT_EVENT_PROCESSED_IN_MILLIS), anyLong());
    }

    @Test
    public void whenConsumeMessagesSuccess_thenExecutionStateUpdated() throws SQLException, FlmAlgorithmException, InterruptedException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(executionMock.getNumOptimizationElementsSent()).thenReturn(2);
        when(executionConsumerHandlerImplMock.waitMessages(anyString(), anyLong())).thenReturn(true);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE, false, true, EXECUTION_DATE);
        verify(optimizationsDaoMock).getNumberOfPolicyOutputEvents(any());
        verify(executionMock).setNumOptimizationElementsReceived(any());
        verify(executionDaoMock).update(any());
    }

    @Test
    public void whenConsumeMessagesTimeouts_thenExceptionIsThrown() throws SQLException, InterruptedException, FlmAlgorithmException {
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(executionMock.getNumOptimizationElementsSent()).thenReturn(2);
        when(executionConsumerHandlerImplMock.waitMessages(anyString(), anyLong())).thenReturn(false);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(0);

        thrown.expect(FlmAlgorithmException.class);
        objectUnderTest.execute(OPTIMIZATION_PROCESSING_RECEIVE_FROM_POLICYENGINE, false, true, EXECUTION_DATE);
    }

    @Test
    public void whenExecutingOptimizationExecutor_thenLogCellsWithNoSector() throws FlmAlgorithmException, SQLException {
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(
                generateSectorWithCells(1L, 1, 0, 0));
        when(executionMock.getState()).thenReturn(SUCCEEDED);
        when(executionMock.getId()).thenReturn(EXECUTION_ID);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(1);
        final CellIdentifier cellInASector = new CellIdentifier(1, "fdn1");
        when(mockedCellKpiCollection.collect(any(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.singletonMap(cellInASector, new HashMap<>()));
        final CellIdentifier cellNotInASector = new CellIdentifier(2, "fdn2");
        final Set<CellIdentifier> includedCells = Stream.of(cellInASector, cellNotInASector)
                .collect(Collectors.toSet());
        when(mockedCmSectorCellStore.getAllIncludedCellIds()).thenReturn(includedCells);
        when(mockedCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true,  true,  true,  true,  "UNDEFINED", true), "nodetype"));

        final Logger loggerLoggingFormatterMock = mock(Logger.class);

        Whitebox.setInternalState(LoggingFormatter.class, "LOGGER", loggerLoggingFormatterMock);

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, false, EXECUTION_DATE);

        verify(mockedCmSectorCellStore, times(1)).getAllIncludedCellIds();
        verifyLoggerFormatter(loggerLoggingFormatterMock, cellNotInASector);
    }

    private void verifyLoggerFormatter(final Logger loggerLoggingFormatterMock, final CellIdentifier cellNotInASector) {
        final String formatString = "Execution_ID: {}, Oss_ID: {}, Sector_ID: {}, Cell_ID: {}, Exclusion_Reason: {}";
        final String exclusionReason = "Cell excluded from optimization because it is not in any Sector";
        verify(loggerLoggingFormatterMock).info(formatString,
                EXECUTION_ID,
                String.valueOf(cellNotInASector.getOssId()),
                "<none>",
                cellNotInASector.getFdn(),
                exclusionReason);
    }

    @Test
    public void whenExecutingOptimizationExecutorAndNoSectorsExist_thenExceptionThrown() throws FlmAlgorithmException {
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(Collections.emptyList());

        thrown.expect(FlmAlgorithmException.class);
        thrown.expectMessage("No sectors found for optimization");

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);
        verify(persistenceHandlerMock, never()).persistExecutionStatus(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false);
        verify(executionMock, never()).setState(CELL_SETTINGS_HISTORY);
    }

    @Test
    public void whenExecutingOptimizationExecutor_andAllCellsGetExcludedForVariousReasons_thenNoPolicyInputEventsGetCreated()
            throws FlmAlgorithmException, SQLException {
        when(executionMock.getState()).thenReturn(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE);
        when(mockedCmStore.getCmSectorCellStore()).thenReturn(mockedCmSectorCellStore);
        final List<TopologySector> topologySectors = generateSectorWithCells(10L, 1, 1, 1);
        topologySectors.addAll(generateSectorWithCells(12L, 1, 1, 2));

        final List<Cell> sector14Cells = new ArrayList<>();
        sector14Cells.add(new Cell(3L, 3, FDN + 3, 1400, OUTDOOR, UNDEFINED));
        topologySectors.add(new TopologySector(14L, sector14Cells));

        when(mockedCmSectorCellStore.getSectorsWithInclusionListCells()).thenReturn(topologySectors);
        when(optimizationsDaoMock.getNumberOfPolicyOutputEvents(any())).thenReturn(2);
        when(mockedCmStore.getCmNodeObjectsStore(any(), any())).thenReturn(mockedCmNodeObjectsStore);
        when(mockedCmNodeObjectsStore.getNodeForCellFdn(any(), anyInt())).thenReturn(new Node(0L, "fdn", 0, new Node.FeatureState(true,  true,  true,  true,  "UNDEFINED", true), "nodetype"));

        final Map<CellIdentifier, Map<String, Object>> cellKpis = new HashMap<>();
        cellKpis.put(new CellIdentifier(0, "fdn0"), new HashMap<>());
        cellKpis.put(new CellIdentifier(1, "fdn1"), new HashMap<>());
        cellKpis.put(new CellIdentifier(2, FDN_2), new HashMap<>());
        cellKpis.put(new CellIdentifier(3, "fdn3"), new HashMap<>());
        final Map<CellIdentifier, Map<String, String>> cellSettings = new HashMap<>();
        cellSettings.put(new CellIdentifier(0, "fdn0"), new HashMap<>());
        cellSettings.put(new CellIdentifier(2, FDN_2), new HashMap<>());
        final CellIdentifier cellFdn3 = new CellIdentifier(3, "fdn3");
        cellSettings.put(cellFdn3, new HashMap<>());
        cellSettings.get(cellFdn3).put(EXCLUSION_LIST, "testExclusionList");

        doReturn(cellKpis).when(mockedCellKpiCollection).collect(any(), any(), any(), any());
        doReturn(cellSettings).when(mockedCellSettingCollection).collect(any());

        objectUnderTest.execute(OPTIMIZATION_PROCESSING_SEND_TO_POLICYENGINE, false, true, EXECUTION_DATE);

        final Map<CellIdentifier, Set<Long>> expectedMultiSectorCellsArgument = new HashMap<>();
        expectedMultiSectorCellsArgument.put(new CellIdentifier(0, "fdn0"), new HashSet<>(Arrays.asList(10L, 12L)));
        expectedMultiSectorCellsArgument.put(new CellIdentifier(1, "fdn1"), new HashSet<>(Arrays.asList(10L, 12L)));

        assertThat(objectUnderTest.getPolicyInputEvents()).hasSize(0);
        verify(objectUnderTest, times(1)).createInputEventGenerator(cellKpis, cellSettings, expectedMultiSectorCellsArgument);
    }

    private List<TopologySector> generateSectorWithCells(final long sectorId, final int outdoor, final int outdoorIndoor, final int indoor) {
        final List<TopologySector> sectors = new ArrayList<>();
        sectors.add(new TopologySector(sectorId, generateCells(outdoor, outdoorIndoor, indoor)));
        return sectors;
    }

    private List<TopologySector> generateSectorWithCells(final int outdoor, final int outdoorIndoor, final int indoor) {
        final List<TopologySector> sectors = new ArrayList<>();
        sectors.add(new TopologySector(12L, generateCells(outdoor, outdoorIndoor, indoor)));
        return sectors;
    }

    private List<Cell> generateCells(final int outdoor, final int outdoorIndoor, final int indoor) {
        final int capacity = outdoor + indoor + outdoorIndoor;
        final List<Cell> cellList = new ArrayList<>(capacity);
        for (int i = 0; i < outdoor; i++) {
            cellList.add(new Cell((long) i, i, FDN + i, 1400, OUTDOOR, UNDEFINED));
        }
        for (int i = outdoor; i < outdoor + outdoorIndoor; i++) {
            cellList.add(new Cell((long) i, i, FDN + i, 1400, "outdoor_indoor", UNDEFINED));
        }
        for (int i = outdoor + outdoorIndoor; i < indoor + outdoorIndoor; i++) {
            cellList.add(new Cell((long) i, i, FDN + i, 1400, "indoor", UNDEFINED));
        }
        return cellList;
    }

    static class OptimizationExecutorBuilder {
        private CmStore cmStore;
        private Execution execution;
        private PersistenceHandler persistenceHandler;
        private FlmPolicyInputEventHandler flmPolicyInputEventHandler;
        private OptimizationsDao optimizationsDao;
        private ExecutionDao executionDao;
        private PoeExecutionConsumerHandlerImpl executionConsumerHandlerImpl;
        private CellKpiCollection cellKpiCollection;
        private CellSettingCollection cellSettingCollection;
        private FlmMetricHelper flmMetricHelper;

        protected OptimizationExecutorBuilder cmStore(final CmStore cmStore) {
            this.cmStore = cmStore;
            return this;
        }

        protected OptimizationExecutorBuilder execution(final Execution execution) {
            this.execution = execution;
            return this;
        }

        protected OptimizationExecutorBuilder persistenceHandler(final PersistenceHandler persistenceHandler) {
            this.persistenceHandler = persistenceHandler;
            return this;
        }

        protected OptimizationExecutorBuilder flmPolicyInputEventHandler(final FlmPolicyInputEventHandler flmPolicyInputEventHandler) {
            this.flmPolicyInputEventHandler = flmPolicyInputEventHandler;
            return this;
        }

        protected OptimizationExecutorBuilder optimizationsDao(final OptimizationsDao optimizationsDao) {
            this.optimizationsDao = optimizationsDao;
            return this;
        }

        protected OptimizationExecutorBuilder executionDao(final ExecutionDao executionDao) {
            this.executionDao = executionDao;
            return this;
        }

        protected OptimizationExecutorBuilder executionConsumerHandlerImpl(final PoeExecutionConsumerHandlerImpl executionConsumerHandlerImpl) {
            this.executionConsumerHandlerImpl = executionConsumerHandlerImpl;
            return this;
        }

        protected OptimizationExecutorBuilder cellKpiCollection(final CellKpiCollection cellKpiCollection) {
            this.cellKpiCollection = cellKpiCollection;
            return this;
        }

        protected OptimizationExecutorBuilder cellSettingCollection(final CellSettingCollection cellSettingCollection) {
            this.cellSettingCollection = cellSettingCollection;
            return this;
        }

        protected OptimizationExecutorBuilder flmMetricHelper(final FlmMetricHelper flmMetricHelper) {
            this.flmMetricHelper = flmMetricHelper;
            return this;
        }

        protected OptimizationExecutor build() {
            final OptimizationExecutor optimizationExecutor = new OptimizationExecutor();

            optimizationExecutor.setCmStore(cmStore);
            optimizationExecutor.setExecution(execution);
            optimizationExecutor.setPersistenceHandler(persistenceHandler);
            optimizationExecutor.setExecutionDao(executionDao);
            optimizationExecutor.setFlmPolicyInputEventHandler(flmPolicyInputEventHandler);
            optimizationExecutor.setOptimizationsDao(optimizationsDao);
            optimizationExecutor.setExecutionConsumerHandler(executionConsumerHandlerImpl);
            optimizationExecutor.setCellKpiCollection(cellKpiCollection);
            optimizationExecutor.setCellSettingCollection(cellSettingCollection);
            optimizationExecutor.setFlmMetricHelper(flmMetricHelper);
            return optimizationExecutor;
        }
    }
}
