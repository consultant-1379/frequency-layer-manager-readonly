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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.cm.service.change.api.ParameterChanges;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease.DistributionInfo;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class conatins common methods used while creating Optimization and Reversion ChangeElement.
 */
public class ParameterChangesBuilder {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private ParameterChangesBuilder() {
    }

    static List<ParameterChanges> buildCellParameterChanges(final String idleModePrioAtReleaseRef) {
        final List<ParameterChanges> parameterChanges = new ArrayList<>();
        parameterChanges.add(new ParameterChanges(ModelConstants.IDLE_MODE_PRIO_AT_RELEASE_REF, idleModePrioAtReleaseRef));
        return parameterChanges;
    }

    static List<ParameterChanges> buildProfileParameterChanges(
            final IdleModePrioAtRelease idleModePrioAtRelease) {
        final List<ParameterChanges> parameterChanges = new ArrayList<>();
        parameterChanges.add(new ParameterChanges(ModelConstants.LOW_LOAD_THRESHOLD,
                String.valueOf(idleModePrioAtRelease.getLowLoadThreshold())));
        parameterChanges.add(new ParameterChanges(ModelConstants.LOW_MEDIUM_LOAD_THRESHOLD,
                String.valueOf(idleModePrioAtRelease.getLowMediumLoadThreshold())));
        parameterChanges.add(new ParameterChanges(ModelConstants.MEDIUM_LOAD_THRESHOLD,
                String.valueOf(idleModePrioAtRelease.getMediumLoadThreshold())));
        parameterChanges.add(new ParameterChanges(ModelConstants.MEDIUM_HIGH_LOAD_THRESHOLD,
                String.valueOf(idleModePrioAtRelease.getMediumHighLoadThreshold())));
        parameterChanges.add(new ParameterChanges(ModelConstants.HIGH_LOAD_THRESHOLD,
                String.valueOf(idleModePrioAtRelease.getHighLoadThreshold())));
        parameterChanges.add(new ParameterChanges(ModelConstants.LOW_LOAD_DISTRIBUTION_INFO,
                buildLoadDistributionInfo(idleModePrioAtRelease.getLowLoadDistributionInfo())));
        parameterChanges.add(new ParameterChanges(ModelConstants.LOW_MEDIUM_LOAD_DISTRIBUTION_INFO,
                buildLoadDistributionInfo(idleModePrioAtRelease.getLowMediumLoadDistributionInfo())));
        parameterChanges.add(new ParameterChanges(ModelConstants.MEDIUM_LOAD_DISTRIBUTION_INFO,
                buildLoadDistributionInfo(idleModePrioAtRelease.getMediumLoadDistributionInfo())));
        parameterChanges.add(new ParameterChanges(ModelConstants.MEDIUM_HIGH_LOAD_DISTRIBUTION_INFO,
                buildLoadDistributionInfo(idleModePrioAtRelease.getMediumHighLoadDistributionInfo())));
        parameterChanges.add(new ParameterChanges(ModelConstants.HIGH_LOAD_DISTRIBUTION_INFO,
                buildLoadDistributionInfo(idleModePrioAtRelease.getHighLoadDistributionInfo())));
        return parameterChanges;
    }

    static String buildLoadDistributionInfo(final DistributionInfo distributionInfo) {
        final List<Integer> modifiedDistributionList = distributionInfo.getFreqDistributionList()
                .stream()
                .map(Float::intValue)
                .collect(Collectors.toList());
        return "{" +
                ModelConstants.EUTRAN_FREQ_REF_LIST + ": " + GSON.toJson(distributionInfo.getEUtranFreqRefList()) + "," +
                ModelConstants.FREQ_DISTRIBUTION_LIST + ": " + GSON.toJson(modifiedDistributionList) +
                "}";
    }
}
