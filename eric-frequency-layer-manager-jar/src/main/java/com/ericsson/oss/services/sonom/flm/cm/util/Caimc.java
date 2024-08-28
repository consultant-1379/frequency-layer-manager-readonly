/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.util;

/**
 * enum for storing all the possible values for CapabilityAwareIdleModeControl.
 *
 * <ul>
 *   <li>ACTIVATED - Screening will use formula for activated CapabilityAwareIdleModeControl.</li>
 *   <li>DEACTIVATED - Screening will use formula for deactivated CapabilityAwareIdleModeControl.</li>
 *   <li>undefined - Screening will use spid formula ignoring the value of CapabilityAwareIdleModeControl.</li>
 *   <li>NULL - Cells with a null value will be removed during screening.</li>
 * </ul>
 *
 */
public enum Caimc {
    ACTIVATED("ACTIVATED"),
    DEACTIVATED("DEACTIVATED"),
    UNDEFINED("undefined"),
    NULL("null");

    private final String caimcValue;

    Caimc(final String caimcValue) {
        this.caimcValue = caimcValue;
    }

    /**
     * Return the CapabilityAwareIdleModeControl value.
     * @return String with CapabilityAwareIdleModeControl value.
     */
    public String getCaimcValue() {
        return caimcValue;
    }
}
