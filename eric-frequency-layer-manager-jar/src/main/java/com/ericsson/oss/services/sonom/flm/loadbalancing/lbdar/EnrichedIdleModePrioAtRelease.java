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

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.GenericIdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;

/**
 * This class is used to hold the structure to be able to search with carrier value in the {@link IdleModePrioAtRelease} object.
 */
public class EnrichedIdleModePrioAtRelease extends GenericIdleModePrioAtRelease<EnrichedIdleModePrioAtRelease.EnrichedDistributionInfo> {

    public EnrichedIdleModePrioAtRelease(final long id, final String fdn, final int ossId, final String name, final List<Integer> thresholds,
                                 final List<EnrichedDistributionInfo> distributionInfos, final Set<String> reservedBy) {
        super(id, fdn, ossId, name, thresholds, distributionInfos, reservedBy);
    }

    public EnrichedIdleModePrioAtRelease(final IdleModePrioAtRelease idleModePrioAtRelease,
                                         final Map<String, Integer> frequencyToCarrier) {
        super(idleModePrioAtRelease.getId(), idleModePrioAtRelease.getFdn(), idleModePrioAtRelease.getOssId(), idleModePrioAtRelease.getName(),
                idleModePrioAtRelease.getThresholds(), createDistributionInfos(idleModePrioAtRelease, frequencyToCarrier),
                idleModePrioAtRelease.getReservedBy());
    }

    public IdleModePrioAtRelease getIdleModePrioAtReleaseCopy() {
        return new IdleModePrioAtRelease(getId(), getFdn(), getOssId(), getName(), getThresholds(), convertToDistributionInfo(getDistributionInfos()),
                getReservedBy());
    }

    private List<DistributionInfo> convertToDistributionInfo(final List<EnrichedDistributionInfo> enrichedDistributionInfos) {
        return enrichedDistributionInfos.stream()
                .map(EnrichedDistributionInfo::getDistributionInfoCopy)
                .collect(Collectors.toList());
    }

    /**
     * Returns a copy of {@link EnrichedIdleModePrioAtRelease} object where one of the {@link EnrichedDistributionInfo} is replaced.
     * 
     * @param distributionInfo
     *            A {@link EnrichedDistributionInfo} instance that will be used as replacement
     * @return a new copy of {@link EnrichedIdleModePrioAtRelease} instance
     */
    public EnrichedIdleModePrioAtRelease getModifiedCopy(final EnrichedDistributionInfo distributionInfo) {
        return new EnrichedIdleModePrioAtRelease(
                this.getId(),
                this.getFdn(),
                this.getOssId(),
                this.getName(),
                this.getThresholds(),
                getModifiedDistributionList(distributionInfo),
                this.getReservedBy());
    }

    private static List<EnrichedDistributionInfo> createDistributionInfos(final IdleModePrioAtRelease idleModePrioAtRelease,
                                                                          final Map<String, Integer> frequencyToCarrier) {
        return idleModePrioAtRelease.getDistributionInfos().stream()
                .map(distributionInfo -> new EnrichedDistributionInfo(distributionInfo, frequencyToCarrier))
                .collect(Collectors.toList());
    }

    /**
     * This class is an extension of DistributionInfo that holds the carrier values for each of the frequencies in the {@link DistributionInfo}.
     */
    public static class EnrichedDistributionInfo extends IdleModePrioAtRelease.DistributionInfo {

        // map from carrier to array index of the frequency in the distribution and frequency list
        private final Map<Integer, Integer> carrierIndex = new HashMap<>();
        private final List<Integer> carriers = new ArrayList<>();

        public EnrichedDistributionInfo(final GenericIdleModePrioAtRelease.ThresholdLevel thresholdLevel, final List<Float> freqDistributionList,
                                        final List<String> eUtranFreqRefList, final Map<String, Integer> frequencyToCarrier) {
            super(thresholdLevel, freqDistributionList, eUtranFreqRefList);
            initCarriers(frequencyToCarrier);
        }

        public EnrichedDistributionInfo(final DistributionInfo distributionInfo, final Map<String, Integer> frequencyToCarrier) {
            super(distributionInfo.getThresholdLevel(), distributionInfo.getFreqDistributionList(), distributionInfo.getEUtranFreqRefList());
            initCarriers(frequencyToCarrier);
        }

        public EnrichedDistributionInfo(final GenericIdleModePrioAtRelease.ThresholdLevel thresholdLevel, final List<Float> freqDistributionList,
                                        final List<String> eUtranFreqRefList, final List<Integer> carriers) {
            super(thresholdLevel, freqDistributionList, eUtranFreqRefList);
            if (eUtranFreqRefList.size() != carriers.size()) {
                throw new IllegalArgumentException("The size of frequency and carrier list is not the same, " +
                        "freq size=" + eUtranFreqRefList.size() + ", carrier size=" + carriers.size());
            }
            initCarriers(zip(eUtranFreqRefList, carriers));
        }

        public Float getDistributionOfFrequency(final int carrier) {
            final Integer indexOfFrequency = carrierIndex.get(carrier);
            return indexOfFrequency == null ? null : getFreqDistributionList().get(indexOfFrequency);
        }

        public boolean containsCarrier(final int carrier) {
            return carrierIndex.containsKey(carrier);
        }

        public boolean containsCarriers(final List<Integer> targetCarriers) {
            return carrierIndex.keySet().containsAll(targetCarriers);
        }

        public List<Integer> getCarriers() {
            return new ArrayList<>(carriers);
        }

        public int getIndexOfCarrier(final int carrier) {
            return carrierIndex.get(carrier) == null ? -1 : carrierIndex.get(carrier);
        }

        /**
         * If the source cell distribution has carrier defined then the percentage should not be negative. If the carrier does not exist or is 0 that
         * is ok as we will include a default source push later.
         * 
         * @param carrier
         *            A carrier belonging to a frequency in the profile
         * @return Returns false if distribution value exists for carrier and it is negative
         */
        public boolean distributionValueIsPositive(final int carrier) {
            final Integer indexOfFrequency = carrierIndex.get(carrier);
            if (indexOfFrequency == null || getFreqDistributionList().get(indexOfFrequency) == null) {
                return true;
            }
            return getFreqDistributionList().get(indexOfFrequency) >= 0;
        }

        public DistributionInfo getDistributionInfoCopy() {
            return new DistributionInfo(getThresholdLevel(), getFreqDistributionList(), getEUtranFreqRefList());
        }

        public String getFrequency(final int carrier) {
            final int index = getIndexOfCarrier(carrier);
            return index == -1 ? null : getEUtranFreqRefList().get(index);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            final EnrichedDistributionInfo that = (EnrichedDistributionInfo) o;
            return Objects.equals(carrierIndex, that.carrierIndex) && Objects.equals(carriers, that.carriers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), carrierIndex);
        }

        private void initCarriers(final Map<String, Integer> frequencyToCarrier) {
            final List<String> freqFdns = getEUtranFreqRefList();
            for (int i = 0; i < freqFdns.size(); i++) {
                final Integer carrier = frequencyToCarrier.get(freqFdns.get(i));
                if (carrier == null) {
                    throw new NoSuchElementException(String.format("Failed to find carrier for fdn %s", freqFdns.get(i)));
                } else {
                    carrierIndex.put(carrier, i);
                    carriers.add(carrier);
                }
            }
        }

        private Map<String, Integer> zip(final List<String> eUtranFreqRefList, final List<Integer> carriers) {
            final Map<String, Integer> frequencyToCarrier = new HashMap<>();
            for (int i = 0; i < eUtranFreqRefList.size(); i++) {
                frequencyToCarrier.put(eUtranFreqRefList.get(i), carriers.get(i));
            }
            return frequencyToCarrier;
        }
    }
}
