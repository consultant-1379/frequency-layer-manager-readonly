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
package com.ericsson.oss.services.sonom.flm.test.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Utility class to read testing assertion files in CSV format
 */
public class CsvReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReader.class);

    private CsvReader() {
        // intentionally private, utility class
    }

    /**
     * Given a list of Strings, each representing a row, this fetches the header assuming its the first row.
     * 
     * @param csvList
     *            the list containing the header on the first row.
     * @return the header a list, each element representing a column in the header.
     */
    public static List<String> getHeader(final List<String> csvList) {
        return Arrays.asList(csvList.get(0).split(","));
    }

    /**
     * Given a resource file path, will parse the CSV file found.
     * 
     * @param includeHeaderInResult
     *            true to include the header as the first row in the table.
     * @param resourceFilePath
     *            the resource relative path to the csv file.
     * @return list of Strings, each element representing a row in the CSV file.
     */
    public static List<String> getCsvAsList(final boolean includeHeaderInResult, final String resourceFilePath) {

        final Reader reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilePath));
        final ICsvListReader csvListReader = new CsvListReader(reader, CsvPreference.STANDARD_PREFERENCE);
        final List<String> rows = new ArrayList<>();
        try {
            csvListReader.read();

            if (includeHeaderInResult) {
                rows.add(csvListReader.getUntokenizedRow());
            }
            while (csvListReader.read() != null) {
                rows.add(csvListReader.getUntokenizedRow());
            }
            LOGGER.info("Successfully read CSV file {}", resourceFilePath);
        } catch (final IOException e) {
            LOGGER.error("Failed to read CSV file {}", resourceFilePath, e);
        }
        return rows;
    }

    /**
     * Given a resource file path and filter, will parse the CSV file found and include rows that contain the filter.
     *
     * @param includeHeaderInResult
     *            true to include the header as the first row in the table.
     * @param resourceFilePath
     *            the resource relative path to the csv file.
     * @param filter
     *            a string used to specify what rows to be included
     * @return list of Strings, each element representing a row in the CSV file.
     */
    public static List<String> getCsvAsListOfRowsContainingString(final boolean includeHeaderInResult,
                                                                  final String resourceFilePath, final String filter) {
        final List<String> allRows = getCsvAsList(includeHeaderInResult, resourceFilePath);
        final List<String> filteredRows = new ArrayList<>();

        if (includeHeaderInResult) {
            filteredRows.add(allRows.get(0));
        }

        allRows.remove(0); // Remove header

        for (final String row : allRows) {
            if(row.contains(filter)) {
                filteredRows.add(row);
            }
        }

        return filteredRows;
    }
}
