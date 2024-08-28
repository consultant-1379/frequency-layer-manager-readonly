/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.ericsson.oss.services.sonom.flm.test.FlmConfigurationIT.FlmConfigurationParameterizedSettingsIT;

@RunWith(FailHardSuite.class)
@SuiteClasses({
        FlmIT.class,
        FlmConfigurationIT.class,
        FlmConfigurationParameterizedSettingsIT.class,
        FlmAlgorithmIT.class,
        FlmOptimizationsDaoIT.class,
        PerformanceAssuranceIT.class,
        FlmCucumberITRunner.class,
        FlmPaCucumberITRunner.class,
        BackupAndRestoreIT.class,
        FlmLbdarDaoIT.class
})
public class IntegrationTests {
}
