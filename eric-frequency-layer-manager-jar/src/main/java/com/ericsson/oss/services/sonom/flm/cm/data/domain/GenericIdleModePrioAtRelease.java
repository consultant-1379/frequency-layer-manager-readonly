/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This generic class represents an IdleModePrioAtRelease topology object.
 * @param <T> A {@link DistributionInfo} or descendant of this class can be used as template parameter
 */
public class GenericIdleModePrioAtRelease<T extends GenericIdleModePrioAtRelease.DistributionInfo> {
    /**
     * Holder enum for threshold level.
     */
    public enum ThresholdLevel {
        LOW_LOAD_THRESHOLD,
        LOW_MEDIUM_LOAD_THRESHOLD,
        MEDIUM_LOAD_THRESHOLD,
        MEDIUM_HIGH_LOAD_THRESHOLD,
        HIGH_LOAD_THRESHOLD
    }

    private final TopologyObjectId topologyObjectId;

    private final long id;

    private final String name;

    //List of 5 thresholds levels (low, low-middle, middle, middle-high, high)
    private final List<Integer> thresholds;

    //List of 5 distribution infos (low, low-middle, middle, middle-high, high)
    private List<T> distributionInfos;

    private final Set<String> reservedBy;

    public GenericIdleModePrioAtRelease(final long id, final String fdn, final int ossId, final String name, final List<Integer> thresholds,
                                 final List<T> distributionInfos, final Set<String> reservedBy) {
        this.id = id;
        this.topologyObjectId = TopologyObjectId.of(fdn, ossId);
        this.name = name;
        this.thresholds = new ArrayList<>(thresholds);
        this.distributionInfos = new ArrayList<>(distributionInfos);
        this.reservedBy = new HashSet<>(reservedBy);
    }

    public long getId() {
        return id;
    }

    public String getFdn() {
        return topologyObjectId.getFdn();
    }

    public int getOssId() {
        return topologyObjectId.getOssId();
    }

    public TopologyObjectId getTopologyObjectId() {
        return topologyObjectId;
    }

    public String getName() {
        return name;
    }

    public Integer getLowLoadThreshold() {
        return thresholds.get(ThresholdLevel.LOW_LOAD_THRESHOLD.ordinal());
    }

    public Integer getLowMediumLoadThreshold() {
        return thresholds.get(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD.ordinal());
    }

    public Integer getMediumLoadThreshold() {
        return thresholds.get(ThresholdLevel.MEDIUM_LOAD_THRESHOLD.ordinal());
    }

    public Integer getMediumHighLoadThreshold() {
        return thresholds.get(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD.ordinal());
    }

    public Integer getHighLoadThreshold() {
        return thresholds.get(ThresholdLevel.HIGH_LOAD_THRESHOLD.ordinal());
    }

    public T getLowLoadDistributionInfo() {
        return distributionInfos.get(ThresholdLevel.LOW_LOAD_THRESHOLD.ordinal());
    }

    public T getLowMediumLoadDistributionInfo() {
        return distributionInfos.get(ThresholdLevel.LOW_MEDIUM_LOAD_THRESHOLD.ordinal());
    }

    public T getMediumLoadDistributionInfo() {
        return distributionInfos.get(ThresholdLevel.MEDIUM_LOAD_THRESHOLD.ordinal());
    }

    public T getMediumHighLoadDistributionInfo() {
        return distributionInfos.get(ThresholdLevel.MEDIUM_HIGH_LOAD_THRESHOLD.ordinal());
    }

    public T getHighLoadDistributionInfo() {
        return distributionInfos.get(ThresholdLevel.HIGH_LOAD_THRESHOLD.ordinal());
    }

    public List<Integer> getThresholds() {
        return new ArrayList<>(thresholds);
    }

    public List<T> getDistributionInfos() {
        return new ArrayList<>(distributionInfos);
    }

    public void setDistributionInfos(final List<T> distributionInfos) {
        this.distributionInfos = Collections.unmodifiableList(distributionInfos);
    }

    public Set<String> getReservedBy() {
        return new HashSet<>(reservedBy);
    }

    protected List<T> getModifiedDistributionList(final T newDistributionInfo) {
        final int indexOfThresholdToAlignAgainst = newDistributionInfo.getThresholdLevel().ordinal();
        final int thresholdToAlignAgainst = thresholds.get(indexOfThresholdToAlignAgainst);

        final List<T> newDistributionInfos = new ArrayList<>();
        for (final T distributionInfo : getDistributionInfos()) {
            final int threshold = thresholds.get(distributionInfo.getThresholdLevel().ordinal());
            if (distributionInfo.getThresholdLevel().equals(newDistributionInfo.getThresholdLevel())) {
                newDistributionInfos.add(newDistributionInfo);
            } else if (threshold == thresholdToAlignAgainst) {
                distributionInfo.setFreqDistributionList(new ArrayList<>(newDistributionInfo.getFreqDistributionList()));
                newDistributionInfos.add(distributionInfo);
            } else {
                newDistributionInfos.add(distributionInfo);
            }
        }
        return newDistributionInfos;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GenericIdleModePrioAtRelease that = (GenericIdleModePrioAtRelease) o;
        return id == that.id && topologyObjectId.equals(that.topologyObjectId) && name.equals(that.name) && thresholds.equals(that.thresholds)
                && distributionInfos.equals(that.distributionInfos) && reservedBy.equals(that.reservedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, topologyObjectId, name, thresholds, distributionInfos, reservedBy);
    }

    /**
     * This class represent a distribution and frequency information for a given threshold.
     */
    public static class DistributionInfo {
        private final GenericIdleModePrioAtRelease.ThresholdLevel thresholdLevel;
        // List of percentages of users to move to each target frequency (cell)
        private List<Float> freqDistributionList;
        // The list of fdns of EutranFrequency (cell)
        private final List<String> eUtranFreqRefList;

        public DistributionInfo(final GenericIdleModePrioAtRelease.ThresholdLevel thresholdLevel, final List<Float> freqDistributionList,
                                final List<String> eUtranFreqRefList) {
            this.thresholdLevel = thresholdLevel;
            this.freqDistributionList = new ArrayList<>(freqDistributionList);
            this.eUtranFreqRefList = new ArrayList<>(eUtranFreqRefList);
        }

        public List<Float> getFreqDistributionList() {
            return new ArrayList<>(freqDistributionList);
        }

        public void setFreqDistributionList(final List<Float> freqDistributionList) {
            this.freqDistributionList = Collections.unmodifiableList(freqDistributionList);
        }

        public List<String> getEUtranFreqRefList() {
            return new ArrayList<>(eUtranFreqRefList);
        }

        public GenericIdleModePrioAtRelease.ThresholdLevel getThresholdLevel() {
            return thresholdLevel;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final DistributionInfo that = (DistributionInfo) o;
            return thresholdLevel == that.thresholdLevel && freqDistributionList.equals(that.freqDistributionList) &&
                    eUtranFreqRefList.equals(that.eUtranFreqRefList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(thresholdLevel, freqDistributionList, eUtranFreqRefList);
        }

        @Override
        public String toString() {
            final String thresholdLevelString = thresholdLevel == null
                    ? "null"
                    : thresholdLevel.toString();
            final String freqDistributionListString = freqDistributionList.toString();
            final String eUtranFreqRefListString = eUtranFreqRefList.toString();

            return String.join(":", thresholdLevelString + freqDistributionListString + eUtranFreqRefListString);
        }
    }
}
