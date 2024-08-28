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

/**
 * Utility class to generate profile name.
 */
public class ProfileNameGenerator {
    private ProfileNameGenerator() {
    }

    /**
     * This method generates a new name for idleModePrioAtRelease based on the cgi value of a cell and old value of idleModePrioAtRelease.
     * returns cgi as the profile name if it unique or cgi_x where x is the next unused number
     * example cgi: 310-410-43234-23
     * example profile name: 310-410-43234-23, 310-410-43234-23_1, 310-410-43234-23_2
     * @param cgi a cgi value of a cell
     * @param oldName the old name of the profile
     * @return it returns the nome for the idleModePrioAtRelease Topology Object
     */
    public static String generateProfileName(final String cgi, final String oldName) {
        try {
            if (oldName.startsWith(cgi)) {
                final String suffix = oldName.substring(cgi.length());
                if (suffix.isEmpty()) {
                    return cgi + '_' + 1;
                }

                if (suffix.charAt(0) == '_') {
                    final int number = Integer.parseInt(suffix.substring(1));
                    return cgi + '_' + (number + 1);
                }
            }
            return cgi;
        } catch (NumberFormatException e) {
            return cgi;
        }
    }
}
