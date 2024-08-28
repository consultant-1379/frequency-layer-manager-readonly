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
package com.ericsson.oss.services.sonom.flm.database.utils;

import java.util.Objects;

import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;

/**
 * A utility class for comparing {@link PaPolicyOutputEvent}s in tests.
 */
public class PAPolicyOutputEventCompare {

    private PAPolicyOutputEventCompare() { //util class
    }

    /**
     * For unit testing only. PAPolicyOutputEvents returned from the database will have an empty Sector containing only sectorId, this method is used
     * to compare output events in tests.
     *
     * @param p1
     *            a PaPolicyOutputEvent
     * @param p2
     *            a PaPolicyOutputEvent
     * @return true or false
     */

    public static boolean testEquals(final PaPolicyOutputEvent p1, final PaPolicyOutputEvent p2) {
        if (p1.equals(p2)) {
            return true;
        }
        if (p2 == null || p1.getClass() != p2.getClass()) {
            return false;
        }

        return Objects.equals(p1.getNameSpace(), p2.getNameSpace()) &&
                Objects.equals(p1.getName(), p2.getName()) &&
                Objects.equals(p1.getVersion(), p2.getVersion()) &&
                Objects.equals(p1.getSource(), p2.getSource()) &&
                Objects.equals(p1.getTarget(), p2.getTarget()) &&
                Objects.equals(p1.getFlmExecutionId(), p2.getFlmExecutionId()) &&
                Objects.equals(p1.getPaExecutionId(), p2.getPaExecutionId()) &&
                Objects.equals(p1.getSector().getSectorId(), p2.getSector().getSectorId()) &&
                Objects.equals(p1.getDegradationStatus(), p2.getDegradationStatus());
    }
}
