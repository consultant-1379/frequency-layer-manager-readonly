/**
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2020
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.cm.data.domain;

import java.util.Objects;

/**
 * POJO to represent the Sector.
 */
public class Sector {

    private final Long sectorId;

    public Sector(final Long sectorId) {
        this.sectorId = sectorId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Sector that = (Sector) o;
        return Objects.equals(sectorId, that.sectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectorId);
    }

    @Override
    public String toString() {
        return String.format("%s:: { sectorId: '%s' }", getClass().getSimpleName(), sectorId);
    }
}
