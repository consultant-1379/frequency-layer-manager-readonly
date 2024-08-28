/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.service.api.exceptions;

/**
 * <code>flm-algorithm</code> error codes.
 */
public enum FlmServiceExceptionCode {

    EXECUTION_STATE_PERSISTENCE_ERROR(2001, "Error persisting execution state"),
    EXECUTION_STATE_RETRIEVAL_ERROR(2002, "Error retrieving execution state"),
    CELL_SETTINGS_EVALUATION_ERROR(2003, "Error evaluating cell settings"),
    KPI_CALCULATION_RETRIES_EXPIRED(2004, "Error exhausted all retries for KPI request"),
    KPI_CALCULATION_LOST(2005, "Error KPI request status is LOST"),
    KPI_CALCULATION_ERROR(2006, "Error calculating KPIs in KPI Service"),
    ALGORITHM_FAILURE_ERROR(2007, "Error executing algorithm"),
    CELL_SETTINGS_HISTORY_ERROR(2008, "Error inserting cell settings execution into historical cell settings table"),
    OPTIMIZATION_EXECUTOR_ERROR(2009, "Error during generation of optimizations"),
    NO_SECTORS_FOUND(2010, "No sectors found for optimization"),
    SEND_RECORD_TO_TOPIC_ERROR(2011, "Error sending records to Kafka topic"),
    OPTIMIZATION_ELEMENT_SENT_PERSISTENCE_ERROR(2012, "Error persisting number of optimization elements sent"),
    LOAD_BALANCING_ERROR(2013, "Error creating ChangeElements from LoadBalancingQuantas"),
    OPTIMIZATION_ELEMENT_RECEIVED_NO_POLICYOUTPUTEVENT(2014, "Error receiving records from Policy Engine"),
    CUSTOMIZED_GLOBAL_SETTINGS_PARAMETERS_MISSING_ERROR(2015, "Error creating Customized Global Settings KPI parameters"),
    READING_CELL_KPIS_LOAD_BALANCER_ERROR(2016, "Error reading kpis for cells in LoadBalancing stage"),
    OPTIMIZATION_ELEMENT_READING_ERROR(2017, "Error reading Optimization elements"),
    CHANGE_ELEMENTS_SENDING_ERROR(2018, "Error sending ChangeElements"),
    SEND_ACTIVATION_POLICY_TO_TOPIC_ERROR(2019, "Error sending Activation Policy to Kafka topic"),
    NO_CELLS_FOUND_FOR_INCLUSION(2020, "Groups found in inclusion list contain no cells"),
    ALL_SECTORS_ARE_OVERLAPPING(2021, "All sectors are overlapping and had to be dropped"),
    EXHAUSTED_TERMINATING_PA_RETRIES(2022, "Exhausted retries terminating PA for previous execution");

    private final int errorCode;
    private final String errorMessage;

    FlmServiceExceptionCode(final int errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}