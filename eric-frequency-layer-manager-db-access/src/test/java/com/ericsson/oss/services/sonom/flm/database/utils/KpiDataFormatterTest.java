/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database.utils;

import static com.ericsson.oss.services.sonom.flm.database.kpi.KpiDbCommands.COVERAGE_BALANCE_RATIO_DISTANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link KpiDataFormatter} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiDataFormatterTest {

    @Mock
    private ResultSet resultSet;

    @Test
    public void whenKpiValueIsNotNull_thenExpectedKpiValueIsReturned() throws SQLException {
        final double value = 2.3D;
        when(resultSet.getDouble(COVERAGE_BALANCE_RATIO_DISTANCE)).thenReturn(value);
        when(resultSet.getObject(COVERAGE_BALANCE_RATIO_DISTANCE)).thenReturn(value);
        final String kpiValue = KpiDataFormatter.getStringKpiValue(resultSet, COVERAGE_BALANCE_RATIO_DISTANCE);
        assertThat(kpiValue).isEqualTo(String.valueOf(value));
    }

    @Test
    public void whenKpiValueIsNull_thenNullStringIsReturned() throws SQLException {
        final String value = null;
        when(resultSet.getObject(COVERAGE_BALANCE_RATIO_DISTANCE)).thenReturn(value);
        final String kpiValue = KpiDataFormatter.getStringKpiValue(resultSet, COVERAGE_BALANCE_RATIO_DISTANCE);
        assertThat(kpiValue).isEqualTo(String.valueOf(value));
    }
}
