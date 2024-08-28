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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link ProfileNameGenerator} class.
 */
public class ProfileNameGeneratorTest {

    private static final String CGI = "310-260-34534-3";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void whenOldNameNotCgi_thenReturnCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, "something")).isEqualTo(CGI);
    }

    @Test
    public void whenOldNameContainsOnlyCgi_thenReturnIncrementedCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, CGI)).isEqualTo("310-260-34534-3_1");
    }

    @Test
    public void whenOldNameContainsCgiWithNumber_thenIncrementedCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, "310-260-34534-3_10")).isEqualTo("310-260-34534-3_11");
    }

    @Test
    public void whenOldNameStartsWithCgiButSuffixIsNotANumber_thenReturnCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, "310-260-34534-3_10BRT")).isEqualTo(CGI);
    }

    @Test
    public void whenOldNameStartsWithCgiButSuffixNotProper_thenReturnIncrementedCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, "310-260-34534-32332")).isEqualTo(CGI);
    }

    @Test
    public void whenOldNameStartsWithcCgiButSuffixIsNotComplete_thenReturnCgiAsName() {
        assertThat(ProfileNameGenerator.generateProfileName(CGI, "310-260-34534-3_")).isEqualTo(CGI);
    }

    @Test
    public void whenCgiIsNull_thenThrowNullPointerException() {
        thrown.expect(NullPointerException.class);
        ProfileNameGenerator.generateProfileName(null, "310-260-34534-3_");
    }
}