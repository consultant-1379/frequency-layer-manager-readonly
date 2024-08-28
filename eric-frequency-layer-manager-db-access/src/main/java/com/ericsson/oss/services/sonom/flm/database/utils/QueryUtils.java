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
package com.ericsson.oss.services.sonom.flm.database.utils;

/**
 * Helper methods to build SQL queries.
 */
public final class QueryUtils {

    private QueryUtils() {
        // intentionally private
    }

    /**
     * Given an array of values, produces values as {@link String} in SQL format.
     * E.g.
     * Given true, 1.3, 'c', "D", ExecutionState.STARTED
     * result will be (including leading white space):
     * ('true','1.3','c','D','STARTED')
     *
     * @param values any object that can be converted to a String.  Passing null will result in {@link IllegalArgumentException}
     * @return {@link String} of SQL formatted values
     */
    public static String inQueryBuilder(final Object... values) {

        final StringBuilder inValues = new StringBuilder(" (");
        for (final Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException("null values not valid for in query");
            }
            inValues.append('\'').append(value).append("',");
        }
        inValues.deleteCharAt(inValues.length() - 1);
        inValues.append(')');

        return inValues.toString();
    }

    /**
     * Given a size for parameters, produces values as {@link String} in SQL format.
     * E.g.
     * Given 3
     * result will be (including leading white space):
     * (?,?,?)
     *
     * @param numberOfValues the number of parameters for the in query
     * @return {@link String} of SQL parameters
     */
    public static String inQueryBuilder(final int numberOfValues) {

        if (numberOfValues <= 0) {
            throw new IllegalArgumentException("Must be a positive integer");
        }
        final StringBuilder inValues = new StringBuilder(" (");
        for (int i = 0; i < numberOfValues; i++) {

            inValues.append('?').append(',');
        }
        inValues.deleteCharAt(inValues.length() - 1);
        inValues.append(')');

        return inValues.toString();
    }
}
