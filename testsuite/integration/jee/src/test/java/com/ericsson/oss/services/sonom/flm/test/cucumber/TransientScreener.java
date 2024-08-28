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
package com.ericsson.oss.services.sonom.flm.test.cucumber;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/flm/Transient_Screener.feature", glue = {
        "com.ericsson.oss.services.sonom.flm.test.steps.flm",
        "com.ericsson.oss.services.sonom.flm.test.util"
}, plugin = {
        "pretty"
}, tags = "@RunAllTests")
public class TransientScreener {
}
