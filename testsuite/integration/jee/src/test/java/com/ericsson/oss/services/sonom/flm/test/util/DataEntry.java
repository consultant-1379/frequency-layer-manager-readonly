/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.test.util;

/**
 * Represents a field of data for a cell (e.g. KPI/SETTING/CM_ATTRIBUTE).
 */
public class DataEntry {

    private final String fdn;
    private final String dataType;
    private final String dataName;
    private final String dataValue;

    public DataEntry(final String fdn, final String dataType, final String dataName, final String dataValue) {
        this.fdn = fdn;
        this.dataType = dataType;
        this.dataName = dataName;
        this.dataValue = dataValue;
    }

    public String getFdn() {
        return fdn;
    }
    public String getDataType() {
        return dataType;
    }

    public String getDataName() {
        return dataName;
    }

    public String getDataValue() {
        return dataValue;
    }
}
