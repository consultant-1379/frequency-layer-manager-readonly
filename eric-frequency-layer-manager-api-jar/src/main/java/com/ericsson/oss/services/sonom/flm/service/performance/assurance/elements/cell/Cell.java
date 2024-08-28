/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A POJO to represent a cell passed to the FLM performance assurance policy.
 */
public class Cell implements Serializable {

    private static final long serialVersionUID = 1288496833554976798L;

    private final String fdn;
    private final int ossId;
    private final Map<String, CellLevelKpi> kpis; //NOSONAR Implementations of Map are serializable

    public Cell(final String fdn, final int ossId, final Map<String, CellLevelKpi> kpis) {
        this.fdn = fdn;
        this.ossId = ossId;
        this.kpis = kpis;
    }

    public String getFdn() {
        return fdn;
    }

    public int getOssId() {
        return ossId;
    }

    public Map<String, CellLevelKpi> getKpis() {
        return kpis;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Cell cell = (Cell) o;
        return Objects.equals(fdn, cell.fdn) &&
                Objects.equals(ossId, cell.ossId) &&
                Objects.equals(kpis, cell.kpis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fdn, ossId, kpis);
    }

    @Override
    public String toString() {
        return String.format("%s:: { ossId: '%d', fdn: '%s', kpis: '%s' }",
                getClass().getSimpleName(), ossId, fdn, kpis);
    }
}
