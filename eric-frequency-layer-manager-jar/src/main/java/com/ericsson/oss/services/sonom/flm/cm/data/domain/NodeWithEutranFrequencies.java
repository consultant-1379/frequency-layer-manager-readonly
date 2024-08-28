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
 * This class is a POJO for representing a node and EUtranFrequencies associated.
 */
public class NodeWithEutranFrequencies {
    private final Node node;
    private final Collection<EUtranFrequency> associatedFrequencies;

    public NodeWithEutranFrequencies(final Node node, final Collection<EUtranFrequency> associatedFrequencies) {
        this.node = node;
        this.associatedFrequencies = new ArrayList<>(associatedFrequencies);
    }

    public Node getNode() {
        return node;
    }

    /**
     * Gets the associated {@link EUtranFrequency} list that are children of the node.
     * @return returns a list of {@link EUtranFrequency}s, never null
     */
    public Collection<EUtranFrequency> getAssociatedFrequencies() {
        return new ArrayList<>(associatedFrequencies);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeWithEutranFrequencies that = (NodeWithEutranFrequencies) o;
        return node.equals(that.node) && associatedFrequencies.equals(that.associatedFrequencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, associatedFrequencies);
    }
}
