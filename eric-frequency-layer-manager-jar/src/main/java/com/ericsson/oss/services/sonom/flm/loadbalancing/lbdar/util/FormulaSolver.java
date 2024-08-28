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
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.util;

/**
 * Simply reusable calculation elements for the profile change calculation process.
 */
public class FormulaSolver {

    private static final int MAX_ITERATION_SIZE = 20;

    private FormulaSolver() {
    }

    /**
     * Calculates Coupling Factor at the profile change calculation step.
     * @param sourceUsers number of users at Source cell.
     * @param sourceToTarget distribution value from Source to Target
     * @param targetUsers number of users at Target cell.
     * @param targetToSource distribution value from Target to Source
     * @return the Coupling Factor.
     */
    public static float calculateCValue(final float sourceUsers,
                                        final float sourceToTarget,
                                        final float targetUsers,
                                        final float targetToSource) {
        return (sourceUsers * sourceToTarget) / (targetUsers * targetToSource);
    }

    /**
     * Calculates the new balanced distribution value at the profile change calculation step.
     * @param numberOfUsersInTarget number of users at target cell.
     * @param targetToSource distribution value from Target to Source.
     * @param c Coupling factor
     * @param numberOfUsersInSource number of users at Source cell.
     * @return the new distribution value which is needed for the balanced state between the cells.
     */
    public static int calculateBalancedDistribution(final long numberOfUsersInTarget,
                                                    final float targetToSource,
                                                    final float c,
                                                    final long numberOfUsersInSource) {
        return Math.round(((float) numberOfUsersInTarget * targetToSource * c) / (float) numberOfUsersInSource);
    }

    /**
     * Calculates the number of users which are leaking from target cell to 3rd cell.
     * @param targetUsers number of users at Target cell
     * @param targetToThirdDist distribution value from target cell to 3rd cell.
     * @param thirdPartyUsers number of users at 3rd cell
     * @param thirdPartyToTarget distribution value from 3rd cell to target cell
     * @param step index of iterator.
     * @return the size of leakage.
     */
    public static long leakageSimulation(final long targetUsers, final Float targetToThirdDist, final long thirdPartyUsers,
                                          final Float thirdPartyToTarget, final long step) {
        final long newTargetUsers = Math.round(targetUsers * (1 - targetToThirdDist / 100) + thirdPartyUsers * thirdPartyToTarget / 100);
        final long newThirdPartyUsers = Math.round(thirdPartyUsers * (1 - thirdPartyToTarget / 100) + targetUsers * targetToThirdDist / 100);

        final long actualDifference = Math.abs(Math.round(thirdPartyUsers * thirdPartyToTarget / 100 - targetUsers * targetToThirdDist / 100));

        long result = newTargetUsers;

        if (actualDifference > 0 && step < MAX_ITERATION_SIZE) {
            result = leakageSimulation(newTargetUsers, targetToThirdDist, newThirdPartyUsers, thirdPartyToTarget, step + 1);
        }
        return result;
    }
}
