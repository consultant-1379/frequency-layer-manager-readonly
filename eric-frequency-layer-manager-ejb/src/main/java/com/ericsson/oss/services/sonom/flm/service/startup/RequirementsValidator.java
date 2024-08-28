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

package com.ericsson.oss.services.sonom.flm.service.startup;

import javax.ejb.Local;

/**
 * Implementations of this interface will validate required CM, PM and KPI data against the relevant services.
 */
@Local
public interface RequirementsValidator {

    /**
     * Validates required CM data against the relevant services.
     */
    void validateCm();

    /**
     * Validates required PM and KPI data against the relevant services.
     */
    void validateKpis();

}
