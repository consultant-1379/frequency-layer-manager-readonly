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

import java.util.Objects;

/**
 * POJO to represent the Cell.
 */
public class Cell {

    private final Long cellId;
    private final TopologyObjectId topologyObjectId;
    private final int carrier;
    private final String idleModePrioAtReleaseRef;
    private final String cgi;
    private final Integer bandwidth;
    private final String installationType;
    private final String lteNrSpectrumShared;

    public Cell(final Long cellId, final int ossId, final String fdn, final Integer bandwidth, final String installationType,
                final String lteNrSpectrumShared) {
        this(cellId, ossId, fdn, -1, null, null, bandwidth, installationType, lteNrSpectrumShared);
    }

    @SuppressWarnings("squid:S107")
    public Cell(final Long cellId, final int ossId, final String fdn, final int carrier, final String idleModePrioAtReleaseRef, final String cgi,
                final Integer bandwidth, final String installationType, final String lteNrSpectrumShared) {
        this.cellId = cellId;
        this.topologyObjectId = TopologyObjectId.of(fdn, ossId);
        this.carrier = carrier;
        this.idleModePrioAtReleaseRef = idleModePrioAtReleaseRef;
        this.cgi = cgi;
        this.bandwidth = bandwidth;
        this.installationType = installationType;
        this.lteNrSpectrumShared = lteNrSpectrumShared;
    }

    public Long getCellId() {
        return cellId;
    }

    public int getOssId() {
        return topologyObjectId.getOssId();
    }

    public String getFdn() {
        return topologyObjectId.getFdn();
    }

    public int getCarrier() {
        return carrier;
    }

    public String getIdleModePrioAtReleaseRef() {
        return idleModePrioAtReleaseRef;
    }

    public String getCgi() {
        return cgi;
    }

    /**
     * Get the bandwidth for the cell.
     *
     * @return {@link Integer} value for bandwidth which can be null
     */
    public Integer getBandwidth() {
        return bandwidth;
    }

    public String getInstallationType() {
        return installationType;
    }

    public String getlteNrSpectrumShared() {
        return lteNrSpectrumShared;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Cell that = (Cell) o;
        return Objects.equals(cellId, that.cellId) &&
                Objects.equals(topologyObjectId, that.topologyObjectId) &&
                Objects.equals(carrier, that.carrier) &&
                Objects.equals(idleModePrioAtReleaseRef, that.idleModePrioAtReleaseRef) &&
                Objects.equals(cgi, that.cgi) &&
                Objects.equals(bandwidth, that.bandwidth) &&
                Objects.equals(installationType, that.installationType) &&
                Objects.equals(lteNrSpectrumShared, that.lteNrSpectrumShared);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellId, topologyObjectId);
    }

    @Override
    public String toString() {
        return String.format("%s:: { cellId: '%s', ossId: %d, fdn: '%s', carrier: '%d', idleModePrioAtReleaseRef: '%s', cgi: '%s', " +
                        "bandwidth: '%d', installationType: '%s', lteNrSpectrumShared: '%s' }",
                getClass().getSimpleName(), cellId, topologyObjectId.getOssId(), topologyObjectId.getFdn(), carrier, idleModePrioAtReleaseRef, cgi,
                bandwidth, installationType, lteNrSpectrumShared);
    }

    public TopologyObjectId getTopologyObjectId() {
        return topologyObjectId;
    }
}
