/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.cm.schema;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.exception.CmModelValidationException;
import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;

/**
 * Loads required CM elements and attributes and passes them over REST to <code>eric-cm-topology-model-sn</code>.
 */
public class CmModelValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmModelValidator.class);
    private static final String REQUIRED_ELEMENTS_FILE_PATH = "RequiredElements.json";

    public CmModelValidator() {
        // nothing to do at construction
    }

    /**
     * Reads the required CM elements and attributes from a JSON file and passes them to <code>eric-cm-topology-model-sn</code>.
     *
     * @param executor
     *            {@link CmRestExecutor} rest executor used to send POST request with CM model
     * @return {@link String} value of message returned from CM service
     * @throws CmModelValidationException
     *             thrown if an error occurs validating the CM model
     */
    public RestResponse<String> sendRequiredCmElementsForMediation(final CmRestExecutor executor) throws CmModelValidationException {
        final String model = loadResource();
        final RestResponse<String> requestResponse = executor.validateAndParseCmElementsAndAttributes(model);
        if (HttpStatus.SC_ACCEPTED != requestResponse.getStatus()) {
            throw new CmModelValidationException("Error validating CM model: " + requestResponse.getEntity());
        }
        return requestResponse;
    }

    private static String loadResource() {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(REQUIRED_ELEMENTS_FILE_PATH);
        } catch (final Exception e) { //NOSONAR Exception suitably logged
            LOGGER.warn("Error loading resource through filepath: {} - {}", REQUIRED_ELEMENTS_FILE_PATH, e.getClass());
            throw new IllegalStateException("Error reading required CM elements and attributes for FLM", e);
        }
    }

}