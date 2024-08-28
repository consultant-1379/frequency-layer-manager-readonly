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
package com.ericsson.oss.services.sonom.flm.service.api.exceptions;


/**
 * Exception produced by <code>flm-service</code>.
 */
public class FlmAlgorithmException extends Exception {

    private static final long serialVersionUID = 3025158244592667232L;
    private final FlmServiceExceptionCode errorCode;

    public FlmAlgorithmException(final FlmServiceExceptionCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public FlmAlgorithmException(final FlmServiceExceptionCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FlmAlgorithmException(final FlmServiceExceptionCode errorCode, final Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode.getErrorCode();
    }

    public String getErrorMessage() {
        return errorCode.getErrorMessage();
    }

}
