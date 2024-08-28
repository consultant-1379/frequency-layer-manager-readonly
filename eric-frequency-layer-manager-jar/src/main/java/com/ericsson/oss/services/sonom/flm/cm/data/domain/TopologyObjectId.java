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

import java.util.Objects;

/**
 * This class is used to represent Topology Object Id.
 */
public class TopologyObjectId {
    private final String fdn;
    private final int ossId;

    public TopologyObjectId(final String fdn, final int ossId) {
        this.fdn = fdn;
        this.ossId = ossId;
    }

    public static TopologyObjectId of(final String fdn, final int ossId) {
        return new TopologyObjectId(fdn, ossId);
    }

    public String getFdn() {
        return fdn;
    }

    public int getOssId() {
        return ossId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TopologyObjectId that = (TopologyObjectId) o;
        return Objects.equals(ossId, that.ossId) &&
                Objects.equals(fdn, that.fdn);
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
