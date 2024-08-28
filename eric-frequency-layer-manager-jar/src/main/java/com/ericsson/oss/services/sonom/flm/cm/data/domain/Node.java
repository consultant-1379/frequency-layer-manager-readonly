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

import java.util.Objects;

/**
 * POJO represents a Node.
 */
public class Node {
    private final Long nodeId;
    private final TopologyObjectId topologyObjectId;
    private final FeatureState featureState;
    private final String nodeType;

    public Node(final Long nodeId, final String fdn, final int ossId, final FeatureState featureState, final String nodeType) {
        this.nodeId = nodeId;
        this.topologyObjectId = TopologyObjectId.of(fdn, ossId);
        this.featureState = featureState;
        this.nodeType = nodeType;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getFdn() {
        return topologyObjectId.getFdn();
    }

    public int getOssId() {
        return topologyObjectId.getOssId();
    }

    public FeatureState getFeatureState() {
        return featureState;
    }

    public String getNodeType() {
        return nodeType;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Node that = (Node) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(topologyObjectId, that.topologyObjectId) &&
                Objects.equals(featureState, that.featureState) &&
                Objects.equals(nodeType, that.nodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, topologyObjectId, featureState, nodeType);
    }

    @Override
    public String toString() {
        return String.format("%s:: { nodeId '%d', ossId: %d, fdn: '%s', featureState: '%s', nodeType: '%s' }",
                getClass().getSimpleName(), nodeId, topologyObjectId.getOssId(), topologyObjectId.getFdn(), featureState, nodeType);
    }

    public TopologyObjectId getTopologyObjectId() {
        return topologyObjectId;
    }

    /**
     * POJO for feature state.
     */
    public static class FeatureState {
        private final boolean isSubscriberTriggeredMobilityActivated;
        private final boolean isLoadBasedDistributionAtReleaseActivated;
        private final boolean isEvolvedLoadBasedDistributionAtReleaseActivated;
        private final boolean isInterFrequencyLoadBalancingActivated;
        private final String capabilityAwareIdleModeControlActivated;
        private final boolean isEndcTriggeredHandoverDuringSetupActivated;

        public FeatureState(final boolean isSubscriberTriggeredMobilityActivated,
                            final boolean isLoadBasedDistributionAtReleaseActivated,
                            final boolean isEvolvedLoadBasedDistributionAtReleaseActivated,
                            final boolean isInterFrequencyLoadBalancingActivated,
                            final String capabilityAwareIdleModeControlActivated,
                            final boolean isEndcTriggeredHandoverDuringSetupActivated) {
            this.isSubscriberTriggeredMobilityActivated = isSubscriberTriggeredMobilityActivated;
            this.isLoadBasedDistributionAtReleaseActivated = isLoadBasedDistributionAtReleaseActivated;
            this.isEvolvedLoadBasedDistributionAtReleaseActivated = isEvolvedLoadBasedDistributionAtReleaseActivated;
            this.isInterFrequencyLoadBalancingActivated = isInterFrequencyLoadBalancingActivated;
            this.capabilityAwareIdleModeControlActivated = capabilityAwareIdleModeControlActivated;
            this.isEndcTriggeredHandoverDuringSetupActivated = isEndcTriggeredHandoverDuringSetupActivated;
        }

        public boolean isSubscriberTriggeredMobilityActivated() {
            return isSubscriberTriggeredMobilityActivated;
        }

        public boolean isLoadBasedDistributionAtReleaseActivated() {
            return isLoadBasedDistributionAtReleaseActivated;
        }

        public boolean isEvolvedLoadBasedDistributionAtReleaseActivated() {
            return isEvolvedLoadBasedDistributionAtReleaseActivated;
        }

        public boolean isInterFrequencyLoadBalancingActivated() {
            return isInterFrequencyLoadBalancingActivated;
        }

        public String getCapabilityAwareIdleModeControlActivated() {
            return capabilityAwareIdleModeControlActivated;
        }

        public boolean isEndcTriggeredHandoverDuringSetupActivated() {
            return isEndcTriggeredHandoverDuringSetupActivated;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final FeatureState that = (FeatureState) o;
            return Objects.equals(isSubscriberTriggeredMobilityActivated, that.isSubscriberTriggeredMobilityActivated) &&
                    Objects.equals(isLoadBasedDistributionAtReleaseActivated, that.isLoadBasedDistributionAtReleaseActivated) &&
                    Objects.equals(isEvolvedLoadBasedDistributionAtReleaseActivated, that.isEvolvedLoadBasedDistributionAtReleaseActivated) &&
                    Objects.equals(isInterFrequencyLoadBalancingActivated, that.isInterFrequencyLoadBalancingActivated) &&
                    Objects.equals(capabilityAwareIdleModeControlActivated, that.capabilityAwareIdleModeControlActivated) &&
                    Objects.equals(isEndcTriggeredHandoverDuringSetupActivated, that.isEndcTriggeredHandoverDuringSetupActivated);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isSubscriberTriggeredMobilityActivated, isLoadBasedDistributionAtReleaseActivated,
                                isEvolvedLoadBasedDistributionAtReleaseActivated, isInterFrequencyLoadBalancingActivated,
                    capabilityAwareIdleModeControlActivated, isEndcTriggeredHandoverDuringSetupActivated);
        }

        @Override
        public String toString() {
            return String.format("%s:: { %b, %b, %b, %b, %s, %b }",
                    getClass().getSimpleName(),
                    isSubscriberTriggeredMobilityActivated,
                    isLoadBasedDistributionAtReleaseActivated,
                    isEvolvedLoadBasedDistributionAtReleaseActivated,
                    isInterFrequencyLoadBalancingActivated,
                    capabilityAwareIdleModeControlActivated, isEndcTriggeredHandoverDuringSetupActivated);
        }
    }
}
