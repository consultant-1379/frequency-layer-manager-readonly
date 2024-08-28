/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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

/**
 * Utility functions for when database rows have been updated.
 */
public class RowsUpdated {

    /**
     * Determines if rows have been updated and runs the relevant {@link Runnable} for logging etc...
     * 
     * @param rowsUpdated
     *            The number of rows updated.
     * @param noRowsUpdated
     *            This will be run if no rows were updated.
     * @param someRowsUpdated
     *            This will be run if some rows were updated.
     */
    public void verifyRowsUpdated(final int rowsUpdated, final Runnable noRowsUpdated, final Runnable someRowsUpdated) {

        if (rowsUpdated == 0) {
            noRowsUpdated.run();
        } else {
            someRowsUpdated.run();
        }
    }
}
