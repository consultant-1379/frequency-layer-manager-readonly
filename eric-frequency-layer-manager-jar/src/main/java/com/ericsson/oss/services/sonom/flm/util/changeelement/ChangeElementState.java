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

package com.ericsson.oss.services.sonom.flm.util.changeelement;

/**
 * ENUM defining the possible states returned from trying to persist a change to the CM DB.
 * SENT - The change was successfully persisted
 * EXISTS - The change was already existing in the CM DB.
 * FAILED - The change failed to be persisted in the CM DB.
 */
public enum ChangeElementState {
    SENT, EXISTS, FAILED
}
