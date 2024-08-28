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
package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import static com.ericsson.oss.services.sonom.flm.cm.data.domain.ModelConstants.INDOOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * POJO to represent the Sector with its associated cells.
 */
public class TopologySector extends Sector {

    private final Collection<Cell> associatedCells;
    private final Collection<Cell> associatedOutdoorCells;

    /**
     * Create the TopologySector with associated cells.
     *
     * @param sectorId
     *            the sector id
     * @param associatedCells
     *            the collection of associated cells
     */
    public TopologySector(final Long sectorId, final Collection<Cell> associatedCells) {
        super(sectorId);
        this.associatedCells = new ArrayList<>(associatedCells);
        associatedOutdoorCells = associatedCells.stream()
                .filter(TopologySector::isOutdoorCell)
                .collect(Collectors.toList());
    }

    /**
     * Creates a {@code shallow-copy} from a sector.
     * @param sector to copy
     * @return a {@code shallow-copy} of the original sector
     */
    public static TopologySector newInstance(final TopologySector sector) {
        return new TopologySector(sector.getSectorId(), sector.getAssociatedCells());
    }

    /**
     * Check cell is 'outdoor' or 'outdoor_indoor'.
     * @param cell a single {@link Cell}
     * @return boolean
     */
    private static boolean isOutdoorCell(final Cell cell) {
        return !INDOOR.equals(cell.getInstallationType());
    }

    /**
     * Removes all the {@link Cell}s that are not contained by the {@code inclusionList} from the {@link TopologySector#associatedCells}.
     * <p>
     * Only the intersection of the two collections will remain in {@link TopologySector#associatedCells}.
     * <p>
     * Removes all the {@link Cell}s that are not contained by the {@code inclusionList} from the {@link TopologySector#associatedOutdoorCells}.
     * <p>
     * Only the intersection of the two collections will remain in {@link TopologySector#associatedOutdoorCells}.
     * @param includedCells
     *            {@link Cell}s contained in the {@code inclusionList}.
     */
    public void applyInclusionList(final Collection<Cell> includedCells) {
        associatedCells.retainAll(includedCells);
        associatedOutdoorCells.retainAll(includedCells);
    }

    /**
     * Checks if {@link TopologySector#associatedCells} is empty.
     * @return {@code true} if {@link TopologySector#associatedCells} contains no elements otherwise {@code false}
     */
    public boolean isAssociatedCellsEmpty() {
        return associatedCells.isEmpty();
    }

    public Collection<Cell> getAssociatedCells() {
        return new ArrayList<>(associatedCells);
    }

    public Collection<Cell> getAssociatedOutdoorCells() {
        return new ArrayList<>(associatedOutdoorCells);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TopologySector that = (TopologySector) o;
        return super.equals(o) &&
                Objects.equals(associatedCells, that.associatedCells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), associatedCells);
    }

    @Override
    public String toString() {
        return String.format("%s:: { sectorId: '%s', associations: '%s'}", getClass().getSimpleName(), getSectorId(), associatedCells);
    }
}
