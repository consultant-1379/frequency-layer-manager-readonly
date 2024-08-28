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

/**
 * ChangeElementCalculator is responsible the create ChangeElements from the given list of {@link EnrichedProfileChanges}.
 * The implementation of this interface should handle restricted properties of lbdar profile, shared profiles and number free profiles on node
 */
public interface ChangeElementCalculator {
    /**
     * It calculates and returns a ChangeElement for the given profileChanges.
     * @param profileChanges in instance of {@link EnrichedProfileChanges}
     * @return returns a {@link ChangeElement} or null if was not able to create the ChangeElement
     */
    ChangeElement calculateChangeElement(EnrichedProfileChanges profileChanges);
}
