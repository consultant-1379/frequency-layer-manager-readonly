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

package com.ericsson.oss.services.sonom.flm.util;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link RowsUpdated} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RowsUpdatedTest {

    private static final RowsUpdated ROWS_UPDATED = new RowsUpdated();
    private static final Logger LOGGER = LoggerFactory.getLogger(RowsUpdatedTest.class);

    @Mock
    private final Runnable noRowsUpdated = () -> LOGGER.info("No Rows Updated");
    @Mock
    private final Runnable someRowsUpdated = () -> LOGGER.info("Some Rows Updated");

    @Test
    public void whenRowsUpdated_thenReturnTrue() {
        ROWS_UPDATED.verifyRowsUpdated(1, noRowsUpdated, someRowsUpdated);
        verify(someRowsUpdated, times(1)).run();
        verify(noRowsUpdated, never()).run();
    }

    @Test
    public void whenNoRowsUpdated_thenReturnFalse() {
        ROWS_UPDATED.verifyRowsUpdated(0, noRowsUpdated, someRowsUpdated);
        verify(someRowsUpdated, never()).run();
        verify(noRowsUpdated, times(1)).run();
    }
}