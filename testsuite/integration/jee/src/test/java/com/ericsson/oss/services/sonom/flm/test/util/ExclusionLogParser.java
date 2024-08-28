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

package com.ericsson.oss.services.sonom.flm.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExclusionLogParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExclusionLogParser.class);
    private static final String PATH_TO_LOG_FILE = "./logs/exclusionReason.log";

    private ExclusionLogParser() {
    }

    public static boolean containsExpectedLog(final String expectedExclusionLog) {
        try (final Stream<String> lines = Files.lines(Paths.get(PATH_TO_LOG_FILE))) {
            final Optional<String> containsExpectedLine = lines.filter(line -> line.contains(expectedExclusionLog))
                                                               .findFirst();

            if (containsExpectedLine.isPresent()) {
                LOGGER.info("Found exclusion log: {}", containsExpectedLine.get());
                return true;
            }

        } catch (final IOException e) {
            LOGGER.error("Unable to parse exclusionReason.log file", e);
        }

        LOGGER.warn("Exclusion log not found: {}", expectedExclusionLog);
        return false;
    }
}
