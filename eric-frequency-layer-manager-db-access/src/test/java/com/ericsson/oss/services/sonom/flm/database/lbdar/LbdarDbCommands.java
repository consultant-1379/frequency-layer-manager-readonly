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
package com.ericsson.oss.services.sonom.flm.database.lbdar;

import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.EXECUTION_ID;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.FLM_LBDAR;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.LEAKAGE_CELLS;
import static com.ericsson.oss.services.sonom.flm.database.lbdar.LbdarDbConstants.SECTOR_ID;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.database.optimization.OptimizationsWithOverlapInfoDaoImplTest;

/**
 * Class which contains SQL queries for the {@link OptimizationsWithOverlapInfoDaoImplTest} unit tests.
 */
public final class LbdarDbCommands {
    private LbdarDbCommands() {

    }

    public static List<String> createLbdarTable() {
        return Arrays.asList(String.format(
                "CREATE TABLE IF NOT EXISTS %s(" +
                        "%s VARCHAR(255) NOT NULL,%n" +
                        "%s bigint NOT NULL,%n" +
                        "%s text%n" + // H2 does not handle JSON yet so storing as text
                        ");",
                FLM_LBDAR,
                EXECUTION_ID,
                SECTOR_ID,
                LEAKAGE_CELLS),
                String.format("ALTER TABLE %s ADD PRIMARY KEY (%s, %s)",
                        FLM_LBDAR,
                        EXECUTION_ID,
                        SECTOR_ID));
    }
}
