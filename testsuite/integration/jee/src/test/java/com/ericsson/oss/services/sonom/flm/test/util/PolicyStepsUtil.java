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
package com.ericsson.oss.services.sonom.flm.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;

import io.cucumber.java.DataTableType;

/**
 * Utility class for the Cucumber PolicySteps.
 */
public final class PolicyStepsUtil {
    private static final String DATA_VALUE = "dataValue";
    private static final String DATA_TYPE = "dataType";
    private static final String DATA_NAME = "dataName";
    private static final String FDN = "fdn";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String NAMESPACE = "nameSpace";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String SECTOR_ID = "sectorId";
    private static final String EXECUTION_ID = "executionId";
    private static final String SOURCE_CELL_FDN = "sourceCellFdn";
    private static final String SOURCE_CELL_OSS_ID = "sourceCellOssId";
    private static final String SOURCE_USERS_MOVE = "sourceUsersMove";
    private static final String TARGET_CELL_FDN = "targetCellFdn";
    private static final String TARGET_CELL_OSS_ID = "targetCellOssId";
    private static final String TARGET_USERS_MOVE = "targetUsersMove";
    private static final String SIZES_DIFFER_MESSAGE = "The amount of %s (%d) should be equal to the amount of currently-set optimization cells (%d).";

    private static List<OptimizationCell> optimizationCells = Collections.emptyList();
    private static List<TargetCell> targetCells = Collections.emptyList();
    private static ProposedLoadBalancingQuanta proposedLBQ;

    /**
     * Util method to convert Cucumber Datatable to {@link PolicyInputEvent}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link PolicyInputEvent}
     */
    @DataTableType
    public PolicyInputEvent definePolicyInputEvent(final Map<String, String> entry) {
        return new PolicyInputEvent(optimizationCells, entry.get(SECTOR_ID), entry.get(EXECUTION_ID));
    }

    /**
     * Util method to convert Cucumber Datatable to {@link DataEntry}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link DataEntry}
     */
    @DataTableType
    public DataEntry defineDataEntry(final Map<String, String> entry) {
        return new DataEntry(entry.get(FDN), entry.get(DATA_TYPE), entry.get(DATA_NAME), entry.getOrDefault(DATA_VALUE, null));
    }

    /**
     * Util method to convert Cucumber Datatable to {@link TargetCell}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link TargetCell}
     */
    @DataTableType
    public TargetCell defineTargetCell(final Map<String, String> entry) {
        return new TargetCell(entry.get(TARGET_CELL_FDN), Integer.parseInt(entry.get(TARGET_CELL_OSS_ID)), entry.get(TARGET_USERS_MOVE));
    }

    /**
     * Util method to convert Cucumber Datatable to {@link ProposedLoadBalancingQuanta}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link ProposedLoadBalancingQuanta}
     */
    @DataTableType
    public ProposedLoadBalancingQuanta defineLoadBalancingQuanta(final Map<String, String> entry) {
        return new ProposedLoadBalancingQuanta(entry.get(SOURCE_CELL_FDN),
                Integer.parseInt(entry.get(SOURCE_CELL_OSS_ID)),
                entry.get(SOURCE_USERS_MOVE),
                targetCells);
    }

    /**
     * Util method to convert Cucumber Datatable to {@link PolicyOutputEvent}
     *
     * @param entry
     *            a map of table entries from feature file.
     * @return the parsed {@link PolicyOutputEvent}
     */
    @DataTableType
    public PolicyOutputEvent definePolicyOutputEvent(final Map<String, String> entry) {
        return new PolicyOutputEvent(entry.get(NAME),
                entry.get(VERSION),
                entry.get(NAMESPACE),
                entry.get(SOURCE),
                entry.get(TARGET),
                Long.parseLong(entry.get(SECTOR_ID)),
                entry.get(EXECUTION_ID), proposedLBQ, optimizationCells);
    }

    /**
     * Set the {@link List} of {@link OptimizationCell}'s to pass into {@link PolicyInputEvent} or {@link PolicyOutputEvent} constructor.
     *
     * @param optimizationCells
     *            the optimization cells to be set.
     */
    public static void setOptimizationCells(final List<OptimizationCell> optimizationCells) {
        PolicyStepsUtil.optimizationCells = new ArrayList<>(optimizationCells);
    }

    /**
     * Get the {@link List} of {@link OptimizationCell}'s for test.
     *
     * @return optimizationCells the optimization cells.
     */
    public static List<OptimizationCell> getOptimizationCells() {
        return PolicyStepsUtil.optimizationCells;
    }

    /**
     * Set the KPI map for {@link List} of {@link OptimizationCell}'s to pass into {@link PolicyInputEvent} or {@link PolicyOutputEvent} constructor.
     *
     * @param kpis
     *            the KPIs to be set
     */
    public static void setOptimizationCellsKpis(final List<Map<String, String>> kpis) {
        final int kpiSize = kpis.size();
        final int optimizationCellsSize = optimizationCells.size();
        if (kpiSize != optimizationCellsSize) {
            throw new IllegalArgumentException(String.format(SIZES_DIFFER_MESSAGE, "KPIs", kpiSize, optimizationCellsSize));
        }
        IntStream.range(0, optimizationCellsSize)
                .forEach(i -> kpis.get(i)
                        .forEach(optimizationCells.get(i)::addKpi));
    }

    /**
     * Set the CM Attributes map for {@link List} of {@link OptimizationCell}'s to pass into {@link PolicyInputEvent} or {@link PolicyOutputEvent}
     * constructor.
     *
     * @param cmAttributes
     *            the CM Attributes to be set
     */
    public static void setOptimizationCellsCmAttributes(final List<Map<String, String>> cmAttributes) {
        final int cmAttributesSize = cmAttributes.size();
        final int optimizationCellsSize = optimizationCells.size();
        if (cmAttributesSize != optimizationCellsSize) {
            throw new IllegalArgumentException(String.format(SIZES_DIFFER_MESSAGE, "CM Attributes", cmAttributesSize, optimizationCellsSize));
        }
        IntStream.range(0, optimizationCellsSize)
                .forEach(i -> cmAttributes.get(i)
                        .forEach(optimizationCells.get(i)::addCmAttribute));
    }

    /**
     * Set the Settings map for {@link List} of {@link OptimizationCell}'s to pass into {@link PolicyInputEvent} or {@link PolicyOutputEvent}
     * constructor.
     *
     * @param settings
     *            the Settings to be set
     */
    public static void setOptimizationCellsSettings(final List<Map<String, String>> settings) {
        final int settingsSize = settings.size();
        final int optimizationCellsSize = optimizationCells.size();
        if (settingsSize != optimizationCellsSize) {
            throw new IllegalArgumentException(String.format(SIZES_DIFFER_MESSAGE, "Settings", settingsSize, optimizationCellsSize));
        }
        IntStream.range(0, optimizationCellsSize)
                .forEach(i -> settings.get(i)
                        .forEach(optimizationCells.get(i)::addSetting));
    }

    /**
     * Set the {@link List} of {@link TargetCell}'s to pass into {@link ProposedLoadBalancingQuanta} constructor.
     *
     * @param targetCells
     *            the optimization cells to be set.
     */
    public static void setTargetCells(final List<TargetCell> targetCells) {
        PolicyStepsUtil.targetCells = new ArrayList<>(targetCells);
    }

    /**
     * Set the {@link ProposedLoadBalancingQuanta} to pass into {@link PolicyOutputEvent} constructor.
     *
     * @param proposedLBQ
     *            the ProposedLoadBalancingQuanta to be set.
     */
    public static void setProposedLoadBalancingQuanta(final ProposedLoadBalancingQuanta proposedLBQ) {
        PolicyStepsUtil.proposedLBQ = proposedLBQ;
    }
}
