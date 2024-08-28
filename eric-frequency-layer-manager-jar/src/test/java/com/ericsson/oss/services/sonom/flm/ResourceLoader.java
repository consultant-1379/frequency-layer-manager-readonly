/**
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;

/**
 * Utility class for loading in files specific to the Test resources root folder.
 */
public class ResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

    private ResourceLoader() {
        // Utility class with intentional private constructor.
    }

    /**
     * Loads the {@link String} filename from the Resources test folder.
     * 
     * @param fileName
     *            The filename to load in.
     * @return A {@link String} representation of the file specified.
     */
    public static String loadResource(final String fileName) {
        try {
            return ResourceLoaderUtils.getClasspathResourceAsString(fileName);
        } catch (final Exception e) { //NOSONAR Exception is suitably logged
            LOGGER.warn("Error loading resource through filepath: {} - {}", fileName, e.getClass());
            throw new IllegalStateException("Error reading required file. Failing test case", e);
        }
    }
}
