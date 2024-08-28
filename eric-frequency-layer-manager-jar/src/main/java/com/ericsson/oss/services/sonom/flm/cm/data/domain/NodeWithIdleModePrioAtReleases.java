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

package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * This class represents Node with the associated idleModePrioAtReleases.
 */
public class NodeWithIdleModePrioAtReleases {
    private final Node node;
    private final Collection<IdleModePrioAtRelease> associatedProfiles;
    public NodeWithIdleModePrioAtReleases(final Node node, final Collection<IdleModePrioAtRelease> associatedProfiles) {
        this.node = node;
        this.associatedProfiles = new ArrayList<>(associatedProfiles);
    }

    public Node getNode() {
        return node;
    }

    public Collection<IdleModePrioAtRelease> getAssociatedProfiles() {
        return new ArrayList<>(associatedProfiles);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeWithIdleModePrioAtReleases that = (NodeWithIdleModePrioAtReleases) o;
        return node.equals(that.node) && associatedProfiles.equals(that.associatedProfiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, associatedProfiles);
    }
}