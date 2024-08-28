/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.database.settings;

import static com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.getSettingsColumnNamesAsList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

import org.junit.Test;

import com.ericsson.oss.services.sonom.flm.database.settings.CellSettingsDbConstants.PolicyInputSettings;

/**
 * Unit tests for {@link CellSettingsDbConstants} class.
 */
public class CellSettingsDbConstantsTest {

    private static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    private static final String COMMA = ",";

    /**
     * Test is vital to ensure the order of the backing array of the {@link PolicyInputSettings} constants is not changed.
     */
    @Test
    public void whenGetPolicyInputSettingsColumns_thenOrderIsPreserved() {
        final String expected = new StringJoiner(COMMA).add(PolicyInputSettings.QOS_FOR_CAPACITY_ESTIMATION.getValue())
                .add(PolicyInputSettings.PERCENTILE_FOR_MAX_CONNECTED_USER.getValue())
                .add(PolicyInputSettings.MIN_NUM_CELL_FOR_CDF_CALCULATION.getValue())
                .add(PolicyInputSettings.TARGET_THROUGHPUT_R.getValue())
                .add(PolicyInputSettings.DELTA_GFS_OPTIMIZATION_THRESHOLD.getValue())
                .add(PolicyInputSettings.TARGET_SOURCE_COVERAGE_BALANCE_RATIO_THRESHOLD.getValue())
                .add(PolicyInputSettings.SOURCE_TARGET_SAMPLES_OVERLAP_THRESHOLD.getValue())
                .add(PolicyInputSettings.TARGET_SOURCE_CONTIGUITY_RATIO_THRESHOLD.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_INITIAL_ERAB_ESTAB_SUCC_RATE.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_INITIAL_ERAB_ESTAB_SUCC_RATE_FOR_QCI1.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_ERAB_PERCENTAGE_LOST_FOR_QCI1.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_CELL_HO_SUCC_RATE.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_CELL_AVAILABILITY.getValue())
                .add(PolicyInputSettings.OPTIMIZATION_SPEED.getValue())
                .add(PolicyInputSettings.OPTIMIZATION_SPEED_FACTOR_TABLE.getValue())
                .add(PolicyInputSettings.BANDWIDTH_TO_STEP_SIZE_TABLE.getValue())
                .add(PolicyInputSettings.LB_THRESHOLD_FOR_ENDC_USERS.getValue())
                .add(PolicyInputSettings.NUM_CALLS_CELL_HOURLY_RELIABILITY_THRESHOLD_IN_HOURS.getValue())
                .add(PolicyInputSettings.SYNTHETIC_COUNTERS_CELL_RELIABILITY_THRESHOLD_IN_ROPS.getValue())
                .add(PolicyInputSettings.ENABLE_ESS_SETTING.getValue())
                .add(PolicyInputSettings.MIN_NUM_SAMPLES_FOR_TRANSIENT_CALCULATION.getValue())
                .add(PolicyInputSettings.SIGMA_FOR_TRANSIENT_CALCULATION.getValue())
                .add(PolicyInputSettings.UPLINK_PUSCH_SINR_RATIO_THRESHOLD.getValue())
                .add(PolicyInputSettings.MIN_TARGET_UPLINK_PUSCH_SINR.getValue())
                .add(PolicyInputSettings.PERCENTAGE_BAD_RSRP_RATIO_THRESHOLD.getValue())
                .add(PolicyInputSettings.MIN_CONNECTED_USERS.getValue())
                .toString();

        assertThat(CellSettingsDbConstants.getPolicyInputSettingsColumns()).isEqualTo(expected);
    }

    @Test
    public void whenGetAllColumnNames_thenOrderIsPreserved() {
        final String expected = new StringJoiner(COMMA).add(CellSettingsDbConstants.ID)
                .add(CellSettingsDbConstants.OSS_ID)
                .add(CellSettingsDbConstants.FDN)
                .add(CellSettingsDbConstants.EXECUTION_ID)
                .add(CellSettingsDbConstants.CONFIGURATION_ID)
                .add(CellSettingsDbConstants.getPolicyInputSettingsColumns())
                .add(CellSettingsDbConstants.EXCLUSION_LIST)
                .add(CellSettingsDbConstants.MINIMUM_SOURCE_RETAINED)
                .add(CellSettingsDbConstants.MIN_ROPS_FOR_APP_COV_RELIABILITY)
                .add(CellSettingsDbConstants.MIN_NUM_CQI_SAMPLES)
                .add(CellSettingsDbConstants.SECTOR_ID)
                .toString();

        assertThat(CellSettingsDbConstants.getAllColumnNames()).isEqualTo(expected);
    }

    @Test
    public void whenGetAllColumnNamesForCellConfigHistory_thenOrderIsPreserved() {
        final String expected = new StringJoiner(COMMA).add(CellSettingsDbConstants.ID)
                .add(CellSettingsDbConstants.OSS_ID)
                .add(CellSettingsDbConstants.FDN)
                .add(CellSettingsDbConstants.EXECUTION_ID)
                .add(CellSettingsDbConstants.CREATED)
                .add(CellSettingsDbConstants.getPolicyInputSettingsColumns())
                .add(CellSettingsDbConstants.EXCLUSION_LIST)
                .add(CellSettingsDbConstants.MINIMUM_SOURCE_RETAINED)
                .add(CellSettingsDbConstants.MIN_ROPS_FOR_APP_COV_RELIABILITY)
                .add(CellSettingsDbConstants.MIN_NUM_CQI_SAMPLES)
                .add(CellSettingsDbConstants.SECTOR_ID)
                .toString();

        assertThat(CellSettingsDbConstants.getAllColumnNamesForCellConfigHistory()).isEqualTo(expected);
    }

    @Test
    public void whenGetAllColumnNamesForInsertionSelectForCellConfigHistory_thenOrderIsPreserved() {
        final String expected = new StringJoiner(COMMA).add(CellSettingsDbConstants.ID)
                .add(CellSettingsDbConstants.OSS_ID)
                .add(CellSettingsDbConstants.FDN)
                .add(CellSettingsDbConstants.EXECUTION_ID)
                .add(CURRENT_TIMESTAMP)
                .add(CellSettingsDbConstants.getPolicyInputSettingsColumns())
                .add(CellSettingsDbConstants.EXCLUSION_LIST)
                .add(CellSettingsDbConstants.MINIMUM_SOURCE_RETAINED)
                .add(CellSettingsDbConstants.MIN_ROPS_FOR_APP_COV_RELIABILITY)
                .add(CellSettingsDbConstants.MIN_NUM_CQI_SAMPLES)
                .add(CellSettingsDbConstants.SECTOR_ID)
                .toString();

        assertThat(CellSettingsDbConstants.getAllColumnNamesForInsertionSelectForCellConfigHistory()).isEqualTo(expected);
    }

    @Test
    public void whenGetUpdateSetStatementString_thenFormatIsCorrectAndOrderIsPreserved() {
        final ArrayList<String> expectedSetStatementColumns = new ArrayList<>(
                Arrays.asList(
                        CellSettingsDbConstants.OSS_ID,
                        CellSettingsDbConstants.FDN,
                        CellSettingsDbConstants.CONFIGURATION_ID));
        expectedSetStatementColumns.addAll(getSettingsColumnNamesAsList());
        final String[] elements = CellSettingsDbConstants.getUpdateSetStatementString().split(COMMA);
        final ArrayList<String> setStatementColumns = new ArrayList<>(elements.length);
        for (final String element : elements) {
            setStatementColumns.add(element.split("=excluded")[0].trim());
        }
        assertThat(setStatementColumns).containsAll(setStatementColumns);
    }
}