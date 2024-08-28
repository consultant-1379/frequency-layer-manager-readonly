/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.pa.retrieverexecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDao;
import com.ericsson.oss.services.sonom.flm.database.lbdar.LeakageCell;
import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDao;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.util.changeelement.ChangeElementRetriever;

@RunWith(MockitoJUnitRunner.class)
public class PACellIdRetrieverExecutorTest {

    private static final String SOURCE_OF_CHANGE = "alg_FLM_1";
    private static final String EXECUTION_ID = "EXECUTION_1";
    private static final String SCHEDULE = "SCHEDULE";
    private static final Long SIX_HOURS = 21600000L;
    private static final Long TEST_PA_WINDOW_END_TIME = 1617984000000L;
    private static final long CHANGE_ID_1 = 1L;
    private static final long CHANGE_ID_2 = 2L;
    private static List<Long> EXPECTED_SECTORS = Arrays.asList(CHANGE_ID_1, CHANGE_ID_2);
    private static final String SOURCE_CELL_FDN_1 = "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=1";
    private static final String SOURCE_CELL_FDN_2 = "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=2";
    private static final String TARGET_CELL_FDN_11 =
            "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=1,RetSubUnit=1";
    private static final String TARGET_CELL_FDN_12 =
            "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=1,RetSubUnit=2";
    private static final String TARGET_CELL_FDN_21 =
            "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=2,RetSubUnit=1";
    private static final String TARGET_CELL_FDN_22 =
            "SubNetwork=SON,MeContext=Node_1,ManagedElement=1,AntennaUnitGroup=5,AntennaNearUnit=2,RetSubUnit=2";
    private static final String TARGET_USER_MOVE = "TARGET_USER_MOVE";

    private static final String LEAKAGE_CELL_FDN_1 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=NODE1," +
            "ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=NODE100003-1";
    private static final String LEAKAGE_CELL_FDN_2 = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=ERBS-SUBNW-1,MeContext=NODE1," +
            "ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=NODE100003-2";
    private static final Integer LEAKAGE_CELL_OSSID_1 = 1;

    private static PAExecution paExecution;
    private static PACellIdRetrieverExecutor objectUnderTest;
    private static List<ChangeElement> changeElementList;
    private static List<PolicyOutputEvent> policyOutputEvents;
    private static Set<LeakageCell> leakagedCells;

    private static List<TopologyObjectId> expectedCells;
    private static List<TopologyObjectId> expectedleakagedCells;

    @Mock
    private ChangeElementRetriever changeElementRetriever;

    @Mock
    private OptimizationsDao optimizationsDao;

    @Mock
    private CmRestExecutor cmRestExecutor;

    @Mock
    private LbdarDao lbdarDao;

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUp() {
        paExecution = new PAExecution(0,
                SCHEDULE,
                new Timestamp(TEST_PA_WINDOW_END_TIME - SIX_HOURS),
                new Timestamp(TEST_PA_WINDOW_END_TIME),
                EXECUTION_ID);
        changeElementList = new ArrayList<>();
        policyOutputEvents = new ArrayList<>();
    }

    @Before
    public void updateLists() {
        updateChangeElementList();
        updatePolicyOutputEvent();
    }

    @After
    public void clearLists() {
        changeElementList.clear();
        policyOutputEvents.clear();
    }

    @Test
    public void whenRetrieveLBQ_andNoLeakageCell_thenSectorAndCellIdAvailableWithJustTheCellsInTheLBQ() throws PAExecutionException, SQLException {
        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenReturn(policyOutputEvents);
        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);
        final Map<Long, List<TopologyObjectId>> result = objectUnderTest.execute();

        softly.assertThat(result).hasSize(2);
        assertThat(result).containsOnlyKeys(EXPECTED_SECTORS);

        for (final List<TopologyObjectId> cells : result.values()) {
            assertThat(expectedOptimizationCells()).containsAll(cells);
        }
    }

    @Test
    public void whenRetrieveLBQ_andLeakageCellExist_thenSectorAndCellIdAvailableWithBothCellsInTheLBQAndLeakageCells()
            throws PAExecutionException, SQLException {
        leakagedCells = new HashSet<>(2);
        leakagedCells.add(new LeakageCell(LEAKAGE_CELL_FDN_1, LEAKAGE_CELL_OSSID_1));
        leakagedCells.add(new LeakageCell(LEAKAGE_CELL_FDN_2, LEAKAGE_CELL_OSSID_1));

        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenReturn(policyOutputEvents);
        when(lbdarDao.getLeakageCells(any(String.class), any(Long.class))).thenReturn(leakagedCells);

        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);
        final Map<Long, List<TopologyObjectId>> result = objectUnderTest.execute();

        softly.assertThat(result).hasSize(2);
        assertThat(result).containsOnlyKeys(EXPECTED_SECTORS);
        final List<TopologyObjectId> expectedLbqAndLeakageCells = expectedOptimizationCells();
        expectedLbqAndLeakageCells.addAll(expectedLeakageCells());

        for (final List<TopologyObjectId> cells : result.values()) {
            assertThat(expectedLbqAndLeakageCells).containsAll(cells);
        }
    }

    @Test
    public void whenRetrieveLBQ_andLeakageCellIsAlreadyASourceCell_thenTheLeakageCellShouldNotBeAddedTwice()
            throws PAExecutionException, SQLException {
        leakagedCells = new HashSet<>(1);
        leakagedCells.add(new LeakageCell(SOURCE_CELL_FDN_1, LEAKAGE_CELL_OSSID_1));

        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenReturn(policyOutputEvents);
        when(lbdarDao.getLeakageCells(any(String.class), any(Long.class))).thenReturn(leakagedCells);

        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);
        final Map<Long, List<TopologyObjectId>> result = objectUnderTest.execute();

        softly.assertThat(result).hasSize(2);
        assertThat(result).containsOnlyKeys(EXPECTED_SECTORS);

        assertThat(result.get(CHANGE_ID_1)).hasSize(5);
        assertThat(result.get(CHANGE_ID_2)).hasSize(2);
    }

    @Test
    public void whenRetrievedChangeElementListIsEmpty_thenReturnEmptyMap() throws PAExecutionException {
        changeElementList.clear();
        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);

        final Map<Long, List<TopologyObjectId>> result = objectUnderTest.execute();

        assertThat(result).isEmpty();
    }

    @Test
    public void whenRetrieveLBQIsEmpty_thenReturnEmptyMap() throws PAExecutionException, SQLException {
        policyOutputEvents.clear();
        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenReturn(policyOutputEvents);
        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);

        final Map<Long, List<TopologyObjectId>> result = objectUnderTest.execute();

        assertThat(result).isEmpty();
    }

    @Test
    public void whenSQLExceptionOccursReadingOptimizations_thenThrowPAExecutionException() throws SQLException {
        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenThrow(new SQLException());
        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);
        assertThatThrownBy(() -> objectUnderTest.execute())
                .isInstanceOf(PAExecutionException.class)
                .hasMessageContaining("SQLException");
    }

    @Test
    public void whenSQLExceptionOccursReadingLeakageCells_thenThrowPAExecutionException() throws SQLException {
        when(changeElementRetriever.retrieveChangeElementList()).thenReturn(changeElementList);
        when(optimizationsDao.getOptimizationsFiltered(EXECUTION_ID)).thenReturn(policyOutputEvents);
        when(lbdarDao.getLeakageCells(any(String.class), any(Long.class))).thenThrow(new SQLException());
        objectUnderTest = new PACellIdRetrieverExecutor(paExecution, SOURCE_OF_CHANGE, cmRestExecutor,
                changeElementRetriever, optimizationsDao, lbdarDao);

        assertThatThrownBy(() -> objectUnderTest.execute())
                .isInstanceOf(PAExecutionException.class)
                .hasMessageContaining("SQLException");
    }

    private void updateChangeElementList() {
        final ChangeElement ce1 = new ChangeElement();
        final ChangeElement ce2 = new ChangeElement();
        ce1.setChangeId(Long.toString(CHANGE_ID_1));
        ce1.setExecutionId(EXECUTION_ID);
        ce2.setChangeId(Long.toString(CHANGE_ID_2));
        ce2.setExecutionId(EXECUTION_ID);
        changeElementList.add(ce1);
        changeElementList.add(ce2);
    }

    private void updatePolicyOutputEvent() {
        final TargetCell tc11 = new TargetCell(TARGET_CELL_FDN_11, 11, TARGET_USER_MOVE);
        final TargetCell tc12 = new TargetCell(TARGET_CELL_FDN_12, 12, TARGET_USER_MOVE);
        final TargetCell tc21 = new TargetCell(TARGET_CELL_FDN_21, 21, TARGET_USER_MOVE);
        final TargetCell tc22 = new TargetCell(TARGET_CELL_FDN_22, 22, TARGET_USER_MOVE);
        final List<TargetCell> tcList1 = new ArrayList<>();
        tcList1.add(tc11);
        tcList1.add(tc12);
        final List<TargetCell> tcList2 = new ArrayList<>();
        tcList1.add(tc21);
        tcList1.add(tc22);
        final ProposedLoadBalancingQuanta lbq1 = new ProposedLoadBalancingQuanta(SOURCE_CELL_FDN_1, 1, "2", tcList1);
        final ProposedLoadBalancingQuanta lbq2 = new ProposedLoadBalancingQuanta(SOURCE_CELL_FDN_2, 2, "2", tcList2);

        final PolicyOutputEvent policyOutputEvent1 = new PolicyOutputEvent(null, null, null, null, null, CHANGE_ID_1, EXECUTION_ID, lbq1, null);
        final PolicyOutputEvent policyOutputEvent2 = new PolicyOutputEvent(null, null, null, null, null, CHANGE_ID_2, EXECUTION_ID, lbq2, null);
        policyOutputEvents.add(policyOutputEvent1);
        policyOutputEvents.add(policyOutputEvent2);
    }

    private List<TopologyObjectId> expectedOptimizationCells() {
        expectedCells = new ArrayList<>();
        expectedCells.add(new TopologyObjectId(SOURCE_CELL_FDN_1, 1));
        expectedCells.add(new TopologyObjectId(TARGET_CELL_FDN_11, 11));
        expectedCells.add(new TopologyObjectId(TARGET_CELL_FDN_12, 12));
        expectedCells.add(new TopologyObjectId(SOURCE_CELL_FDN_2, 2));
        expectedCells.add(new TopologyObjectId(TARGET_CELL_FDN_21, 21));
        expectedCells.add(new TopologyObjectId(TARGET_CELL_FDN_22, 22));
        return expectedCells;
    }

    private List<TopologyObjectId> expectedLeakageCells() {
        expectedleakagedCells = new ArrayList<>();
        expectedleakagedCells.add(new TopologyObjectId(LEAKAGE_CELL_FDN_1, LEAKAGE_CELL_OSSID_1));
        expectedleakagedCells.add(new TopologyObjectId(LEAKAGE_CELL_FDN_2, LEAKAGE_CELL_OSSID_1));
        return expectedleakagedCells;
    }
}
