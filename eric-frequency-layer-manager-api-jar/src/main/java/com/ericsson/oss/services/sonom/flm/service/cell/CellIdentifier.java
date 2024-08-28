/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.service.cell;

import java.util.Objects;

/**
 * A POJO to hold the cell identifier information based on the cell ossId and FDN.
 */
public class CellIdentifier {

    private final int ossId;
    private final String fdn;

    /**
     * Create a cell identifier.
     * 
     * @param ossId
     *            the oss identity
     * @param fdn
     *            the FDN identity
     */
    public CellIdentifier(final int ossId, final String fdn) {
        this.ossId = ossId;
        this.fdn = fdn;
    }

    public int getOssId() {
        return ossId;
    }

    public String getFdn() {
        return fdn;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CellIdentifier that = (CellIdentifier) o;
        return ossId == that.ossId &&
                fdn.equals(that.fdn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ossId, fdn);
    }

    @Override
    public String toString() {
        return String.format("%s:: { ossId: %d, fdn: '%s' }",
                getClass().getSimpleName(), ossId, fdn);
    }
}
