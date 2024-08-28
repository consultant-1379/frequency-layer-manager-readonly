/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;

/**
 * Unit tests for {@link CronMaker} class.
 */
public class CronMakerTest {

    private static final LocalDateTime CHRISTMAS_2015 = LocalDateTime.of(2015, Month.DECEMBER, 25, 12, 0);

    @Test
    public void runCronMakerOnChristmas() {
        assertThat(CronMaker.make(CHRISTMAS_2015, 0)).isEqualTo("0 0 12 25 12 ? *");
    }

    @Test
    public void runCronMakerOnChristmasWithThirtySecondOffset() {
        assertThat(CronMaker.make(CHRISTMAS_2015, 30)).isEqualTo("30 0 12 25 12 ? *");
    }
}