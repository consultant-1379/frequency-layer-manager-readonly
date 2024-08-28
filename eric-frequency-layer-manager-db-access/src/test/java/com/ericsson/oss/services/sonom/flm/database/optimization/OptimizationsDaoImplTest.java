/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
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

import static com.ericsson.oss.services.sonom.flm.database.optimization.TestDataBuilders.buildSampleLoadBalancingQuanta;
import static com.ericsson.oss.services.sonom.flm.database.optimization.TestDataBuilders.buildSampleTargetCells;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.shouldHaveThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.sonom.flm.database.FlmDatabaseAccess;
import com.ericsson.oss.services.sonom.flm.database.handlers.PolicyOutputEventListHandler;
import com.ericsson.oss.services.sonom.flm.database.parameters.PreparedStatementHandler;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

/**
 * Unit tests for {@link OptimizationsDaoImpl} class.
 */
public class OptimizationsDaoImplTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int WAIT_PERIOD_IN_SECONDS = 1;
    private static final String DUPLICATE_OBJECT_ERROR_CODE = "42710";
    private static final String EXEC_ID = "exec_id";
    private static final Long SECTOR_ID = 10L;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    public final OptimizationsDao objectUnderTest = new OptimizationsDaoImpl(MAX_RETRY_ATTEMPTS, WAIT_PERIOD_IN_SECONDS);

    @Mock
    private FlmDatabaseAccess databaseAccessMock;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void whenInsertOptimization_thenUpdateCountReturned() throws SQLException {
        final PolicyOutputEvent samplePolicyOutputEvent = new TestDataBuilders.PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenReturn(1);

        final int nrOfRecordsInserted = objectUnderTest.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isEqualTo(1);
        verify(databaseAccessMock, times(1)).executeUpdate(anyString(), any(Object[].class));
    }

    @Test
    public void whenFailToInsertOptimization_thenSQLExceptionThrown() throws SQLException {
        final PolicyOutputEvent samplePolicyOutputEvent = new TestDataBuilders.PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        when(databaseAccessMock.executeUpdate(
                anyString(), any(Object[].class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        objectUnderTest.insertOptimization(samplePolicyOutputEvent);
    }

    @Test
    public void whenFailToGetOptimizations_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(
                anyString(), any(PolicyOutputEventListHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        objectUnderTest.getOptimizations(EXEC_ID);
    }

    @Test
    public void whenFailToGetOptimizationsFiltered_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(
                anyString(), any(PolicyOutputEventListHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        objectUnderTest.getOptimizationsFiltered(EXEC_ID);
    }

    @Test
    public void whenFailToGetNumberOfSectors_thenSQLExceptionThrown() throws SQLException {
        when(databaseAccessMock.executeQuery(anyString(), any(PolicyOutputEventListHandler.class), any(PreparedStatementHandler.class)))
                .thenThrow(new SQLException());
        thrown.expect(SQLException.class);
        objectUnderTest.getNumberOfPolicyOutputEvents(EXEC_ID);
    }

    @Test
    public void whenFailToInsertOptimization_thenItRetried() throws SQLException {
        final PolicyOutputEvent samplePolicyOutputEvent = new TestDataBuilders.PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenThrow(new SQLException());

        try {
            objectUnderTest.insertOptimization(samplePolicyOutputEvent);
            shouldHaveThrown(SQLException.class);
        } catch (final SQLException e) {
            verify(databaseAccessMock, times(MAX_RETRY_ATTEMPTS)).executeUpdate(anyString(), any(Object[].class));
        }
    }

    @Test
    public void whenFailToInsertOptimizationTwice_thenUpdateCountReturnedOnThirdRetry() throws SQLException {
        final PolicyOutputEvent samplePolicyOutputEvent = new TestDataBuilders.PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenThrow(new SQLException())
                .thenThrow(new SQLException())
                .thenReturn(1);

        final int nrOfRecordsInserted = objectUnderTest.insertOptimization(samplePolicyOutputEvent);

        assertThat(nrOfRecordsInserted).isEqualTo(1);
        verify(databaseAccessMock, times(3)).executeUpdate(anyString(), any(Object[].class));
    }

    @Test
    public void whenDuplicateObjectErrorCodeInException_thenNoExceptionIsThrown() throws SQLException {
        final PolicyOutputEvent samplePolicyOutputEvent = new TestDataBuilders.PolicyOutputEventBuilder(EXEC_ID, SECTOR_ID)
                .withLbq(buildSampleLoadBalancingQuanta(buildSampleTargetCells()))
                .build();
        when(databaseAccessMock.executeUpdate(anyString(), any(Object[].class)))
                .thenThrow(new SQLException("test reason", DUPLICATE_OBJECT_ERROR_CODE));

        final int nrOfRecordsInserted = objectUnderTest.insertOptimization(samplePolicyOutputEvent);
        assertThat(nrOfRecordsInserted).isZero();
        verify(databaseAccessMock, times(1)).executeUpdate(anyString(), any(Object[].class));
    }
}
