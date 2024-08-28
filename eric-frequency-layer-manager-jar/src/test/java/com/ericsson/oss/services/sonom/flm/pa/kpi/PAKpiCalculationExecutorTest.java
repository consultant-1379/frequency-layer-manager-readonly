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

package com.ericsson.oss.services.sonom.flm.pa.kpi;

import static com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.oss.services.sonom.flm.kpi.KpiCalculationRequest;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionException;
import com.ericsson.oss.services.sonom.flm.pa.exceptions.PAExecutionInterruptedException;
import com.ericsson.oss.services.sonom.flm.pa.executor.PAExecutionLatch;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;

/**
 * Unit tests for {@link PAKpiCalculationExecutor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class PAKpiCalculationExecutorTest {

    private static final Timestamp PA_EXECUTION_WINDOW_START_TIME_STAMP = Timestamp.valueOf("2020-05-07 10:53:15.930");
    private static final Timestamp PA_EXECUTION_WINDOW_END_TIME_STAMP = Timestamp.valueOf("2020-05-07 16:53:15.930");
    private static final Map<String, String> stubbedCustomGlobalSettings = new HashMap<>();
    private static final String FLM_EXECUTION_ID = "FLM_1600701252-162";
    private static final String PA_EXECUTION_ID = "FLM_1600701252-162_1";
    private static final String WEEKEND_DAYS = "Saturday,Sunday";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private PAKpiRequestProcessor paKpiRequestProcessorMock;

    @Mock
    private PAExecution paExecutionMock;

    @Mock
    private Execution executionMock;

    @Mock
    private PAExecutionLatch paExecutionLatch;

    @Captor
    private ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor;

    @InjectMocks
    private PAKpiCalculationExecutor objectUnderTestMock;

    static {
        stubbedCustomGlobalSettings.put("stubbedGlobalSetting", "stub");
    }

    @Test
    public void whenStartingAPAKpiCalculation_thenCalculationsExecuteInOrder() throws Exception {

        when(paExecutionMock.getPaWindowStartTime()).thenReturn(PA_EXECUTION_WINDOW_START_TIME_STAMP);
        when(paExecutionMock.getPaWindowEndTime()).thenReturn(PA_EXECUTION_WINDOW_END_TIME_STAMP);
        when(paExecutionMock.getId()).thenReturn(PA_EXECUTION_ID);

        when(executionMock.getId()).thenReturn(FLM_EXECUTION_ID);
        when(executionMock.getWeekendDays()).thenReturn(WEEKEND_DAYS);
        when(executionMock.getCustomizedGlobalSettings()).thenReturn(stubbedCustomGlobalSettings);

        objectUnderTestMock.execute();

        final GroupAsserter groupAsserter = new GroupAsserter(kpiCalculationRequestArgumentCaptor, FLM_EXECUTION_ID);

        final InOrder inOrder = inOrder(executionMock, paKpiRequestProcessorMock);

        inOrder.verify(paKpiRequestProcessorMock).processRequest(kpiCalculationRequestArgumentCaptor.capture());
        groupAsserter.assertPAKpis();

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void whenPAKpiCalculationFailsAndThrowsAnFlmAlgorithmException_thenPAExecutionExceptionIsThrownByPAKpiCalculationExecutor()
            throws Exception {

        when(paExecutionMock.getId()).thenReturn(FLM_EXECUTION_ID);
        when(paExecutionMock.getPaWindowStartTime()).thenReturn(PA_EXECUTION_WINDOW_START_TIME_STAMP);
        when(paExecutionMock.getPaWindowEndTime()).thenReturn(PA_EXECUTION_WINDOW_END_TIME_STAMP);

        when(executionMock.getId()).thenReturn(FLM_EXECUTION_ID);
        when(executionMock.getWeekendDays()).thenReturn(WEEKEND_DAYS);
        when(executionMock.getCustomizedGlobalSettings()).thenReturn(stubbedCustomGlobalSettings);

        doThrow(new FlmAlgorithmException(FlmServiceExceptionCode.KPI_CALCULATION_RETRIES_EXPIRED)).when(paKpiRequestProcessorMock)
                .processRequest(kpiCalculationRequestArgumentCaptor.capture());

        thrown.expect(instanceOf(PAExecutionException.class));

        objectUnderTestMock.execute();
    }

    @Test
    public void whenStartingAPAKpiCalculation_andInterruptSignalIsCalled_beforeFirstKpiCall_thenPAExecutionInterruptedExceptionIsThrown()
            throws Exception {
        doThrow(PAExecutionInterruptedException.class).when(paExecutionLatch).verifyNotInterruptedAndContinue();

        when(paExecutionMock.getPaWindowStartTime()).thenReturn(PA_EXECUTION_WINDOW_START_TIME_STAMP);
        when(paExecutionMock.getPaWindowEndTime()).thenReturn(PA_EXECUTION_WINDOW_END_TIME_STAMP);

        verifyExecutionFails();

        final InOrder inOrder = inOrder(executionMock, paKpiRequestProcessorMock);
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyExecutionFails() throws PAExecutionException {
        try {
            objectUnderTestMock.execute();
            shouldHaveThrown(PAExecutionInterruptedException.class);
        } catch (final PAExecutionInterruptedException ignored) {
        }
    }

    private static final class GroupAsserter {

        private static final String WINDOW_START_TIMESTAMP = DateTimeFormatter.ISO_DATE_TIME
                .format(PA_EXECUTION_WINDOW_START_TIME_STAMP.toLocalDateTime());
        private static final String WINDOW_END_TIMESTAMP = DateTimeFormatter.ISO_DATE_TIME
                .format(PA_EXECUTION_WINDOW_END_TIME_STAMP.toLocalDateTime());
        private final String flmExecutionId;
        private final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor;

        private GroupAsserter(final ArgumentCaptor<KpiCalculationRequest> kpiCalculationRequestArgumentCaptor, final String flmExecutionId) {
            this.kpiCalculationRequestArgumentCaptor = kpiCalculationRequestArgumentCaptor;
            this.flmExecutionId = flmExecutionId;
        }

        private void assertPAKpis() throws IOException {
            final KpiCalculationRequest expectedRequestCallOfGroup_1 = KpiCalculationRequest.KpiCalculationRequestCreator.create(SUCCEEDED, false)
                    .loadGroupKpis("kpiCalculationRequests/pa_kpis.json")
                    .withStartTimeStamp(WINDOW_START_TIMESTAMP)
                    .withEndTimeStamp(WINDOW_END_TIMESTAMP)
                    .withExecutionId(flmExecutionId)
                    .withWeekendDays(WEEKEND_DAYS)
                    .withAdditionalParameters(Collections.singletonMap("param.stubbed_global_setting", "stub"))
                    .build();

            assertThat(kpiCalculationRequestArgumentCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(expectedRequestCallOfGroup_1);
        }
    }
}
