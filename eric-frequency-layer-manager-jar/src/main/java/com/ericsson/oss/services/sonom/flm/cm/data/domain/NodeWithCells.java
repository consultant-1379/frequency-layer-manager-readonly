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
 * This class is used to store a node with associated cells.
 */
public class NodeWithCells {
    private final Node node;
    private final Collection<Cell> associatedCells;
    public NodeWithCells(final Node node, final Collection<Cell> associatedCells) {
        this.node = node;
        this.associatedCells = new ArrayList<>(associatedCells);
    }

    public Node getNode() {
        return node;
    }

    public Collection<Cell> getAssociatedCells() {
        return new ArrayList<>(associatedCells);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeWithCells that = (NodeWithCells) o;
        return node.equals(that.node) && associatedCells.equals(that.associatedCells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, associatedCells);
    }
}
