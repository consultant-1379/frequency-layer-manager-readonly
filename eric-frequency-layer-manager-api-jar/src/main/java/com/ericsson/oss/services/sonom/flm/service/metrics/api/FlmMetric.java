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

package com.ericsson.oss.services.sonom.flm.service.metrics.api;

/**
 * Defines the metrics which will be collected for <code>son-frequency-layer-manager</code>.
 */
public enum FlmMetric {
    SETTINGS_PROCESSING_TIME_IN_MILLIS,
    FLM_ALG_EXECUTION,
    FLM_ALG_EXECUTION_TIME_IN_MILLIS,
    FLM_KPI_CALCULATION_TIME_IN_MILLIS,
    FLM_KPI_ON_DEMAND_CALCULATION_REQUESTS,
    FLM_KPI_ON_DEMAND_CALCULATION_TIME_IN_MILLIS,
    FLM_CONFIGURATION_GET_REQUESTS,
    FLM_CONFIGURATION_GET_TIME_IN_MILLIS,
    FLM_CONFIGURATION_UPDATE_REQUESTS,
    FLM_CONFIGURATION_UPDATE_TIME_IN_MILLIS,
    FLM_CONFIGURATION_CREATE_REQUESTS,
    FLM_CONFIGURATION_CREATE_TIME_IN_MILLIS,
    FLM_CONFIGURATION_DELETE_REQUESTS,
    FLM_CONFIGURATION_DELETE_TIME_IN_MILLIS,
    FLM_EXECUTION_GET_REQUEST,
    FLM_EXECUTION_GET_REQUEST_TIME_IN_MILLIS,
    FLM_CELL_SETTINGS_HISTORY_COPY_TIME_IN_MILLIS,
    FLM_SECTOR_BUSY_HOUR_LOAD_IN_MILLIS,
    FLM_SETTINGS_LOAD_IN_MILLIS,
    FLM_BUSY_HOUR_KPI_LOAD_IN_MILLIS,
    FLM_COVERAGE_BALANCE_RATIO_KPI_LOAD_IN_MILLIS,
    FLM_CELL_DAILY_KPI_LOAD_IN_MILLIS,
    FLM_POLICY_INPUT_EVENT_GENERATION_IN_MILLIS,
    FLM_POLICY_OUTPUT_EVENT_PROCESSED,
    FLM_POLICY_OUTPUT_EVENT_PROCESSED_IN_MILLIS,
    FLM_LOAD_BALANCING_IN_MILLIS,
    FLM_NUMBER_OF_CHANGE_ELEMENTS_SENT,
    FLM_CHANGE_ELEMENT_SENDING_TIME_IN_MILLIS,
    FLM_CELL_SECTOR_KPI_LOAD_IN_MILLIS
}
