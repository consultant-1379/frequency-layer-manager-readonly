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
package com.ericsson.oss.services.sonom.flm.service.startup.bra;

import javax.ejb.Local;

/**
 * Implementations of this interface will listen to notifications from the Backup and Restore service.
 */
@Local
public interface BackupRestoreController {

    /**
     * Listen for Backup and Restore notifications.
     */
    void listen();
}