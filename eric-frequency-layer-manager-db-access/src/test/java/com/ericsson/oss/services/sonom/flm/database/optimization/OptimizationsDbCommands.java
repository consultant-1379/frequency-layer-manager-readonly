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
package com.ericsson.oss.services.sonom.flm.database.optimization;

import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.CREATED;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.FLM_OPTIMIZATIONS;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.LBQ;
import static com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsDbConstants.SECTOR_ID;

import java.util.Arrays;
import java.util.List;

/**
 * Class which contains SQL queries for the {@link OptimizationsWithOverlapInfoDaoImplTest} unit tests.
 */
public final class OptimizationsDbCommands {
    private OptimizationsDbCommands() {

    }

    public static List<String> createOptimizationTable() {
        return Arrays.asList(String.format(
                "CREATE TABLE IF NOT EXISTS %s(" +
                        "%s VARCHAR(255) NOT NULL,%n" +
                        "%s bigint NOT NULL,%n" +
                        "%s json,%n" +
                        "%s TIMESTAMP NOT NULL%n" +
                        ");",
                FLM_OPTIMIZATIONS,
                EXECUTION_ID,
                SECTOR_ID,
                LBQ,
                CREATED),
                String.format("ALTER TABLE %s ADD PRIMARY KEY (%s, %s)",
                FLM_OPTIMIZATIONS,
                EXECUTION_ID,
                SECTOR_ID));
    }
}
