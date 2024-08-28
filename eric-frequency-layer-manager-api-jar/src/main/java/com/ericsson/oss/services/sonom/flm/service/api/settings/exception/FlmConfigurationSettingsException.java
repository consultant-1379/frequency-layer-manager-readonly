/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.api.settings.exception;

/**
 * Exception produced by <code>flm-service</code>.
 */
public class FlmConfigurationSettingsException extends Exception {

    private static final long serialVersionUID = 2439925899954461925L;
    private final FlmConfigurationSettingsExceptionCode flmServiceExceptionCode;

    public FlmConfigurationSettingsException(final FlmConfigurationSettingsExceptionCode flmServiceExceptionCode) {
        super(flmServiceExceptionCode.getErrorMessage());
        this.flmServiceExceptionCode = flmServiceExceptionCode;
    }

    public FlmConfigurationSettingsException(final FlmConfigurationSettingsExceptionCode flmServiceExceptionCode, final Throwable throwable) {
        super(flmServiceExceptionCode.getErrorMessage(), throwable);
        this.flmServiceExceptionCode = flmServiceExceptionCode;
    }

    public int getErrorCode() {
        return flmServiceExceptionCode.getErrorCode();
    }

    public String getErrorMessage() {
        return flmServiceExceptionCode.getErrorMessage();
    }

}
