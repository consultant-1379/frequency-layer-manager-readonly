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

package com.ericsson.oss.services.sonom.flm.cm.data.stores.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;

/**
 * Utility class for idle mode prio at release.
 */
public class IdleModePrioAtReleaseUtils {

    private IdleModePrioAtReleaseUtils() {
    }

    /**
     * Remove Grouping from frequency distribution list.
     *
     * If a distribution list contains a 0 value, it means that this frequency is groupped to
     * the one before( or the one before, or the one before, whichever differs from 0 ) and the  
     * distribution should be splitted among the group members. If no real value can be found in the 
     * list before the 0, that means it is a frequency with 0 value, it does not belong to any group.
     *
     * This function eliminates the groups by calculation the values after the split and creates 
     * a list with the calculated values.
     *
     * Examples:
     *  [100, 0, 0, 0] == [25, 25, 25, 25]
     *  [100, 0, 0] == [34, 33, 33]
     *  [0, 50, 50] == [0, 50, 50]
     *  [0, 0, 50, 0, 40, 0, 10] == [0, 0, 25, 25, 20, 20, 10]
     *
     * @param distributionInfo {@link IdleModePrioAtRelease.DistributionInfo} the DistributionInfo which is to be filtered.
     * @return {@link IdleModePrioAtRelease.DistributionInfo} newly created DistributionInfo object with
     *         new frequency distribution list from which the grouping is removed.
     */
    public static IdleModePrioAtRelease.DistributionInfo removeGrouping(final IdleModePrioAtRelease.DistributionInfo distributionInfo) {
        if (distributionInfo == null) {
            return null;
        }
        final List<Float> freqDistributionList = distributionInfo.getFreqDistributionList();
        if (isRemovingGroupsNeeded(freqDistributionList)) {
            final int listSize = freqDistributionList.size();
            int startIdx = 0;
            for (int i = 0; i < listSize; i++) {
                  if (freqDistributionList.get(i) != 0.0f) {
                      deGroupFreqDistributionListIfNeeded(freqDistributionList, startIdx, i - 1);
                      startIdx = i;
                  }
            }
            deGroupFreqDistributionListIfNeeded(freqDistributionList, startIdx, listSize - 1);
            return new IdleModePrioAtRelease.DistributionInfo(distributionInfo.getThresholdLevel(),
                                                              freqDistributionList,
                                                              distributionInfo.getEUtranFreqRefList());
        }
        return distributionInfo;
    }

    private static boolean isRemovingGroupsNeeded(final List<Float> freqDistributionList) {
       return freqDistributionList.stream().reduce(0F, (a, b) -> a == -1 || (a > 0 && b == 0) ? -1 : a + b) == -1;
    }

    private static void deGroupFreqDistributionListIfNeeded(final List<Float> freqDistributionList, final int startIdx, final int stopIdx) {
        if (startIdx < stopIdx) {
            final int numberOfItems = stopIdx - startIdx + 1;
            final int itemFloorValue = (int) (freqDistributionList.get(startIdx) / numberOfItems);
            final int remainingValue = (int) (freqDistributionList.get(startIdx) - numberOfItems * itemFloorValue);

            for (int k = 0; k < remainingValue; k++) {
                freqDistributionList.set(startIdx + k, (float) itemFloorValue + 1);
            }

            for (int k = startIdx + remainingValue; k <= stopIdx; k++) {
                freqDistributionList.set(k, (float) itemFloorValue);
            }
        }
    }

    /**
     * Remove duplicated frequencies from frequency distribution list.
     *
     * @param distributionInfo {@link IdleModePrioAtRelease.DistributionInfo} the DistributionInfo which is to be filtered.
     * @return {@link IdleModePrioAtRelease.DistributionInfo} newly created DistributionInfo object with
     *         new frequency distribution list from which the duplicated frequencies are removed.
     */
    public static IdleModePrioAtRelease.DistributionInfo removeDuplicateFreqs(final IdleModePrioAtRelease.DistributionInfo distributionInfo) {
        if (distributionInfo == null) {
            return null;
        }
        final List<Float> freqDistributionList = distributionInfo.getFreqDistributionList();
        final List<String> eUtranFreqRefList = distributionInfo.getEUtranFreqRefList();
        if (isRemovingDuplicatedFreqsNeeded(eUtranFreqRefList)) {
            final Map<String, Float> tempMap = new LinkedHashMap<>();
            for (int i = 0; i < eUtranFreqRefList.size(); i++) {
                tempMap.merge(eUtranFreqRefList.get(i),
                              freqDistributionList.get(i), Float::sum);
            }
            final List<String> newFreqList = new ArrayList<>();
            final List<Float> newDistList = new ArrayList<>();
            for (final Map.Entry<String, Float> entry : tempMap.entrySet()) {
                newFreqList.add(entry.getKey());
                newDistList.add(entry.getValue());
            }

            return new IdleModePrioAtRelease.DistributionInfo(distributionInfo.getThresholdLevel(),
                                                              newDistList,
                                                              newFreqList);
        }
        return distributionInfo;
    }

    private static boolean isRemovingDuplicatedFreqsNeeded(final List<String> eUtranFreqRefList) {
        return new HashSet<>(eUtranFreqRefList).size() != eUtranFreqRefList.size();
    }
}
