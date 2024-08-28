/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import java.sql.SQLException;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarException;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarUnexpectedException;

/**
 * ProfileChangeCalculator returns a list of desired idleModePrioAtRelease ({@link ProfileChanges}) for the given {@link EnrichedProfileChanges}
 * object. The list can contain LBDAR profiles for the source cell and for some target cells if needed
 */
public interface ProfileChangesCalculator {

    ProfileChanges calculateProfileChanges(EnrichedPolicyOutputEvent changesCalculationInput)
            throws LbdarException, LbdarUnexpectedException, SQLException;
}
