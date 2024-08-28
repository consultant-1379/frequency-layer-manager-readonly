/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.service.startup;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.client.CmRestExecutor;
import com.ericsson.oss.services.sonom.cm.service.api.exception.CmModelValidationException;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.flm.cm.schema.CmModelValidator;
import com.ericsson.oss.services.sonom.flm.kpi.KpiAndCounterValidator;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;

/**
 * Implements {@link RequirementsValidator}. Validates the required CM, PM and KPI data required for FLM.
 */
@Stateless(name = "flmRequirementsValidator")
public class FlmRequirementsValidator implements RequirementsValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlmRequirementsValidator.class);
    private static final CmRestExecutor CM_REST_EXECUTOR = CmKpiRestResilienceCreator.getCmRestExecutor();
    private static final KpiAndCounterValidator KPI_AND_COUNTER_VALIDATOR = CmKpiRestResilienceCreator.getKpiAndCounterValidator();

    private final CmModelValidator cmModelValidator = new CmModelValidator();

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void validateCm() {
        try {
            final RestResponse<String> requestResponse = cmModelValidator.sendRequiredCmElementsForMediation(CM_REST_EXECUTOR);
            LOGGER.info("Required CM model has been accepted: {}", requestResponse);
        } catch (final CmModelValidationException e) {
            LOGGER.error("Error validating the CM model", e);
        }
    }

    @Override
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void validateKpis() {
        try {
            final RestResponse<String> kpiRestResponse = KPI_AND_COUNTER_VALIDATOR.sendRequiredKpisForMediation();
            LOGGER.info("Required KPIs have been accepted. Status code: {}", kpiRestResponse.getStatus());

            final RestResponse<String> counterRestResponse = KPI_AND_COUNTER_VALIDATOR.sendRequiredCountersForMediation();
            LOGGER.info("Required counters have been accepted: {}", counterRestResponse);

        } catch (final KpiModelVerificationException e) {
            LOGGER.error("Error validating KPIs and counters", e);
        }
    }

}