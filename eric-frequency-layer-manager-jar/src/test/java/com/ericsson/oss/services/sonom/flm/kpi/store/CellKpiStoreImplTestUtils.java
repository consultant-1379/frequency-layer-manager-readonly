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

package com.ericsson.oss.services.sonom.flm.kpi.store;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.database.CellKpis;
import com.ericsson.oss.services.sonom.flm.database.handlers.CellKpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class CellKpiStoreImplTestUtils {
    public static final String DEFAULT_CELL_FDN_PREFIX = "cellFdn";
    public static final String DEFAULT_CELL_CGI_PREFIX = "cgi";
    public static final int DEFAULT_OSS_ID = 1;

    private CellKpiStoreImplTestUtils() {}

    public static class BusyHourCellKpiListBuilder {
        private final List<String> busyHours;
        private int cellNumberPerBusyHour = 4;
        private String cellFdnPrefix = DEFAULT_CELL_FDN_PREFIX;
        private int ossId = DEFAULT_OSS_ID;
        private CellKpis kpis;

        public BusyHourCellKpiListBuilder(final List<String> busyHours) {
            this.busyHours = busyHours;
        }

        public BusyHourCellKpiListBuilder withCellNumberPerBusyHour(final int cellNumberPerBusyHour) {
            this.cellNumberPerBusyHour = cellNumberPerBusyHour;
            return this;
        }

        public BusyHourCellKpiListBuilder withCellFdnPrefix(final String cellFdnPrefix) {
            this.cellFdnPrefix = cellFdnPrefix;
            return this;
        }

        public BusyHourCellKpiListBuilder withOssId(final int ossId) {
            this.ossId = ossId;
            return this;
        }

        public BusyHourCellKpiListBuilder withKpis(final CellKpis kpis) {
            this.kpis = kpis;
            return this;
        }

        /**
         * @return a {@link Map} of a busy hour cell kpis per {@link CellKpi} as follows:
         *
         *      Key                                                        |           Value
         * ----------------------------------------------------------------------------------------------
         * CellKpi(fdn="cellFdn_1_1", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * CellKpi(fdn="cellFdn_1_2", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * CellKpi(fdn="cellFdn_1_3", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * CellKpi(fdn="cellFdn_1_4", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * CellKpi(fdn="cellFdn_2_1", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * CellKpi(fdn="cellFdn_2_2", ossId=1, ts="2021-01-18 09:00:00")   |   CellKpis kpis
         * ...                                                             |   ...
         */
        public Map<CellKpi, CellKpis> build() {
            final Map<CellKpi, CellKpis> cellKpis = new HashMap<>();
            for (int i = 1; i < this.busyHours.size() + 1; i++) {
                for (int j = 1; j < this.cellNumberPerBusyHour + 1; j++) {
                    final CellKpi cellKpi = new CellKpi(this.cellFdnPrefix + "_" + i + "_" + j, this.ossId, busyHours.get(i - 1));
                    cellKpis.put(cellKpi, getKpis());
                }
            }
            return cellKpis;
        }

        private CellKpis getKpis() {
            if (this.kpis == null) {
                return new CellKpis(getRandomDouble(), getRandomDouble(), getRandomInt(),
                        getRandomInt(), getRandomInt(), getRandomInt(), getRandomInt());
            } else {
                return this.kpis;
            }
        }

        private static double getRandomDouble() {
            final double min = 100.0D;
            final double max = 10_000.0D;
            return min + new Random().nextDouble() * (max - min);
        }

        private static int getRandomInt() {
            final int min = 1;
            final int max = 1000;
            return min + (int) (new Random().nextFloat() * (max - min));
        }
    }

    public static class SectorBusyHourListBuilder {
        private final List<String> busyHours;
        public SectorBusyHourListBuilder(final List<String> busyHours) {
            this.busyHours = busyHours;
        }
        public Map<Long, String> build() {
            final Map<Long, String> busyHourList = new HashMap<>();
            for (int i = 0; i < this.busyHours.size(); i++) {
                busyHourList.put((long) (i + 1), busyHours.get(i));
            }
            return busyHourList;
        }
    }

    public static class SectorListBuilder {
        private int sectorNumber = 4;
        private int cellNumberPerSector = 4;
        private String cellFdnPrefix = DEFAULT_CELL_FDN_PREFIX;
        private String cgiPrefix = DEFAULT_CELL_CGI_PREFIX;
        private int ossId = DEFAULT_OSS_ID;

        public SectorListBuilder withSectorNumber(final int sectorNumber) {
            this.sectorNumber = sectorNumber;
            return this;
        }

        public SectorListBuilder withCellNumberPerSector(final int cellNumberPerSector) {
            this.cellNumberPerSector = cellNumberPerSector;
            return this;
        }

        public SectorListBuilder withCellOssId(final int ossId) {
            this.ossId = ossId;
            return this;
        }

        public SectorListBuilder withCellFdnPrefix(final String cellFdnPrefix) {
            this.cellFdnPrefix = cellFdnPrefix;
            return this;
        }

        public SectorListBuilder withCellCgiPrefix(final String cgiPrefix) {
            this.cgiPrefix = cgiPrefix;
            return this;
        }

        /**
         * @return a {@link Collection} of {@link TopologySector} as follows:
         *
         * Sector id = 1    |   Cell fdn = "cellFdn_1_1"
         * Sector id = 1    |   Cell fdn = "cellFdn_1_2"
         * Sector id = 1    |   Cell fdn = "cellFdn_1_3"
         * Sector id = 1    |   Cell fdn = "cellFdn_1_4"
         * ------------------------------------------------
         * Sector id = 2    |   Cell fdn = "cellFdn_2_1"
         * Sector id = 2    |   Cell fdn = "cellFdn_2_2"
         * ...              |   ...
         */
        public Collection<TopologySector> build() {
            final Collection<TopologySector> sectorList = new ArrayList<>();
            for (int i = 1; i < sectorNumber + 1; i++) {
                final Collection<Cell> cells = new CellListBuilder()
                        .withCellNumber(this.cellNumberPerSector)
                        .withOssId(this.ossId)
                        .withCellFdnPrefix(this.cellFdnPrefix + "_" + i)
                        .withCgiPrefix(this.cgiPrefix + "_" + i)
                        .build();
                sectorList.add(new TopologySector((long) i, cells));
            }
            return sectorList;
        }
    }

    public static class CellListBuilder {
        private int cellNumber = 4;
        private int ossId = DEFAULT_OSS_ID;
        private String cellFdnPrefix = DEFAULT_CELL_FDN_PREFIX;
        private int carrier = 1200;
        private String idleModePrioAtReleaseRef;
        private String cgiPrefix = DEFAULT_CELL_CGI_PREFIX;
        private int bandwidth = 10000;
        private String installationType = "outdoor";
        private String lteNrSpectrumShared = "undefined";

        public CellListBuilder withCellNumber(final int cellNumber) {
            this.cellNumber = cellNumber;
            return this;
        }

        public CellListBuilder withOssId(final int ossId) {
            this.ossId = ossId;
            return this;
        }

        public CellListBuilder withCellFdnPrefix(final String cellFdnPrefix) {
            this.cellFdnPrefix = cellFdnPrefix;
            return this;
        }

        public CellListBuilder withCarrier(final int carrier) {
            this.carrier = carrier;
            return this;
        }

        public CellListBuilder withIdleModePrioAtReleaseRef(final String idleModePrioAtReleaseRef) {
            this.idleModePrioAtReleaseRef = idleModePrioAtReleaseRef;
            return this;
        }

        public CellListBuilder withCgiPrefix(final String cgiPrefix) {
            this.cgiPrefix = cgiPrefix;
            return this;
        }

        public CellListBuilder withBandwidth(final int bandwidth) {
            this.bandwidth = bandwidth;
            return this;
        }

        public CellListBuilder withlteNrSpectrumShared(final String lteNrSpectrumShared) {
            this.lteNrSpectrumShared = lteNrSpectrumShared;
            return this;
        }

        public CellListBuilder withInstallationType(final String installationType) {
            this.installationType = installationType;
            return this;
        }

        public Collection<Cell> build() {
            final Collection<Cell> cellList = new ArrayList<>();
            for (int i = 1; i < cellNumber + 1; i++) {
                cellList.add(new Cell(
                        (long) i,
                        this.ossId,
                        this.cellFdnPrefix + "_" + i,
                        this.carrier,
                        this.idleModePrioAtReleaseRef,
                        this.cgiPrefix + "_" + i,
                        this.bandwidth,
                        this.installationType,
                        this.lteNrSpectrumShared));
            }
            return cellList;
        }
    }
}