/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.database;

/**
 * Class to store column names for the KPI cell table.
 */
public final class KpiCellDbConstants {

    public static final String PM_IDLE_MODE_REL_DISTR_HIGH_LOAD = "pm_idle_mode_rel_distr_high_load";
    public static final String PM_IDLE_MODE_REL_DISTR_MEDIUM_HIGH_LOAD = "pm_idle_mode_rel_distr_medium_high_load";
    public static final String PM_IDLE_MODE_REL_DISTR_MEDIOUM_LOAD = "pm_idle_mode_rel_distr_medium_load";
    public static final String PM_IDLE_MODE_REL_DISTR_LOW_MEDIUM_LOAD = "pm_idle_mode_rel_distr_low_medium_load";
    public static final String PM_IDLE_MODE_REL_DISTR_LOW_LOAD = "pm_idle_mode_rel_distr_low_load";
    public static final String SUBSCRIPTION_RATIO = "subscription_ratio";
    public static final String GUID = "guid";
    public static final String FDN = "fdn";
    public static final String OSS_ID = "oss_id";
    public static final String LOCAL_TIMESTAMP = "local_timestamp";
    public static final String UTC_TIMESTAMP = "utc_timestamp";
    public static final String CONNECTED_USERS = "connected_users";
    public static final String DISTANCE_Q1 = "distance_q1";
    public static final String DISTANCE_Q2 = "distance_q2";
    public static final String DISTANCE_Q3 = "distance_q3";
    public static final String DISTANCE_Q4 = "distance_q4";
    public static final String UE_PERCENTAGE_Q1 = "ue_percentage_q1";
    public static final String UE_PERCENTAGE_Q2 = "ue_percentage_q2";
    public static final String UE_PERCENTAGE_Q3 = "ue_percentage_q3";
    public static final String UE_PERCENTAGE_Q4 = "ue_percentage_q4";
    public static final String CELL_AVAILABILITY = "cell_availability";

    private KpiCellDbConstants() {

    }

}