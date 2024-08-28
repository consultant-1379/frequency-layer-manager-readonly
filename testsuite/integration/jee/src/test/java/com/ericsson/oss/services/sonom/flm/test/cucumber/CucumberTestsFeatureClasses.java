/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
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

import java.util.ArrayList;
import java.util.List;

/**
 * All the cucumber classes.
 */
public class CucumberTestsFeatureClasses {

    static List<Class> classes = new ArrayList<>();
    static List<Class> paClasses = new ArrayList<>();

    static {
        classes.add(AppCoverageReliabilityScreener.class);
        classes.add(BadRsrpPercentageScreener.class);
        classes.add(ContiguityScreener.class);
        classes.add(CoverageBalanceRatioDistanceScreener.class);
        classes.add(DetermineStepSizeAndOptimizationSpeed.class);
        classes.add(EssScreener.class);
        classes.add(GfsScreener.class);
        classes.add(IdentifySourceCells.class);
        classes.add(LowConnectedUsersScreener.class);
        classes.add(InputEventScreener.class);
        classes.add(MaxSourceUsersMove.class);
        classes.add(NumericStepSizeAndDistributeUsers.class);
        classes.add(PercentageEndcUsersScreener.class);
        classes.add(PerformanceAndAvailabilityScreener.class);
        classes.add(TransientScreener.class);
        classes.add(UlPuschSinrScreener.class);

        paClasses.add(PerformanceAssuranceDegradation.class);
    }

    private CucumberTestsFeatureClasses() {
    }

    public static List<Class> getAllCucumberClasses() {
        return classes;
    }

    public static List<Class> getAllPaCucumberClasses() {
        return paClasses;
    }

}
