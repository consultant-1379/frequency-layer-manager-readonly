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

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarReversionException;

/**
 * ChangeElementCalculator is responsible the create ChangeElements from the given list of {@link EnrichedProfileChanges}.
 * The implementation of this interface should handle restricted properties of lbdar profile, shared profiles and number free profiles on node
 */
public interface ReversionElementCalculator {
    /**
     * It calculates the reversions for all the ProposedChanges present in the optimizationElement passed.
     * Then creates and returns a Reversion ChangeElement containing these reversed ProposedChanges.
     *
     * @param optimizationElement instance of {@link ChangeElement}
     * @return returns a {@link ChangeElement} or null if was not able to create the ChangeElement
     * @throws LbdarReversionException if failed to calculate Reversion
     */
    ChangeElement calculateReversionElement(ChangeElement optimizationElement) throws LbdarReversionException;
}
