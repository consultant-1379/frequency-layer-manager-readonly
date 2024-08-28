/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020-2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.api.settings.exception;

import java.util.Collections;
import java.util.List;
import javax.ejb.ApplicationException;

/**
 * Exception produced by <code>flm-service</code>.
 */
@ApplicationException
public class ConfigurationSettingsJsonValidationException extends Exception {

    private static final long serialVersionUID = 6419164357525729559L;
    private final int errorCode;
    private final List<String> errorMessage;

    public ConfigurationSettingsJsonValidationException(final FlmConfigurationSettingsExceptionCode errorCode,
            final List<String> errorMessage, final Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = Collections.unmodifiableList(errorMessage);
    }

    public ConfigurationSettingsJsonValidationException(final FlmConfigurationSettingsExceptionCode errorCode,
            final String errorMessage, final Throwable throwable) {
        this(errorCode, Collections.singletonList(errorMessage), throwable);
    }

    public ConfigurationSettingsJsonValidationException(final FlmConfigurationSettingsExceptionCode errorCode,
            final List<String> errorMessage) {
        super();
        this.errorCode = errorCode.getErrorCode();
        this.errorMessage = Collections.unmodifiableList(errorMessage);
    }

    public ConfigurationSettingsJsonValidationException(final FlmConfigurationSettingsExceptionCode errorCode,
            final String errorMessage) {
        this(errorCode, Collections.singletonList(errorMessage));
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return String.format("errorCode: '%s', errorMessage: '%s'", errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return String.format("%s:: {%s}", getClass().getSimpleName(), getMessage());
    }
}
