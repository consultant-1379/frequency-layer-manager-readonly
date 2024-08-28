/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/flm/LowConnectedUsers_Screener.feature", glue = {
        "com.ericsson.oss.services.sonom.flm.test.steps.flm",
        "com.ericsson.oss.services.sonom.flm.test.util"
}, plugin = {
        "pretty"
}, tags = "@RunAllTests")
public class LowConnectedUsersScreener {
}
