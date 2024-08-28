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

/**
 * A POJO for EutranFrequency MO.
 */
public class EUtranFrequency {
    private final long id;
    private final TopologyObjectId topologyObjectId;
    private final String name;
    private final int arfcnValueEUtranDl;

    public EUtranFrequency(final long id, final String fdn, final int ossId, final String name, final int arfcnValueEUtranDl) {
        this.id = id;
        this.topologyObjectId = TopologyObjectId.of(fdn, ossId);
        this.name = name;
        this.arfcnValueEUtranDl = arfcnValueEUtranDl;
    }

    public int getArfcnValueEUtranDl() {
        return arfcnValueEUtranDl;
    }

    public String getFdn() {
        return topologyObjectId.getFdn();
    }

    public int getOssId() {
        return topologyObjectId.getOssId();
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public TopologyObjectId getTopologyObjectId() {
        return topologyObjectId;
    }
}
