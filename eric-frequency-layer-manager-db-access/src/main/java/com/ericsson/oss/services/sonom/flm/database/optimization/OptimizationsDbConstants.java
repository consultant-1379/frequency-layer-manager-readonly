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

/**
 * Class for holding database constants.
 */
public final class OptimizationsDbConstants {
    public static final String EXECUTION_ID = "execution_id";
    public static final String SECTOR_ID = "sector_id";
    public static final String LBQ = "lbq";
    public static final String CREATED = "created";
    public static final String FLM_OPTIMIZATIONS = "flm_optimizations";
    public static final String NUMBER_OF_SECTORS = "number_of_sectors";
    public static final String EMPTY_LBQ_TO_FILTER = "{\"sourceCellFdn\":\"\",\"sourceCellOssId\":-1,\"sourceUsersMove\":\"\"," +
     "\"targetCells\":[{\"targetCellFdn\":\"\",\"targetCellOssId\":-1,\"targetUsersMove\":\"\"}]}";

    private OptimizationsDbConstants() {
    }
}