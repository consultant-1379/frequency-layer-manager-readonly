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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;


/**
 * Unit tests for {@link QueryUtilsTest} class.
 */
public class QueryUtilsTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void inQueryBuilderThrowsIllegalArgumentExceptionOnNull() {
        thrown.expect(IllegalArgumentException.class);
        QueryUtils.inQueryBuilder('a', 'b', null);
    }

    @Test
    public void inQueryBuilderWorksForMixOfTypes() {
        final String queryResult = QueryUtils.inQueryBuilder(Boolean.TRUE, 1.3, 'c', "D", ExecutionState.KPI_PROCESSING_GROUP_1);
        assertThat(queryResult).isEqualTo(" ('true','1.3','c','D','KPI_PROCESSING_GROUP_1')");
    }

    @Test
    public void inQueryWorksForParameterizedTypes() {
        final String queryResult = QueryUtils.inQueryBuilder(3);
        assertThat(queryResult).isEqualTo(" (?,?,?)");
    }

    @Test
    public void inQueryThrowsExceptionForParameterizedTypesIfSizeLessThanOrEqualZero() {
        thrown.expect(IllegalArgumentException.class);
        QueryUtils.inQueryBuilder(0);
    }
}
